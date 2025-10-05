package com.github.manu585.manusgroups.messaging;

import com.github.manu585.manusgroups.service.MessageService;
import com.github.manu585.manusgroups.service.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MessageServiceTest {
    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Test
    void rendersExistingKeyWithStringPlaceholder() {
        final YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("Hello", "<yellow>Hello</yellow> <gray><name></gray>!");
        final MessageService messageService = new MessageService(yaml);

        final Component component = messageService.formatToComponent("Hello", Msg.str("name", "Manu"));
        assertThat(plain(component)).isEqualTo("Hello Manu!");
    }

    @Test
    void rendersComponentPlaceholders() {
        final YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("Line", "<prefix> <gray>World</gray>");
        final MessageService messageService = new MessageService(yaml);

        final Component prefix = Component.text("[Prefix]");
        final Component component = messageService.formatToComponent("Line", Msg.comp("prefix", prefix));

        assertThat(plain(component)).isEqualTo("[Prefix] World");
    }

    @Test
    void missingKeyProducesFallback() {
        final YamlConfiguration yaml = new YamlConfiguration();
        final MessageService messageService = new MessageService(yaml);

        final Component component = messageService.formatToComponent("Does.Not.Exist");

        assertThat(plain(component)).contains("Missing lang key").contains("Does.Not.Exist");
    }

    @Test
    void permanentHelperInsertsSuffix() {
        final YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("Perm", "<gray>Group:</gray> <yellow><group></yellow><permanent>");
        final MessageService messageService = new MessageService(yaml);

        // permanent = true
        final Component component1 = messageService.formatToComponent("Perm", Msg.str("group", "vip"), Msg.permanent(true));
        assertThat(plain(component1)).isEqualTo("Group: vip (permanent)");

        // permanent = false
        final Component component2 = messageService.formatToComponent("Perm", Msg.str("group", "vip"), Msg.permanent(false));
        assertThat(plain(component2)).isEqualTo("Group: vip");
    }

    @Test
    void renderWithResolversApiAlsoWorks() {
        final YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("Hi", "Hi <name>");
        final MessageService messageService = new MessageService(yaml);

        final Component component = messageService.render("Hi", TagResolver.resolver("name", Msg.str("name", "Alex").tag()));

        assertThat(plain(component)).isEqualTo("Hi Alex");
    }
}
