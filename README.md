Spring Boot GitHub API Client

This project is a Spring Boot application designed to interact with the GitHub API. It offers two primary endpoints for fetching a user's repositories and a specific repository's commits.

1. To get started, clone the prod branch of this repository to your local machine:

2. View the Code Structure

    config
   
        ConnectorClients: This is where all third-party web clients are declared. Currently, it includes the client for the GitHub API.
   
    controllers
   
        GithubController: Contains the two GET API endpoints for fetching repositories and commits.
   
    services
   
        GithubServiceSkeleton: Defines the skeleton for the main service.
        GithubService: Implements the skeleton, including the logic for making API calls to fetch repository and commit data with unified error handling.

3. Add your github PAT token to application.properties file
   
        github.token=<your-github-token>

5. Run the Server
   
    You can run the Spring Boot server on port 8086. You can use your IDE or the command line to start the application.

        ./mvnw spring-boot:run -Dserver.port=8086

Once the server is up and running, you can use a tool like POSTMAN to test the API endpoints.

1. Fetch Repositories
   
    This API endpoint retrieves a paginated list of repositories for a given GitHub username.

        Endpoint: http://localhost:8086/github/repositories
        Method: GET
        Parameters:
        username: Your GitHub username.
        page: The page number of results(Optional, default=1)
        
        Example:
        curl --location 'http://localhost:8086/github/repositories?username=srivnaimish&page=1'

2. Fetch Commits
   
    This API endpoint retrieves a paginated list of commits for a specific repository.

        Endpoint: http://localhost:8086/github/repo/commits
        Method: GET
        Parameters:
        username: The GitHub username.
        repository: The name of the repository.
        page: The page number of results(Optional, default=1)
        
        Example:
        curl --location 'http://localhost:8086/github/repo/commits?username=srivnaimish&repository=autoads&page=1'
