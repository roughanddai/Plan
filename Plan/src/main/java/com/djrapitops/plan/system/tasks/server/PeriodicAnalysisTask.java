package com.djrapitops.plan.system.tasks.server;

import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plan.system.info.request.InfoRequestFactory;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.AbsRunnable;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PeriodicAnalysisTask extends AbsRunnable {

    private final InfoSystem infoSystem;
    private final InfoRequestFactory infoRequestFactory;
    private final ServerInfo serverInfo;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;
    private final WebExceptionLogger webExceptionLogger;

    @Inject
    public PeriodicAnalysisTask(
            InfoSystem infoSystem,
            InfoRequestFactory infoRequestFactory, ServerInfo serverInfo,
            PluginLogger logger,
            ErrorHandler errorHandler,
            WebExceptionLogger webExceptionLogger
    ) {
        this.infoSystem = infoSystem;
        this.infoRequestFactory = infoRequestFactory;
        this.serverInfo = serverInfo;
        this.logger = logger;
        this.errorHandler = errorHandler;
        this.webExceptionLogger = webExceptionLogger;
    }

    @Override
    public void run() {
        try {
            webExceptionLogger.logIfOccurs(this.getClass(), () ->
                    infoSystem.sendRequest(infoRequestFactory.generateAnalysisPageRequest(serverInfo.getServerUUID()))
            );
        } catch (IllegalStateException ignore) {
            /* Plugin was reloading */
        } catch (Exception | NoClassDefFoundError | NoSuchMethodError | NoSuchFieldError e) {
            logger.error("Periodic Analysis Task Disabled due to error, reload Plan to re-enable.");
            errorHandler.log(L.ERROR, this.getClass(), e);
            cancel();
        }
    }
}