/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.advancedban;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.pluginbridge.plan.Hook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Hook for AdvancedBan plugin.
 *
 * @author Vankka
 */
@Singleton
public class AdvancedBanHook extends Hook {

    private final Formatter<Long> timestampFormatter;

    @Inject
    public AdvancedBanHook(
            Formatters formatters
    ) {
        super("me.leoko.advancedban.Universal");

        timestampFormatter = formatters.yearLong();
    }

    @Override
    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
            handler.addPluginDataSource(new AdvancedBanData(timestampFormatter));
        }
    }
}