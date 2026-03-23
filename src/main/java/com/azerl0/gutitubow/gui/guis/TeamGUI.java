package com.azerl0.gutitubow.gui.guis;

import com.azerl0.gutitubow.data.item.GutitubowItem;
import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.game.GutitubowGameMode;
import com.azerl0.gutitubow.gui.AbstractGUI;
import com.azerl0.gutitubow.team.GameTeam;
import com.azerl0.gutitubow.team.TeamManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TeamGUI extends AbstractGUI {
    private final TeamManager teamManager;
    private final Game game;


    public TeamGUI(TeamManager teamManager) {
        super(54, Component.text("チームセレクター"));
        this.teamManager = teamManager;
        this.game = teamManager.getGame();
    }

    public void updateGUI() {
        clear();
        switch (game.getGameMode()) {
            case DUO -> {
                inventory.setItem(9, getJumpMasterButtonItem());
                inventory.setItem(36, getJumpMasterButtonItem());
                int i = 0;
                for (GameTeam gameTeam : teamManager.getTeams()) {
                    setTeamComponent(i, gameTeam);
                    i++;
                }
            }
            case TRIO -> {
                Set<Integer> invalidSlots = new HashSet<>(Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 49, 50, 51, 52, 53));
                for (int slot : invalidSlots) {
                    inventory.setItem(slot, GutitubowItem.INVALID_SLOT_BLACK);
                }
                inventory.setItem(18, getJumpMasterButtonItem());
                int i = 0;
                for (GameTeam gameTeam : teamManager.getTeams()) {
                    setTeamComponent(i, gameTeam);
                    i++;
                }
            }
        }
    }

    private void setTeamComponent(int index, GameTeam gameTeam) {
        GutitubowGameMode gameMode = game.getGameMode();
        if (gameMode == GutitubowGameMode.DUO && index >= 16) return;
        if (gameMode == GutitubowGameMode.TRIO && index >= 9) return;
        int currentSlot = -1;
        if (gameMode == GutitubowGameMode.DUO) {
            currentSlot = index + 1;
            if (index >= 8) currentSlot += 19;
        } else if (gameMode == GutitubowGameMode.TRIO) {
            currentSlot = index + 10;
        }
        if (currentSlot == -1) return;
        inventory.setItem(currentSlot, gameTeam.getTeamIconItem());

        int counter = 2;
        if (gameMode == GutitubowGameMode.TRIO) counter = 3;
        for (int i = 0; i < counter; i++) {
            currentSlot += 9;

            @Nullable UUID uuid = null;
            if (gameTeam.getPlayers().size() > i) uuid = gameTeam.getPlayers().get(i);
            @Nullable Player player = null;
            if (uuid != null) player = Bukkit.getPlayer(uuid);
            setTeamPlayerButton(currentSlot, player, gameTeam);
        }
    }

    private void setTeamPlayerButton(int slot, @Nullable Player player, GameTeam team) {
        if (player == null) {
            addButton(slot, getEmptyPlayerButtonItem(), event -> {
                if (!(event.getWhoClicked() instanceof Player clicker)) return;
                TeamPlayerSelectorGUI teamPlayerSelectorGUI = new TeamPlayerSelectorGUI(select  -> {
                    teamManager.joinTeam(select, team);
                    this.open(clicker);
                });
                teamPlayerSelectorGUI.open(clicker);
            });
        } else {
            ItemStack itemStack = ItemStack.of(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(player);
            }

            itemStack.setItemMeta(meta);
            addButton(slot, itemStack, event -> {
                if (event.getClick() == ClickType.SHIFT_LEFT) {
                    team.setJumpMaster(player);
                } else {
                    teamManager.leaveTeam(player);
                }
            });
        }
    }

    @Override
    public void open(Player player) {
        updateGUI();
        super.open(player);
    }

    private ItemStack getJumpMasterButtonItem() {
        return getAbstractSimpleButtonItem(Material.FEATHER, Component.text("ジャンプマスター"));
    }

    private ItemStack getEmptyPlayerButtonItem() {
        return getAbstractSimpleButtonItem(Material.BIRCH_BUTTON, Component.text("空き"));
    }
}
