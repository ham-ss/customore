package marvtechnology.marvore.managers;

import marvtechnology.marvore.MarvOrePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class CustomOreManager {

    private final MarvOrePlugin plugin;
    private final NamespacedKey customOreKey;
    private final NamespacedKey modelDataKey;
    private final NamespacedKey rarityKey;

    public CustomOreManager(MarvOrePlugin plugin) {
        this.plugin = plugin;
        this.customOreKey = new NamespacedKey(plugin, "custom_ore");
        this.modelDataKey = new NamespacedKey(plugin, "custom_model_data");
        this.rarityKey = new NamespacedKey(plugin, "rarity");
    }

    public MarvOrePlugin getPlugin() {
        return plugin;
    }

    public NamespacedKey getCustomOreKey() {
        return customOreKey;
    }

    public NamespacedKey getModelDataKey() {
        return modelDataKey;
    }

    public NamespacedKey getRarityKey() {
        return rarityKey;
    }

    public List<String> getCustomOreNames() {
        ConfigurationSection oresSection = plugin.getConfig().getConfigurationSection("custom_ores.ores");
        if (oresSection == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(oresSection.getKeys(false));
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        plugin.getLogger().info("カスタム鉱石の設定がリロードされました！");
    }

    public ItemStack getCustomOre(String oreName, int amount) {
        ConfigurationSection oreSection = plugin.getConfig().getConfigurationSection("custom_ores.ores." + oreName);
        if (oreSection == null) {
            return null;
        }

        Material material = Material.matchMaterial(oreSection.getString("base_material", "STONE"));
        if (material == null) {
            return null;
        }

        int modelData = oreSection.getInt("custom_model_data", 0);
        List<String> lore = oreSection.getStringList("lore");

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("§6" + oreName));

            // カスタムモデルデータを設定
            if (modelData > 0) {
                meta.setCustomModelData(modelData);
            }

            // エンチャントを適用 (視覚的には隠す)
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            // NBTデータを追加
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(customOreKey, PersistentDataType.STRING, "true");
            data.set(modelDataKey, PersistentDataType.INTEGER, modelData);
            data.set(rarityKey, PersistentDataType.STRING, oreName);

            // Loreを設定
            if (!lore.isEmpty()) {
                meta.lore(lore.stream().map(Component::text).toList());
            }

            item.setItemMeta(meta);
        }
        return item;
    }
}

