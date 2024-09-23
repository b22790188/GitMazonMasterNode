package org.example.gitmazonmasternode.controller;

import lombok.extern.log4j.Log4j2;
import org.example.gitmazonmasternode.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Log4j2
@Controller
public class AuthController {

    @Autowired
    JwtUtil jwtUtil;

    @GetMapping("/toFrontend")
    public String redirectToFrontend(@AuthenticationPrincipal OAuth2User principal) {
        // Get user info
        String username = principal.getAttribute("login");
        log.info(username);
        return "redirect:http://localhost:8080/registerService.html?username=" + username;
    }

    @GetMapping("/generateJwtToken")
    @ResponseBody
    public ResponseEntity<String> generateJwtToken(@RequestParam String username) {
        String jwtToken = jwtUtil.generateToken(username);
        return ResponseEntity.ok(jwtToken);
    }
}
