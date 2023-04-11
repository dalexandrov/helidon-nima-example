package io.helidon.nima.movies;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.helidon.common.http.InternalServerException;
import io.helidon.nima.webserver.http.HttpRules;
import io.helidon.nima.webserver.http.HttpService;
import io.helidon.nima.webserver.http.ServerRequest;
import io.helidon.nima.webserver.http.ServerResponse;

public class MovieService implements HttpService {

    private static final Random RANDOM = new Random();

    private static final AtomicInteger COUNTER = new AtomicInteger();

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
    public void routing(HttpRules httpRules) {
        httpRules.get("/", this::nextMovie);
    }

    private void nextMovie(ServerRequest req, ServerResponse res) {
        int sleepMillis = RANDOM.nextInt(500);
        int counter = COUNTER.incrementAndGet();

        try {
            TimeUnit.MILLISECONDS.sleep(sleepMillis);
        } catch (InterruptedException e) {
            throw new InternalServerException("Something went wrong", e);
        }
        res.send(movies[counter % 10]);
    }
}
