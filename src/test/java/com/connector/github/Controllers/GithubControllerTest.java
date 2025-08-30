package com.connector.github.Controllers;

import com.connector.github.models.Commit;
import com.connector.github.models.CommitAuthor;
import com.connector.github.models.CommitDetails;
import com.connector.github.models.ControllerResponse;
import com.connector.github.models.Repository;
import com.connector.github.models.User;
import com.connector.github.services.GithubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GithubControllerTest {

    @Mock
    private GithubService githubService;

    @InjectMocks
    private GithubController githubController;

    private Repository testRepository;
    private Commit testCommit;
    private List<Repository> testRepositories;
    private List<Commit> testCommits;

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
                .login("testuser")
                .id(BigInteger.valueOf(123))
                .avatar_url("https://example.com/avatar.jpg")
                .build();

        testRepository = Repository.builder()
                .name("test-repo")
                .fullName("testuser/test-repo")
                .description("A test repository")
                .isPrivate(false)
                .owner(testUser)
                .build();

        testRepositories = Arrays.asList(testRepository);

        CommitAuthor commitAuthor = CommitAuthor.builder()
                .name("Test Author")
                .email("test@example.com")
                .date("2025-08-30T00:00:00Z")
                .build();

        CommitDetails commitDetails = CommitDetails.builder()
                .message("Test commit message")
                .author(commitAuthor)
                .build();

        testCommit = Commit.builder()
                .sha("abc123def")
                .commit(commitDetails)
                .build();

        testCommits = Arrays.asList(testCommit);
    }

    @Test
    void testGetGithubRepos_Success() {
        String username = "testuser";
        int page = 1;
        when(githubService.fetchGithubUserRepositories(username, page))
                .thenReturn(Mono.just(testRepositories));

        Mono<ResponseEntity<ControllerResponse<List<Repository>>>> result = 
                githubController.getGithubRepos(username, page);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    ControllerResponse<List<Repository>> response = responseEntity.getBody();
                    return responseEntity.getStatusCode() == HttpStatus.OK &&
                           response != null &&
                           response.getResponse() != null &&
                           response.getResponse().size() == 1 &&
                           response.getResponse().get(0).getName().equals("test-repo") &&
                           response.getError() == null;
                })
                .verifyComplete();
    }

    @Test
    void testGetGithubRepos_WithDefaultPage() {
        String username = "testuser";
        when(githubService.fetchGithubUserRepositories(username, 1))
                .thenReturn(Mono.just(testRepositories));

        Mono<ResponseEntity<ControllerResponse<List<Repository>>>> result = 
                githubController.getGithubRepos(username, 1);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    ControllerResponse<List<Repository>> response = responseEntity.getBody();
                    return responseEntity.getStatusCode() == HttpStatus.OK &&
                           response != null &&
                           response.getResponse() != null &&
                           response.getError() == null;
                })
                .verifyComplete();
    }

    @Test
    void testGetGithubRepos_WithCustomPage() {
        String username = "testuser";
        int page = 2;
        when(githubService.fetchGithubUserRepositories(username, page))
                .thenReturn(Mono.just(testRepositories));

        Mono<ResponseEntity<ControllerResponse<List<Repository>>>> result = 
                githubController.getGithubRepos(username, page);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    ControllerResponse<List<Repository>> response = responseEntity.getBody();
                    return responseEntity.getStatusCode() == HttpStatus.OK &&
                           response != null &&
                           response.getResponse() != null &&
                           response.getError() == null;
                })
                .verifyComplete();
    }

    @Test
    void testGetGithubRepos_ServiceError() {
        String username = "testuser";
        int page = 1;
        String errorMessage = "User not found";
        when(githubService.fetchGithubUserRepositories(username, page))
                .thenReturn(Mono.error(new RuntimeException(errorMessage)));

        Mono<ResponseEntity<ControllerResponse<List<Repository>>>> result = 
                githubController.getGithubRepos(username, page);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    ControllerResponse<List<Repository>> response = responseEntity.getBody();
                    return responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST &&
                           response != null &&
                           response.getResponse() == null &&
                           errorMessage.equals(response.getError());
                })
                .verifyComplete();
    }

    @Test
    void testGetGithubRepos_EmptyResponse() {
        String username = "testuser";
        int page = 1;
        when(githubService.fetchGithubUserRepositories(username, page))
                .thenReturn(Mono.just(Arrays.asList()));

        Mono<ResponseEntity<ControllerResponse<List<Repository>>>> result = 
                githubController.getGithubRepos(username, page);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    ControllerResponse<List<Repository>> response = responseEntity.getBody();
                    return responseEntity.getStatusCode() == HttpStatus.OK &&
                           response != null &&
                           response.getResponse() != null &&
                           response.getResponse().isEmpty() &&
                           response.getError() == null;
                })
                .verifyComplete();
    }

    @Test
    void testGetGithubRepoCommits_Success() {
        String username = "testuser";
        String repository = "test-repo";
        int page = 1;
        when(githubService.fetchGithubRepoCommits(username, repository, page))
                .thenReturn(Mono.just(testCommits));

        Mono<ResponseEntity<ControllerResponse<List<Commit>>>> result = 
                githubController.getGithubRepos(username, repository, page);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    ControllerResponse<List<Commit>> response = responseEntity.getBody();
                    return responseEntity.getStatusCode() == HttpStatus.OK &&
                           response != null &&
                           response.getResponse() != null &&
                           response.getResponse().size() == 1 &&
                           response.getResponse().get(0).getSha().equals("abc123def456") &&
                           response.getError() == null;
                })
                .verifyComplete();
    }

    @Test
    void testGetGithubRepoCommits_WithDefaultPage() {
        String username = "testuser";
        String repository = "test-repo";
        when(githubService.fetchGithubRepoCommits(username, repository, 1))
                .thenReturn(Mono.just(testCommits));

        Mono<ResponseEntity<ControllerResponse<List<Commit>>>> result = 
                githubController.getGithubRepos(username, repository, 1);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    ControllerResponse<List<Commit>> response = responseEntity.getBody();
                    return responseEntity.getStatusCode() == HttpStatus.OK &&
                           response != null &&
                           response.getResponse() != null &&
                           response.getError() == null;
                })
                .verifyComplete();
    }

    @Test
    void testGetGithubRepoCommits_WithCustomPage() {
        String username = "testuser";
        String repository = "test-repo";
        int page = 3;
        when(githubService.fetchGithubRepoCommits(username, repository, page))
                .thenReturn(Mono.just(testCommits));

        Mono<ResponseEntity<ControllerResponse<List<Commit>>>> result = 
                githubController.getGithubRepos(username, repository, page);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    ControllerResponse<List<Commit>> response = responseEntity.getBody();
                    return responseEntity.getStatusCode() == HttpStatus.OK &&
                           response != null &&
                           response.getResponse() != null &&
                           response.getError() == null;
                })
                .verifyComplete();
    }

    @Test
    void testGetGithubRepoCommits_ServiceError() {
        String username = "testuser";
        String repository = "test-repo";
        int page = 1;
        String errorMessage = "Repository not found";
        when(githubService.fetchGithubRepoCommits(username, repository, page))
                .thenReturn(Mono.error(new RuntimeException(errorMessage)));

        Mono<ResponseEntity<ControllerResponse<List<Commit>>>> result = 
                githubController.getGithubRepos(username, repository, page);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    ControllerResponse<List<Commit>> response = responseEntity.getBody();
                    return responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST &&
                           response != null &&
                           response.getResponse() == null &&
                           errorMessage.equals(response.getError());
                })
                .verifyComplete();
    }

    @Test
    void testGetGithubRepoCommits_EmptyResponse() {
        String username = "testuser";
        String repository = "test-repo";
        int page = 1;
        when(githubService.fetchGithubRepoCommits(username, repository, page))
                .thenReturn(Mono.just(Arrays.asList()));

        Mono<ResponseEntity<ControllerResponse<List<Commit>>>> result = 
                githubController.getGithubRepos(username, repository, page);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    ControllerResponse<List<Commit>> response = responseEntity.getBody();
                    return responseEntity.getStatusCode() == HttpStatus.OK &&
                           response != null &&
                           response.getResponse() != null &&
                           response.getResponse().isEmpty() &&
                           response.getError() == null;
                })
                .verifyComplete();
    }

    @Test
    void testPing_Success() {
        ResponseEntity<Void> result = githubController.ping();

        assert result.getStatusCode() == HttpStatus.OK;
        assert result.getBody() == null;
    }

    @Test
    void testGetGithubRepos_NullUsername() {
        String username = null;
        int page = 1;
        when(githubService.fetchGithubUserRepositories(username, page))
                .thenReturn(Mono.error(new IllegalArgumentException("Username cannot be null")));

        Mono<ResponseEntity<ControllerResponse<List<Repository>>>> result = 
                githubController.getGithubRepos(username, page);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    ControllerResponse<List<Repository>> response = responseEntity.getBody();
                    return responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST &&
                           response != null &&
                           response.getResponse() == null &&
                           response.getError() != null;
                })
                .verifyComplete();
    }

    @Test
    void testGetGithubRepoCommits_NullRepository() {
        String username = "testuser";
        String repository = null;
        int page = 1;
        when(githubService.fetchGithubRepoCommits(username, repository, page))
                .thenReturn(Mono.error(new IllegalArgumentException("Repository cannot be null")));

        Mono<ResponseEntity<ControllerResponse<List<Commit>>>> result = 
                githubController.getGithubRepos(username, repository, page);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    ControllerResponse<List<Commit>> response = responseEntity.getBody();
                    return responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST &&
                           response != null &&
                           response.getResponse() == null &&
                           response.getError() != null;
                })
                .verifyComplete();
    }

    @Test
    void testGetGithubRepos_EmptyUsername() {
        String username = "";
        int page = 1;
        when(githubService.fetchGithubUserRepositories(username, page))
                .thenReturn(Mono.error(new IllegalArgumentException("Username cannot be empty")));

        Mono<ResponseEntity<ControllerResponse<List<Repository>>>> result = 
                githubController.getGithubRepos(username, page);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    ControllerResponse<List<Repository>> response = responseEntity.getBody();
                    return responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST &&
                           response != null &&
                           response.getResponse() == null &&
                           response.getError() != null;
                })
                .verifyComplete();
    }

    @Test
    void testGetGithubRepoCommits_EmptyRepository() {
        String username = "testuser";
        String repository = "";
        int page = 1;
        when(githubService.fetchGithubRepoCommits(username, repository, page))
                .thenReturn(Mono.error(new IllegalArgumentException("Repository cannot be empty")));

        Mono<ResponseEntity<ControllerResponse<List<Commit>>>> result = 
                githubController.getGithubRepos(username, repository, page);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    ControllerResponse<List<Commit>> response = responseEntity.getBody();
                    return responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST &&
                           response != null &&
                           response.getResponse() == null &&
                           response.getError() != null;
                })
                .verifyComplete();
    }

    @Test
    void testGetGithubRepos_InvalidPage() {
        String username = "testuser";
        int page = 0;
        when(githubService.fetchGithubUserRepositories(username, page))
                .thenReturn(Mono.error(new IllegalArgumentException("Page must be greater than 0")));

        Mono<ResponseEntity<ControllerResponse<List<Repository>>>> result = 
                githubController.getGithubRepos(username, page);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    ControllerResponse<List<Repository>> response = responseEntity.getBody();
                    return responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST &&
                           response != null &&
                           response.getResponse() == null &&
                           response.getError() != null;
                })
                .verifyComplete();
    }

    @Test
    void testGetGithubRepoCommits_InvalidPage() {
        String username = "testuser";
        String repository = "test-repo";
        int page = -1;
        when(githubService.fetchGithubRepoCommits(username, repository, page))
                .thenReturn(Mono.error(new IllegalArgumentException("Page must be greater than 0")));

        Mono<ResponseEntity<ControllerResponse<List<Commit>>>> result = 
                githubController.getGithubRepos(username, repository, page);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    ControllerResponse<List<Commit>> response = responseEntity.getBody();
                    return responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST &&
                           response != null &&
                           response.getResponse() == null &&
                           response.getError() != null;
                })
                .verifyComplete();
    }
}
