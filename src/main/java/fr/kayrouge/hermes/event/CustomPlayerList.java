package fr.kayrouge.hermes.event;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import fr.kayrouge.hermes.Hermes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.UUID;

public class CustomPlayerList implements Listener {

    public static void sendCustomTab(Player player) {
        Bukkit.getOnlinePlayers().forEach(onlinePlayers -> removePlayerFromTab(onlinePlayers, player));
        addFakePlayer("COLONE1", player, createTeamIfDontExist("001column1", "", ""));
        Team playersTeam = createTeamIfDontExist("002onlineplayers", "", "");
        if(playersTeam != null) {
            playersTeam.addEntry(player.getName());
        }
        addFakePlayer(" ", player, createTeamIfDontExist("003fakeplayers", "", ""));
        addFakePlayer(" ", player, createTeamIfDontExist("003fakeplayers", "", ""));
        addFakePlayer(" ", player, createTeamIfDontExist("003fakeplayers", "", ""));
        addFakePlayer("COLONE2", player, createTeamIfDontExist("004column2", "", ""));

    }

    @Nullable
    public static Team createTeamIfDontExist(String name, String prefix, String suffix) {
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        if(scoreboardManager == null) return null;
        Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
        Team team = scoreboard.getTeam(name);
        if (team == null) team = scoreboard.registerNewTeam(name);

        team.setPrefix(prefix);
        team.setSuffix(suffix);
        return team;
    }

    public static void removePlayerFromTab(Player target, Player viewer) {
//        PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO_REMOVE);
//        packet.getUUIDLists().write(0, Collections.singletonList(target.getUniqueId()));
//        try {
//            Hermes.PROTOCOL.sendServerPacket(viewer, packet);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);

        PlayerInfoData playerInfoData = new PlayerInfoData(
                WrappedGameProfile.fromPlayer(target),
                target.getPing(),
                EnumWrappers.NativeGameMode.fromBukkit(target.getGameMode()),
                WrappedChatComponent.fromText(target.getName())
        );

        packet.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));
        try {
            Hermes.PROTOCOL.sendServerPacket(viewer, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addFakePlayer(String name, Player viewer, @Nullable Team team) {
        WrappedGameProfile fakeProfile = new WrappedGameProfile(
                UUID.randomUUID(),
                name
        );

        PlayerInfoData fakeInfo = new PlayerInfoData(
                fakeProfile,
                0,
                EnumWrappers.NativeGameMode.SPECTATOR,
                WrappedChatComponent.fromText(name)
        );

        if(team != null) {
            team.addEntry(name);
        }

        PacketContainer packet = Hermes.PROTOCOL.createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        packet.getPlayerInfoDataLists().write(0, Collections.singletonList(fakeInfo));

        try {
            Hermes.PROTOCOL.sendServerPacket(viewer, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        if(Hermes.isMohist() && Hermes.mohistHermes().communicationAvailable(event.getPlayer())) {
            // TODO custom tab in Hestia
        }
        else {
            sendCustomTab(event.getPlayer());
//            Bukkit.getScheduler().runTaskLater(Hermes.PLUGIN, () -> {
//                PlayerList list = new PlayerList(event.getPlayer(),PlayerList.SIZE_FOUR);
//                //You can choose any size for this.
//                list.initTable();
//                list.setHeaderFooter("HERMES "+Hermes.PLUGIN.getDescription().getVersion(), "");
//                list.updateSlot(0,"Top left");
//            Collection<? extends Player> players = Bukkit.getOnlinePlayers();
//            for(int i = 1; i < Math.min(20, players.size()); i++) {
//                Player player = players.stream().toList().get(i);
//               list.addExistingPlayer(i, player);
//           }
//                list.updateSlot(19,"Bottom left");
//                list.updateSlot(60,"Top right");
//                list.updateSlot(79,"Bottom right");
//            }, 20*5L);
        }
    }
}
