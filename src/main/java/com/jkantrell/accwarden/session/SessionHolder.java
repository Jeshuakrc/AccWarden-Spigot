package com.jkantrell.accwarden.session;

import com.jkantrell.accwarden.accoint.Account;
import com.jkantrell.accwarden.accoint.Platform;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public final class SessionHolder {

    //FIELDS
    private final HashMap<UUID, OpenSession> sessions_ = new HashMap<>();
    private final HashMap<UUID, BukkitRunnable> closers_ = new HashMap<>();
    private final JavaPlugin plugin_;
    private boolean crossPlatformSessions_ = false;
    private int holdTime_ = 0;

    //CONSTRUCTORS
    public SessionHolder(JavaPlugin plugin) {
        this.plugin_ = plugin;
    }

    //SETTERS
    public void setCrossPlatformSessions(boolean b) {
        this.crossPlatformSessions_ = b;
    }
    public void setHoldTime(int seconds) {
        this.holdTime_ = seconds;
    }

    //METHODS
    public Optional<OpenSession> get(UUID uuid) {
        return Optional.ofNullable(this.sessions_.get(uuid));
    }
    public Optional<OpenSession> get(String uuid) {
        return this.get(UUID.fromString(uuid));
    }
    public Optional<OpenSession> get(Player player) {
        return this.get(player.getUniqueId());
    }
    public boolean has(UUID uuid) {
        return this.get(uuid).isPresent();
    }
    public boolean has(String uuid) {
        return this.get(uuid).isPresent();
    }
    public boolean has(Player player) {
        return this.get(player).isPresent();
    }
    public void dispose(UUID uuid) {
        if (!this.sessions_.containsKey(uuid)) { return; }
        this.sessions_.remove(uuid);
        BukkitRunnable closer = this.closers_.remove(uuid);
        if (closer == null) { return; }
        if (!closer.isCancelled()) { closer.cancel(); }
    }
    public void dispose(String uuid) {
        this.dispose(UUID.fromString(uuid));
    }
    public void dispose(Player player) {
        this.dispose(player.getUniqueId());
    }
    public boolean claim(UUID uuid, InetSocketAddress address, Platform platform) {
        OpenSession session = this.sessions_.get(uuid);
        if (session == null) { return false; }
        this.dispose(uuid);
        if (!session.address().equals(address)) { return false; }
        if (!this.crossPlatformSessions_) {
            return session.platform().equals(platform);
        } else if (!session.platform().equals(platform)) { return false; }
        return session.account().hasPlatform(platform);
    }
    public boolean claim(String uuid, InetSocketAddress address, Platform platform) {
        return this.claim(UUID.fromString(uuid), address, platform);
    }
    public boolean claim(Player player, Platform platform) {
        return this.claim(player.getUniqueId(), player.getAddress(), platform);
    }
    public void openNew(Account account, InetSocketAddress address, Platform platform) {
        if (this.holdTime_ < 1) { return; }
        UUID id = account.getId();

        BukkitRunnable closer = new BukkitRunnable() {
            private UUID id_ = id;

            @Override
            public void run() {
                SessionHolder.this.dispose(id_);
            }
        };

        this.sessions_.put(id, new OpenSession(account, address, platform));
        this.closers_.put(id, closer);

        closer.runTaskLater(this.plugin_, ((long) this.holdTime_) * 20);

    }
    public void openNew(Account account, Player player, Platform platform) {
        this.openNew(account,player.getAddress(),platform);
    }
}
