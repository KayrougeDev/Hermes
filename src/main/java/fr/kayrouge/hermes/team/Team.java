package fr.kayrouge.hermes.team;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Team {

    private final Color color;
    private final String name;

    private final Map<Player, TeamRole> members = new HashMap<>();

    private static final Map<String, Team> TEAMS = new HashMap<>();
    public static final Team NEUTRAL = new Team("neutral", Color.WHITE, null);

    public Team(String name, Color color, @Nullable Player chief) {
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

    public static Team create(String name, Color color, Player chief) {
        Team team = new Team(name, color, chief);
        TEAMS.put(name, team);
        return team;
    }

    public static Map<String, Team> getTeams() {
        return TEAMS;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public enum TeamRole {

        CHIEF,
        SPECTATOR;

    }

}