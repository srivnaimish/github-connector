package com.connector.github.Controllers;
import com.connector.github.models.Commit;
import com.connector.github.models.Repository;
import com.connector.github.models.RepositoryWithCommits;
import com.connector.github.models.ControllerResponse;
import com.connector.github.services.GithubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class GithubController {

    private final GithubService repoService;

    public GithubController(GithubService repoService) {
        this.repoService = repoService;
    }

    @GetMapping("/repositories")
    public Mono<ResponseEntity<ControllerResponse<List<Repository>>>> getGithubRepos(@RequestParam() String username,
                                                                               @RequestParam(required = false, defaultValue = "1") int page) {
        return repoService.fetchGithubUserRepositories(username, page)
                .map(repositories ->
                        ResponseEntity.status(HttpStatus.OK)
                                .body(new ControllerResponse<>(repositories, null))
                )
                .onErrorResume(throwable -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ControllerResponse<>(null, throwable.getMessage()))
                ));
    }

    @GetMapping("/repo/commits")
    public Mono<ResponseEntity<ControllerResponse<List<Commit>>>> getGithubCommits(@RequestParam() String username,
                                                                                 @RequestParam() String repository,
                                                                                 @RequestParam(required = false, defaultValue = "1") int page) {

        return repoService.fetchGithubRepoCommits(username, repository, page)
                .map(commits ->
                        ResponseEntity.status(HttpStatus.OK)
                                .body(new ControllerResponse<>(commits, null))
                )
                .onErrorResume(throwable -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ControllerResponse<>(null, throwable.getMessage()))
                ));
    }

    @GetMapping("/repositories-with-commits")
    public Mono<ResponseEntity<ControllerResponse<List<RepositoryWithCommits>>>> getGithubReposWithCommits(@RequestParam() String username,
                                                                               @RequestParam(required = false, defaultValue = "1") int page) {
        return repoService.fetchGithubUserRepositoriesWithCommits(username, page)
                .map(repositoriesWithCommits ->
                        ResponseEntity.status(HttpStatus.OK)
                                .body(new ControllerResponse<>(repositoriesWithCommits, null))
                )
                .onErrorResume(throwable -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ControllerResponse<>(null, throwable.getMessage()))
                ));
    }

    @GetMapping("/ping2")
    public ResponseEntity<Void> ping() {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
