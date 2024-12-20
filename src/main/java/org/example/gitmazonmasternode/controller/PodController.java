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
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    public ResponseEntity<?> getInstanceInfo(@RequestParam String username, @RequestParam String repoName) {
        try {
            Map<String, String> instanceInfo = podService.getInstanceInfo(username, repoName);
            return ResponseEntity.ok(instanceInfo);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
        try {
            Map<String, String> userInfo = getUserInfoFromJwtInRequest(request);
            String username = userInfo.get("username");

            Map<String, String> response = new HashMap<>();
            response.put("username", username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/userService")
    public ResponseEntity<?> getServiceInfo(@RequestParam String username) {
        try {
            log.info(username);
            List<ServiceInfoResponseDTO> serviceInfoResponseDTOList = podService.getServiceInfoByUserName(username);
            return ResponseEntity.ok(serviceInfoResponseDTOList);
        } catch (JwtException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/registerService")
    public ResponseEntity<?> registerService(@RequestBody RegisterServiceRequestDTO registerServiceRequestDTO, HttpServletRequest request) {
        try {
            Map<String, String> userInfo = getUserInfoFromJwtInRequest(request);
            String accessToken = userInfo.get("accessToken");

            Map<String, String> response = podService.registerService(registerServiceRequestDTO, accessToken);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/unRegisterService")
    public ResponseEntity<?> unRegisterService(@RequestParam String username, @RequestParam String repoName) {
        try {
            boolean isSuccess = podService.unRegisterService(username, repoName);
            String response = isSuccess ? "unregister successfully" : "unregister failed";
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/restartContainer")
    public ResponseEntity<?> restartContainer(@RequestParam String username, @RequestParam String repoName) {
        try {
            boolean isSuccess = podService.restartContainer(username, repoName);
            String response = isSuccess ? "restart successfully" : "restart failed";
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


    private Map<String, String> getUserInfoFromJwtInRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        Map<String, String> userInfo = new HashMap<>();

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
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