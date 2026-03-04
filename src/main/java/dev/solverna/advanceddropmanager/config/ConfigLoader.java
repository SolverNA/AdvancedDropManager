package dev.solverna.advanceddropmanager.config;

import dev.solverna.advanceddropmanager.model.EnchantmentEntry;
import dev.solverna.advanceddropmanager.model.FortuneAffects;
import dev.solverna.advanceddropmanager.model.LootItem;
import dev.solverna.advanceddropmanager.model.LootTable;
import dev.solverna.advanceddropmanager.model.RollType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Logger;

/**
 * Загрузчик конфигурации плагина.
 * Парсит config.yml и создаёт карты LootTable для блоков и мобов.
 */
public class ConfigLoader {

    private final JavaPlugin plugin;
    private final Logger logger;

    private volatile Map<Material, LootTable> blockTables;
    private volatile Map<EntityType, LootTable> mobTables;

    public ConfigLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.blockTables = new HashMap<>();
        this.mobTables = new HashMap<>();
    }

    /**
     * Загружает или перезагружает конфигурацию.
     */
    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();

        blockTables = loadBlockSection(config);
        mobTables = loadMobSection(config);

        logger.info("Загружено " + blockTables.size() + " таблиц дропа для блоков.");
        logger.info("Загружено " + mobTables.size() + " таблиц дропа для мобов.");
    }

    private Map<Material, LootTable> loadBlockSection(FileConfiguration config) {
        Map<Material, LootTable> result = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("blocks");
        if (section == null) return result;

        for (String key : section.getKeys(false)) {
            ConfigurationSection entrySection = section.getConfigurationSection(key);
            if (entrySection == null) continue;
            try {
                Material material = Material.valueOf(key.toUpperCase());
                LootTable table = parseLootTable(entrySection, key);
                if (table != null) result.put(material, table);
            } catch (IllegalArgumentException e) {
                logger.warning("Неизвестный Material '" + key + "' в секции 'blocks'. Пропускаем.");
            }
        }
        return result;
    }

    private Map<EntityType, LootTable> loadMobSection(FileConfiguration config) {
        Map<EntityType, LootTable> result = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("mobs");
        if (section == null) return result;

        for (String key : section.getKeys(false)) {
            ConfigurationSection entrySection = section.getConfigurationSection(key);
            if (entrySection == null) continue;
            try {
                EntityType entityType = EntityType.valueOf(key.toUpperCase());
                LootTable table = parseLootTable(entrySection, key);
                if (table != null) result.put(entityType, table);
            } catch (IllegalArgumentException e) {
                logger.warning("Неизвестный EntityType '" + key + "' в секции 'mobs'. Пропускаем.");
            }
        }

        return result;
    }

    private LootTable parseLootTable(ConfigurationSection section, String key) {
        LootTable table = new LootTable();

        table.setReplaceDefault(section.getBoolean("replace-default", true));
        table.setIgnoreSilkTouch(section.getBoolean("ignore-silk-touch", true));

        String rollTypeStr = section.getString("roll-type", "INDEPENDENT").toUpperCase();
        try {
            table.setRollType(RollType.valueOf(rollTypeStr));
        } catch (IllegalArgumentException e) {
            logger.warning("Неизвестный roll-type '" + rollTypeStr + "' для '" + key + "'. Используем INDEPENDENT.");
            table.setRollType(RollType.INDEPENDENT);
        }

        List<Map<?, ?>> lootList = section.getMapList("loot");
        List<LootItem> items = new ArrayList<>();

        for (Map<?, ?> lootMap : lootList) {
            LootItem item = parseLootItem(lootMap, key);
            if (item != null) {
                items.add(item);
            }
        }

        // Валидация: в WEIGHTED-режиме fortune: true может быть только у одного предмета
        if (table.getRollType() == RollType.WEIGHTED) {
            long fortuneCount = items.stream().filter(LootItem::isFortune).count();
            if (fortuneCount > 1) {
                logger.warning("В WEIGHTED-группе '" + key + "' fortune: true у " + fortuneCount +
                        " предметов. Разрешён только один! Отключаем fortune у всех кроме первого.");
                boolean foundFirst = false;
                for (LootItem item : items) {
                    if (item.isFortune()) {
                        if (foundFirst) {
                            item.setFortune(false);
                        } else {
                            foundFirst = true;
                        }
                    }
                }
            }
        }

        table.setLoot(items);
        return table;
    }

    @SuppressWarnings("unchecked")
    private LootItem parseLootItem(Map<?, ?> map, String parentKey) {
        LootItem item = new LootItem();

        // ID
        item.setId(getStringValue(map, "id", "unknown"));

        // Provider
        item.setProvider(getStringValue(map, "provider", "MINECRAFT"));

        // Material
        String materialStr = getStringValue(map, "material", null);
        if (materialStr == null) {
            logger.warning("Предмет без material в группе '" + parentKey + "'. Пропускаем.");
            return null;
        }
        try {
            item.setMaterial(Material.valueOf(materialStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            logger.warning("Неизвестный material '" + materialStr + "' в группе '" + parentKey + "'. Пропускаем.");
            return null;
        }

        // Chance / Weight
        item.setChance(getDoubleValue(map, "chance", 100.0));
        item.setWeight(getDoubleValue(map, "weight", 1.0));

        // Fortune
        item.setFortune(getBooleanValue(map, "fortune", false));
        item.setFortuneFactor(getDoubleValue(map, "fortune-factor", 1.0));

        String fortuneAffectsStr = getStringValue(map, "fortune-affects", "CHANCE").toUpperCase();
        try {
            item.setFortuneAffects(FortuneAffects.valueOf(fortuneAffectsStr));
        } catch (IllegalArgumentException e) {
            logger.warning("Неизвестный fortune-affects '" + fortuneAffectsStr + "'. Используем CHANCE.");
            item.setFortuneAffects(FortuneAffects.CHANCE);
        }

        // Amount (диапазон)
        Object amountObj = map.get("amount");
        if (amountObj != null) {
            item.setAmount(amountObj.toString());
        }

        // Drop Count (взвешенное количество)
        Object dropCountObj = map.get("drop-count");
        if (dropCountObj instanceof Map<?, ?> dropCountMap) {
            if (map.get("amount") != null) {
                logger.warning("Предмет '" + item.getId() + "': указаны и 'drop-count' и 'amount' одновременно. " +
                        "Используется 'drop-count'.");
            }
            Map<Integer, Integer> parsedDropCount = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : dropCountMap.entrySet()) {
                try {
                    int count = Integer.parseInt(entry.getKey().toString());
                    int weight = Integer.parseInt(entry.getValue().toString());
                    parsedDropCount.put(count, weight);
                } catch (NumberFormatException e) {
                    logger.warning("Ошибка парсинга drop-count для предмета '" + item.getId() + "': " +
                            entry.getKey() + " -> " + entry.getValue());
                }
            }
            item.setDropCount(parsedDropCount);
        }

        // Display Name
        item.setDisplayName(getStringValue(map, "display-name", null));

        // Lore
        Object loreObj = map.get("lore");
        if (loreObj instanceof List<?> loreList) {
            List<String> loreLines = new ArrayList<>();
            for (Object line : loreList) {
                if (line != null) {
                    loreLines.add(line.toString());
                }
            }
            item.setLore(loreLines);
        }

        // Enchantments
        Object enchObj = map.get("enchantments");
        if (enchObj instanceof List<?> enchList) {
            List<EnchantmentEntry> entries = new ArrayList<>();
            for (Object enchRaw : enchList) {
                if (enchRaw instanceof Map<?, ?> enchMap) {
                    String enchName = getStringValue(enchMap, "enchantment", null);
                    if (enchName == null) {
                        logger.warning("Зачарование без поля 'enchantment' в группе '" + parentKey + "'. Пропускаем.");
                        continue;
                    }
                    int level = (int) getDoubleValue(enchMap, "level", 1);
                    double enchChance = getDoubleValue(enchMap, "chance", 100.0);
                    EnchantmentEntry entry = new EnchantmentEntry(enchName, level, enchChance);
                    // Опциональный namespace для кастомных плагинов
                    String ns = getStringValue(enchMap, "namespace", null);
                    if (ns != null && !ns.isEmpty()) {
                        entry.setNamespace(ns.toLowerCase());
                    }
                    entries.add(entry);
                }
            }
            item.setEnchantments(entries);
        }

        return item;
    }

    // --- Вспомогательные методы для безопасного чтения из Map ---

    private String getStringValue(Map<?, ?> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private double getDoubleValue(Map<?, ?> map, String key, double defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean getBooleanValue(Map<?, ?> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value.toString());
    }

    // --- Публичные геттеры ---

    public Map<Material, LootTable> getBlockTables() {
        return blockTables;
    }

    public Map<EntityType, LootTable> getMobTables() {
        return mobTables;
    }
}
