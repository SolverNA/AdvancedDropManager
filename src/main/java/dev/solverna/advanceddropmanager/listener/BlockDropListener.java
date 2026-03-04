package dev.solverna.advanceddropmanager.listener;

import dev.solverna.advanceddropmanager.config.ConfigLoader;
import dev.solverna.advanceddropmanager.engine.DropEngine;
import dev.solverna.advanceddropmanager.model.LootTable;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Слушатель события разрушения блоков.
 * Подменяет стандартный дроп на настроенный через конфиг.
 */
public class BlockDropListener implements Listener {

    private final ConfigLoader configLoader;
    private final DropEngine dropEngine;

    public BlockDropListener(ConfigLoader configLoader, DropEngine dropEngine) {
        this.configLoader = configLoader;
        this.dropEngine = dropEngine;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material blockType = block.getType();

        LootTable table = configLoader.getBlockTables().get(blockType);
        if (table == null) return;

        Player player = event.getPlayer();

        // В креативном режиме предметы не выпадают
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Если у игрока Silk Touch и для этого блока настроен ignore-silk-touch - не вмешиваемся
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (table.isIgnoreSilkTouch() && tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
            return;
        }

        // Определяем уровень Fortune
        int fortuneLevel = tool.getEnchantmentLevel(Enchantment.FORTUNE);

        // Отменяем стандартный дроп если нужно
        if (table.isReplaceDefault()) {
            event.setDropItems(false);
        }

        // Рассчитываем и дропаем предметы из центра блока
        List<ItemStack> drops = dropEngine.calculateDrops(table, fortuneLevel);
        Location center = block.getLocation().add(0.5, 0.0, 0.5);
        for (ItemStack drop : drops) {
            block.getWorld().dropItemNaturally(center, drop);
        }
    }
}
