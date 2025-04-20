package marvtechnology.marvore.commands;

import marvtechnology.marvore.managers.CustomOreManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OreTabCompleter implements TabCompleter {

    private final CustomOreManager customOreManager;

    public OreTabCompleter(CustomOreManager customOreManager) {
        this.customOreManager = customOreManager;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        // 引数が1つ目の場合 (list, give, nbt, reload, create)
        if (args.length == 1) {
            completions.addAll(Arrays.asList("list", "give", "nbt", "reload", "create"));
        } else if (args.length >= 2) {
            // `give` コマンドの補完
            if (args[0].equalsIgnoreCase("give")) {
                if (args.length == 2) {
                    // プレイヤー名の補完（Folia互換）
                    sender.getServer().getOnlinePlayers().forEach(player -> completions.add(player.getName()));
                } else if (args.length == 3) {
                    // カスタム鉱石名の補完
                    completions.addAll(customOreManager.getCustomOreNames());
                } else if (args.length == 4) {
                    // 個数の補完
                    completions.addAll(Arrays.asList("1", "5", "10", "64"));
                }
            }

            // `nbt` コマンドの補完
            if (args[0].equalsIgnoreCase("nbt") && args.length == 2) {
                completions.addAll(customOreManager.getCustomOreNames());
            }
        }

        // 入力中の文字でフィルタリング
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
