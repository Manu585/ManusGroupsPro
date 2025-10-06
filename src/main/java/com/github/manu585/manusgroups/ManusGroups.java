package com.github.manu585.manusgroups;

import com.github.manu585.manusgroups.cache.GroupCatalogCache;
import com.github.manu585.manusgroups.cache.GroupPermissionCache;
import com.github.manu585.manusgroups.cache.GroupPlayerCache;
import com.github.manu585.manusgroups.cache.PrefixCache;
import com.github.manu585.manusgroups.commands.Commands;
import com.github.manu585.manusgroups.config.ConfigManager;
import com.github.manu585.manusgroups.domain.Group;
import com.github.manu585.manusgroups.expiry.ExpiryQueue;
import com.github.manu585.manusgroups.expiry.ExpiryScheduler;
import com.github.manu585.manusgroups.listeners.ChatListener;
import com.github.manu585.manusgroups.listeners.GroupChangeListener;
import com.github.manu585.manusgroups.listeners.JoinQuitListener;
import com.github.manu585.manusgroups.listeners.SignListener;
import com.github.manu585.manusgroups.manager.SignSelectionManager;
import com.github.manu585.manusgroups.repo.Database;
import com.github.manu585.manusgroups.repo.DbExecutor;
import com.github.manu585.manusgroups.repo.GroupRepository;
import com.github.manu585.manusgroups.repo.jdbc.JdbcGroupRepository;
import com.github.manu585.manusgroups.repo.jdbc.dao.*;
import com.github.manu585.manusgroups.service.GroupService;
import com.github.manu585.manusgroups.service.MessageService;
import com.github.manu585.manusgroups.service.impl.ChatFormatServiceImpl;
import com.github.manu585.manusgroups.service.impl.GroupSignServiceImpl;
import com.github.manu585.manusgroups.service.impl.PermissionServiceImpl;
import com.github.manu585.manusgroups.service.impl.PrefixServiceImpl;
import com.github.manu585.manusgroups.service.spi.ChatFormatService;
import com.github.manu585.manusgroups.service.spi.GroupSignService;
import com.github.manu585.manusgroups.service.spi.PermissionService;
import com.github.manu585.manusgroups.service.spi.PrefixService;
import com.github.manu585.manusgroups.util.DefaultGroup;
import com.github.manu585.manusgroups.util.General;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.util.concurrent.CompletableFuture;

public class ManusGroups extends JavaPlugin {
    private ConfigManager configManager;

    private DbExecutor executorService;
    private Database database;

    private GroupRepository groupRepository;
    private GroupCatalogCache groupCatalogCache;
    private GroupPlayerCache groupPlayerCache;
    private GroupPermissionCache groupPermissionCache;
    private PrefixCache prefixCache;

    private ExpiryScheduler expiryScheduler;

    private GroupService groupService;

    private PrefixService prefixService;
    private ChatFormatService chatFormatService;
    private PermissionService permissionService;
    private GroupSignService signService;

    private MessageService messageService;

    private SignSelectionManager selectionManager;

    private Commands commands;

    @Override
    public void onEnable() {
        bootAsync();
    }

    @Override
    public void onDisable() {
        shutdown();
    }

    /* ===============
     * Boot Sequence
     * =============== */

    private void bootAsync() {
        getLogger().info("Booting up " + getName() + "...");

        // Config and Executor Thread Pool instantiation
        configManager = new ConfigManager(this);
        executorService = new DbExecutor();

        // Async: load configs -> init Database -> migrate Database -> init core on main thread
        configManager.prepareAllAsync(executorService)
                .thenRun(() -> database = new Database(this, executorService))
                .thenRun(this::initDatabaseFromConfig)
                .thenCompose(__ -> database.migrateAsync())
                .thenRun(() -> General.runSync(this, this::initCore))

                // EXCEPTION HANDLING
                .exceptionally(exception -> {
                    General.runSync(this, () -> {
                        getLogger().severe("Boot process of " + getName() + " failed!" + exception);
                        getServer().getPluginManager().disablePlugin(this);
                    });
                    return null;
                });
    }

    /**
     * Init Database with credentials from Configuration
     */
    private void initDatabaseFromConfig() {
        database.init(
                configManager.getMainConfig().dbHost(),
                configManager.getMainConfig().dbPort(),
                configManager.getMainConfig().dbName(),
                configManager.getMainConfig().dbUser(),
                configManager.getMainConfig().dbPass()
        );
    }

    /**
     * Init core functionality
     */
    private void initCore() {
        // DAOs & Repository Instantiation
        createRepository(database.getHikariDataSource());

        // Build and init default group
        final Group defaultGroup = buildDefaultGroup();
        DefaultGroup.initialize(defaultGroup);

        // Cache Instantiation
        initCaches();

        // Finish
        warmCachesAndFinish(defaultGroup);
    }

    private void createRepository(final DataSource dataSource) {
        final JdbcGroupUserDao groupUserDao = new JdbcGroupUserDao(dataSource);
        final JdbcGroupDao groupDao = new JdbcGroupDao(dataSource);
        final JdbcGroupAssignmentDao groupAssignmentDao = new JdbcGroupAssignmentDao(dataSource);
        final JdbcGroupPermissionDao groupPermissionDao = new JdbcGroupPermissionDao(dataSource);
        final JdbcGroupSignDao groupSignDao = new JdbcGroupSignDao(dataSource);

        groupRepository = new JdbcGroupRepository(
                groupUserDao,
                groupDao,
                groupAssignmentDao,
                groupPermissionDao,
                groupSignDao,
                executorService
        );
    }

    private Group buildDefaultGroup() {
        return new Group(
                configManager.getMainConfig().getDefaultGroupName(),
                configManager.getMainConfig().getDefaultGroupPrefix(),
                configManager.getMainConfig().getDefaultGroupWeight(),
                true
        );
    }

    private void initCaches() {
        groupCatalogCache = new GroupCatalogCache(groupRepository);
        groupPlayerCache = new GroupPlayerCache(groupRepository, groupCatalogCache);
        groupPermissionCache = new GroupPermissionCache(groupRepository);
        prefixCache = new PrefixCache();
    }

    private void warmCachesAndFinish(Group defaultGroup) {
        groupRepository.upsertGroup(defaultGroup)
                // Put all Groups from DB into Cache
                .thenCompose(__ -> groupCatalogCache.warmAll())
                .thenCompose(__ -> groupPermissionCache.warmAll(groupCatalogCache.snapshot().keySet()))
                .thenRun(() -> General.runSync(this, this::finishInit))

                // EXCEPTION HANDLING
                .exceptionally(ex -> {
                    General.runSync(this, () -> {
                        getLogger().severe("Default group / group warming failed! " + ex);
                        getServer().getPluginManager().disablePlugin(this);
                    });
                    return null;
                });
    }

    private void finishInit() {
        // Services
        messageService = new MessageService(configManager.getLanguageConfig().yaml());
        prefixService = new PrefixServiceImpl(this, groupPlayerCache, prefixCache, messageService);
        chatFormatService = new ChatFormatServiceImpl(prefixService, configManager.getLanguageConfig().getChatFormat(), messageService);
        permissionService = new PermissionServiceImpl(this, groupPermissionCache, groupPlayerCache);
        signService = new GroupSignServiceImpl(this, groupRepository, groupPlayerCache, messageService);

        selectionManager = new SignSelectionManager();

        // Register Temp Group Scheduler
        expiryScheduler = new ExpiryQueue();

        // Business Logic: Group Service Instantiation
        groupService = new GroupService(
                this,
                groupRepository,
                groupCatalogCache,
                groupPlayerCache,
                prefixService,
                permissionService,
                signService,
                expiryScheduler
        );

        // Expiry handling
        expiryScheduler.registerListener(uuid -> groupService.clearToDefault(uuid).thenRun(() -> signService.refreshFor(uuid)));

        // Bootstrap Expiry Scheduler
        groupRepository.listAllWithExpiry()
                .thenAccept(list -> {
                    expiryScheduler.bootstrap(list);
                    expiryScheduler.start();
                }).exceptionally(ex -> {
                    getLogger().severe("Expiry bootstrap failed! " + ex);
                    return null;
                });

        // Listeners and Commands
        registerListeners(
                new JoinQuitListener(this, groupService, prefixService, permissionService),
                new GroupChangeListener(prefixService),
                new ChatListener(chatFormatService),
                new SignListener(signService, selectionManager, messageService)
        );

        commands = new Commands(
                this,
                messageService,
                groupService,
                groupRepository,
                groupCatalogCache,
                groupPermissionCache,
                permissionService,
                signService,
                selectionManager
        );

        // Prime players already online
        primeOnlinePlayers();

        getLogger().info(getName() + " initialized.");
    }

    private void primeOnlinePlayers() {
        for (Player player : getServer().getOnlinePlayers()) {
            groupService.ensureDefaultPersisted(player.getUniqueId())
                    .thenCompose(__ -> groupService.load(player.getUniqueId()))
                    .thenCompose(__ -> prefixService.primePrefix(player.getUniqueId()))
                    .thenCompose(__ -> permissionService.refreshFor(player.getUniqueId()))
                    .thenRun(() -> General.runSync(this, () -> prefixService.refreshDisplayName(player)));
        }
    }

    /* ===========
     * Reload
     * =========== */

    public CompletableFuture<Boolean> reloadAsync() {
        final CompletableFuture<Boolean> result = new CompletableFuture<>();

        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try {
                // Reload language.yml and rewire MessageService with possibly new values
                configManager.getLanguageConfig().reload();
                messageService.reload(configManager.getLanguageConfig().yaml());

                // Update chat format
                chatFormatService.updateFormat(configManager.getLanguageConfig().getChatFormat());

                // Refresh Default group
                configManager.getMainConfig().reload();
                final Group newDefault = buildDefaultGroup();
                DefaultGroup.set(newDefault);

                // Persist new Default group and re-warm cache
                groupRepository.upsertGroup(newDefault).thenCompose(__ -> groupCatalogCache.warmAll()).join();

                // re-prime online users
                primeOnlinePlayers();

                result.complete(true);
            } catch (Exception e) {
                result.complete(false);
            }
        });

        return result;
    }

    /* ===========
     * Shutdown
     * =========== */

    private void shutdown() {
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

    private void registerListeners(Listener @NotNull ... listeners) {
        for (Listener listener : listeners) {
            this.getServer().getPluginManager().registerEvents(listener, this);
        }
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

    public GroupRepository getGroupRepository() {
        return groupRepository;
    }

    public GroupCatalogCache getGroupCatalogCache() {
        return groupCatalogCache;
    }

    public GroupPlayerCache getGroupPlayerCache() {
        return groupPlayerCache;
    }

    public GroupPermissionCache getGroupPermissionCache() {
        return groupPermissionCache;
    }

    public ExpiryScheduler getExpiryScheduler() {
        return expiryScheduler;
    }

    public GroupService getGroupService() {
        return groupService;
    }

    public PrefixService getPrefixService() {
        return prefixService;
    }

    public ChatFormatService getChatFormatService() {
        return chatFormatService;
    }

    public PermissionService getPermissionService() {
        return permissionService;
    }

    public GroupSignService getSignService() {
        return signService;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public SignSelectionManager getSelectionManager() {
        return selectionManager;
    }

    public Commands getCommands() {
        return commands;
    }
}
