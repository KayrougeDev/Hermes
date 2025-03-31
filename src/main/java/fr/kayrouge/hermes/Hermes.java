package fr.kayrouge.hermes;

//import com.mohistmc.api.ServerAPI;
//import com.mohistmc.api.event.BukkitHookForgeEvent;
import fr.kayrouge.hermes.commands.CommandMod;
import fr.kayrouge.hermes.event.MiscEvents;
import fr.kayrouge.hermes.team.TeamsCommand;
import fr.kayrouge.hermes.territory.TerritoryCommand;
import fr.kayrouge.hermes.territory.TerritoryManager;
import fr.kayrouge.hermes.util.Style;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.logging.Logger;

public class Hermes extends JavaPlugin implements Listener {

    public static Hermes PLUGIN;
    public static Logger LOGGER;

    private BukkitAudiences adventure;

    public @Nonnull BukkitAudiences adventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    @Override
    public void onEnable() {
        PLUGIN = this;
        LOGGER = PLUGIN.getLogger();

        this.adventure = BukkitAudiences.create(this);

        Bukkit.getLogger().info(Style.getASCIILine());
        for(String s : Style.getASCIILogo().split("\n")) {
            Bukkit.getLogger().info(Style.getColor()+s);
        }
        Bukkit.getLogger().info(Style.getASCIILine());

        getConfig().options().copyDefaults(true);
        saveConfig();
        saveDefaultConfig();
        reloadConfig();

        TerritoryManager.load();

        registerEvents();
        registerCommands();
    }

    public void onDisable() {
        TerritoryManager.save();
        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new MiscEvents(), this);
        //ServerAPI.registerBukkitEvents(this, this);
    }

    private void registerCommands() {
        registerCommand("invsee", new CommandMod());
        registerCommand("territory", new TerritoryCommand());
        registerCommand("teams", new TeamsCommand());
    }



    private void registerCommand(String command, CommandExecutor executor) {
        PluginCommand pluginCommand = getCommand(command);
        if(pluginCommand != null) {
            pluginCommand.setExecutor(executor);
            if(executor instanceof TabCompleter) {
                pluginCommand.setTabCompleter((TabCompleter)executor);
            }
            if(executor instanceof Listener) {
                getServer().getPluginManager().registerEvents((Listener)executor, this);
            }
        }
    }



    //    @EventHandler
//    public void onTest(final BukkitHookForgeEvent e) {
//        if (e.getEvent() instanceof ServerChatEvent) {
//            ServerChatEvent chatEvent = (ServerChatEvent) e.getEvent();
//            Bukkit.getLogger().info(chatEvent.getMessage());
//        }
//    }

}
