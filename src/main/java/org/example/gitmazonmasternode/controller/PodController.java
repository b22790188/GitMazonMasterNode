package org.example.gitmazonmasternode.controller;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.example.gitmazonmasternode.dto.RegisterServiceRequestDTO;
import org.example.gitmazonmasternode.dto.ServiceInfoResponseDTO;
import org.example.gitmazonmasternode.service.PodService;
import org.example.gitmazonmasternode.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Log4j2
@RestController
public class PodController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PodService podService;

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getInstanceInfo(@RequestParam String username, @RequestParam String repoName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        log.info(principal);

        Map<String, String> instanceInfo = podService.getInstanceInfo(username, repoName);
        return ResponseEntity.ok(instanceInfo);
    }

    @GetMapping("/userService")
    public ResponseEntity<?> getUserInfo(@RequestParam String username) {
        log.info(username);
        List<ServiceInfoResponseDTO> serviceInfoResponseDTOList = podService.getServiceInfoByUserName(username);
        return ResponseEntity.ok(serviceInfoResponseDTOList);
    }

    @PostMapping("/registerService")
    public ResponseEntity<Map<String, String>> registerService(@RequestBody RegisterServiceRequestDTO registerServiceRequestDTO, HttpServletRequest request) {
        Map<String, String> response = podService.registerService(registerServiceRequestDTO);
        return ResponseEntity.ok(response);
    }

    private String getUsernameFromJwtInRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        log.info(authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            try {
                // Validate the token and extract the username
                String username = jwtUtil.extractUsername(token);
                log.info("JWT Token belongs to user: {}", username);
                return username;
            } catch (JwtException e) {
                log.error("Invalid JWT token: {}", e.getMessage());
                return null;
            }
        } else {
            return "Missing Authorization header";
        }
    }
}