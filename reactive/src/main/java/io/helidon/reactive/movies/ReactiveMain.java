package io.helidon.reactive.movies;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import io.helidon.config.Config;
import io.helidon.logging.common.LogConfig;
import io.helidon.reactive.webserver.Routing;
import io.helidon.reactive.webserver.WebServer;

public class ReactiveMain {
    private static final AtomicInteger COUNTER = new AtomicInteger();
    // no need to use secure random to compute sleep times
    private static final Random RANDOM = new Random();
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) {
        LogConfig.configureRuntime();

        WebServer ws = WebServer.builder()
                .routing(ReactiveMain::routing)
                .config(Config.create().get("server"))
                .port(8081)
                .build()
                .start()
                .await(Duration.ofSeconds(10));

        MovieReactiveService.port(ws.port());
    }

    static void routing(Routing.Rules rules) {
        rules
                .register("/movies", new MovieService())
                .register("/", new MovieReactiveService());
    }

}
