package ru.elementcraft.elementmeteor;

import com.projectkorra.projectkorra.ability.CoreAbility;
import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Главный класс плагина ElementMeteor.
 */
public class ElementMeteor extends JavaPlugin {
    private LiteCommands<CommandSender> liteCommands;
    private MariaDbManager dbManager;
    private MeteorUsageRepository usageRepository;

    @Override
    public void onEnable() {
        dbManager = new MariaDbManager("localhost", 3306, "meteor_plugin", "meteor", "meteorpassword");
        usageRepository = new MeteorUsageRepository(dbManager);
        ElementMeteorListener.register(this);
        CoreAbility.registerPluginAbilities(this, "ru.elementcraft.elementmeteor");
        liteCommands = LiteBukkitFactory.builder("fallback-prefix", this)
                .commands(new ElementMeteorCommand())
                .build();
        getLogger().info("ElementMeteor started!");
    }

    @Override
    public void onDisable() {
        if (liteCommands != null) liteCommands.unregister();
        if (dbManager != null) dbManager.close();
    }

    public MeteorUsageRepository getUsageRepository() {
        return usageRepository;
    }
}