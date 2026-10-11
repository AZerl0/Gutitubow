package com.azerl0.gutitubow.api.game;

import com.azerl0.gutitubow.GutitubowManagers;
import com.azerl0.gutitubow.arena.Arena;
import com.azerl0.gutitubow.damage.DamageSource;
import com.azerl0.gutitubow.game.*;
import com.azerl0.gutitubow.gamemap.GameMap;
import com.azerl0.gutitubow.gamemap.MapRepairManager;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.gui.guis.GameMapSelectorGUI;
import com.azerl0.gutitubow.manager.GlassBreakManager;
import com.azerl0.gutitubow.scoreboard.PlayerBoard;
import com.azerl0.gutitubow.team.GameTeam;
import com.azerl0.gutitubow.team.TeamManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface Game {
    /**
     * @return GameSetting
     */
    @NotNull GameSetting getGameSetting();

    /**
     * @param player 取得したいプレイヤー
     * @return GamePlayer
     */
    @NotNull GamePlayer getGamePlayer(@NotNull final Player player);

    /**
     * @param uuid GamePlayerを取得したいプレイヤーのUUID
     * @return GamePlayer
     */
    @Nullable GamePlayer getGamePlayer(@NotNull final UUID uuid);

    /**
     * @param player 取得したいプレイヤー
     * @return GameTeam
     */
    @Nullable GameTeam getGameTeam(@NotNull final Player player);

    /**
     * @param uuid 取得したいUUID
     * @return GameTeam
     */
    @Nullable GameTeam getGameTeam(@NotNull final UUID uuid);

    /**
     * @param player プレイヤー
     * @return プレイヤーがこのゲームに参加しているか
     */
    boolean isThisGamePlaying(@NotNull Player player);

    /**
     * @param player 比較するプレイヤー
     * @param target 比較されるプレイヤー
     * @return 二人のプレイヤーがチームメイトであるか
     */
    boolean isTeammate(@NotNull final Player player, @NotNull final Player target);

    /**
     * @param player 比較するUUID
     * @param target 比較されるUUID
     * @return 両UUIDの属するチームが同じであるか
     */
    boolean isTeammate(@NotNull final UUID player, @NotNull final UUID target);

    /**
     * @return ゲームが開始されているか
     */
    boolean isStarted();

    /**
     * @return 現在のフェーズ
     */
    @NotNull GamePhase getPhase();

    /**
     * @return 現在最終フェーズであるか
     */
    boolean isFinalPhase();

    /**
     * @return セーフエリアの取得
     */
    @NotNull Location getSafeArea();

    /**
     * @return 現在ボーダー縮小中であるか
     */
    boolean isShrinking();

    /**
     * @return 現在のフェーズの進行度
     */
    float getPhaseProgress();

    /**
     * このゲームに参加している全てのGamePlayer、生存状態は問わない
     * @return GamePlayer
     */
    @NotNull Set<GamePlayer> getPlayers();

    /**
     * GamePlayerManagerに保持されている全てのGamePlayer、オンラン状態やこのゲームへの参加状態は問わない
     * @return GamePlayer
     */
    @NotNull Set<GamePlayer> getAllGamePlayers();

    /**
     * @return 生存している全てのGamePlayer
     */
    @NotNull Set<GamePlayer> getAlivePlayers();

    /**
     * 死亡している全てのGamePlayer、死亡順に格納されている
     * @return 死亡している全てのGamePlayer
     */
    @NotNull List<GamePlayer> getDeadPlayers();

    /**
     * プレイヤーを死亡させる
     * @param gamePlayer 死亡させるプレイヤー
     * @param damageSource 死亡理由となるDamageSource
     * @return 処理が成功したか
     */
    boolean kill(@NotNull final GamePlayer gamePlayer, @NotNull final DamageSource damageSource);

    /**
     * プレイヤーを蘇生する。
     * ログや花火などのエフェクトが伴う
     * @param player 蘇生されるプレイヤー
     * @param reviver 蘇生したプレイヤー
     */
    void revive(@NotNull final Player player, @NotNull final Player reviver);


    /**
     * プレイヤーを蘇生する。
     * ログや花火などのエフェクトは伴わない。
     * ドッペルゲンガーのために使用される
     * @param gamePlayer 蘇生されるプレイヤ
     */
    void reviveSilent(@NotNull final GamePlayer gamePlayer);

    /**
     * ゲームのモードを設定する
     * @param gameMode　設定するGameMode
     */
    void setGameMode(GutitubowGameMode gameMode);

    /**
     * ゲームを破棄する
     */
    void destroy();

    /**
     * ゲームの状態をリセットする、ゲーム中であっても強制的にリセットする
     */
    void reset();

    /**
     * @return ゲーム開始トリガー
     */
    @NotNull GameStartResult start();

    /**
     * ゲームオーバーの状態にあるか確認
     */
    void checkGameOver();

    /**
     * 次のセーフエリアのサイズを取得
     * @return セーフエリアのサイズ
     */
    double getNextSafeAreaSize();

    /**
     * 座標が次のセーフエリアの範囲内であるか
     * @param location 判定する座標
     * @return 判定結果
     */
    boolean isInsideNextSafeArea(@NotNull final Location location);

    GameMap getGameMap();

    void setGameMap(@NotNull final GameMap gameMap);

    @Nullable Arena getArena();

    /**
     * アリーナをリセット、
     * アリーナにいるプレイヤーはオーバーワールドに戻されリセットが完了したら、
     * アリーナに戻される
     */
    void resetArena();

    /**
     * タイマーのボスバーをプレイヤーに表示する
     * @param player 表示させるプレイヤー
     */
    void showBossBar(@NotNull final Player player);

    /**
     * タイマーのボスバーをプレイヤーから非表示にする
     * @param player 非表示にするプレイヤー
     */
    void hideBossBar(@NotNull final Player player);

    @Nullable PlayerBoard getPlayerBoard(@NotNull final GamePlayer gamePlayer);

    @NotNull GutitubowManagers getGutitubowManagers();

    @NotNull TeamManager getTeamManager();

    @NotNull MapRepairManager getMapRepairManager();

    @NotNull GlassBreakManager getGlassBreakManager();

    @NotNull SpectatorManager getSpectatorManager();

    @NotNull GameMapSelectorGUI getGameMapSelectorGUI();
}
