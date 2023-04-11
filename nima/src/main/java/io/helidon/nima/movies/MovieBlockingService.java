package io.helidon.nima.movies;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.helidon.nima.webclient.http1.Http1Client;
import io.helidon.nima.webserver.http.HttpRules;
import io.helidon.nima.webserver.http.HttpService;
import io.helidon.nima.webserver.http.ServerRequest;
import io.helidon.nima.webserver.http.ServerResponse;

class MovieBlockingService implements HttpService {
    private static Http1Client client;

    static void client(Http1Client client) {
        MovieBlockingService.client = client;
    }

    @Override
    public void routing(HttpRules httpRules) {
        httpRules.get("/nextMovie", this::nextMovie)
                .get("/sequence", this::sequence)
                .get("/parallel", this::parallel);
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
                .request(String.class);
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
}
