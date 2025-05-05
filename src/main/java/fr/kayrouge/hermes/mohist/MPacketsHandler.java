package fr.kayrouge.hermes.mohist;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.kayrouge.hera.Choice;
import fr.kayrouge.hera.Hera;
import fr.kayrouge.hera.util.type.PacketType;
import fr.kayrouge.hera.util.PacketUtils;
import fr.kayrouge.hera.util.type.QuestionsType;
import fr.kayrouge.hermes.Hermes;
import fr.kayrouge.hermes.event.ChatEvents;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

public class MPacketsHandler implements PluginMessageListener {

    private static final Map<UUID, Map<Integer, QuestionContext>> playerQuestions = new HashMap<>();

    private final MHermes mHermes;

    public MPacketsHandler(MHermes mHermes) {
        this.mHermes = mHermes;
    }

    public static void createAndSendQuestion(Player player, String questionName, ChatEvents.IQuestion question, Choice... choices) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(PacketType.QUESTION.getId());
        out.writeByte(QuestionsType.ANSWER.getId());
        out.writeUTF(questionName);
        int id = 0;
        playerQuestions.putIfAbsent(player.getUniqueId(), new HashMap<>());
        Map<Integer, QuestionContext> playerQuestion = playerQuestions.get(player.getUniqueId());
        Optional<Integer> idOptional = playerQuestion.keySet().stream().max(Integer::compareTo);
        if(idOptional.isPresent()) {
            id = idOptional.get()+1;
        }
        playerQuestion.putIfAbsent(id, new QuestionContext(question, questionName, choices));
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
            int typeID = in.readUnsignedByte();
            PacketType type = PacketType.getById(typeID);
            switch (type) {
                case JOIN:
                    long clientHera = in.readLong();
                    if(Hera.VERSION == clientHera) {
                        player.sendMessage("[Hermes] Connected successfully with Hestia !");
                        mHermes.getCommunicationList().add(player.getUniqueId());
                    } else if (Hera.VERSION > clientHera) {
                        player.sendMessage("Hera outdated, please update Hestia");
                    }
                    else {
                        player.sendMessage("This Hera version ("+clientHera+") is not supported please use Hera-"+Hera.VERSION);
                    }
                    break;
                case QUESTION:
                    handleQuestionPacket(player, in);
                    break;
                default: Hermes.LOGGER.warning("Unsupported packet id received: "+type.name()+" "+typeID);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleQuestionPacket(Player player, DataInputStream in) throws IOException {
        QuestionsType type = QuestionsType.getById(in.readUnsignedByte());
        switch (type) {
            case LIST: sendQuestionsList(player);
                break;
            case ANSWER:
                int id = in.readInt();
                String choiceName = in.readUTF();
                QuestionContext questionContext = playerQuestions.getOrDefault(player.getUniqueId(), new HashMap<>()).get(id);
                Object data = PacketUtils.readObject(in);
                if(questionContext != null) {
                    if(questionContext.question.answer(choiceName, id, true, data)) {
                        removeQuestion(player, id);
                    }
                }
                else {
                    player.sendMessage("Unknown question for this answer");
                }
                break;
        }
    }

    public void sendQuestionsList(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(PacketType.QUESTION.getId());
        out.writeByte(QuestionsType.LIST.getId());
        Map<Integer, QuestionContext> questionMap = playerQuestions.getOrDefault(player.getUniqueId(), new HashMap<>());

        out.writeInt(questionMap.size());

        questionMap.forEach((id, questionContext) -> {
            out.writeInt(id);
            out.writeUTF(questionContext.message);
        });

        player.sendPluginMessage(Hermes.PLUGIN, "hermes:hestia", out.toByteArray());
    }

    public static class QuestionContext {
        public final ChatEvents.IQuestion question;
        public final String message;
        public final Choice[] choices;
        public QuestionContext(ChatEvents.IQuestion question, String message, Choice... choices) {
            this.question = question;
            this.message = message;
            this.choices = choices;
        }
    }
}
