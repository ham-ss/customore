package marvtechnology.marvore.events;

import marvtechnology.marvore.managers.CustomOreManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CraftRestrictionListener implements Listener {

    private final CustomOreManager customOreManager;

    public CraftRestrictionListener(CustomOreManager customOreManager) {
        this.customOreManager = customOreManager;
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getInventory() instanceof CraftingInventory) {
            CraftingInventory inventory = (CraftingInventory) event.getInventory();
            ItemStack[] matrix = inventory.getMatrix();

            for (ItemStack item : matrix) {
                if (isCustomOre(item)) {
                    inventory.setResult(new ItemStack(Material.AIR));
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        if (isCustomOre(result)) {
            event.setResult(null);
            event.getView().getPlayer().sendMessage("");
        }
    }

    @EventHandler
    public void onPrepareGrindstone(PrepareGrindstoneEvent event) {
        for (ItemStack item : event.getInventory().getContents()) {
            if (isCustomOre(item)) {
                event.setResult(null);
                event.getView().getPlayer().sendMessage("§cカスタム鉱石は砥石で使用できません！");
                return;
            }
        }
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        ItemStack source = event.getSource();
        if (isCustomOre(source)) {
            event.setCancelled(true); // 精錬をキャンセル
            event.getBlock().getWorld().getPlayers().forEach(player ->
                    player.sendMessage("§cカスタム鉱石はかまどで精錬できません！"));
        }
    }

    private boolean isCustomOre(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        return data.has(customOreManager.getCustomOreKey(), PersistentDataType.STRING);
    }
}
