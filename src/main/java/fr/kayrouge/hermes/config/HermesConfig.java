package fr.kayrouge.hermes.config;

import fr.kayrouge.hermes.Hermes;
import org.bukkit.configuration.file.FileConfiguration;

public class HermesConfig {

    private static final FileConfiguration config = Hermes.PLUGIN.getConfig();
    public static final boolean displayAscii = config.getBoolean("hermes.displayAscii", true);

}
