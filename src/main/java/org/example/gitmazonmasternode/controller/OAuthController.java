package org.example.gitmazonmasternode.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Log4j2
@Controller
public class OAuthController {

    @GetMapping("/toFrontend")
    public String registerService(@AuthenticationPrincipal OAuth2User principal) {
        // Get user info
        log.info(principal.getAttributes().get("login"));
        return "redirect:http://localhost:8080/registerService.html";
    }
}
