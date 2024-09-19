package org.example.gitmazonmasternode.controller;

import lombok.extern.log4j.Log4j2;
import org.example.gitmazonmasternode.PodService;
import org.example.gitmazonmasternode.dto.RegisterServiceRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Log4j2
@RestController
public class PodController {

    @Autowired
    private PodService podService;

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getInstanceInfo(@RequestParam String username, @RequestParam String repoName) {

        Map<String, String> instanceInfo = podService.getInstanceInfo(username, repoName);

        return ResponseEntity.ok(instanceInfo);
    }

    @PostMapping("/registerService")
    public ResponseEntity<Map<String, String>> registerService(@RequestBody RegisterServiceRequestDTO registerServiceRequestDTO) {

        Map<String, String> response = podService.registerService(registerServiceRequestDTO);

        return ResponseEntity.ok(response);
    }
}