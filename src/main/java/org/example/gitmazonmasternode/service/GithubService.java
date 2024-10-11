package org.example.gitmazonmasternode.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
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


    private static final String GITHUB_API_URL = "https://api.github.com/user/repos";

    public List<String> getUserRepositories(String token) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            GITHUB_API_URL,
            HttpMethod.GET,
            entity,
            String.class
        );

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(response.getBody());
            List<String> cloneUrls = new ArrayList<>();

            for (JsonNode node : root) {
                String cloneUrl = node.get("clone_url").asText();
                cloneUrls.add(cloneUrl);
            }

            return cloneUrls;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing GitHub API response", e);
        }
    }
}

