package com.azerl0.gutitubow.util;

import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostEffectUtil {
    private static final Map<Key, Float> flashEffects = new HashMap<>();
    private static final Map<Float, Key[]> flashCash = new HashMap<>();
    private static final Map<Key, Float> grayEffects = new HashMap<>();
    private static final Map<Float, Key[]> grayCash = new HashMap<>();


    public static void init() {
        flashEffects.put(Key.key("gutitubow:flash/5"), 0.05f);
        flashEffects.put(Key.key("gutitubow:flash/10"), 0.1f);
        flashEffects.put(Key.key("gutitubow:flash/15"), 0.15f);
        flashEffects.put(Key.key("gutitubow:flash/20"), 0.2f);
        flashEffects.put(Key.key("gutitubow:flash/25"), 0.25f);
        flashEffects.put(Key.key("gutitubow:flash/30"), 0.3f);
        flashEffects.put(Key.key("gutitubow:flash/35"), 0.35f);
        flashEffects.put(Key.key("gutitubow:flash/40"), 0.4f);
        flashEffects.put(Key.key("gutitubow:flash/45"), 0.45f);
        flashEffects.put(Key.key("gutitubow:flash/50"), 0.5f);
        flashEffects.put(Key.key("gutitubow:flash/55"), 0.55f);
        flashEffects.put(Key.key("gutitubow:flash/60"), 0.6f);
        flashEffects.put(Key.key("gutitubow:flash/65"), 0.65f);
        flashEffects.put(Key.key("gutitubow:flash/70"), 0.7f);
        flashEffects.put(Key.key("gutitubow:flash/75"), 0.75f);
        flashEffects.put(Key.key("gutitubow:flash/80"), 0.8f);
        flashEffects.put(Key.key("gutitubow:flash/85"), 0.85f);
        flashEffects.put(Key.key("gutitubow:flash/90"), 0.9f);
        flashEffects.put(Key.key("gutitubow:flash/95"), 0.95f);
        flashEffects.put(Key.key("gutitubow:flash/100"), 1.0f);

        grayEffects.put(Key.key("gutitubow:gray/5"), 0.05f);
        grayEffects.put(Key.key("gutitubow:gray/10"), 0.1f);
        grayEffects.put(Key.key("gutitubow:gray/15"), 0.15f);
        grayEffects.put(Key.key("gutitubow:gray/20"), 0.2f);
        grayEffects.put(Key.key("gutitubow:gray/25"), 0.25f);
        grayEffects.put(Key.key("gutitubow:gray/30"), 0.3f);
        grayEffects.put(Key.key("gutitubow:gray/35"), 0.35f);
        grayEffects.put(Key.key("gutitubow:gray/40"), 0.4f);
        grayEffects.put(Key.key("gutitubow:gray/45"), 0.45f);
        grayEffects.put(Key.key("gutitubow:gray/50"), 0.5f);
        grayEffects.put(Key.key("gutitubow:gray/55"), 0.55f);
        grayEffects.put(Key.key("gutitubow:gray/60"), 0.6f);
        grayEffects.put(Key.key("gutitubow:gray/65"), 0.65f);
        grayEffects.put(Key.key("gutitubow:gray/70"), 0.7f);
        grayEffects.put(Key.key("gutitubow:gray/75"), 0.75f);
        grayEffects.put(Key.key("gutitubow:gray/80"), 0.8f);
        grayEffects.put(Key.key("gutitubow:gray/85"), 0.85f);
        grayEffects.put(Key.key("gutitubow:gray/90"), 0.9f);
        grayEffects.put(Key.key("gutitubow:gray/95"), 0.95f);
        grayEffects.put(Key.key("gutitubow:gray/100"), 1.0f);



        for (int i = 0; i < 20; i++) {
            getFlashEffects((float) 1 / i);
        }

        for (int i = 0; i < 20; i++) {
            getGrayEffects((float) 1 / i);
        }
    }

    public static void applyFlashEffect(Player player, float strength) {
        clearFlashEffect(player);
        for (Key key : getFlashEffects(strength)) {
            player.postEffects().add(key);
        }
    }

    public static void clearFlashEffect(Player player) {
        for (Key key : flashEffects.keySet()) {
            player.postEffects().remove(key);
        }
    }

    public static void applyGrayEffect(Player player, float strength) {
        clearGrayEffect(player);
        for (Key key : getGrayEffects(strength)) {
            player.postEffects().add(key);
        }
    }

    public static void clearGrayEffect(Player player) {
        for (Key key : grayEffects.keySet()) {
            player.postEffects().remove(key);
        }
    }

    private static Key[] getGrayEffects(float target) {
        return findEffectCombination(target, grayCash, grayEffects);
    }

    private static Key[] getFlashEffects(float target) {
        return findEffectCombination(target, flashCash, flashEffects);
    }

    private static Key[] findEffectCombination(float target, Map<Float, Key[]> cash, Map<Key, Float> effects) {
        if (cash.containsKey(target)) {
            return cash.get(target);
        }

        List<Key> keys = new ArrayList<>(effects.keySet());

        int n = keys.size();

        float bestError = Float.MAX_VALUE;
        int bestCount = Integer.MAX_VALUE;
        int bestMask = 0;

        for (int mask = 0; mask < (1 << n); mask++) {
            float remaining = 1.0f;
            int count = 0;

            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    float strength = effects.get(keys.get(i));

                    remaining *= 1.0f - strength;
                    count++;
                }
            }

            float result = 1.0f - remaining;
            float error = Math.abs(target - result);

            if (error < bestError ||
                    (error == bestError && count < bestCount)) {

                bestError = error;
                bestCount = count;
                bestMask = mask;
            }
        }

        List<Key> result = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if ((bestMask & (1 << i)) != 0) {
                result.add(keys.get(i));
            }
        }

        cash.put(target, result.toArray(Key[]::new));
        return result.toArray(Key[]::new);
    }
}
