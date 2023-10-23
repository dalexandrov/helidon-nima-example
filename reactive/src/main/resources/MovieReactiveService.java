package io.helidon.reactive.movies;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import io.helidon.common.reactive.Multi;
import io.helidon.common.reactive.Single;
import io.helidon.reactive.faulttolerance.Async;
import io.helidon.reactive.webclient.WebClient;
import io.helidon.reactive.webserver.Routing;
import io.helidon.reactive.webserver.ServerRequest;
import io.helidon.reactive.webserver.ServerResponse;
import io.helidon.reactive.webserver.Service;

class MovieReactiveService implements Service {
    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    private static WebClient client;

    static void port(int port) {
        client = WebClient.builder()
                .baseUri("http://localhost:" + port + "/movies")
                .build();
    }

    @Override
    public void update(Routing.Rules rules) {
        rules.get("/nextMovie", this::nextMovie)
                .get("/sequence", this::sequence)
                .get("/parallel", this::parallel)
                .get("/encode", this::encode);
    }

    private static WebClient client() {
        if (client == null) {
            throw new RuntimeException("Client must be configured");
        }
        return client;
    }
    private void nextMovie(ServerRequest req, ServerResponse res) {
        Single<String> response = client.get()
                .request(String.class);

        response.forSingle(res::send)
                .exceptionally(res::send);
    }

    private void sequence(ServerRequest req, ServerResponse res) {
        int count = count(req);

        Multi.range(0, count)
                .flatMap(i -> client.get().request(String.class))
                .collectList()
                .map(it -> "Movies: " + it)
                .onError(res::send)
                .forSingle(res::send);
    }

    private void parallel(ServerRequest req, ServerResponse res) {
        int count = count(req);

        Multi.range(0, count)
                .flatMap(i -> Single.create(CompletableFuture
                                                    .supplyAsync(() -> client()
                                                            .get()
                                                            .request(String.class), EXECUTOR))
                        .flatMap(Function.identity()))
                .collectList()
                .map(it -> "Movies: " + it)
                .onError(res::send)
                .forSingle(res::send);
    }

    private int count(ServerRequest req) {
        return req.queryParams()
                .first("count")
                .map(Integer::parseInt)
                .orElse(3);
    }

    private void encode(ServerRequest req, ServerResponse res) {
        //obstruct threads with encoding

        for (int value = 0; value < Integer.MAX_VALUE; value++){};

        res.send("Encoding done!");
    }
}
