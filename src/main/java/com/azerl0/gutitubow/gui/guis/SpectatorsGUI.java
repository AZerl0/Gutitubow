package com.azerl0.gutitubow.gui.guis;

import com.azerl0.gutitubow.game.SpectatorManager;
import com.azerl0.gutitubow.gui.AbstractGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;

public class SpectatorsGUI extends AbstractGUI {
    private SpectatorManager spectatorManager;

    public SpectatorsGUI(SpectatorManager spectatorManager) {
        super(54, Component.text("観戦者"));
        this.spectatorManager = spectatorManager;
        updateGUI();
    }

    @Override
    public void updateGUI() {
        clear();
        for (UUID uuid : spectatorManager.getSpectators()) {
            if (!hasEmptySlot()) return;
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            addPlayerButton(player);
        }
        if (hasEmptySlot()) {
            ItemStack itemStack = ItemStack.of(Material.BIRCH_BUTTON);
            ItemMeta meta = itemStack.getItemMeta();
            meta.displayName(Component.text("[クリック] 観戦者を追加").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE));
            itemStack.setItemMeta(meta);
            addButton(getEmptySlot(), itemStack, event -> {
                if (!(event.getWhoClicked() instanceof Player clicker)) return;
                PlayerSelectorGUI teamPlayerSelectorGUI = new PlayerSelectorGUI(select -> {
                    spectatorManager.addPlayer(select);
                    this.open(clicker);
                });
                teamPlayerSelectorGUI.open(clicker);
            });
        }
    }

    private void addPlayerButton(Player player) {
        ItemStack itemStack = ItemStack.of(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setOwningPlayer(player);
        meta.displayName(Component.text(player.getName()).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("[クリック] このプレイヤーを観戦者から削除").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY)
        ));
        itemStack.setItemMeta(meta);
        addButton(getEmptySlot(), itemStack, event -> {
            spectatorManager.removePlayer(player);
        });
    }
}
