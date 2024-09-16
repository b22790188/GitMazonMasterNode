package org.example.gitmazonmasternode;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PodController {

    @GetMapping("/info")
    public Map<String, String> getInstanceInfo(@RequestParam String username) {

        String instanceIp = "18.182.42.57";
        String containerName = "pusheventtest";

        Map<String, String> instanceInfo = new HashMap<>();
        instanceInfo.put("podIP", instanceIp);
        instanceInfo.put("container", containerName);

        return instanceInfo;
    }
}

