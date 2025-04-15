package fr.kayrouge.hermes.config;

import fr.kayrouge.hermes.Hermes;
import org.bukkit.configuration.file.FileConfiguration;

public class MHermesConfig {

    private static final FileConfiguration config = Hermes.PLUGIN.getConfig();
    public static final boolean customQuestionGUI = config.getBoolean("mHermes.customQuestionGUI", true);

}
