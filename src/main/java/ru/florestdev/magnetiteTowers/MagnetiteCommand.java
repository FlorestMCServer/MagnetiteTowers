package ru.florestdev.magnetiteTowers;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MagnetiteCommand implements CommandExecutor, TabCompleter {
    private final MagnetitePlugin plugin;

    public MagnetiteCommand(MagnetitePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§7/magnetite give <канал> [игрок] §f— выдать настроенный магнетит\n§7/magnetite info §f— канал вышки, на которую вы смотрите\n§7/magnetite list §f— список всех активных каналов");
            return true;
        } else {
            switch (args[0].toLowerCase()) {
                case "give" -> this.handleGive(sender, args);
                case "info" -> this.handleInfo(sender);
                case "list" -> this.handleList(sender);
                default -> sender.sendMessage("§cНеизвестная подкоманда. Введите /magnetite для справки.");
            }

            return true;
        }
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("magnetite.give")) {
            sender.sendMessage("§cНедостаточно прав.");
        } else if (args.length < 2) {
            sender.sendMessage("§cИспользование: /magnetite give <канал> [игрок]");
        } else {
            int channel;
            try {
                channel = Integer.parseInt(args[1]);
            } catch (NumberFormatException var6) {
                sender.sendMessage("§cКанал должен быть целым числом.");
                return;
            }

            Player target;
            if (args.length >= 3) {
                target = Bukkit.getPlayerExact(args[2]);
                if (target == null) {
                    sender.sendMessage("§cИгрок §e" + args[2] + " §cне найден.");
                    return;
                }
            } else {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cИз консоли укажите игрока: /magnetite give <канал> <игрок>");
                    return;
                }

                Player p = (Player)sender;
                target = p;
            }

            target.getInventory().addItem(new ItemStack[]{MagnetiteItems.create(this.plugin, channel)});
            sender.sendMessage("§bВыдан магнетит на канале §e" + channel + " §bигроку §f" + target.getName());
        }
    }

    private void handleInfo(CommandSender sender) {
        if (sender instanceof Player p) {
            Block target = p.getTargetBlockExact(6);
            if (target != null && target.getType() == Material.LODESTONE) {
                BlockPos pos = BlockPos.of(target);
                ChannelManager cm = this.plugin.getChannelManager();
                if (!cm.isRegistered(pos)) {
                    sender.sendMessage("§7Это обычный лодстоун, не магнетит.");
                } else {
                    int channel = cm.getChannel(pos);
                    sender.sendMessage("§bКанал: §e" + channel + " §7| Сигнал сети: §e" + cm.getChannelPower(channel) + " §7| Вышек на канале: §e" + cm.getTowers(channel).size());
                }
            } else {
                sender.sendMessage("§cПосмотрите на блок магнетита.");
            }
        } else {
            sender.sendMessage("§cЭта команда только для игроков.");
        }
    }

    private void handleList(CommandSender sender) {
        ChannelManager cm = this.plugin.getChannelManager();
        if (cm.getChannels().isEmpty()) {
            sender.sendMessage("§7Нет активных каналов.");
        } else {
            for(int ch : cm.getChannels()) {
                sender.sendMessage("§bКанал §e" + ch + "§b: §f" + cm.getTowers(ch).size() + " вышек, сигнал §e" + cm.getChannelPower(ch));
            }

        }
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("give", "info", "list");
        } else {
            return args.length == 2 && args[0].equalsIgnoreCase("give") ? List.of("1", "2", "3") : List.of();
        }
    }
}
