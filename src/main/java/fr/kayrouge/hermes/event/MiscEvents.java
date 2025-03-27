package fr.kayrouge.hermes.event;

import fr.kayrouge.hermes.Hermes;
import fr.kayrouge.hermes.util.Style;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class MiscEvents implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        Hermes.PLUGIN.getServer().getOnlinePlayers().forEach(player -> {
            String s = Style.getColor().toString() + event.getPlayer().getDisplayName() +
                    ChatColor.WHITE +
                    ": " +
                    Style.getAccentColor() +
                    event.getMessage().replace("£", "§");
            player.sendMessage(s);
        });
    }

}
