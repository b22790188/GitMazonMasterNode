package org.example.gitmazonmasternode.controller;

import lombok.extern.log4j.Log4j2;
import org.example.gitmazonmasternode.dto.RegisterServiceRequestDTO;
import org.example.gitmazonmasternode.model.Service;
import org.example.gitmazonmasternode.model.User;
import org.example.gitmazonmasternode.repository.ServiceRepository;
import org.example.gitmazonmasternode.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestController
public class PodController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getInstanceInfo(@RequestParam String username, @RequestParam String serviceName) {

        Service service = serviceRepository.findByUserUsernameAndServiceName(username, serviceName);

        if(service == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Service not found"));
        }

        Map<String, String> instanceInfo = new HashMap<>();
        instanceInfo.put("podIP", service.getInstanceIp());
        instanceInfo.put("container", service.getContainerName());

        return ResponseEntity.ok(instanceInfo);
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


        String repoUrl = registerServiceRequestDTO.getRepoUrl();
        String serviceName = registerServiceRequestDTO.getServiceName();
        String serviceUrl = "https://stylish.monster/" + registerServiceRequestDTO.getUsername()
            + "/" + registerServiceRequestDTO.getServiceName();

        String instanceIp = "18.182.42.57";
        String availablePortUrl = "http://" + instanceIp + ":8081/availablePort";

        ResponseEntity<Map> responseEntity = restTemplate.getForEntity(availablePortUrl, Map.class);
        if(!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to fetch available port"));
        }

        //todo: add port into service table
        int availablePort = (int) responseEntity.getBody().get("availablePort");

        log.info(availablePort);

        String containerName = extractContainerNameFromRepoUrl(repoUrl);

        // Create service and associate with user
        Service service = new Service();
        service.setUser(user);
        service.setRepoUrl(repoUrl);
        service.setServiceName(serviceName);
        service.setEndpoint(serviceUrl);
        service.setInstanceIp(instanceIp);
        service.setContainerName(containerName);

        user.addService(service);
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("serviceUrl", serviceUrl);
        response.put("message", "您的服務網址是" + serviceUrl +"，將會在幾分鐘之內啟動");

        return ResponseEntity.ok(response);
    }

    private String extractContainerNameFromRepoUrl(String repoUrl) {
        if (repoUrl != null && repoUrl.contains("/") && repoUrl.endsWith(".git")) {
            // get string after last "/" and remove .git, only support https format now
            String path = repoUrl.substring(repoUrl.indexOf("://") + 3, repoUrl.lastIndexOf(".git"));
            String[] parts = path.split("/");

            String username = parts[1];
            String serviceName = parts[2];

            return username + "_" + serviceName;
        }

        return null;
    }
}

