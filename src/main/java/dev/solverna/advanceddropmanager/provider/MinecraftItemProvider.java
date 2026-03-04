package dev.solverna.advanceddropmanager.provider;

import dev.solverna.advanceddropmanager.model.LootItem;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemStack;

/**
 * Провайдер стандартных предметов Minecraft.
 * Создаёт ItemStack из Material и применяет displayName через MiniMessage.
 */
public class MinecraftItemProvider implements ItemProvider {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Override
    public ItemStack createItem(LootItem lootItem, int amount) {
        ItemStack item = new ItemStack(lootItem.getMaterial(), amount);

        if (lootItem.getDisplayName() != null && !lootItem.getDisplayName().isEmpty()) {
            item.editMeta(meta ->
                    meta.displayName(MINI_MESSAGE.deserialize(lootItem.getDisplayName()))
            );
        }

        return item;
    }
}
