package fr.kayrouge.hermes;

//import com.mohistmc.api.ServerAPI;
//import com.mohistmc.api.event.BukkitHookForgeEvent;
import fr.kayrouge.hermes.commands.CommandMod;
import fr.kayrouge.hermes.event.MiscEvents;
import fr.kayrouge.hermes.team.TeamsCommand;
import fr.kayrouge.hermes.territory.TerritoryCommand;
import fr.kayrouge.hermes.territory.TerritoryManager;
import fr.kayrouge.hermes.util.Style;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Hermes extends JavaPlugin implements Listener {

    public static Hermes PLUGIN;
    public static Logger LOGGER;

    private static final TerritoryManager territoryManager = new TerritoryManager();

    @Override
    public void onEnable() {
        PLUGIN = this;
        LOGGER = PLUGIN.getLogger();

        Bukkit.getLogger().info(Style.getASCIILine());
        for(String s : Style.getASCIILogo().split("\n")) {
            Bukkit.getLogger().info(Style.getColor()+s);
        }
        Bukkit.getLogger().info(Style.getASCIILine());

        getConfig().options().copyDefaults(true);
        saveConfig();
        saveDefaultConfig();
        reloadConfig();
        registerEvents();
        registerCommands();
    }

    public void onDisable() {
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

    public static TerritoryManager getTerritoryManager() {
        return territoryManager;
    }

    //    @EventHandler
//    public void onTest(final BukkitHookForgeEvent e) {
//        if (e.getEvent() instanceof ServerChatEvent) {
//            ServerChatEvent chatEvent = (ServerChatEvent) e.getEvent();
//            Bukkit.getLogger().info(chatEvent.getMessage());
//        }
//    }

}
