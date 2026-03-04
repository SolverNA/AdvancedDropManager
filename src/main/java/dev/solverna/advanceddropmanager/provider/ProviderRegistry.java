package dev.solverna.advanceddropmanager.provider;

import java.util.HashMap;
import java.util.Map;

/**
 * Реестр провайдеров предметов.
 * Позволяет регистрировать и получать провайдеры по имени.
 */
public class ProviderRegistry {

    private final Map<String, ItemProvider> providers = new HashMap<>();

    public ProviderRegistry() {
        // Регистрируем стандартный провайдер
        register("MINECRAFT", new MinecraftItemProvider());
    }

    /**
     * Регистрирует провайдер по имени.
     *
     * @param name     имя провайдера (например, "MINECRAFT", "ITEMSADDER")
     * @param provider реализация провайдера
     */
    public void register(String name, ItemProvider provider) {
        providers.put(name.toUpperCase(), provider);
    }

    /**
     * Получает провайдер по имени.
     *
     * @param name имя провайдера
     * @return провайдер или null, если не найден
     */
    public ItemProvider getProvider(String name) {
        return providers.get(name.toUpperCase());
    }
}
