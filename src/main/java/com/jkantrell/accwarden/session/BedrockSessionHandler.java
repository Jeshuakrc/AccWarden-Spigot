package com.jkantrell.accwarden.session;

import com.jkantrell.accwarden.accoint.AccountRepository;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.UUID;

public class BedrockSessionHandler extends SessionHandler {

    //FIELDS
    private final FloodgateApi floodgateApi_ = FloodgateApi.getInstance();

    public BedrockSessionHandler(AccountRepository repository, SessionHolder sessionHolder) {
        super(repository, sessionHolder);
    }

    //METHODS
    public boolean isBedrock(Player player) {
        return this.isBedrock(player.getUniqueId());
    }
    public boolean isBedrock(UUID uuid) {
        return this.floodgateApi_.isFloodgatePlayer(uuid);
    }
    @Override
    public void handle(Player player) {

    }
}
