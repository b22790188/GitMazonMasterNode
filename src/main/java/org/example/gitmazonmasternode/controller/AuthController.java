package org.example.gitmazonmasternode.controller;

import lombok.extern.log4j.Log4j2;
import org.example.gitmazonmasternode.model.User;
import org.example.gitmazonmasternode.repository.UserRepository;
import org.example.gitmazonmasternode.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Log4j2
@Controller
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${cors.allowed.origins}")
    private String frontendServer;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/toFrontend")
    public String redirectToFrontend(OAuth2AuthenticationToken authentication) {
        // Get user info
        OAuth2User principal = authentication.getPrincipal();
        String username = principal.getAttribute("login");
        log.info(username);

        // Get token info
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
            authentication.getAuthorizedClientRegistrationId(),
            authentication.getName());

        if (client != null) {
            String accessToken = client.getAccessToken().getTokenValue();
            log.info(accessToken);

            User user = userRepository.findByUsername(username);
            if (user != null) {
                userRepository.updateAccessTokenByUsername(username, accessToken);
            } else {
                user = new User();
                user.setUsername(username);
                user.setGithubAccessToken(accessToken);
                userRepository.save(user);
            }
        }

        return "redirect:" + frontendServer + "/registerService.html?username=" + username;
    }

    @GetMapping("/generateJwtToken")
    @ResponseBody
    public ResponseEntity<String> generateJwtToken(@RequestParam String username) {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        String githubAccessToken = user.getGithubAccessToken();
        String jwtToken = jwtUtil.generateToken(username, githubAccessToken);
        return ResponseEntity.ok(jwtToken);
    }
}
