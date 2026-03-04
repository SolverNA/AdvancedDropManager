package dev.solverna.advanceddropmanager.provider;

import dev.solverna.advanceddropmanager.model.EnchantmentEntry;
import dev.solverna.advanceddropmanager.model.LootItem;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * Провайдер стандартных предметов Minecraft.
 * Создаёт ItemStack из Material и применяет displayName, lore и enchantments через MiniMessage.
 */
public class MinecraftItemProvider implements ItemProvider {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Logger LOGGER = Logger.getLogger("AdvancedDropManager");

    @Override
    public ItemStack createItem(LootItem lootItem, int amount) {
        ItemStack item = new ItemStack(lootItem.getMaterial(), amount);

        item.editMeta(meta -> {
            // Display Name
            if (hasContent(lootItem.getDisplayName())) {
                meta.displayName(MINI_MESSAGE.deserialize(lootItem.getDisplayName()));
            }

            // Lore
            if (hasContent(lootItem.getLore())) {
                List<net.kyori.adventure.text.Component> loreComponents = new ArrayList<>();
                for (String line : lootItem.getLore()) {
                    loreComponents.add(MINI_MESSAGE.deserialize(line));
                }
                meta.lore(loreComponents);
            }
        });

        // Enchantments (применяем отдельно, так как unsafeEnchant не через editMeta)
        if (hasContent(lootItem.getEnchantments())) {
            for (EnchantmentEntry entry : lootItem.getEnchantments()) {
                applyEnchantment(item, entry);
            }
        }

        return item;
    }

    /** Возвращает true если строка не null и не пустая. */
    private boolean hasContent(String s) {
        return s != null && !s.isEmpty();
    }

    /** Возвращает true если коллекция не null и не пустая. */
    private boolean hasContent(Collection<?> c) {
        return c != null && !c.isEmpty();
    }

    /**
     * Применяет одно зачарование к предмету с учётом шанса.
     */
    private void applyEnchantment(ItemStack item, EnchantmentEntry entry) {
        if (ThreadLocalRandom.current().nextDouble() * 100.0 < entry.getChance()) {
            Enchantment enchantment = resolveEnchantment(entry);
            if (enchantment != null) {
                // unsafe — позволяет применять зачарования к любому предмету и превышать лимит уровней
                item.addUnsafeEnchantment(enchantment, entry.getLevel());
            }
        }
    }

    /**
     * Ищет зачарование по имени.
     *
     * <p>Порядок разрешения:
     * <ol>
     *   <li>Если в {@code entry} задан {@code namespace} — ищет {@code namespace:enchantment}.</li>
     *   <li>Если {@code enchantment} содержит ":" — использует как готовый {@link NamespacedKey}.</li>
     *   <li>Иначе пробует {@code minecraft:enchantment}.</li>
     *   <li>Если не найдено — перебирает все зарегистрированные зачарования и ищет по имени ключа
     *       (позволяет найти зачарования из сторонних плагинов без явного namespace).</li>
     * </ol>
     */
    private Enchantment resolveEnchantment(EnchantmentEntry entry) {
        String name = entry.getEnchantment().toLowerCase();
        String ns = entry.getNamespace();
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);

        // 1. Явный namespace из поля "namespace:"
        if (ns != null && !ns.isEmpty()) {
            NamespacedKey key = new NamespacedKey(ns, name);
            Enchantment found = registry.get(key);
            if (found != null) return found;
            LOGGER.warning("[AdvancedDropManager] Зачарование не найдено: " + ns + ":" + name);
            return null;
        }

        // 2. Уже содержит ":" — полный NamespacedKey (например "mmoitems:sharpness")
        if (name.contains(":")) {
            NamespacedKey key = NamespacedKey.fromString(name);
            if (key != null) {
                Enchantment found = registry.get(key);
                if (found != null) return found;
            }
            LOGGER.warning("[AdvancedDropManager] Зачарование не найдено: " + name);
            return null;
        }

        // 3. Пробуем minecraft:name
        NamespacedKey vanillaKey = NamespacedKey.minecraft(name);
        Enchantment vanilla = registry.get(vanillaKey);
        if (vanilla != null) return vanilla;

        // 4. Fallback: перебираем все зарегистрированные зачарования по ключу
        //    Помогает найти зачарования сторонних плагинов без явного namespace
        for (Enchantment ench : registry) {
            if (ench.getKey().getKey().equalsIgnoreCase(name)) {
                return ench;
            }
        }

        LOGGER.warning("[AdvancedDropManager] Неизвестное зачарование: \"" + entry.getEnchantment()
                + "\". Убедитесь, что плагин с этим зачарованием загружен и укажите namespace явно.");
        return null;
    }
}
