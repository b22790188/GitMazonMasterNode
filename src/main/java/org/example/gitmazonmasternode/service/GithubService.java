package org.example.gitmazonmasternode.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.example.gitmazonmasternode.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
public class GithubService {

    @Autowired
    private ServiceRepository serviceRepository;

    private static final String GITHUB_API_URL = "https://api.github.com/user/repos?visibility=public";
    private static final String GITHUB_USER_URL = "https://api.github.com/user";

    public List<String> getUserRepositories(String token) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> userResponse = restTemplate.exchange(
            GITHUB_USER_URL,
            HttpMethod.GET,
            entity,
            String.class
        );

        ObjectMapper mapper = new ObjectMapper();
        String username;
        try {
            JsonNode userRoot = mapper.readTree(userResponse.getBody());
            username = userRoot.get("login").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error fetching GitHub user data", e);
        }

        List<String> registeredRepoUrls = serviceRepository.findByUserUsername(username).stream()
            .map(org.example.gitmazonmasternode.model.Service::getRepoUrl)
            .toList();

        ResponseEntity<String> reposResponse = restTemplate.exchange(
            GITHUB_API_URL,
            HttpMethod.GET,
            entity,
            String.class
        );

        try {
            JsonNode reposRoot = mapper.readTree(reposResponse.getBody());
            List<String> cloneUrls = new ArrayList<>();

            for (JsonNode repoNode : reposRoot) {
                String cloneUrl = repoNode.get("clone_url").asText();
                if (!registeredRepoUrls.contains(cloneUrl)) {
                    cloneUrls.add(cloneUrl);
                }
            }

            return cloneUrls;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing GitHub API response", e);
        }
    }
}

