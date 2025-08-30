package com.connector.github.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Consumer;


@Configuration
@PropertySource("classpath:application.properties")
public class ConnectorClients {

    @Autowired
    protected Environment env;

    @Bean
    @Qualifier("githubWebClient")
    public WebClient githubWebClient() {
        String baseUrl = env.getProperty("github.api.base-url");
        String apiKey = env.getProperty("github.token");

        if (apiKey == null) {
            System.err.println("Please add property {github.token} to application.properties");
        }

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.set(HttpHeaders.ACCEPT, "application/vnd.github+json");
                    httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
                    httpHeaders.set("X-GitHub-Api-Version", "2022-11-28");
                })
                //.filter(logRequestAsCurl())
                .build();
    }

    private ExchangeFilterFunction logRequestAsCurl() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            StringBuilder curlCommand = new StringBuilder("curl -v");

            curlCommand.append(" -X ").append(clientRequest.method());

            clientRequest.headers().forEach((name, values) -> {
                values.forEach(value -> {
                    curlCommand.append(" -H '").append(name).append(": ").append(value).append("'");
                });
            });

            Optional<Object> body = clientRequest.attribute("requestBody");
            body.ifPresent(b -> curlCommand.append(" -d '").append(b).append("'"));

            curlCommand.append(" '").append(clientRequest.url()).append("'");

            System.out.println("--- WebClient cURL Request ---");
            System.out.println(curlCommand);
            System.out.println("-----------------------------");

            return Mono.just(clientRequest);
        });
    }

}
