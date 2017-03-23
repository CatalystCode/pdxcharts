package com.badbeeker.pdxazure.server.config.guice;

import com.nike.riposte.metrics.codahale.CodahaleMetricsListener;
import com.nike.riposte.server.config.AppInfo;
import com.nike.riposte.server.config.impl.DependencyInjectionProvidedServerConfigValuesBase;
import com.nike.riposte.server.error.handler.RiposteErrorHandler;
import com.nike.riposte.server.error.handler.RiposteUnhandledErrorHandler;
import com.nike.riposte.server.error.validation.BasicAuthSecurityValidator;
import com.nike.riposte.server.error.validation.RequestValidator;
import com.nike.riposte.server.http.Endpoint;

import com.badbeeker.pdxazure.server.config.AppServerConfig;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * An extension of {@link DependencyInjectionProvidedServerConfigValuesBase} that includes all the extra
 * dependency-injected properties needed by {@link AppServerConfig}.
 *
 * @author Nic Munroe
 */
public class GuiceProvidedServerConfigValues extends DependencyInjectionProvidedServerConfigValuesBase {

    public final RiposteErrorHandler riposteErrorHandler;
    public final RiposteUnhandledErrorHandler riposteUnhandledErrorHandler;
    public final RequestValidator validationService;
    public final CompletableFuture<AppInfo> appInfoFuture;
    public final CodahaleMetricsListener metricsListener;
    public final BasicAuthSecurityValidator basicAuthSecurityValidator;

    @Inject
    public GuiceProvidedServerConfigValues(
        @Named("endpoints.port") Integer endpointsPort,
        @Named("endpoints.sslPort") Integer endpointsSslPort,
        @Named("endpoints.useSsl") Boolean endpointsUseSsl,
        @Named("netty.bossThreadCount") Integer numBossThreads,
        @Named("netty.workerThreadCount") Integer numWorkerThreads,
        @Named("netty.maxRequestSizeInBytes") Integer maxRequestSizeInBytes,
        @Named("appEndpoints") Set<Endpoint<?>> appEndpoints,
        @Named("debugActionsEnabled") Boolean debugActionsEnabled,
        @Named("debugChannelLifecycleLoggingEnabled") Boolean debugChannelLifecycleLoggingEnabled,
        RiposteErrorHandler riposteErrorHandler,
        RiposteUnhandledErrorHandler riposteUnhandledErrorHandler,
        RequestValidator validationService,
        @Named("appInfoFuture") CompletableFuture<AppInfo> appInfoFuture,
        @Nullable CodahaleMetricsListener metricsListener,
        BasicAuthSecurityValidator basicAuthSecurityValidator
    ) {
        super(
            endpointsPort, endpointsSslPort, endpointsUseSsl, numBossThreads, numWorkerThreads, maxRequestSizeInBytes,
            appEndpoints, debugActionsEnabled, debugChannelLifecycleLoggingEnabled
        );

        this.riposteErrorHandler = riposteErrorHandler;
        this.riposteUnhandledErrorHandler = riposteUnhandledErrorHandler;
        this.validationService = validationService;
        this.metricsListener = metricsListener;
        this.appInfoFuture = appInfoFuture;
        this.basicAuthSecurityValidator = basicAuthSecurityValidator;
    }
}
