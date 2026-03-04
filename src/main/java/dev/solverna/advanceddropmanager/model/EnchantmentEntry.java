package dev.solverna.advanceddropmanager.model;

/**
 * Модель одного зачарования для предмета лута.
 * Содержит название зачарования, уровень, шанс применения и опциональный namespace.
 *
 * <p>Поддерживаемые форматы поля {@code enchantment}:
 * <ul>
 *   <li>{@code SHARPNESS} — ванильное зачарование Minecraft</li>
 *   <li>{@code minecraft:sharpness} — явный namespace Minecraft</li>
 *   <li>{@code mmoitems:mmo_sharpness} — зачарование из стороннего плагина</li>
 *   <li>{@code itemsadder:my_ench} — зачарование из ItemsAdder и т.д.</li>
 * </ul>
 * Можно также задать namespace отдельным полем {@code namespace}.
 */
public class EnchantmentEntry {

    private String enchantment;
    private int level;
    private double chance;
    /**
     * Опциональный namespace плагина/мода (например, "mmoitems", "itemsadder").
     * Если задан — итоговый ключ будет {@code namespace:enchantment}.
     * Если {@code enchantment} уже содержит ":" — это поле игнорируется.
     */
    private String namespace;

    public EnchantmentEntry() {
        this.level = 1;
        this.chance = 100.0;
    }

    public EnchantmentEntry(String enchantment, int level, double chance) {
        this.enchantment = enchantment;
        this.level = level;
        this.chance = chance;
    }

    /**
     * Название зачарования. Может содержать namespace через ":" или быть просто именем.
     */
    public String getEnchantment() {
        return enchantment;
    }

    public void setEnchantment(String enchantment) {
        this.enchantment = enchantment;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Уровень зачарования (1 и выше).
     */
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Шанс применения зачарования в % (0.0 – 100.0).
     */
    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }
}
