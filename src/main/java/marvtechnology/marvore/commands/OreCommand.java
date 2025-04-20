package marvtechnology.marvore.commands;

import marvtechnology.marvore.managers.CustomOreManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class OreCommand implements CommandExecutor {

    private final CustomOreManager customOreManager;
    private boolean hasSentMessage = false; // メッセージを一度だけ送るためのフラグ

    public OreCommand(CustomOreManager customOreManager) {
        this.customOreManager = customOreManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // チャットメッセージを一度だけ表示する
        if (!hasSentMessage) {
            sender.sendMessage("§c使用方法: /ore <list　|　give　|　nbt　|　reload　|　create>");
            hasSentMessage = true; // メッセージを一度送信したことを記録
        }

        if (args.length == 0) {
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                handleListCommand(sender);
                break;
            case "give":
                handleGiveCommand(sender, args);
                break;
            case "nbt":
                handleNBTCommand(sender, args);
                break;
            case "reload":
                customOreManager.reloadConfig();
                sender.sendMessage("§a設定をリロードしました！");
                break;
            case "create":
                handleCreateCommand(sender, args);
                break;
            default:
                sender.sendMessage("§c不明なコマンドです: /ore <list | give | nbt | reload | create>");
        }
        return true;
    }

    private void handleListCommand(CommandSender sender) {
        List<String> oreList = customOreManager.getCustomOreNames();
        if (oreList.isEmpty()) {
            sender.sendMessage("§c登録されているカスタム鉱石はありません。");
        } else {
            sender.sendMessage("§6登録されているカスタム鉱石:");
            for (String ore : oreList) {
                sender.sendMessage(" §7- §e" + ore);
            }
        }
    }

    private void handleGiveCommand(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§c使用方法: /ore give <プレイヤー名> <鉱石名> <個数>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§c指定されたプレイヤーが見つかりません: " + args[1]);
            return;
        }

        String oreName = args[2];
        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cエラー: 個数は正しい数値で指定してください。");
            return;
        }

        ItemStack item = customOreManager.getCustomOre(oreName, amount);
        if (item != null) {
            target.getInventory().addItem(item);
            sender.sendMessage("§a" + target.getName() + " に " + oreName + " を渡しました！");
        } else {
            sender.sendMessage("§cエラー: 鉱石の作成中に問題が発生しました。");
        }
    }

    private void handleNBTCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§c使用方法: /ore nbt <鉱石名>");
            return;
        }

        String oreName = args[1];
        if (!customOreManager.getCustomOreNames().contains(oreName)) {
            sender.sendMessage("§cエラー: 指定された鉱石 '" + oreName + "' は登録されていません。");
            return;
        }

        ItemStack item = customOreManager.getCustomOre(oreName, 1);
        if (item == null || !item.hasItemMeta()) {
            sender.sendMessage("§cエラー: " + oreName + " の NBT データを取得できません。");
            return;
        }

        sender.sendMessage("§6NBTデータ:");
        sender.sendMessage(" §7- custom_ore: true");
        sender.sendMessage(" §7- custom_model_data: " + item.getItemMeta().getCustomModelData());
    }

    private void handleCreateCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cこのコマンドはプレイヤーのみ使用可能です。");
            return;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            sender.sendMessage("§c使用方法: /ore create <鉱石名> [ベース素材]");
            return;
        }

        String oreName = args[1];
        Material baseMaterial;

        if (args.length >= 3) {
            baseMaterial = Material.matchMaterial(args[2]);
            if (baseMaterial == null) {
                sender.sendMessage("§cエラー: 無効なベース素材です: " + args[2]);
                return;
            }
        } else {
            baseMaterial = player.getInventory().getItemInMainHand().getType();
            if (baseMaterial == Material.AIR) {
                sender.sendMessage("§cエラー: ベース素材を指定するか、右手にアイテムを持ってください。");
                return;
            }
        }

        if (customOreManager.getCustomOreNames().contains(oreName)) {
            sender.sendMessage("§cエラー: 鉱石 '" + oreName + "' は既に存在します。");
            return;
        }

        customOreManager.getPlugin().getConfig().set("custom_ores.ores." + oreName + ".base_material", baseMaterial.name());
        customOreManager.getPlugin().getConfig().set("custom_ores.ores." + oreName + ".custom_model_data", 1000);
        customOreManager.getPlugin().getConfig().set("custom_ores.ores." + oreName + ".nbt_tags.custom_ore", "true");
        customOreManager.getPlugin().getConfig().set("custom_ores.ores." + oreName + ".nbt_tags.rarity", oreName);
        customOreManager.getPlugin().getConfig().set("custom_ores.ores." + oreName + ".lore", List.of("§7特別な鉱石: " + oreName));

        customOreManager.getPlugin().saveConfig();

        // カスタムクラフトが作成された場合はメッセージを送信しない
        if (!customOreManager.getCustomOreNames().contains(oreName)) {
            sender.sendMessage("§a新しいカスタム鉱石 '" + oreName + "' を作成しました！");
            sender.sendMessage("§7ベース素材: " + baseMaterial.name());
        }
    }
}

