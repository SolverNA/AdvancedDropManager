package dev.solverna.advanceddropmanager.listener;

import dev.solverna.advanceddropmanager.config.ConfigLoader;
import dev.solverna.advanceddropmanager.engine.DropEngine;
import dev.solverna.advanceddropmanager.model.LootTable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Слушатель события смерти мобов.
 * Подменяет стандартный дроп моба на настроенный через конфиг.
 */
public class MobDropListener implements Listener {

    private final ConfigLoader configLoader;
    private final DropEngine dropEngine;

    public MobDropListener(ConfigLoader configLoader, DropEngine dropEngine) {
        this.configLoader = configLoader;
        this.dropEngine = dropEngine;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        EntityType entityType = entity.getType();

        LootTable table = configLoader.getMobTables().get(entityType);
        if (table == null) return;

        // Определяем уровень Looting от оружия убийцы
        int lootingLevel = 0;
        Player killer = entity.getKiller();
        if (killer != null) {
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            lootingLevel = weapon.getEnchantmentLevel(Enchantment.LOOTING);
        }

        // Отменяем стандартный дроп если нужно
        if (table.isReplaceDefault()) {
            event.getDrops().clear();
        }

        // Рассчитываем и добавляем предметы
        List<ItemStack> drops = dropEngine.calculateDrops(table, lootingLevel);
        event.getDrops().addAll(drops);
    }
}
