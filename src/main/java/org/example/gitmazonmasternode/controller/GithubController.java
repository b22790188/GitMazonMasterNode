package org.example.gitmazonmasternode.controller;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.example.gitmazonmasternode.service.GithubService;
import org.example.gitmazonmasternode.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api/github")
public class GithubController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private GithubService githubService;

    @GetMapping("/repos")
    public ResponseEntity<?> getUserRepos(HttpServletRequest request) {
        try {
            Map<String, String> userInfo = getUserInfoFromJwtInRequest(request);
            String accessToken = userInfo.get("accessToken");
            List<String> cloneUrls = githubService.getUserRepositories(accessToken);
            return ResponseEntity.ok(cloneUrls);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage() + "error fetching user repositories");
        }
    }

    private Map<String, String> getUserInfoFromJwtInRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        Map<String, String> userInfo = new HashMap<>();

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            try {
                // Validate the token and extract the username
                String username = jwtUtil.extractUsername(token);
                String accessToken = jwtUtil.extractAccessToken(token);
                userInfo.put("username", username);
                userInfo.put("accessToken", accessToken);

                return userInfo;
            } catch (JwtException e) {
                log.error("Invalid JWT token: {}", e.getMessage());
                return null;
            }
        } else {
            userInfo.put("error", "Missing or malformed Authorization header");
            return userInfo;
        }
    }
}
