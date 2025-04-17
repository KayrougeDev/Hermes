package fr.kayrouge.hermes;

import fr.kayrouge.dionysios.GameManager;
import fr.kayrouge.hermes.commands.CommandMod;
import fr.kayrouge.hermes.commands.CommandTest;
import fr.kayrouge.hermes.config.HermesConfig;
import fr.kayrouge.hermes.event.ChatEvents;
import fr.kayrouge.hermes.mohist.MHermes;
import fr.kayrouge.hermes.team.TeamsCommand;
import fr.kayrouge.hermes.territory.TerritoryCommand;
import fr.kayrouge.hermes.territory.TerritoryManager;
import fr.kayrouge.hermes.util.Style;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.logging.Logger;

public class Hermes extends JavaPlugin implements Listener {

    public static Hermes PLUGIN;
    public static Logger LOGGER;
    private static GameManager gameManager;

    private static @Nullable MHermes MOHIST_HERMES;

    private BukkitAudiences adventure;

    public static NamespacedKey ACTION_ITEM_DATA;

    public @Nonnull BukkitAudiences adventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    public static @Nonnull MHermes mohistHermes() {
        if(MOHIST_HERMES == null) {
            throw new IllegalStateException("Tried to access MHermes on a non Mohist server!");
        }
        return MOHIST_HERMES;
    }

    @Override
    public void onLoad() {
        PLUGIN = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        gameManager = new GameManager(this);
        LOGGER = this.getLogger();
        gameManager.setAfterGameLocation(Bukkit.getWorlds().get(0).getSpawnLocation());

        boolean isMohistServer;
        try {
            Class.forName("com.mohistmc.MohistMC");
            isMohistServer =  true;
        } catch (ClassNotFoundException e) {
            isMohistServer = false;
        }

        if(isMohistServer) {
            MOHIST_HERMES = new MHermes(this);
        }

        ACTION_ITEM_DATA = new NamespacedKey(PLUGIN, "action_item");

        this.adventure = BukkitAudiences.create(this);

        if(isMohist()) {
            LOGGER.info("Server running on mohist !");
        }

        if(HermesConfig.displayAscii) {
            Bukkit.getLogger().info(Style.getASCIILine());
            for(String s : Style.getASCIILogo().split("\n")) {
                Bukkit.getLogger().info(Style.getColor()+s);
            }
            Bukkit.getLogger().info(Style.getASCIILine());
        }

        TerritoryManager.load();
        registerEvents();
        registerCommands();

        if(isMohist()) {
            mohistHermes().onEnable();
        }
    }

    public void onDisable() {
        TerritoryManager.save();
        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
        if(isMohist()) {
            mohistHermes().onDisable();
        }
    }

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new ChatEvents(), this);
    }

    private void registerCommands() {
        registerCommand("invsee", new CommandMod());
        registerCommand("territory", new TerritoryCommand());
        registerCommand("teams", new TeamsCommand());
        registerCommand("game", new CommandTest());
    }



    public void registerCommand(String command, CommandExecutor executor) {
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

    public static boolean isMohist() {
        return MOHIST_HERMES != null;
    }

    public static GameManager getGameManager() {
        return gameManager;
    }

    //    @EventHandler
//    public void onTest(final BukkitHookForgeEvent e) {
//        if (e.getEvent() instanceof ServerChatEvent) {
//            ServerChatEvent chatEvent = (ServerChatEvent) e.getEvent();
//            Bukkit.getLogger().info(chatEvent.getMessage());
//        }
//    }

}
