package dev.solverna.advanceddropmanager.model;

/**
 * Модель одного зачарования для предмета лута.
 * Содержит название зачарования, уровень и шанс применения.
 */
public class EnchantmentEntry {

    private String enchantment;
    private int level;
    private double chance;

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
     * Название зачарования в формате Bukkit (например, SHARPNESS, UNBREAKING).
     */
    public String getEnchantment() {
        return enchantment;
    }

    public void setEnchantment(String enchantment) {
        this.enchantment = enchantment;
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
