package fr.kayrouge.hermes.mohist;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.kayrouge.hera.Choice;
import fr.kayrouge.hera.Hera;
import fr.kayrouge.hera.PacketUtils;
import fr.kayrouge.hermes.Hermes;
import fr.kayrouge.hermes.event.ChatEvents;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

public class MQuestionHandlers implements PluginMessageListener {

    private static final Map<UUID, Map<Integer, ChatEvents.IQuestion>> playerQuestions = new HashMap<>();


    public static void createAndSendQuestion(Player player, String questionName, ChatEvents.IQuestion question, Choice... choices) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("question");
        out.writeUTF(questionName);
        int id = 0;
        playerQuestions.putIfAbsent(player.getUniqueId(), new HashMap<>());
        Map<Integer, ChatEvents.IQuestion> playerQuestion = playerQuestions.get(player.getUniqueId());
        Optional<Integer> idOptional = playerQuestion.keySet().stream().max(Integer::compareTo);
        if(idOptional.isPresent()) {
            id = idOptional.get()+1;
        }
        playerQuestion.putIfAbsent(id, question);
        out.writeInt(id);
        out.writeInt(choices.length);

        for (Choice choice : choices) {
            choice.toPacket(out);
        }

        player.sendPluginMessage(Hermes.PLUGIN, "hermes:hestia", out.toByteArray());
    }

    public static void removeQuestion(Player player, int id) {
        playerQuestions.getOrDefault(player.getUniqueId(), new HashMap<>()).remove(id);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if(!channel.equalsIgnoreCase("hermes:hestia")) return;

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
        try {
            String sousCanal = in.readUTF();
            if (sousCanal.equals("answer")) {
                int id = in.readInt();
                String choiceName = in.readUTF();
                ChatEvents.IQuestion question = playerQuestions.getOrDefault(player.getUniqueId(), new HashMap<>()).get(id);
                Object data = PacketUtils.readObject(in);
                if(question != null) {
                    if(question.answer(choiceName, id, true, data)) {
                        removeQuestion(player, id);
                    }
                }
                else {
                    player.sendMessage("Error with the answer, please retry");
                }
            } else if (sousCanal.equals("heraVersion")) {
                long clientHera = in.readLong();
                if(Hera.VERSION == clientHera) {
                    Hermes.mohistHermes().addToCommunication(player);
                    player.sendMessage("Server and client Hera version match !");
                } else if (Hera.VERSION > clientHera) {
                    player.sendMessage("Hera outdated, please update Hestia");
                }
                else {
                    player.kickPlayer("This Hera version ("+clientHera+") don't exist, please update Hestia");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
