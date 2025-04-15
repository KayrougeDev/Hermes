package fr.kayrouge.hermes.config;

import fr.kayrouge.hermes.Hermes;
import org.bukkit.configuration.file.FileConfiguration;

public class TerritoryConfig {

    private static final FileConfiguration config = Hermes.PLUGIN.getConfig();
    public static final boolean autoSave = config.getBoolean("territory.autoSave", true);

}
