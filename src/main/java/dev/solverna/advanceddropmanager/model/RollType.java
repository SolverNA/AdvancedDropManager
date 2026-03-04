package dev.solverna.advanceddropmanager.model;

/**
 * Тип расчёта дропа для группы лута.
 */
public enum RollType {
    /**
     * Один из списка - суммируются веса, выбирается ровно один предмет.
     */
    WEIGHTED,

    /**
     * Каждый сам за себя - для каждого предмета бросается отдельная кость.
     */
    INDEPENDENT
}
