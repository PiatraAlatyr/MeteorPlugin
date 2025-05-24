package ru.elementcraft.elementmeteor;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Слушатель для активации способности Meteor по клику ЛКМ.
 */
public class ElementMeteorListener implements Listener {
    @EventHandler
    public void onPlayerLeftClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        if (bPlayer == null) return;
        String abilName = bPlayer.getBoundAbilityName();
        if (abilName == null || !abilName.equalsIgnoreCase("Meteor")) return;
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (!player.hasPermission("bending.ability.Meteor")) return;
        if (!CoreAbility.hasAbility(player, Meteor.class)) new Meteor(player);
    }

    /**
     * Регистрирует слушатель для данного плагина.
     */
    public static void register(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new ElementMeteorListener(), plugin);
    }
}
