/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities.uuid;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plugin.api.utility.UUIDFetcher;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

/**
 * @author Rsl1122
 */
@Singleton
public class UUIDUtility {

    private final DataCache dataCache;
    private final DBSystem dbSystem;
    private final ErrorHandler errorHandler;

    @Inject
    public UUIDUtility(DataCache dataCache, DBSystem dbSystem, ErrorHandler errorHandler) {
        this.dataCache = dataCache;
        this.dbSystem = dbSystem;
        this.errorHandler = errorHandler;
    }

    /**
     * Get UUID of a player.
     *
     * @param playerName Player's name
     * @return UUID of the player
     */
    public UUID getUUIDOf(String playerName) {
        UUID uuid = null;
        UUID uuidOf = dataCache.getUUIDof(playerName);
        if (uuidOf != null) {
            return uuidOf;
        }
        try {
            uuid = dbSystem.getDatabase().fetch().getUuidOf(playerName);
        } catch (DBOpException e) {
            errorHandler.log(L.ERROR, UUIDUtility.class, e);
        }
        try {
            if (uuid == null) {
                uuid = UUIDFetcher.getUUIDOf(playerName);
            }
        } catch (Exception | NoClassDefFoundError ignored) {
            /* Ignored */
        }
        return uuid;
    }
}