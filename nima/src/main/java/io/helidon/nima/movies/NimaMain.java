package io.helidon.nima.movies;

import io.helidon.common.http.Http;
import io.helidon.nima.webclient.http1.Http1Client;
import io.helidon.nima.webserver.WebServer;
import io.helidon.nima.webserver.http.HttpRouting;

public class NimaMain {
    private static final Http.HeaderValue SERVER = Http.Header.create(Http.Header.SERVER, "Nima");

    public static void main(String[] args) {
        WebServer ws = WebServer.builder()
                .routing(NimaMain::routing)
                .port(8080)
                .start();

        MovieBlockingService.client(Http1Client.builder()
                                       .baseUri("http://localhost:" + ws.port())
                                       .build());
    }

    static void routing(HttpRouting.Builder rules) {
        rules.addFilter((chain, req, res) -> {
                    res.header(SERVER);
                    chain.proceed();
                })
                .register("/movies", new MovieService())
                .register("/", new MovieBlockingService());
    }

}
