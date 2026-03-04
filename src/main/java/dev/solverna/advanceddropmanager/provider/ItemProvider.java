package dev.solverna.advanceddropmanager.provider;

import dev.solverna.advanceddropmanager.model.LootItem;
import org.bukkit.inventory.ItemStack;

/**
 * Интерфейс провайдера предметов.
 * Позволяет расширять плагин для поддержки кастомных предметов
 * из других плагинов или модов.
 */
public interface ItemProvider {

    /**
     * Создаёт ItemStack на основе модели LootItem.
     *
     * @param lootItem модель предмета из конфига
     * @param amount   количество предметов
     * @return готовый ItemStack
     */
    ItemStack createItem(LootItem lootItem, int amount);
}
