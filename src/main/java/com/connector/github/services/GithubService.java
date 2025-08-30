package com.connector.github.services;

import com.connector.github.models.Commit;
import com.connector.github.models.Repository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class GithubService implements GithubServiceSkeleton {

    private final WebClient githubApiClient;
    public GithubService(@Qualifier("githubWebClient") WebClient githubApiClient) {
        this.githubApiClient = githubApiClient;
    }

    @Override
    public Mono<Throwable> handleApiErrors(ClientResponse response) {
        if (response.statusCode() == HttpStatus.NOT_FOUND) {
            return Mono.error(new IllegalArgumentException("No data found for requested params"));
        }

        if (response.statusCode() == HttpStatus.FORBIDDEN) {
            List<String> rateLimitRemainingStr = response.headers().header("X-RateLimit-Remaining");
            List<String> resetTimestampStr = response.headers().header("X-RateLimit-Reset");
            List<String> rateLimitLimitStr = response.headers().header("X-RateLimit-Limit");
            
            if (!rateLimitRemainingStr.isEmpty()) {
                String remainingRequests = rateLimitRemainingStr.get(0);
                String resetTimestamp = resetTimestampStr.get(0);
                String limitRequests = rateLimitLimitStr.isEmpty() ? "5000" : rateLimitLimitStr.get(0);
                
                try {
                    int remaining = Integer.parseInt(remainingRequests);
                    long timestamp = Long.parseLong(resetTimestamp);
                    ZonedDateTime resetDateTime = Instant.ofEpochSecond(timestamp).atZone(ZoneId.systemDefault());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy hh:mm a");
                    String formattedResetTime = resetDateTime.format(formatter);
                    
                    if (remaining == 0) {
                        return Mono.error(new RuntimeException("GitHub API rate limit exceeded. Retry after: " + formattedResetTime));
                    } else if (remaining < 50) {
                        return Mono.error(new RuntimeException("Rate limit approaching. " + remaining + "/" + limitRequests + " requests remaining. Reset at: " + formattedResetTime));
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Failed to parse rate limit header");
                }
            }
        }

        if (response.statusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            List<String> retryAfterStr = response.headers().header("Retry-After");
            if (!retryAfterStr.isEmpty()) {
                String retryAfter = retryAfterStr.get(0);
                return Mono.error(new RuntimeException("Secondary rate limit exceeded. Retry after " + retryAfter + " seconds"));
            }
        }

        return response.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new RuntimeException("API error: " + response.statusCode() + " - " + body)));
    }

    public Flux<Repository> getRepositories(String username, int page) {
        return githubApiClient.get()
                .uri(uriBuilder -> uriBuilder.path("/users/{username}/repos")
                        .queryParam("page", page).queryParam("per_page", 20)
                        .build(username))
                .retrieve()
                .onStatus(status -> status.isError(),
                          response -> handleApiErrors(response))
                .bodyToFlux(Repository.class)
                .doOnError(throwable -> System.err.println("Repositories fetch failed for " + username + ": " + throwable.getMessage()));
    }

    public Flux<Commit> getCommits(String username, String repository, int page) {
        return githubApiClient.get()
                .uri(uriBuilder -> uriBuilder.path("/repos/{username}/{repo}/commits")
                                             .queryParam("page", page)
                                             .queryParam("per_page", 20)
                                             .build(username, repository))
                .retrieve()
                .onStatus(status -> status.isError(),
                          response -> handleApiErrors(response))
                .bodyToFlux(Commit.class)
                .doOnError(throwable -> {
                    System.err.println("Commits fetch failed for repositories of " + username + ": " + throwable.getMessage());
                });
    }

    @Override
    public Mono<List<Repository>> fetchGithubUserRepositories(String username, int page) {
        return getRepositories(username, page)
                .collectList()
                .onErrorResume(throwable -> Mono.error(throwable));

    }

    @Override
    public Mono<List<Commit>> fetchGithubRepoCommits(String username, String repository, int page) {
        return getCommits(username, repository, page)
                .collectList()
                .onErrorResume(throwable -> Mono.error(throwable));
    }

}
