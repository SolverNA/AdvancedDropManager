package dev.solverna.advanceddropmanager.provider;

import dev.solverna.advanceddropmanager.model.EnchantmentEntry;
import dev.solverna.advanceddropmanager.model.LootItem;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Провайдер стандартных предметов Minecraft.
 * Создаёт ItemStack из Material и применяет displayName, lore и enchantments через MiniMessage.
 */
public class MinecraftItemProvider implements ItemProvider {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Random RANDOM = new Random();
    private static final Logger LOGGER = Logger.getLogger("AdvancedDropManager");

    @Override
    public ItemStack createItem(LootItem lootItem, int amount) {
        ItemStack item = new ItemStack(lootItem.getMaterial(), amount);

        item.editMeta(meta -> {
            // Display Name
            if (lootItem.getDisplayName() != null && !lootItem.getDisplayName().isEmpty()) {
                meta.displayName(MINI_MESSAGE.deserialize(lootItem.getDisplayName()));
            }

            // Lore
            if (lootItem.getLore() != null && !lootItem.getLore().isEmpty()) {
                List<net.kyori.adventure.text.Component> loreComponents = new ArrayList<>();
                for (String line : lootItem.getLore()) {
                    loreComponents.add(MINI_MESSAGE.deserialize(line));
                }
                meta.lore(loreComponents);
            }
        });

        // Enchantments (применяем отдельно, так как unsafeEnchant не через editMeta)
        if (lootItem.getEnchantments() != null && !lootItem.getEnchantments().isEmpty()) {
            for (EnchantmentEntry entry : lootItem.getEnchantments()) {
                applyEnchantment(item, entry);
            }
        }

        return item;
    }

    /**
     * Применяет одно зачарование к предмету с учётом шанса.
     */
    private void applyEnchantment(ItemStack item, EnchantmentEntry entry) {
        if (RANDOM.nextDouble() * 100.0 < entry.getChance()) {
            Enchantment enchantment = resolveEnchantment(entry.getEnchantment());
            if (enchantment != null) {
                // unsafe — позволяет применять зачарования к любому предмету и превышать лимит уровней
                item.addUnsafeEnchantment(enchantment, entry.getLevel());
            } else {
                LOGGER.warning("[AdvancedDropManager] Неизвестное зачарование: " + entry.getEnchantment());
            }
        }
    }

    /**
     * Ищет зачарование по имени через Registry (Paper 1.21+).
     * Поддерживает форматы: "SHARPNESS", "minecraft:sharpness".
     */
    private Enchantment resolveEnchantment(String name) {
        // Нормализуем: SHARPNESS -> minecraft:sharpness
        String normalized = name.toLowerCase();
        if (!normalized.contains(":")) {
            normalized = "minecraft:" + normalized;
        }
        NamespacedKey key = NamespacedKey.fromString(normalized);
        if (key == null) return null;
        return Bukkit.getRegistry(Enchantment.class).get(key);
    }
}
