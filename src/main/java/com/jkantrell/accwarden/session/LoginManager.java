package com.jkantrell.accwarden.session;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class LoginManager {

    private static NamespacedKey notLoggedNSK_;

    public static void setUp(JavaPlugin plugin) {
        LoginManager.notLoggedNSK_ = new NamespacedKey(plugin, "notLogged");
    }

    public static void logIn(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.remove(LoginManager.notLoggedNSK_);
    }

    public static void reset(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.set(LoginManager.notLoggedNSK_, PersistentDataType.BYTE, (byte) 1);
    }

    public static boolean isLogged(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        if (!container.has(LoginManager.notLoggedNSK_, PersistentDataType.BYTE)) { return true; }

        Byte val = container.get(LoginManager.notLoggedNSK_, PersistentDataType.BYTE);
        if (val == null) { return false; }

        return !(val > 0);
    }

}
