package marvtechnology.marvore;

import marvtechnology.marvore.commands.OreCommand;
import marvtechnology.marvore.commands.OreTabCompleter;
import marvtechnology.marvore.events.CraftRestrictionListener;
import marvtechnology.marvore.managers.CustomOreManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MarvOrePlugin extends JavaPlugin {

    private CustomOreManager customOreManager;

    @Override
    public void onEnable() {
        // 設定ファイルの保存
        saveDefaultConfig();

        // CustomOreManager の初期化
        customOreManager = new CustomOreManager(this);
        if (customOreManager == null) {
            getLogger().severe("CustomOreManager の初期化に失敗しました！");
            return;  // プラグインの起動を中止
        }

        // コマンドの登録
        if (getCommand("ore") != null) {
            getCommand("ore").setExecutor(new OreCommand(customOreManager));
            getCommand("ore").setTabCompleter(new OreTabCompleter(customOreManager)); // ここでTabCompleterを設定
        } else {
            getLogger().severe("コマンド 'ore' の登録に失敗しました！");
            return;  // プラグインの起動を中止
        }

        // イベントの登録
        getServer().getPluginManager().registerEvents(new CraftRestrictionListener(customOreManager), this);

        getLogger().info("MarvOre プラグインが有効化されました！");
    }

    @Override
    public void onDisable() {
        getLogger().info("MarvOre プラグインが無効化されました！");
    }

    public CustomOreManager getCustomOreManager() {
        return customOreManager;
    }
}
