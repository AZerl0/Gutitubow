package com.azerl0.gutitubow.entity.explosionparticle;

import com.azerl0.gutitubow.entity.GutitubowEntity;
import com.azerl0.gutitubow.entity.GutitubowEntityManager;
import com.azerl0.gutitubow.entity.GutitubowEntityType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.Set;
import java.util.UUID;

public class ExplosionParticle extends GutitubowEntity {
    private TextDisplay textDisplay;
    private TextColor textColor;
    private int tick;

    public ExplosionParticle(GutitubowEntityManager gutitubowEntityManager, Location location) {
        super(gutitubowEntityManager, location);
    }

    @Override
    public @NotNull GutitubowEntityType getType() {
        return GutitubowEntityType.EXPLOSION_PARTICLE;
    }

    @Override
    public @NotNull Location getLocation() {
        if (textDisplay != null) return textDisplay.getLocation();
        return location;
    }

    @Override
    public void teleport(Location location) {
        textDisplay.teleport(location);
    }

    @Override
    public Set<UUID> getPartEntitiesUUID() {
        return Set.of(textDisplay.getUniqueId());
    }

    @Override
    public void onRemove() {
        textDisplay.remove();
    }

    @Override
    protected void summon(Location location) {
        textDisplay = location.getWorld().spawn(location, TextDisplay.class, display -> {
            display.setViewRange(1024);
            display.setBillboard(Display.Billboard.CENTER);
            display.setShadowed(false);
            display.setSeeThrough(false);
            display.setBrightness(new Display.Brightness(15, 15));
            display.setBackgroundColor(Color.fromARGB(16711680));
            float scale = 0.1f;
            display.setTransformation(
                    new Transformation(
                            new Vector3f(),
                            new AxisAngle4f(),
                            new Vector3f(scale, scale, scale),
                            new AxisAngle4f()
                    )
            );
        });
        tick = 0;
        textColor = TextColor.color(255, 255, 255);
        onTick();
    }

    @Override
    protected void onTick() {
        tick++;

        if (tick > 15) {
            remove();
        } else {
            textDisplay.text(getText(tick).color(textColor));
        }
    }

    public void setColor(TextColor color) {
        textColor = color;
    }

    private Component getText(int step) {
        String text = switch (step) {
            case 0 -> "\uE000";
            case 1 -> "\uE001";
            case 2 -> "\uE002";
            case 3 -> "\uE003";
            case 4 -> "\uE004";
            case 5 -> "\uE005";
            case 6 -> "\uE006";
            case 7 -> "\uE007";
            case 8 -> "\uE008";
            case 9 -> "\uE009";
            case 10 -> "\uE00A";
            case 11 -> "\uE00B";
            case 12 -> "\uE00C";
            case 13 -> "\uE00D";
            case 14 -> "\uE00E";
            case 15 -> "\uE00F";
            default -> "";
        };
        return Component.text(text).font(Key.key("gutitubow:particle"));
    }

}
