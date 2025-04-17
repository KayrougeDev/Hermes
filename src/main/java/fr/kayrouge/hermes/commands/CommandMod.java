package fr.kayrouge.hermes.commands;

import fr.kayrouge.hermes.util.Style;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandMod implements CommandExecutor, TabCompleter, Listener {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(label.equalsIgnoreCase("invsee")) {
            if(!(sender instanceof Player) || args.length != 1) return false;
            Player player = (Player) sender;
            Player target = Bukkit.getPlayer(args[0]);
            if(Objects.isNull(target)) {
                player.sendMessage(Style.getAccentColor()+"Joueur introuvable !");
                return true;
            }

            String title = Style.getColor()+target.getName()+"'s inventory";
            Inventory inv = Bukkit.createInventory(target, 45, title);
            inv.setContents(target.getInventory().getContents());

            player.openInventory(inv);
            return true;
        }

        return false;
    }

    @EventHandler
    public void invClick(InventoryClickEvent event) {
        if(event.getView().getTitle().endsWith("'s inventory") && event.getView().getTitle().contains(Style.getColor().toString())) {
            if(event.getInventory().getHolder() instanceof Player) {
                Player target = (Player)event.getInventory().getHolder();

                ItemStack[] resizedSizeEventInv = Arrays.copyOfRange(event.getInventory().getContents(), 0, Math.min(event.getInventory().getContents().length, target.getInventory().getContents().length));

                target.getInventory().setContents(resizedSizeEventInv);
                target.updateInventory();
            }
        }
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
