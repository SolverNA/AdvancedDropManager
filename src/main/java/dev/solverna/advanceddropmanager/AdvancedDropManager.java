package dev.solverna.advanceddropmanager;

import dev.solverna.advanceddropmanager.command.AdvancedDropCommand;
import dev.solverna.advanceddropmanager.config.ConfigLoader;
import dev.solverna.advanceddropmanager.engine.DropEngine;
import dev.solverna.advanceddropmanager.listener.BlockDropListener;
import dev.solverna.advanceddropmanager.listener.MobDropListener;
import dev.solverna.advanceddropmanager.provider.ProviderRegistry;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Главный класс плагина AdvancedDropManager.
 * Инструмент для полной подмены ванильного дропа блоков и мобов.
 */
public class AdvancedDropManager extends JavaPlugin {

    private ConfigLoader configLoader;
    private ProviderRegistry providerRegistry;
    private DropEngine dropEngine;

    @Override
    public void onEnable() {
        // Инициализация провайдеров
        providerRegistry = new ProviderRegistry();

        // Загрузка конфигурации
        configLoader = new ConfigLoader(this);
        configLoader.load();

        // Инициализация движка дропа
        dropEngine = new DropEngine(providerRegistry, getLogger());

        // Регистрация слушателей событий
        getServer().getPluginManager().registerEvents(
                new BlockDropListener(configLoader, dropEngine), this);
        getServer().getPluginManager().registerEvents(
                new MobDropListener(configLoader, dropEngine), this);

        // Регистрация команды
        PluginCommand admCommand = getCommand("adm");
        if (admCommand != null) {
            AdvancedDropCommand commandHandler = new AdvancedDropCommand(configLoader);
            admCommand.setExecutor(commandHandler);
            admCommand.setTabCompleter(commandHandler);
        }

        getLogger().info("AdvancedDropManager v" + getPluginMeta().getVersion() + " успешно загружен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("AdvancedDropManager выключен.");
    }

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }

    public ProviderRegistry getProviderRegistry() {
        return providerRegistry;
    }

    public DropEngine getDropEngine() {
        return dropEngine;
    }
}
