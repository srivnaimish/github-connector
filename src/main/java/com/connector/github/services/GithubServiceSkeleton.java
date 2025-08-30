package com.connector.github.services;

import com.connector.github.models.Commit;
import com.connector.github.models.Repository;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GithubServiceSkeleton {

    Mono<List<Repository>> fetchGithubUserRepositories(String username, int page);

    Mono<List<Commit>> fetchGithubRepoCommits(String username, String repository, int page);

    Mono<Throwable> handleApiErrors(ClientResponse response);

}
