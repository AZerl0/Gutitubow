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

    public static void init() {
        flashEffects.put(Key.key("gutitubow:flash_5"), 0.05f);
        flashEffects.put(Key.key("gutitubow:flash_10"), 0.1f);
        flashEffects.put(Key.key("gutitubow:flash_15"), 0.15f);
        flashEffects.put(Key.key("gutitubow:flash_20"), 0.2f);
        flashEffects.put(Key.key("gutitubow:flash_25"), 0.25f);
        flashEffects.put(Key.key("gutitubow:flash_30"), 0.3f);
        flashEffects.put(Key.key("gutitubow:flash_35"), 0.35f);
        flashEffects.put(Key.key("gutitubow:flash_40"), 0.4f);
        flashEffects.put(Key.key("gutitubow:flash_45"), 0.45f);
        flashEffects.put(Key.key("gutitubow:flash_50"), 0.5f);
        flashEffects.put(Key.key("gutitubow:flash_75"), 0.75f);
        flashEffects.put(Key.key("gutitubow:flash_100"), 1.0f);



        for (int i = 0; i < 20; i++) {
            findFlashCombination((float) 1 / i);
        }
    }

    public static void applyFlashEffect(Player player, float flash) {
        clearFlashEffect(player);
        for (Key key : findFlashCombination(flash)) {
            player.postEffects().add(key);
        }
    }

    public static void clearFlashEffect(Player player) {
        for (Key key : flashEffects.keySet()) {
            player.postEffects().remove(key);
        }
    }

    private static Key[] findFlashCombination(float target) {
        if (flashCash.containsKey(target)) {
            return flashCash.get(target);
        }

        List<Key> keys = new ArrayList<>(flashEffects.keySet());

        int n = keys.size();

        float bestError = Float.MAX_VALUE;
        int bestCount = Integer.MAX_VALUE;
        int bestMask = 0;

        for (int mask = 0; mask < (1 << n); mask++) {
            float remaining = 1.0f;
            int count = 0;

            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    float strength = flashEffects.get(keys.get(i));

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

        flashCash.put(target, result.toArray(Key[]::new));
        return result.toArray(Key[]::new);
    }
}
