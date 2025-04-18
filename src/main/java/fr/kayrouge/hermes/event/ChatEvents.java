package fr.kayrouge.hermes.event;

import fr.kayrouge.hera.Choice;
import fr.kayrouge.hermes.Hermes;
import fr.kayrouge.hermes.config.MHermesConfig;
import fr.kayrouge.hermes.mohist.MQuestionHandlers;
import fr.kayrouge.hermes.util.Pair;
import fr.kayrouge.hermes.util.Style;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ChatEvents implements Listener {

    public static final HashMap<UUID, Pair<List<String>, IQuestion>> QUESTIONS = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        UUID uuid = event.getPlayer().getUniqueId();
        if(event.getMessage().startsWith("$") && QUESTIONS.containsKey(uuid)) {
            String choiceName;
            String[] args = event.getMessage().split(" ");
            if(args.length > 0) {
                choiceName = args[0];
            }
            else {
                choiceName = event.getMessage();
            }
            choiceName = choiceName.substring(1);
            if(QUESTIONS.get(uuid).getKey().contains(choiceName.toLowerCase())) {
                IQuestion question = QUESTIONS.get(uuid).getValue();
                StringBuilder data = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    if(i != 1) {
                        data.append(" ");
                    }
                    data.append(args[i]);
                }
                if(question.answer(choiceName, -1, false, data.toString())) {
                    QUESTIONS.remove(uuid);
                    return;
                }
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

    private static void askChatQuestion(Component message, Player player, IQuestion question, List<String> choices) {
        Hermes.PLUGIN.adventure().player(player).sendMessage(message);
        QUESTIONS.put(player.getUniqueId(), new Pair<>(choices, question));
    }

    public static void askQuestion(Player player, String questionName, ChatEvents.IQuestion question, Choice... choices) {
        if(Hermes.isMohist() && Hermes.mohistHermes().communicationAvailable(player) && MHermesConfig.customQuestionGUI) {
            MQuestionHandlers.createAndSendQuestion(player, questionName, question, choices);
        }
        else {
            List<String> choiceList = new ArrayList<>();
            Component component = Component.text(questionName);
            for (Choice choice : choices) {
                String name = choice.getName().replace(" ", "-");
                choiceList.add(name);
                component = component.appendNewline();
                if(choice.getType() == Choice.Type.SIMPLE_BUTTON) {
                    component = component.append(Component.text(name).clickEvent(ClickEvent.runCommand("$"+name)));
                } else if (choice.getType() == Choice.Type.TEXT_ENTRY) {
                    component = component.append(Component.text(name).clickEvent(ClickEvent.suggestCommand("$"+name+" <text>")));
                }
                component = component.append(Component.space());
            }

            askChatQuestion(component, player, question, choiceList);
        }
    }

    @FunctionalInterface
    public interface IQuestion {
        boolean answer(String choiceName, int questionId, boolean customClientHandler, @Nullable Object data);
    }
}
