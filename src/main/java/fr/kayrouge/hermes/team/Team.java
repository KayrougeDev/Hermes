package fr.kayrouge.hermes.team;

import fr.kayrouge.hermes.Hermes;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Team {

    private final ChatColor color;
    private final String name;

    private final Map<Player, TeamRole> members = new HashMap<>();

    private static final Map<String, Team> TEAMS = new HashMap<>();
    public static final Team NEUTRAL = create("neutral", ChatColor.WHITE, null);
    private static final Map<Team, Map<UUID, TeamRole>> PLAYERS = new HashMap<>();

    private Team(String name, ChatColor color, @Nullable Player chief) {
        this.name = name;
        this.color = color;
        members.put(chief, TeamRole.CHIEF);
    }


    public Material getBlock() {
        return TeamColorMapper.getMaterialFromColor(this.color);
    }

    public boolean delete() {
        if(TEAMS.containsKey(this.name)) {
            TEAMS.remove(this.name);
            return true;
        }
        return false;
    }

    public static Team create(String name, ChatColor color, Player chief) {
        Team team = new Team(name, color, chief);
        TEAMS.put(name, team);
        return team;
    }

    public static Team getTeam(String name) {
        if(!TEAMS.containsKey(name)) {
            Hermes.LOGGER.info("ca existe pas "+name);
            return NEUTRAL;
        }
        return TEAMS.get(name);
    }

    public static Map<String, Team> getTeams() {
        return TEAMS;
    }

    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }

    public static @Nullable Map<UUID, TeamRole> getPlayersInTeam(Team team) {
        return PLAYERS.get(team);
    }

    public static @Nullable Team getTeamForPlayer(Player player) {
        AtomicReference<Team> team1 = new AtomicReference<>(NEUTRAL);
        PLAYERS.forEach((team2, uuidTeamRoleMap) -> {
            if(uuidTeamRoleMap.containsKey(player.getUniqueId())) {
                team1.set(team2);
            }
        });

        return team1.get();
    }

    public enum TeamRole {

        CHIEF,
        SPECTATOR;

    }

}