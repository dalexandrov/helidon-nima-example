package io.helidon.helidon4.movies;

import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;

public class Main {
    public static void main(String[] args) {
        WebServer ws = WebServer.builder()
                .routing(Main::routing)
                .port(8080)
                .build()
                .start();

        MovieBlockingService.client(Http1Client.builder()
                                       .baseUri("http://localhost:" + ws.port())
                                       .build());
    }

    static void routing(HttpRouting.Builder rules) {
        rules.addFilter((chain, req, res) -> {
                    chain.proceed();
                })
                .register("/movies", new MovieService())
                .register("/", new MovieBlockingService());
    }

}
