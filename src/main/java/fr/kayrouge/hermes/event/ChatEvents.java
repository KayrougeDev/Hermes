package fr.kayrouge.hermes.event;

import fr.kayrouge.hermes.Hermes;
import fr.kayrouge.hermes.util.Style;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.UUID;

public class ChatEvents implements Listener {

    public static final HashMap<UUID, IQuestion> QUESTIONS = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        UUID uuid = event.getPlayer().getUniqueId();
        if(QUESTIONS.containsKey(uuid)) {
            if(QUESTIONS.get(uuid).answer(event.getMessage())) {
                QUESTIONS.remove(uuid);
                return;
            }
        }
        Hermes.PLUGIN.getServer().getOnlinePlayers().forEach(player -> {
            String s = Style.getColor().toString() + event.getPlayer().getDisplayName() +
                    ChatColor.WHITE +
                    ": " +
                    Style.getAccentColor() +
                    event.getMessage().replace("£", "§");
            player.sendMessage(s);
        });
    }

    public static void askChatQuestion(Component message, Player player, IQuestion question) {
        Hermes.PLUGIN.adventure().player(player).sendMessage(message);
        QUESTIONS.put(player.getUniqueId(), question);
    }


    @FunctionalInterface
    public interface IQuestion {
        boolean answer(String answer);
    }

}
