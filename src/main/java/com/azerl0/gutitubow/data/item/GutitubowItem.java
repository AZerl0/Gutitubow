package com.azerl0.gutitubow.data.item;

import com.azerl0.gutitubow.util.ItemUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class GutitubowItem {

    public static final ItemStack RECOVERY_POTION;
    static {
        ItemStack item = new ItemStack(Material.POTION);

        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("回復のポーション").decoration(TextDecoration.ITALIC, false));
            meta.setColor(Color.RED);
            item.setItemMeta(meta);
        }
        RECOVERY_POTION = item.clone();
    }

    public static final ItemStack RESPAWN_BEACON;
    static {
        ItemStack item = new ItemStack(Material.BEACON);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("リスポーンビーコン").decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("§fダウンした味方を蘇生することができるビーコン。"),
                    Component.text("§fただし、蘇生中は付近の敵に通知が届き、"),
                    Component.text("§f自身は発光してしまう。"),
                    Component.empty(),
                    Component.text("§7操作"),
                    Component.text("§7 手に持って[SHIFT(15秒間)] - 味方をリスポーン")
            ));
            item.setItemMeta(meta);
        }
        RESPAWN_BEACON = item.clone();
    }

    public static final ItemStack GUTITUBOW;
    static {
        ItemStack item = new ItemStack(Material.BOW);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.INFINITY, 1, true);
            meta.displayName(Component.text("ぐちつボウ").decoration(TextDecoration.ITALIC, false));
            meta.setUnbreakable(true);
            meta.setEnchantmentGlintOverride(false);
            ToolComponent tool = meta.getTool();
            tool.setRules(new ArrayList<>());
//            tool.setCanDestroyBlocksInCreative();
            tool.setDamagePerBlock(0);
            tool.setDefaultMiningSpeed(0.0f);
            meta.setTool(tool);

            item.setItemMeta(meta);
        }
        ItemUtil.setCantDrop(item, true);
        GUTITUBOW = item.clone();
    }

    public static final ItemStack AREA_COMPASS;
    static {
        ItemStack itemStack = new ItemStack(Material.COMPASS);

        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("エリアコンパス").color(NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
        }
        itemStack.setItemMeta(meta);
        ItemUtil.setCantDrop(itemStack, true);
        AREA_COMPASS = itemStack.clone();
    }

    public static final ItemStack SPYGLASS;
    static {
        ItemStack itemStack = new ItemStack(Material.SPYGLASS);

        ItemUtil.setCantDrop(itemStack, true);
        SPYGLASS = itemStack.clone();
    }

    public static final ItemStack INVALID_SLOT_BLACK;
    static {
        ItemStack itemStack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);

        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setHideTooltip(true);
        }
        itemStack.setItemMeta(meta);
        ItemUtil.setCantDrop(itemStack, true);
        ItemUtil.setCantMove(itemStack, true);
        INVALID_SLOT_BLACK = itemStack.clone();
    }


    public static final ItemStack GAME_MASTER;
    static {
        final NamespacedKey gamemasterKey = new NamespacedKey("gutitubow", "item.gamemaster");
        ItemStack itemStack = new ItemStack(Material.NETHER_STAR);

        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("ゲーム設定").decoration(TextDecoration.ITALIC, false));
            meta.getPersistentDataContainer().set(gamemasterKey, PersistentDataType.BOOLEAN, true);
        }
        itemStack.setItemMeta(meta);
        GAME_MASTER = itemStack.clone();
    }


    public static final ItemStack ABILITY_ICON_NULL;
    static {
        ItemStack itemStack = new ItemStack(Material.BARRIER);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§cまだ説明がありません。")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_NULL = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_BARRIER;
    static {
        ItemStack itemStack = new ItemStack(Material.GLASS);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「障壁」は、自由に展開できるドームで攻撃を防ぐ能力。"),
                Component.text("§f任意の場所にガラスのドームを設置できる。"),
                Component.text("§f「障壁」は最大3つまでチャージできる"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7[左クリック(弓)] - ガラスドーム設置")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_BARRIER = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_FLOAT;
    static {
        ItemStack itemStack = new ItemStack(Material.STRING);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「浮遊」は、空中を浮遊し重力に縛られない移動が可能な能力。"),
                Component.text("§f自身を中心とした半径3メートルに反重力フィールドを生成できる。"),
                Component.empty(),
                Component.text("§fフィールドの範囲外に出てしまうと、即座に能力が解除され、空中に放り投げられてしまう。"),
                Component.text("§fまた、フィールドの維持には「浮遊チャージ」を必要とする。"),
                Component.text("§f「浮遊チャージ」は地面に接している間、徐々に貯まっていく。"),
                Component.text("§fフィールドを利用できるのは自分と味方だけ。"),
                Component.text("§7操作"),
                Component.text("§7[SHIFT(1秒間)] - 反重力フィールドを生成")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_FLOAT = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_SHRINK;
    static {
        ItemStack itemStack = new ItemStack(Material.FERMENTED_SPIDER_EYE);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「縮身」は、小柄で身軽な身体で敵を翻弄する能力。"),
                Component.text("§fあなたは通常より少し体が小さい。"),
                Component.empty(),
                Component.text("§f相手から被弾すると、一定時間さらに小さく素早くなる。"),
                Component.text("§fただし、「縮身」発動中は与えるダメージが半分になる。"),
                Component.text("§7操作"),
                Component.text("§7 なし")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_SHRINK = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_TOUGH;
    static {
        ItemStack itemStack = new ItemStack(Material.GOLDEN_APPLE);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「強靭」は、圧倒的持久力で持続可能な戦いを実現する能力。"),
                Component.text("§f黄色の追加体力が♡1個分付与される。"),
                Component.text("§fこの追加体力を失っても、しばらくダメージを受けないでいると再生する。"),
                Component.text("§f肉体が再生するまでの時間は、戦闘を多く行うほど短くなっていく。"),
                Component.empty(),
                Component.text("§fまた、その強靭な肉体で「♥0.5分のダメージを与える攻撃」を無効化する。"),
                Component.text("§7操作"),
                Component.text("§7 なし")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_TOUGH = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_INVISIBLE;
    static {
        ItemStack itemStack = new ItemStack(Material.LEATHER_HELMET);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「隠密」は、透明化状態で自由に行動できる能力。"),
                Component.text("§f5秒間スニークし続けるか、相手に矢を当てると透明化が発動。"),
                Component.text("§f透明化中は移動速度が上昇する。"),
                Component.text("§f相手から矢を当てられるか、弓を引き絞ると透明化は解除されてしまう。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7 [SHIFT(5秒間)] - 透明化")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_INVISIBLE = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_SHIELD;
    static {
        ItemStack itemStack = new ItemStack(Material.SHIELD);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「大盾」は、敵の攻撃から自分と見方を守り切る能力。"),
                Component.text("§fスニーク中、自分の向いてる方向に大きな盾を構える。"),
                Component.text("§f盾は最大で5発の矢を防ぐことができ、盾を使用していない間"),
                Component.text("§f少しずつ耐久値が回復する。"),
                Component.text("§f盾を完全に破壊されてしまった場合は再生までの時間が増加する。"),
                Component.empty(),
                Component.text("§f味方の放つ矢は盾の影響を受けない。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7 [SHIFT] - 大盾を構える")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_SHIELD = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_FLASH;
    static {
        ItemStack itemStack = new ItemStack(Material.GLOWSTONE_DUST);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「閃光」は、眩い光で相手の目をくらます能力。"),
                Component.text("§f弓を左クリックすると、即座に光の矢が発射される。"),
                Component.text("§f光の矢はダメージはないが、着弾すると炸裂し、視界に入ると相手の視界が真っ白になる。"),
                Component.text("§f使用後は12秒のクールタイムがある。"),
                Component.text("§f発射直後に左クリックすることで、着弾する前にフラッシュを起動することができる。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7 [左クリック(弓)] - 光の矢を発射"),
                Component.text("§7 [左クリック(弓)] - フラッシュを起動")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_FLASH = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_GIANT;
    static {
        ItemStack itemStack = new ItemStack(Material.RAVAGER_SPAWN_EGG);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「巨体」は、巨大で頑丈な身体で全てを耐え抜く能力。"),
                Component.text("§f少しの間スニークすることで、一定時間さらに巨大化し、頑丈になる。"),
                Component.text("§f巨大化の際、近くにいる敵プレイヤーが多いほど、追加の体力を得る。"),
                Component.empty(),
                Component.text("§f巨大化は再度スニークすることで解除できる。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7 [SHIFT(長押し)] - 「巨大化」発動"),
                Component.text("§7 [SHIFT(巨大化中)] - 「巨大化」解除"),
                Component.text("§7 [(素手で)右クリック] - 味方を背負う(チーム戦限定)")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_GIANT = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_PREDATION;
    static {
        ItemStack itemStack = new ItemStack(Material.END_CRYSTAL);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「捕食」は、返り血を浴びる度に無尽蔵の力を得る能力。"),
                Component.text("§f相手にとどめを刺す度に、最大体力が♡1増加する。"),
                Component.text("§fまた、相手が所持していたポーションの数分体力が回復する。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7 なし")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_PREDATION = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_BALLOON;
    static {
        ItemStack itemStack = new ItemStack(Material.GHAST_TEAR);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「風船」は、ご機嫌な風船と共に空を旅する能力。"),
                Component.text("§f十分広いスペースがあればハッピーガストを召喚できる。"),
                Component.text("§fハッピーガストは♥10の体力を持ち、自動で再生する。"),
                Component.text("§fハッピーガストが倒されてしまうと、完全回復するまで再召喚できない。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7 [(十分広いスペースに向かって)左クリック] - 召喚"),
                Component.text("§7 [左クリック(召喚中)] - ハッピーガストをしまう")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_BALLOON = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_TRACKING;
    static {
        ItemStack itemStack = new ItemStack(Material.RECOVERY_COMPASS);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「追跡」は、狙った獲物を確実に追い詰める能力。"),
                Component.text("§f自分が一度でも矢を当てた相手の居場所は常にわかるようになる。"),
                Component.text("§fまた、追跡中の相手が近くにいるとき、移動速度が上昇する。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7 なし")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_TRACKING = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_LAST_STAND;
    static {
        ItemStack itemStack = new ItemStack(Material.BLAZE_POWDER);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「背水」は、追い込まれてから本気だす能力。"),
                Component.text("§f死の淵に立たされた時、能力が発動する。"),
                Component.empty(),
                Component.text("§f死までの時間を少しだけ先延ばしにし、「背水の陣」を敷く。"),
                Component.text("§f「背水の陣」中に誰かをキルできれば、完全な状態で復活する。"),
                Component.text("§f誰もキルできなかった場合、そのままダウンしてしまう。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7 なし (ダメージで自動で発動)")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_LAST_STAND = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_DETECTION;
    static {
        ItemStack itemStack = new ItemStack(Material.BEACON);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「探知」は、脅威を事前に看破する能力。"),
                Component.text("§f自身を中心とした広範囲の敵を探知することが可能。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7 [SHIFT(2秒間)] - 探知能力使用")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_DETECTION = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_INVINCIBLE;
    static {
        ItemStack itemStack = new ItemStack(Material.NETHERITE_CHESTPLATE);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「無敵」は、どんな攻撃でも無効化する能力。"),
                Component.text("§f最大10秒間無敵化でき、無敵化中も自由に行動可能。"),
                Component.text("§f画面下部の「無敵チャージ」の秒数だけ無敵化でき、"),
                Component.text("§fチャージは無敵化していないときに徐々に貯まっていく。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7 [SHIFT(1秒間)] - 無敵化"),
                Component.text("§7 [SHIFT(無敵化中)] - 無敵化解除")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_INVINCIBLE = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_MEDUSA;
    static {
        ItemStack itemStack = new ItemStack(Material.ENDER_EYE);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「魔眼」は、視界にとらえた相手の動きを封じる能力。"),
                Component.text("§f自分の視界に捉えた相手の移動速度が低下する。"),
                Component.text("§fまた、弓の左クリックで「石化」を発動すると"),
                Component.text("§f目が合っている相手を全員石化する。(クールタイム 20秒間)"),
                Component.text("§f石化時間は、相手を視界に捉えていた長さに比例する。"),
                Component.empty(),
                Component.text("§f石化能力クールタイム中は、移動速度低下の効果は発動しない"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7 [左クリック(弓)] - 石化能力を発動"),
                Component.text("§7 [SHIFT(無敵化中)] - 無敵化解除")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_MEDUSA = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_BOMB;
    static {
        ItemStack itemStack = new ItemStack(Material.TNT);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「爆弾」は、小型爆弾の爆風で敵も味方も吹っ飛ばす能力。"),
                Component.text("§f自分が発射する矢が爆弾付きの矢となり、赤く発光する。"),
                Component.text("§f爆弾矢は任意のタイミングで爆発させることができる。"),
                Component.text("§f爆風には♥0.5個分のダメージがあり、周囲のエンティティを吹き飛ばす。"),
                Component.text("§f1本の矢で1人のプレイヤーに与えられる最大ダメージは♡1まで。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7[左クリック] - 矢を爆発させる")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_BOMB = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_CONTROL;
    static {
        ItemStack itemStack = new ItemStack(Material.MAGMA_CREAM);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「操弾」は、矢を魔法のように操作できる能力。"),
                Component.text("§f発射した矢が紫色に発光し、"),
                Component.text("§f自分の視線と同じ方向に飛ぶようになる。"),
                Component.empty(),
                Component.text("§fまた、周りの発光している矢を「回収」でき、"),
                Component.text("§f回収の矢は敵に当たるとダメージになる。"),
                Component.text("§f自分に当たるとその矢をインベントリに追加する。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7[射撃] - 魔法の矢を発射"),
                Component.text("§7[左クリック(弓)] - 魔法の矢を回収")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_CONTROL = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_QUICKFIRE;
    static {
        ItemStack itemStack = new ItemStack(Material.CROSSBOW);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「速射」は、圧倒的手数で相手を押し切る能力。"),
                Component.text("§f弓を引き絞らずとも、即座に矢を発射できる。"),
                Component.text("§fただし、能力を使用すると弓が5秒間クールタイムになる。"),
                Component.text("§f相手に矢を当てた場合、即座に弓のクールタイムが解消される。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7[左クリック(弓)] - 矢を発射")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_QUICKFIRE = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_RICOCHET;
    static {
        ItemStack itemStack = new ItemStack(Material.SLIME_BALL);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「跳弾」は、跳弾する矢で広範囲攻撃する能力。"),
                Component.text("§f放った矢がバウンドするようになる。"),
                Component.text("§fバウンドの度に爆風が発生し、"),
                Component.text("§f爆風は♡0.5のダメージを発生させる。"),
                Component.text("§f1本の矢で1人のプレイヤーに与えられる最大ダメージは♡1まで。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7[射撃] - 6バウンドする矢を発射")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_RICOCHET = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_SNIPE;
    static {
        ItemStack itemStack = new ItemStack(Material.BOW);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「狙撃」は、どんな距離からでも敵を撃ち抜く能力。"),
                Component.text("§f落下せずに即着弾する矢を放つことができる。"),
                Component.text("§fまた、直線状の「ガラスでできたモノ」は全て貫通して撃ち抜ける。"),
                Component.text("§fただし、狙撃対象から36m以上離れていないとHit判定にならない。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7[SHIFT] しながら [射撃] - 狙撃矢を発射")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_SNIPE = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_DASH;
    static {
        ItemStack itemStack = new ItemStack(Material.DIAMOND_HORSE_ARMOR);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「疾走」は、戦場を縦横無尽に駆け回る能力。"),
                Component.text("§f走り続けることで移動速度が上昇する。"),
                Component.text("§f5秒間走り続けることで最高速度に到達。"),
                Component.text("§f最高速度時にジャンプすると通常より飛距離が伸びる。"),
                Component.text("§fまた、最高速度時は1マスの段差を無視して走れる。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7[SPACE] - 速度最大時、長距離ジャンプ")
        ));
        meta.removeAttributeModifier(EquipmentSlot.BODY);

        itemStack.setItemMeta(meta);
        ABILITY_ICON_DASH = itemStack.clone();
    }


    public static final ItemStack ABILITY_ICON_JUMP;
    static {
        ItemStack itemStack = new ItemStack(Material.FEATHER);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「跳躍」は、戦場を自由自在に跳ね回る能力。"),
                Component.text("§f空中で再度ジャンプすることでダブルジャンプが可能。"),
                Component.text("§f(クリエイティブモードになるようなイメージ)"),
                Component.text("§f画面下部の「跳躍チャージ」に応じて飛距離が変わる。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7[SPACE(空中)] - ダブルジャンプ")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_JUMP = itemStack.clone();
    }
    public static final ItemStack ABILITY_ICON_TELEPORT;
    static {
        ItemStack itemStack = new ItemStack(Material.COMPASS);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「転移」は、テレポートで瞬時に移動できる能力。"),
                Component.text("§f任意の座標をコンパスに記憶でき、再度コンパスを使用することで記憶した座標にテレポートできる。"),
                Component.text("§fテレポートした先には時空の歪みが発生し、"),
                Component.text("§f付近にいる敵プレイヤーを盲目状態にさせる。"),
                Component.empty(),
                Component.text("§fまた、DUOモード等で味方がいる場合は"),
                Component.text("§f瞬時に味方の位置にテレポートすることも可能。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7[右クリック(コンパス)] - 現在の座標をコンパスに登録"),
                Component.text("§7[右クリック(コンパス)] - 登録した座標にテレポート"),
                Component.text("§7[左クリック(コンパス)] - 最寄りの味方にテレポート(チーム戦限定)")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_TELEPORT = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_ESCAPE;
    static {
        ItemStack itemStack = new ItemStack(Material.ELYTRA);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「脱出」は、どんな戦場からも即座に離脱できる能力。"),
                Component.text("§fスニーク状態で相手から被弾することで能力発動。"),
                Component.text("§fその場から上空へ射出され、"),
                Component.text("§fエリトラでの滑空で自由に移動できる。"),
                Component.empty(),
                Component.text("§fチーム戦時は、自動的に周りの味方を巻き込んで脱出する。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7[SHIFT] - 脱出発動準備")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_ESCAPE = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_RAPIDFIRE;
    static {
        ItemStack itemStack = new ItemStack(Material.FIREWORK_ROCKET);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「連射」は圧倒的弾幕で全てを蹴散らす能力。"),
                Component.text("§fスニークで能力をチャージし、弓矢を放つと連射開始。"),
                Component.text("§f尋常じゃない早さで矢を連射する。"),
                Component.text("§f発動中は極端に移動力が下がるため、注意が必要。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7[SHIFT (長押し)] - 「連射」チャージ"),
                Component.text("§7[(チャージ完了後) 矢を発射] - 「連射」開始")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_RAPIDFIRE = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_ECHO;
    static {
        ItemStack itemStack = new ItemStack(Material.CLOCK);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「反響」は、過去の自分と共闘する能力。"),
                Component.text("§f過去数秒前の自分の分身が常についてくる。"),
                Component.text("§f任意のタイミングで過去の自分の位置まで戻ることができる。"),
                Component.text("§fまた、過去の自分が放った矢にも攻撃判定がある。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7[左クリック (弓)] - 過去の自分のところまで戻る。"),
                Component.text("§7[右クリック (コンパス)] - 何秒前の自分を反響するか設定。")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_ECHO = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_CLONE;
    static {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「分身」は、自分と瓜二つの分身を召喚する能力。"),
                Component.text("§f任意のアイテムをもって左クリックすることで、"),
                Component.text("§fそのアイテムに対応した動作をする分身を召喚できる。"),
                Component.text("§f分身は矢を撃たれるとその相手に反撃を行う。"),
                Component.empty(),
                Component.text("§f対応アイテム: 弓, 望遠鏡"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7[左クリック] - 分身召喚 (固定)"),
                Component.text("§7[SHIFT] しながら [左クリック] - 分身召喚 (操作)")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_CLONE = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_CHANGE;
    static {
        ItemStack itemStack = new ItemStack(Material.CHEST);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「変化」は、全能力を使用可能な能力。"),
                Component.text("§f60秒ごとにランダムな能力に変化していく。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7 なし")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_CHANGE = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_COPY;
    static {
        ItemStack itemStack = new ItemStack(Material.CREEPER_BANNER_PATTERN);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「複製」は、能力を複製する能力。"),
                Component.text("§f素手で殴った相手の能力を「ストック」する。"),
                Component.text("§fストックした能力は§dインベントリ中央のアイコン§fをクリックして使用できる。"),
                Component.text("§fただし、複製した能力は1分間しか持続しない。"),
                Component.text("§f同じ相手から能力をコピーする場合、30秒間の間隔が必要。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7 [(素手で) 左クリック] - 相手の能力をストックする。")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_COPY = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_APPRAISAL;
    static {
        ItemStack itemStack = new ItemStack(Material.SPYGLASS);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「鑑定」は、相対する敵の全情報を把握する能力。"),
                Component.text("§f相手プレイヤーの頭上に各種情報が表示されるようになる。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7 なし")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_APPRAISAL = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_SMASH;
    static {
        ItemStack itemStack = new ItemStack(Material.NETHERITE_SWORD);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「必殺」は、弱きを挫く能力。"),
                Component.text("§f体力が♡1のプレイヤーを視界にいれると死へのカウントが始まる。"),
                Component.text("§fカウントが100%のプレイヤーが視界内にいる状態で矢を放つと"),
                Component.text("§f必殺必中の一撃で相手を確殺する。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7 なし")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_SMASH = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_ZIPLINE;
    static {
        ItemStack itemStack = new ItemStack(Material.TRIPWIRE_HOOK);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「登攀」は、どんな高所にも移動できる能力。"),
                Component.text("§f自由に使用可能なジップラインを設置可能。"),
                Component.text("§fジップラインは同時に1個までしか設置できない。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7 [SHIFT] しながら [射撃] - ジップラインを設置")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_ZIPLINE = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_GRAPPLINGHOOK;
    static {
        ItemStack itemStack = new ItemStack(Material.FISHING_ROD);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「鉤縄」は、ワイヤーアクション自在に跳びまわる能力。"),
                Component.text("§f弓矢から地形に向けて鍵縄を放ち、再度発動することで跳躍する。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7 [(弓をもって) 左クリック] - 「鉤縄」の発射 / 跳躍")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_GRAPPLINGHOOK = itemStack.clone();
    }

    public static final ItemStack ABILITY_ICON_SWAP;
    static {
        ItemStack itemStack = new ItemStack(Material.POPPED_CHORUS_FRUIT);

        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§6「交換」は、二つのエンティティの座標を入れ替える能力。"),
                Component.empty(),
                Component.text("§7操作"),
                Component.text("§7 [(弓をもって) 左クリック] - 視点先(100ブロック以内)のエンティティと自身を交換する"),
                Component.text("§7 プレイヤーや矢、ケアパッケージとのみ交換可能")
        ));

        itemStack.setItemMeta(meta);
        ABILITY_ICON_SWAP = itemStack.clone();
    }
}
