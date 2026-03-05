package com.azerl0.gutitubow.carepackage;

import com.azerl0.gutitubow.util.EntityUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.*;

public class CarePackageManager {
    private final JavaPlugin plugin;
    private Map<UUID, CarePackage> carePackages = new HashMap<>();
    private final NamespacedKey isFallingKey;
    private final NamespacedKey fallingTickKey;
    private final NamespacedKey remainedPotionKey;



    public CarePackageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        isFallingKey = new NamespacedKey(plugin, "carePackage.isFalling");
        fallingTickKey = new NamespacedKey(plugin, "carePackage.fallingTick");
        remainedPotionKey = new NamespacedKey(plugin, "carePackage.remainedPotion");
    }



    public void summonPackage(Location location) {
        location.setRotation(0.0f, 0.0f);
        World world = location.getWorld();

        BlockDisplay blockDisplay = world.spawn(location, BlockDisplay.class, (BlockDisplay display) -> {
            display.setBlock(Material.CHEST.createBlockData());
            display.setGlowing(true);
            EntityUtil.setGlow(display, NamedTextColor.YELLOW);
            Vector3f translation = new Vector3f(-0.5f, -0.1f, -0.5f);
            Transformation transformation = display.getTransformation();
            Transformation newTransformation = new Transformation(
                    translation,
                    transformation.getLeftRotation(),
                    transformation.getScale(),
                    transformation.getRightRotation()
            );
            display.setTransformation(newTransformation);
            display.addScoreboardTag("gutitubow.carePackageEntity");

        });

        Interaction interaction = world.spawn(location, Interaction.class, (Interaction i) -> {
            i.setInteractionWidth(0.8f);
            i.setInteractionHeight(0.8f);
            i.setCustomNameVisible(true);
            i.customName(Component.text("ケアパッケージ"));
            i.addScoreboardTag("gutitubow.carePackageEntity");

        });

        ArmorStand armorStand = world.spawn(location, ArmorStand.class, (ArmorStand as) -> {
            as.setInvisible(true);
            Objects.requireNonNull(as.getAttribute(Attribute.SCALE)).setBaseValue(0);
            Objects.requireNonNull(as.getAttribute(Attribute.GRAVITY)).setBaseValue(0.005f);
            as.addPassenger(blockDisplay);
            as.addPassenger(interaction);
            as.addScoreboardTag("gutitubow.carePackageEntity");
        });

        for (int i = 0; i < 10; i++) {
            double randomRadius = 0.8;
            Random random = new Random();
            double randomX = random.nextDouble(-randomRadius, randomRadius);
            double randomY = random.nextDouble(-randomRadius, randomRadius);
            world.spawn(location.clone().add(randomX, 4.5, randomY), Chicken.class, (Chicken c) -> {
                Objects.requireNonNull(c.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0);
                c.setLeashHolder(armorStand);
                c.addScoreboardTag("gutitubow.carePackageEntity");
                c.setVariant(Chicken.Variant.TEMPERATE);
                c.setInvulnerable(true);

            });
        }
        CarePackage carePackage = new CarePackage(armorStand.getUniqueId(), isFallingKey, fallingTickKey, remainedPotionKey);
        carePackage.setRemainedPotion(3);
        carePackages.put(armorStand.getUniqueId(), carePackage);
        Bukkit.getScheduler().runTaskLater(plugin, carePackage::onMove, 3L);
    }

    public CarePackage getCarePackage(UUID uuid) {
        return this.carePackages.get(uuid);
    }

    public static boolean isCarePackageEntity(Entity entity) {
        return entity.getScoreboardTags().contains("gutitubow.carePackageEntity");
    }

    public void removeAllCarePackage(){
        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand armorStand : world.getEntitiesByClass(ArmorStand.class)) {
                if (!isCarePackageEntity(armorStand)) return;
                CarePackage carePackage = getCarePackage(armorStand.getUniqueId());
                carePackage.remove();
            }
        }
    }
}
