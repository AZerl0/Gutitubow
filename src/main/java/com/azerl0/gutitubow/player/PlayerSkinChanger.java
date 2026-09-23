package com.azerl0.gutitubow.player;

import com.azerl0.gutitubow.util.PlayerUtil;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class PlayerSkinChanger {
    private final JavaPlugin plugin;
    private static final String PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";
    private static final Map<String, Collection<ProfileProperty>> cache = new HashMap<>();
    private static final Set<String> DEFAULT_SKINS = Set.of("Steave", "Alex", "Efe", "Noor");

    public PlayerSkinChanger(JavaPlugin plugin) {
        this.plugin = plugin;
        cacheDefaultSkins();
    }


    public void setSkin(Player target, String targetSkin) {

        String finalTargetSkin = targetSkin;
        if (!PlayerUtil.isValidJavaName(targetSkin)) {
            List<String> randomSkins = new ArrayList<>(DEFAULT_SKINS);
            Collections.shuffle(randomSkins);
            finalTargetSkin = randomSkins.getFirst();
        }

        final PlayerProfile playerProfile = target.getPlayerProfile();
        playerProfile.setProperties((getTextureProperty(finalTargetSkin)));
        target.setPlayerProfile(playerProfile);

        updateSkin(target);
    }

    public void resetSkin(Player target) {
        setSkin(target, target.getName());
    }

    public void setStoneSkin(Player target) {
        final PlayerProfile playerProfile = target.getPlayerProfile();
        playerProfile.setProperties(getTextureProperty(
                "ewogICJ0aW1lc3RhbXAiIDogMTc3Mjc2NDIzMjM3NSwKICAicHJvZmlsZUlkIiA6ICIzZmM3ZmRmOTM5NjM0YzQxOTExOTliYTNmN2NjM2ZlZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJZZWxlaGEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzcwOTY0NWE3YzQyNTIyYWMxNzIxNGMwMzBmMDAxYTQxYWVmNzM0NTAxZjcxOWY4MzE2MzI5ZWE4NGY2Y2M1NCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                "vPsHlGRpSHwvCJ7e1LyyiJYQl5Wx4oTaonX07oBlA+AUbYyPAoDNgsF4HlONnJQxMcxZDO7h+x8GStfDQfTdGp4aSsBTwywaPU7qExhHdkSSsZClbBiNryAGzIP3Ybjb/nI8GkTIMZ9HjSfAYpFNRonWqegCtHx86BMCgg/CH7xMZWW6HVook9ZCmRtk7iDc2BgCPrShIKkOQXrhYMZ3s3IDA1jYlFYR4BUR3qQdZUhaCKLSeN+du9/3ufA/gJ9ON0ZunMFb7mF8sx5pPcgqWlryQmG0CXffqQ372BTtcQmZASJ+vXWLQ/NqQvI0pMeDzvDhZ24vUk8UmvgJfRKXwZtpGFy+mmg6WejaXmCvuDSG7vFchFKmk7TCzUAy9/I1nD2CakZJybFj51oLzct8i+kzs0qwTLgivghR7nhn3ewAclBo2VmugGfAPCCFkE1P+WqfYy2VFEUovUCHhYSm4MVz8zUGbhckPpQFWf6o7qSSwjE/WXxbY8NC/DWeId3OPvz8BWbY1s5Z7Hds+f80wbAe0GjOiGhAAeH/UDaWez9ToZTUWQlFZ2bTXuGwb6W8HydCsIZ+5phO2pbjhYLFyLdw4MwCGhSjjt6kREP2a/lt0j28qKX5H0EN7fu9wXU3HhnileESM4fIG2QFoPNIEF0Q3dFLU/OqrIE0rLgkqSQ="
        ));
        target.setPlayerProfile(playerProfile);

        updateSkin(target);
    }

    public void setRandomDefaultSkin(Player target) {
        List<String> randomSkins = new ArrayList<>(DEFAULT_SKINS);
        Collections.shuffle(randomSkins);
        String skinName = randomSkins.getFirst();
        setSkin(target, skinName);
    }

    public void cacheDefaultSkins() {
        cache.put(
                "Steave",
                getTextureProperty(
                        "ewogICJ0aW1lc3RhbXAiIDogMTcxNTA4MTE3Njg5MCwKICAicHJvZmlsZUlkIiA6ICI2YmQxZjIwMjhiMTU0ZTQ1OTY0YjMzMDZkYzFmNGZlNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJBZHZlbnR1cmVGaW5uIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I1ZGQyMzYyYjdhOGFhYTc3ZGUyY2Y3NTE0MjBiYzVhMTNjYThmZjE0ZjU0NTg3MTFlNzNjYTc5Mzc3YzIyZTkiCiAgICB9CiAgfQp9",
                        "s6MIg+A7ob6PfUtjgtNpYCZLFjz7UHzA2SmCA971BHVuP13ZdKal47bg3z3YTa5xuVZ66jCWVl6n8/TyCpgL4HA8j+EAxmzB8b4eAhj3Rb0moF31yXD/dJDND75FruI4WvG9kzDA9dnQLg34zdjdm3pQyGMY17sSKWINtOiIZRF11+SIkNbhUY5EJHs3ToobJtpE3VlUbNuNFtVWY6jasB1wy0oQ9Vyd80YHCdnGFDDJUDgTc6Ps9N22nresZ0R+fv2diZYveD7ZPpi9ZnLe1Ao1j1Y0ieBPZrfcZMF/soicEiS+EL6w0DOd5ALOqR2f+pUCWu7d7ADbUwG0Uzgsw8EysPY38D85BiPJ88PYLOeZMQk4Hd6NZPH6R4/OMjlV9WBtt8WHiiffCybi9YvuAxbJUhAX4nRkZMvaDT1dfi2fYDJJBhQUNi72G3UOxugWTvAhcfwP0pV7tISbvDUpGUgs2EREGKIlBbtjcsYG3h0mMOkxeGewQYH10cTMyCI4BRZoiBqCAcajyJ61s4eoLLqsZU6qkl/XmShBDAyJsF0HOVKqG50uGOpeCG/vIVMTRcTOzlG2QZwsLBVbkd/q2WTTtybWSUmslJ4+rZVHNUyeg918rNn0r903hhM2dTWAMmVmfwCTsTKqdYrSC7ZuPGP3b1yN/c8R/F/TNNGmr8o="
                )
        );
        cache.put(
                "Alex",
                getTextureProperty(
                        "ewogICJ0aW1lc3RhbXAiIDogMTY2NzEzMDg3Nzk0NSwKICAicHJvZmlsZUlkIiA6ICJhMjk1ODZmYmU1ZDk0Nzk2OWZjOGQ4ZGE0NzlhNDNlZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJMZXZlMjMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDZhY2QwNmU4NDgzYjE3NmU4ZWEzOWZjMTJmZTEwNWViM2EyYTQ5NzBmNTEwMDA1N2U5ZDg0ZDRiNjBiZGZhNyIKICAgIH0KICB9Cn0=",
                        "KNXiRdBZiwdqnWWevQEoX/+nFG2Tko/DKaIi/1SB8qsQd9GiirS3P2HZfcRhOoeZ3kdDNcI4/U9wlCcgXuS9q/gchkSBit2HA70wvquSQEV7wBcLB/DmWe4xY+lEUuWr9cZp8Ad9RbsvjB/mLbz9avqS9Or5NEzYkBhjp2/VHwjsPtTJeiRrC+TqcM0fFi9ttDb2SGZNdKwHXfItC8xJOFGtRzUeiPAzPlGxMYFMcsLamU2mqI6z92ter2qlSqUNuKOrBq4yOU62AmJxPPk0775vTza88QfB1XODInV7j1yFnJO6aCdYGiqVCwzMF0pqSQbEXxtZT2DS+nIU/uy49bIEiVjoAV3xIulpe8BD4UblV5vkQjDLAahUt+PLTn/sMEAMy+W4gLe+HzLg+h6PtgI1jgzUqkoHyy9fXgZnDaRLFmZ3Z5hL8Eb2P2VZpCrxeK7gocJPUBzQxSggsZQ6Zv+OrE2NefG5jX77nJDKBOL0dWgBbuOd5qtmGRvE/cbQe1qWDPHRL+mc1/m1CIwznH8xBdKRGVCO1wtmcH7oReOb48Q8+wLxEngYUXAqH69ydwyCZdfriygUy51+O+OlrfJNGJ/qpPQQJF7M9lZfIEH/+pwhxRXbm4Em9J8bgeuq0Jo0o9fElNWbazipbEJ5K+O5amtiFbSQPF+5D+FEJJ0="
                )
        );
        cache.put(
                "Efe",
                getTextureProperty(
                        "ewogICJ0aW1lc3RhbXAiIDogMTY4NjE2MTQ4NzcxMiwKICAicHJvZmlsZUlkIiA6ICJiZTQxNTM0NTFiYjQ0MmQ5ODMwNzRhYjRmNDhmMWY5NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJpdHNsZW1vbmNvbGFfIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzUwZDQ2ZjgwZWIyZTQ5YTAzOWYzZGEzMmUyN2M3MGU5ZTgxYmEwYjg3Mzk2NDc2YjA5OTAzMmJmZjM2ODI5MTciCiAgICB9CiAgfQp9",
                        "GrglZze+r1OIEuUvXLVzfptzNM0ZvugUUjFs0EaCiZRzhDUjxa7ztdRa8ftFZ2D7InOO0+CETLKgSRtUaIxRQT26BxExLIitl6EQRH9z7Uj/4By/ZEIGdhzI1XKBMITJ58ZtUbfKLl8K9YwTSXbbCSgqx60JXZS5BjYciIciCuARZn+S5b4c4g6a/9YTtBbwG63nWxbJd2fblx7syCNgEavtIQxwUzqmzEt3Q+obUpcXpPa3wpNBiP0fg1GiEh3wY7irr7XtkkbSgs5xme5iAt8r0z0EtvjTj72Mks6gxRnXe1kWpJuaXhk500147r220W3R502znlZbwNqt4PkSHjTtz/NLWQBdnUxOADfNnIZAwDmOGMuOsl5X3aUFY88ISxQGjZCcpZKWi5ty64rqQoZItAK9Fh42NOFdbcS+d66R7/atfyKwYt/nsuf+UwrNNr1WcnvkBFpNqG3osTY4yIEX5I6VZYo2VWXqHlwVwO8LJgoAH5wppFmbuIuA/lhuGoirxuxKoWDWxkXe8GXD9/seSPrqPpc4ISG1+zUZUho5+EzPZtw8LIKvybLvdk+osffo3OCixO5NEJhWFkw2T1Gkztb4uF5hh6t6ZzNPc9uryus79Ig3ThpTsornnaNa6+fCRNXXGs1iD23l8o9o344tYEuSRikrPV/DNKiJXb4="
                )
        );
        cache.put(
                "Noor",
                getTextureProperty(
                        "ewogICJ0aW1lc3RhbXAiIDogMTcwNDMyMDU3NjAzOCwKICAicHJvZmlsZUlkIiA6ICIzOTg5OGFiODFmMjU0NmQxOGIyY2ExMTE1MDRkZGU1MCIsCiAgInByb2ZpbGVOYW1lIiA6ICI4YjJjYTExMTUwNGRkZTUwIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzZlZDAyY2VlNTE5MDBmZmU3MTA1ODA1ODRhYzkzNzdkNmY2MmVhZTE4MWNkMWNlMWFhM2JmOWM3YTI5MWQzZWYiCiAgICB9CiAgfQp9",
                        "nf/CpA88PVYhBNxgd74f2qSIET1KxRaKTafi3kvT0mJqMUfAx4dsLv6nBrA3DJ7zXqbIBbsDX6Bi+NbALhCTw6vw3Xck75JwHNGF8hK++JKju9qAhgHS5ZBpvQOwvvKWtZ6qtjf4FFLQ98O3SslnzcM1wy544YAMwlAW3LG2qtD32euB3rpJ26AgIzwmOH9uw6chf1CGiWv2PZZEq/qBmRhYN1D/R4chblCV9u3k+oJmamg17WdgRz1aYLqSYHpEU4TsiNmF5T0Q5rbyEgf1VnzKUI14r+MWGMK94jQIR355GONQ91+1Z2LRnLaaTojYIMhYVMh6o52NcHj58e/1mAVAGeuUKrdG7odic5lUNJOGde56t91qY2ruOFDjQ4m4gTJ4Yw4KONwwn/VhfcP/T6U0Hq4p/Vi+c1vIi1IOajm+JJRy+mC3F2spRxAzfLFQH1hCl6r/032vYV02nCaWthW7omeGwkrAy7p3chp85eYgJbiC94OCvRMbke2i+vZwiUbjsTPCnRcNXPalC0FcX1jZ5K0n/OZYDMqgbZIWP26OB63+UhVrUsxU6hy1G4zZJR3ayv5GqM5Cd1w3tP428LtrdY1siynSewIGLcEEsfj2N4f8VIq3do8vgSBUqdREaVfKLFOvmAJUJDHcs4pkZN1OM3Na6Fx47ZqRKI65Haw="
                )
        );
    }


    private Collection<ProfileProperty> getTextureProperty(String value, String signature) {
        final ProfileProperty profileProperty = new ProfileProperty("textures", value, signature);
        return List.of(profileProperty);
    }

    public void cacheSkin(Player player) {
        Bukkit.getServer().getAsyncScheduler().runNow(plugin, task -> {
            cacheSkin(player.getName());
        });
    }

    public void cacheSkin(String playerName) {
        getTextureProperty(playerName);
    }

    private Collection<ProfileProperty> getTextureProperty(String targetSkin) {
        if (cache.containsKey(targetSkin))
            return cache.get(targetSkin);

        final String profileResponse = makeRequest(PROFILE_URL + targetSkin);
        final JsonObject profileObject = JsonParser.parseString(profileResponse).getAsJsonObject();
        final String uuid = profileObject.get("id").getAsString();

        final String skinResponse = makeRequest(SKIN_URL.formatted(uuid));
        final JsonObject skinObject = JsonParser.parseString(skinResponse).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
        final String value = skinObject.get("value").getAsString();
        final String signature = skinObject.get("signature").getAsString();
        final ProfileProperty profileProperty = new ProfileProperty("textures", value, signature);

        cache.put(targetSkin, List.of(profileProperty));

        return List.of(profileProperty);
    }

    private String makeRequest(String url) {

        try (final HttpClient client = HttpClient.newBuilder().build()) {
            final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateSkin(Player target) {
        GameMode gameMode = target.getGameMode();
        Location location = target.getLocation();
        target.setGameMode(GameMode.SPECTATOR);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            target.setGameMode(gameMode);
            target.teleport(location);
        }, 2L);
    }

    public Set<String> getCachePlayers() {
        return cache.keySet();
    }
}
