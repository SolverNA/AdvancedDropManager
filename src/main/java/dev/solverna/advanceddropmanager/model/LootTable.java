package dev.solverna.advanceddropmanager.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Таблица лута для конкретного блока или моба.
 */
public class LootTable {

    private boolean replaceDefault;
    private RollType rollType;
    private List<LootItem> loot;

    public LootTable() {
        this.replaceDefault = true;
        this.rollType = RollType.INDEPENDENT;
        this.loot = new ArrayList<>();
    }

    public boolean isReplaceDefault() {
        return replaceDefault;
    }

    public void setReplaceDefault(boolean replaceDefault) {
        this.replaceDefault = replaceDefault;
    }

    public RollType getRollType() {
        return rollType;
    }

    public void setRollType(RollType rollType) {
        this.rollType = rollType;
    }

    public List<LootItem> getLoot() {
        return loot;
    }

    public void setLoot(List<LootItem> loot) {
        this.loot = loot;
    }
}
