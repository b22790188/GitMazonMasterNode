package org.example.gitmazonmasternode.controller;

import org.example.gitmazonmasternode.dto.RegisterServiceRequestDTO;
import org.example.gitmazonmasternode.model.Service;
import org.example.gitmazonmasternode.model.User;
import org.example.gitmazonmasternode.repository.ServiceRepository;
import org.example.gitmazonmasternode.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PodController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping("/info")
    public Map<String, String> getInstanceInfo(@RequestParam String username) {

        String instanceIp = "18.182.42.57";
        String containerName = "pusheventtest";

        Map<String, String> instanceInfo = new HashMap<>();
        instanceInfo.put("podIP", instanceIp);
        instanceInfo.put("container", containerName);

        return instanceInfo;
    }

    @PostMapping("/registerService")
    public ResponseEntity<Map<String, String>> registerService(@RequestBody RegisterServiceRequestDTO registerServiceRequestDTO) {

        // Get or create User
        User user = userRepository.findByUsername(registerServiceRequestDTO.getUsername());
        if (user == null) {
           user = new User();
           user.setUsername(registerServiceRequestDTO.getUsername());
           userRepository.save(user);
        }

        // Concat serviceUrl
        String serviceUrl = "https://stylish.monster/" + registerServiceRequestDTO.getUsername()
            + "/" + registerServiceRequestDTO.getServiceName();

        // Create service and associate with user
        Service service = new Service();
        service.setUser(user);
        service.setRepoUrl(registerServiceRequestDTO.getRepoUrl());
        service.setServiceName(registerServiceRequestDTO.getServiceName());
        service.setEndpoint(serviceUrl);

        user.addService(service);
        userRepository.save(user);


        Map<String, String> response = new HashMap<>();
        response.put("serviceUrl", serviceUrl);
        response.put("message", "您的服務網址是" + serviceUrl +"，將會在幾分鐘之內啟動");

        return ResponseEntity.ok(response);
    }
}

