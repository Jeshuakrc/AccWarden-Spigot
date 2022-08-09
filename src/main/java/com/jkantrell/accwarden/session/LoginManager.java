package com.jkantrell.accwarden.session;

import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class LoginManager {

    private static NamespacedKey notLoggedNSK_;
    private static NamespacedKey gameModeNSK_;
    private static JavaPlugin plugin_;

    public static void setUp(JavaPlugin plugin) {
        LoginManager.plugin_ = plugin;
        LoginManager.notLoggedNSK_ = new NamespacedKey(plugin, "notLogged");
        LoginManager.gameModeNSK_ = new NamespacedKey(plugin, "loggedGameMode");
    }

    public static void logIn(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.remove(notLoggedNSK_);
        if (container.has(gameModeNSK_, PersistentDataType.STRING)) {
            String gameModeString = container.get(gameModeNSK_,PersistentDataType.STRING);
            container.remove(gameModeNSK_);
            try {
                player.setGameMode(GameMode.valueOf(gameModeString));
                return;
            } catch (IllegalArgumentException ignored) {}
        }
        player.setGameMode(plugin_.getServer().getDefaultGameMode());
    }

    public static void reset(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.set(LoginManager.notLoggedNSK_, PersistentDataType.BYTE, (byte) 1);
        if (!container.has(gameModeNSK_, PersistentDataType.STRING)) {
            container.set(gameModeNSK_, PersistentDataType.STRING, player.getGameMode().toString());
        }
        player.setGameMode(GameMode.SPECTATOR);
    }

    public static boolean isLogged(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        if (!container.has(LoginManager.notLoggedNSK_, PersistentDataType.BYTE)) { return true; }

        Byte val = container.get(LoginManager.notLoggedNSK_, PersistentDataType.BYTE);
        if (val == null) { return false; }

        return !(val > 0);
    }

}
