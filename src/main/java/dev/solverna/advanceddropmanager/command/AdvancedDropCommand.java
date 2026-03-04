package dev.solverna.advanceddropmanager.command;

import dev.solverna.advanceddropmanager.config.ConfigLoader;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Команда /adm для управления плагином.
 * Поддерживает подкоманду reload для перезагрузки конфигурации.
 */
public class AdvancedDropCommand implements CommandExecutor, TabCompleter {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final ConfigLoader configLoader;
    private final JavaPlugin plugin;

    public AdvancedDropCommand(ConfigLoader configLoader, JavaPlugin plugin) {
        this.configLoader = configLoader;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(MINI_MESSAGE.deserialize(
                    "<gold>AdvancedDropManager</gold> <gray>v" + plugin.getPluginMeta().getVersion() + "</gray>"));
            sender.sendMessage(MINI_MESSAGE.deserialize(
                    "<yellow>/adm reload</yellow> <gray>— перезагрузить конфигурацию</gray>"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("advanceddropmanager.reload")) {
                sender.sendMessage(MINI_MESSAGE.deserialize(
                        "<red>У вас нет прав для выполнения этой команды!</red>"));
                return true;
            }

            try {
                configLoader.load();
                sender.sendMessage(MINI_MESSAGE.deserialize(
                        "<green>Конфигурация успешно перезагружена!</green>"));
            } catch (Exception e) {
                // Экранируем сообщение об ошибке, чтобы спецсимволы <> не сломали MiniMessage
                String safeMessage = e.getMessage() == null ? "неизвестная ошибка"
                        : e.getMessage().replace("<", "\\<").replace(">", "\\>");
                sender.sendMessage(MINI_MESSAGE.deserialize(
                        "<red>Ошибка при перезагрузке конфигурации: " + safeMessage + "</red>"));
            }
            return true;
        }

        sender.sendMessage(MINI_MESSAGE.deserialize(
                "<red>Неизвестная подкоманда. Используйте /adm reload</red>"));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("reload");
        }
        return Collections.emptyList();
    }
}
