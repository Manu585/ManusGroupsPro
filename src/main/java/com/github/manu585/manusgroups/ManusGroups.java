package com.github.manu585.manusgroups;

import com.github.manu585.manusgroups.cache.GroupCatalogCache;
import com.github.manu585.manusgroups.cache.GroupPlayerCache;
import com.github.manu585.manusgroups.commands.Commands;
import com.github.manu585.manusgroups.config.ConfigManager;
import com.github.manu585.manusgroups.defaults.DefaultGroup;
import com.github.manu585.manusgroups.domain.Group;
import com.github.manu585.manusgroups.expiry.ExpiryQueue;
import com.github.manu585.manusgroups.expiry.ExpiryScheduler;
import com.github.manu585.manusgroups.imp.ChatFormatServiceImpl;
import com.github.manu585.manusgroups.imp.PrefixServiceImpl;
import com.github.manu585.manusgroups.listeners.ChatListener;
import com.github.manu585.manusgroups.listeners.GroupChangeListener;
import com.github.manu585.manusgroups.listeners.JoinQuitListener;
import com.github.manu585.manusgroups.messaging.MessageService;
import com.github.manu585.manusgroups.repo.Database;
import com.github.manu585.manusgroups.repo.DbExecutor;
import com.github.manu585.manusgroups.repo.GroupRepository;
import com.github.manu585.manusgroups.repo.jdbc.JdbcGroupRepository;
import com.github.manu585.manusgroups.repo.jdbc.dao.JdbcGroupAssignmentDao;
import com.github.manu585.manusgroups.repo.jdbc.dao.JdbcGroupDao;
import com.github.manu585.manusgroups.repo.jdbc.dao.JdbcUserDao;
import com.github.manu585.manusgroups.service.GroupService;
import com.github.manu585.manusgroups.util.General;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.DataSource;

public class ManusGroups extends JavaPlugin {
    private ConfigManager configManager;

    private DbExecutor executorService;
    private Database database;

    private GroupRepository groupRepository;
    private GroupCatalogCache groupCatalogCache;
    private GroupPlayerCache groupPlayerCache;

    private ExpiryScheduler expiryScheduler;

    private GroupService groupService;
    private PrefixServiceImpl prefixService;
    private ChatFormatServiceImpl chatFormatService;

    private MessageService messageService;

    private Commands commands;

    @Override
    public void onEnable() {
        getLogger().info("Booting up ManusGroupsPro...");

        // Configuration Management
        configManager = new ConfigManager(this);

        // Executor Thread Pool
        executorService = new DbExecutor();

        // Async IO -> DB Initiation -> DB Migration -> init Core functionality (Main Thread)
        configManager.prepareAllAsync(executorService)
                .thenRun(() -> database = new Database(this, executorService))
                .thenRun(() -> database.init(
                    configManager.getMainConfig().dbHost(),
                    configManager.getMainConfig().dbPort(),
                    configManager.getMainConfig().dbName(),
                    configManager.getMainConfig().dbUser(),
                    configManager.getMainConfig().dbPass()
                ))
                .thenCompose(__ -> database.migrateAsync())
                .thenRun(() -> General.runSync(this, this::initCore))
                .exceptionally(ex -> {
                    General.runSync(this, () -> {
                        getLogger().severe("Boot process of ManusGroupsPro failed! " + ex);
                        getServer().getPluginManager().disablePlugin(this);
                    });
                    return null;
                });
    }

    private void initCore() {
        DataSource dataSource = database.getHikariDataSource();

        // Data Access Objects
        JdbcUserDao userDao = new JdbcUserDao(dataSource);
        JdbcGroupDao groupDao = new JdbcGroupDao(dataSource);
        JdbcGroupAssignmentDao assignmentDao = new JdbcGroupAssignmentDao(dataSource);

        // Data Repository
        groupRepository = new JdbcGroupRepository(userDao, groupDao, assignmentDao, executorService);

        // Default group "provider"
        Group defaultGroup = new Group(
                configManager.getMainConfig().getDefaultGroupName(),
                configManager.getMainConfig().getDefaultGroupPrefix(),
                configManager.getMainConfig().getDefaultGroupWeight(),
                true);

        DefaultGroup.initialize(defaultGroup);

        // Caches
        groupCatalogCache = new GroupCatalogCache(groupRepository);
        groupPlayerCache = new GroupPlayerCache(groupRepository, groupCatalogCache);

        // Ensure Default group exists
        groupRepository.upsertGroup(defaultGroup)
                // Put all Groups from DB into Cache
                .thenCompose(__ -> groupCatalogCache.warmAll())
                // Finish Initiation
                .thenRun(() -> General.runSync(this, this::finishInit))

                .exceptionally(ex -> {
                    General.runSync(this, () -> {
                        getLogger().severe("Default group / group warming failed! " + ex);
                        getServer().getPluginManager().disablePlugin(this);
                    });
                    return null;
                });
    }

    private void finishInit() {
        // Register Scheduler
        expiryScheduler = new ExpiryQueue();

        // Register Services
        prefixService = new PrefixServiceImpl(this, groupPlayerCache);
        chatFormatService = new ChatFormatServiceImpl(prefixService, configManager.getLanguageConfig().getChatFormat());
        groupService = new GroupService(this, groupRepository, groupCatalogCache, groupPlayerCache, prefixService, expiryScheduler);

        expiryScheduler.registerListener(uuid -> groupService.clearToDefault(uuid));

        groupRepository.listAllWithExpiry()
                .thenAccept(list -> {
                    expiryScheduler.bootstrap(list);
                    expiryScheduler.start();
                }).exceptionally(ex -> {
                    getLogger().severe("Expiry bootstrap failed! " + ex);
                    return null;
                });

        // Register Listeners
        registerListeners(
                new JoinQuitListener(this, groupService, prefixService),
                new GroupChangeListener(prefixService),
                new ChatListener(chatFormatService)
        );

        messageService = new MessageService(configManager.getLanguageConfig().yaml());

        // Register Commands
        commands = new Commands(this, messageService, groupService, groupRepository, groupCatalogCache, chatFormatService);


        // Prime online players that joined before initCore process finished (async)
        for (Player player : getServer().getOnlinePlayers()) {
            groupService.ensureDefaultPersisted(player.getUniqueId())
                    .thenCompose(__ -> groupService.load(player.getUniqueId()))
                    .thenCompose(__ -> prefixService.primePrefix(player.getUniqueId()))
                    .thenRun(() -> General.runSync(this, () -> prefixService.refreshDisplayName(player)));
        }

        getLogger().info("ManusGroupsPro initialized.");
    }

    private void registerListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            this.getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    @Override
    public void onDisable() {
        if (executorService != null) {
            executorService.shutDown();
        }

        if (database != null) {
            database.close();
        }

        if (expiryScheduler != null) {
            expiryScheduler.stop();
        }

        HandlerList.unregisterAll();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DbExecutor getExecutorService() {
        return executorService;
    }

    public Database getDatabase() {
        return database;
    }

    public PrefixServiceImpl getPrefixService() {
        return prefixService;
    }

    public ChatFormatServiceImpl getChatFormatService() {
        return chatFormatService;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public Commands getCommands() {
        return commands;
    }

    public GroupService getGroupService() {
        return groupService;
    }
}
