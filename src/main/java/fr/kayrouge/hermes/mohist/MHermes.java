package fr.kayrouge.hermes.mohist;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mohistmc.api.PlayerAPI;
import fr.kayrouge.hera.Hera;
import fr.kayrouge.hermes.Hermes;
import fr.kayrouge.hermes.commands.CommandTest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MHermes implements Listener {

    private final Hermes plugin;

    private final List<UUID> communicationList = new ArrayList<>();

    public MHermes(Hermes plugin) {
        this.plugin = plugin;
    }

    public void onEnable() {
        Hermes.LOGGER.info("Enabling MHermes");
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "hermes:hestia");
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "hermes:hestia", new PacketListeners());
        registerEvents();

        plugin.registerCommand("question", new CommandTest());
    }

    public void onDisable() {
        Hermes.LOGGER.info("Disabling MHermes");
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
    }

    public void registerEvents() {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(this, plugin);

        //ServerAPI.registerBukkitEvents(this, plugin);
    }

    public boolean hasMod(Player player, String modid) {
        return PlayerAPI.hasMod(player, modid);
    }

    public boolean communicationAvailable(Player player) {
        return communicationList.contains(player.getUniqueId());
    }

    public void addToCommunication(Player player) {
        communicationList.add(player.getUniqueId());
    }

    public List<UUID> getCommunicationList() {
        return communicationList;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if(PlayerAPI.hasMod(player, "hestia")) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                player.sendMessage("Hestia installed, custom GUI available");
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("join");
                out.writeLong(Hera.VERSION);
                player.sendPluginMessage(Hermes.PLUGIN, "hermes:hestia", out.toByteArray());
            }, 25);
        }
        else {
            player.sendMessage("For a better experience install Hestia");
        }
    }
}
