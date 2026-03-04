package dev.solverna.advanceddropmanager.model;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Модель одного предмета в таблице лута.
 */
public class LootItem {

    private String id;
    private String provider;
    private Material material;
    private double chance;
    private double weight;
    private boolean fortune;
    private double fortuneFactor;
    private FortuneAffects fortuneAffects;
    private String amount;
    private Map<Integer, Integer> dropCount;
    private String displayName;
    private List<String> lore;
    private List<EnchantmentEntry> enchantments;

    public LootItem() {
        this.provider = "MINECRAFT";
        this.chance = 100.0;
        this.weight = 1.0;
        this.fortune = false;
        this.fortuneFactor = 1.0;
        this.fortuneAffects = FortuneAffects.CHANCE;
        this.dropCount = new LinkedHashMap<>();
        this.lore = new ArrayList<>();
        this.enchantments = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isFortune() {
        return fortune;
    }

    public void setFortune(boolean fortune) {
        this.fortune = fortune;
    }

    public double getFortuneFactor() {
        return fortuneFactor;
    }

    public void setFortuneFactor(double fortuneFactor) {
        this.fortuneFactor = fortuneFactor;
    }

    public FortuneAffects getFortuneAffects() {
        return fortuneAffects;
    }

    public void setFortuneAffects(FortuneAffects fortuneAffects) {
        this.fortuneAffects = fortuneAffects;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Map<Integer, Integer> getDropCount() {
        return dropCount;
    }

    public void setDropCount(Map<Integer, Integer> dropCount) {
        this.dropCount = dropCount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public List<EnchantmentEntry> getEnchantments() {
        return enchantments;
    }

    public void setEnchantments(List<EnchantmentEntry> enchantments) {
        this.enchantments = enchantments;
    }
}
