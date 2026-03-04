package dev.solverna.advanceddropmanager.model;

/**
 * Определяет, на что именно влияет зачарование Удача/Добыча.
 */
public enum FortuneAffects {
    /**
     * Влияет только на вероятность выпадения.
     */
    CHANCE,

    /**
     * Влияет только на количество предметов.
     */
    AMOUNT,

    /**
     * Влияет и на вероятность, и на количество.
     */
    BOTH
}
