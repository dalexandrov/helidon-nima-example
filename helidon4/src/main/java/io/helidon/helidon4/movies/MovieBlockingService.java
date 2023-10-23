package io.helidon.helidon4.movies;

import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

class MovieBlockingService implements HttpService {
    private static Http1Client client;

    static void client(Http1Client client) {
        MovieBlockingService.client = client;
    }

    @Override
    public void routing(HttpRules httpRules) {
        httpRules.get("/nextMovie", this::nextMovie)
                .get("/sequence", this::sequence)
                .get("/parallel", this::parallel)
                .get("/count", this::count)
                .get("/encode", this::encode);
    }

    private static Http1Client client() {
        if (client == null) {
            throw new RuntimeException("Please configure the client");
        }
        return client;
    }

    private static String callMovieService(Http1Client client) {
        return client.get()
                .path("/movies")
                .requestEntity(String.class);
    }

    private void nextMovie(ServerRequest req, ServerResponse res) {
        String response = callMovieService(client());
        res.send(response);
    }

    private void sequence(ServerRequest req, ServerResponse res) {
        int count = count(req);

        var responses = new ArrayList<String>();

        for (int i = 0; i < count; i++) {
            responses.add(callMovieService(client));
        }

        res.send("Movies: " + responses);
    }

    private void parallel(ServerRequest req, ServerResponse res) throws Exception {

        try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
            int count = count(req);

            var futures = new ArrayList<Future<String>>();
            for (int i = 0; i < count; i++) {
                futures.add(exec.submit(() -> callMovieService(client)));
            }

            var responses = new ArrayList<String>();
            for (var future : futures) {
                responses.add(future.get());
            }

            res.send("Movies : " + responses);
        }
    }

    private int count(ServerRequest req) {
        return req.query().first("count").map(Integer::parseInt).orElse(3);
    }

    private void encode(ServerRequest req, ServerResponse res) throws Exception {
        //obstruct threads with encoding
        for (int value = 0; value < Integer.MAX_VALUE; value++){};

        res.send("Encoding done!");
    }

    private long count(ServerRequest req, ServerResponse res) throws Exception {

        AtomicLong result = new AtomicLong(0);

        try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 1_000_000; i++) {
                exec.submit(result::getAndIncrement);
            }
        }

        return result.get();
    }
}
