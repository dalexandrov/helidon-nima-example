package io.helidon.reactive.movies;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.helidon.reactive.webserver.Routing;
import io.helidon.reactive.webserver.ServerRequest;
import io.helidon.reactive.webserver.ServerResponse;
import io.helidon.reactive.webserver.Service;

public class MovieService implements Service {

    private static final Random RANDOM = new Random();

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    private static final String movies[] = {
            "The Shawshank Redemption",
            "The GodFather",
            "Casablanca",
            "Citizen Kane",
            "Floating Weeds",
            "Gates of Heaven",
            "La Dolce Vita",
            "Notorious",
            "Raging Bull",
            "The Third Man"
    };

    @Override
    public void update(Routing.Rules rules) {
        rules.get("/", this::nextMovie);
    }

    private void nextMovie(ServerRequest req, ServerResponse res) {
        int sleepMillis = RANDOM.nextInt(500);
        int counter = COUNTER.incrementAndGet();

        SCHEDULED_EXECUTOR.schedule(() -> res.send(movies[counter % 10]), sleepMillis, TimeUnit.MILLISECONDS);
    }
}
