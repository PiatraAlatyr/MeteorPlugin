package ru.elementcraft.elementmeteor;

import com.projectkorra.projectkorra.board.BendingBoardManager;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import com.projectkorra.projectkorra.BendingPlayer;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.window.Window;

@Command(name = "elementmeteor")
public class ElementMeteorCommand implements Listener {
    private Window window;

    @Execute
    void elementmeteor(@Context CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Command /elementmeteor only for player");
            return;
        }
        Gui gui = Gui.empty(3, 3);
        gui.setItem(4, new MeteorItem());
        window = Window.single()
                .setGui(gui)
                .setTitle("Element Meteor")
                .setViewer((Player) sender)
                .build();
        window.open();
    }

    private class MeteorItem extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.FIRE_CHARGE)
                    .setDisplayName("Get Meteor");
        }

        @Override
        public void handleClick(ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
            int slot = player.getInventory().getHeldItemSlot() + 1; // ProjectKorra слоты: 1-9
            bPlayer.getAbilities().put(slot, "Meteor");
            BendingBoardManager.updateAllSlots(player); //Обновляем окно Абилок в игре
            player.sendMessage("Ability Meteor added!");
            window.close();
        }
    }
}