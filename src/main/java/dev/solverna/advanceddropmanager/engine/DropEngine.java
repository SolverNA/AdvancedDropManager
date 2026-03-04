package dev.solverna.advanceddropmanager.engine;

import dev.solverna.advanceddropmanager.model.FortuneAffects;
import dev.solverna.advanceddropmanager.model.LootItem;
import dev.solverna.advanceddropmanager.model.LootTable;
import dev.solverna.advanceddropmanager.model.RollType;
import dev.solverna.advanceddropmanager.provider.ItemProvider;
import dev.solverna.advanceddropmanager.provider.ProviderRegistry;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * Движок расчёта дропа.
 * Реализует логику WEIGHTED и INDEPENDENT режимов,
 * расчёт влияния удачи и определение количества предметов.
 */
public class DropEngine {

    private final ProviderRegistry providerRegistry;
    private final Logger logger;

    public DropEngine(ProviderRegistry providerRegistry, Logger logger) {
        this.providerRegistry = providerRegistry;
        this.logger = logger;
    }

    /**
     * Рассчитывает дроп для таблицы лута.
     *
     * @param table        таблица лута
     * @param fortuneLevel уровень зачарования Удача/Добыча (0 если нет)
     * @return список предметов для дропа
     */
    public List<ItemStack> calculateDrops(LootTable table, int fortuneLevel) {
        return switch (table.getRollType()) {
            case WEIGHTED -> calculateWeighted(table, fortuneLevel);
            case INDEPENDENT -> calculateIndependent(table, fortuneLevel);
        };
    }

    /**
     * Режим WEIGHTED: суммируются веса, выбирается ровно один предмет.
     * Если есть предмет с fortune: true и удача активна — сначала проверяется его шанс.
     * При провале — fortune-предмет ИСКЛЮЧАЕТСЯ из обычного пула весов (согласно ТЗ).
     */
    private List<ItemStack> calculateWeighted(LootTable table, int fortuneLevel) {
        List<ItemStack> result = new ArrayList<>();
        List<LootItem> items = table.getLoot();

        if (items.isEmpty()) return result;

        LootItem fortuneItem = null;

        // Ищем fortune-предмет и проверяем удачу
        if (fortuneLevel > 0) {
            for (LootItem item : items) {
                if (item.isFortune()) {
                    fortuneItem = item;
                    double modifiedChance = resolveChance(item, fortuneLevel);
                    if (rollChance(modifiedChance)) {
                        int amount = resolveAmount(item, fortuneLevel);
                        ItemStack stack = createItemStack(item, amount);
                        if (stack != null) result.add(stack);
                        return result;
                    }
                    break; // В WEIGHTED только один предмет с fortune
                }
            }
        }

        // Стандартный взвешенный выбор — fortune-предмет исключается из пула если удача была активна
        double totalWeight = 0;
        for (LootItem item : items) {
            if (item != fortuneItem) {
                totalWeight += item.getWeight();
            }
        }

        if (totalWeight <= 0) return result;

        double roll = ThreadLocalRandom.current().nextDouble() * totalWeight;
        double cumulative = 0;

        for (LootItem item : items) {
            if (item == fortuneItem) continue;
            cumulative += item.getWeight();
            if (roll < cumulative) {
                int amount = resolveAmount(item, 0);
                ItemStack stack = createItemStack(item, amount);
                if (stack != null) result.add(stack);
                return result;
            }
        }

        return result;
    }

    /**
     * Режим INDEPENDENT: каждый предмет бросает свою кость отдельно.
     */
    private List<ItemStack> calculateIndependent(LootTable table, int fortuneLevel) {
        List<ItemStack> result = new ArrayList<>();

        for (LootItem item : table.getLoot()) {
            double chance = resolveChance(item, fortuneLevel);

            if (rollChance(chance)) {
                int amount = resolveAmount(item, fortuneLevel);
                ItemStack stack = createItemStack(item, amount);
                if (stack != null) {
                    result.add(stack);
                }
            }
        }

        return result;
    }

    /**
     * Рассчитывает итоговый шанс с учётом удачи.
     * Формула: Base × (1 + Level × Factor)
     */
    private double resolveChance(LootItem item, int fortuneLevel) {
        double baseChance = item.getChance();

        if (item.isFortune() && fortuneLevel > 0) {
            FortuneAffects affects = item.getFortuneAffects();
            if (affects == FortuneAffects.CHANCE || affects == FortuneAffects.BOTH) {
                baseChance *= fortuneMultiplier(item, fortuneLevel);
            }
        }

        // Ограничиваем шанс максимумом 100%
        return Math.min(baseChance, 100.0);
    }

    /**
     * Определяет количество предметов для дропа.
     * Поддерживает диапазон "min-max" и взвешенный drop-count.
     */
    private int resolveAmount(LootItem item, int fortuneLevel) {
        int baseAmount;

        if (!item.getDropCount().isEmpty()) {
            baseAmount = resolveWeightedCount(item.getDropCount());
        } else if (item.getAmount() != null && !item.getAmount().isEmpty()) {
            baseAmount = resolveRangeAmount(item.getAmount());
        } else {
            baseAmount = 1;
        }

        // Применяем влияние удачи на количество
        if (item.isFortune() && fortuneLevel > 0) {
            FortuneAffects affects = item.getFortuneAffects();
            if (affects == FortuneAffects.AMOUNT || affects == FortuneAffects.BOTH) {
                baseAmount = (int) Math.round(baseAmount * fortuneMultiplier(item, fortuneLevel));
            }
        }

        return Math.max(1, baseAmount);
    }

    /**
     * Вычисляет мультипликатор удачи: (1 + уровень × коэффициент).
     * Используется и для шанса, и для количества — единая формула из ТЗ.
     */
    private double fortuneMultiplier(LootItem item, int fortuneLevel) {
        return 1.0 + fortuneLevel * item.getFortuneFactor();
    }

    /**
     * Выбирает количество из взвешенной карты drop-count.
     */
    private int resolveWeightedCount(Map<Integer, Integer> dropCount) {
        int totalWeight = 0;
        for (int weight : dropCount.values()) {
            totalWeight += weight;
        }

        if (totalWeight <= 0) return 1;

        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        int cumulative = 0;

        for (Map.Entry<Integer, Integer> entry : dropCount.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) {
                return entry.getKey();
            }
        }

        return 1;
    }

    /**
     * Выбирает случайное число из диапазона "min-max".
     * Если min > max — автоматически меняет местами.
     */
    private int resolveRangeAmount(String amountStr) {
        try {
            if (amountStr.contains("-")) {
                String[] parts = amountStr.split("-", 2);
                int min = Integer.parseInt(parts[0].trim());
                int max = Integer.parseInt(parts[1].trim());
                if (min > max) {
                    logger.warning("В amount '" + amountStr + "' min > max, меняем местами.");
                    int tmp = min; min = max; max = tmp;
                }
                if (min == max) return min;
                return ThreadLocalRandom.current().nextInt(min, max + 1);
            } else {
                return Integer.parseInt(amountStr.trim());
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            logger.warning("Ошибка парсинга amount: '" + amountStr + "'. Возвращаем 1.");
            return 1;
        }
    }

    /**
     * Бросает кость на шанс (0-100%).
     */
    private boolean rollChance(double chance) {
        if (chance >= 100.0) return true;
        if (chance <= 0.0) return false;
        return ThreadLocalRandom.current().nextDouble() * 100.0 < chance;
    }

    /**
     * Создаёт ItemStack через провайдер.
     */
    private ItemStack createItemStack(LootItem item, int amount) {
        ItemProvider provider = providerRegistry.getProvider(item.getProvider());
        if (provider == null) {
            logger.warning("Провайдер '" + item.getProvider() + "' не найден для предмета '" + item.getId() + "'.");
            return null;
        }
        return provider.createItem(item, amount);
    }
}
