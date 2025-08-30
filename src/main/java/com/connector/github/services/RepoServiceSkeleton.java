package com.connector.github.services;

import com.connector.github.models.Commit;
import com.connector.github.models.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RepoServiceSkeleton {

    Mono<List<Repository>> fetchGithubUserRepositories(String username, int page);

    Mono<List<Commit>> fetchGithubRepoCommits(String username, String repository, int page);

}
