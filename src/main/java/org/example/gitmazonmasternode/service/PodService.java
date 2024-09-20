package org.example.gitmazonmasternode.service;

import lombok.extern.log4j.Log4j2;
import org.example.gitmazonmasternode.dto.RegisterServiceRequestDTO;
import org.example.gitmazonmasternode.model.User;
import org.example.gitmazonmasternode.repository.ServiceRepository;
import org.example.gitmazonmasternode.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@Service
public class PodService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Ec2Client ec2Client;

    private String securityGroupId = "sg-07fee68e6775cab2f";

    public Map<String, String> getInstanceInfo(String username, String repoName) {

        org.example.gitmazonmasternode.model.Service service = serviceRepository.findByUserUsernameAndRepoName(username, repoName);
        if (service == null) {
            return Map.of("error", "Service not found");
        }

        Map<String, String> instanceInfo = new HashMap<>();
        instanceInfo.put("podIP", service.getWorkerNodeIp());
        instanceInfo.put("container", service.getContainerName());
        instanceInfo.put("port", service.getPort().toString());

        return instanceInfo;
    }

    public Map<String, String> registerService(RegisterServiceRequestDTO registerServiceRequestDTO) {

        // Get user from db, if not found, create it.
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

        // call api to check available port on worker node
        String instanceIp = "18.182.42.57";
        String availablePortUrl = "http://" + instanceIp + ":8081/availablePort";

        ResponseEntity<Map> responseEntity = restTemplate.getForEntity(availablePortUrl, Map.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            return Map.of("error", "Failed to fetch available port");
        }

        Integer availablePort = (Integer) responseEntity.getBody().get("availablePort");

        String repoName = extractRepoNameFromRepoUrl(repoUrl);
        String containerName = extractContainerNameFromRepoUrl(repoUrl);

        // Create service and associate with user
        org.example.gitmazonmasternode.model.Service service = new org.example.gitmazonmasternode.model.Service();
        service.setUser(user);
        service.setRepoUrl(repoUrl);
        service.setRepoName(repoName);
        service.setServiceName(serviceName);
        service.setEndpoint(serviceUrl);
        service.setWorkerNodeIp(instanceIp);
        service.setContainerName(containerName);
        service.setPort(availablePort);

        user.addService(service);
        serviceRepository.save(service);

        addSecurityGroupRule(securityGroupId, availablePort);

        // Notify webhook server to build image upon registration
        notifyWebhookServer(repoUrl);

        // Register endpoint
        registerEndpoint(registerServiceRequestDTO.getUsername(), serviceName, instanceIp, availablePort);

        // todo: add SG Rule here

        Map<String, String> serviceUrlResponse = new HashMap<>();
        serviceUrlResponse.put("serviceUrl", serviceUrl);
        serviceUrlResponse.put("message", "您的服務網址是" + serviceUrl + "，將會在幾分鐘之內啟動");

        return serviceUrlResponse;

    }

    private void notifyWebhookServer(String repoUrl) {
        String webhookUrl = "http://54.168.192.186:8080/deploy";

        String repositoryOwner = extractOwnerFromRepoUrl(repoUrl);
        String repositoryName = extractRepoNameFromRepoUrl(repoUrl);

        // Create format needed by webhook server
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> repository = new HashMap<>();
        Map<String, Object> owner = new HashMap<>();

        owner.put("login", repositoryOwner);
        repository.put("name", repositoryName);
        repository.put("owner", owner);
        payload.put("repository", repository);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(webhookUrl, payload, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            log.info("Webhook server notified successfully.");
        } else {
            log.error("Failed to notify webhook server. Status code: " + responseEntity.getStatusCode());
        }
    }

    private void registerEndpoint(String username, String serviceName, String instanceIp, Integer port) {
        String registerEndpointUrl = "http://stylish.monster:8080/registerEndpoint";
        Map<String, Object> payload = new HashMap<>();

        payload.put("username", username);
        payload.put("serviceName", serviceName);
        payload.put("instanceIp", instanceIp);
        payload.put("port", port.toString());

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(registerEndpointUrl, payload, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            log.info("Register endpoint  successfully.");
        } else {
            log.error("Failed to register endpoint. Status code: " + responseEntity.getStatusCode());
        }
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

    private String extractOwnerFromRepoUrl(String repoUrl) {
        if (repoUrl != null && repoUrl.contains("/") && repoUrl.endsWith(".git")) {
            String path = repoUrl.substring(repoUrl.indexOf("://") + 3, repoUrl.lastIndexOf(".git"));
            String[] parts = path.split("/");

            return parts[1];
        }

        return null;
    }

    private String extractRepoNameFromRepoUrl(String repoUrl) {
        if (repoUrl != null && repoUrl.contains("/") && repoUrl.endsWith(".git")) {
            String path = repoUrl.substring(repoUrl.indexOf("://") + 3, repoUrl.lastIndexOf(".git"));
            String[] parts = path.split("/");

            return parts[2];
        }

        return null;
    }

    private void addSecurityGroupRule(String securityGroupId, int port) {
        try {

            DescribeSecurityGroupsRequest describeRequest = DescribeSecurityGroupsRequest.builder()
                .groupIds(securityGroupId)
                .build();

            DescribeSecurityGroupsResponse describeResponse = ec2Client.describeSecurityGroups(describeRequest);

            // Get first security group
            SecurityGroup securityGroup = describeResponse.securityGroups().get(0);

            // Check if rule already exist
            boolean isRuleExists = securityGroup.ipPermissions().stream().anyMatch(ipPermission ->
                ipPermission.fromPort() == port &&
                    ipPermission.toPort() == port &&
                    ipPermission.ipRanges().stream().anyMatch(ipRange -> ipRange.cidrIp().equals("0.0.0.0/0"))

            );

            if (isRuleExists) {
                log.info("Rule already exists. No need to add rule");
            } else {

                // Create permission rule
                IpPermission ipPermission = IpPermission.builder()
                    .ipProtocol("tcp")
                    .fromPort(port)
                    .toPort(port)
                    .ipRanges(IpRange.builder().cidrIp("0.0.0.0/0").build())
                    .build();

                AuthorizeSecurityGroupIngressRequest ingressRequest = AuthorizeSecurityGroupIngressRequest.builder()
                    .groupId(securityGroupId)
                    .ipPermissions(ipPermission)
                    .build();

                ec2Client.authorizeSecurityGroupIngress(ingressRequest);
                log.info("Security group rule added successfully.");
            }
        } catch (Ec2Exception e) {
            log.error("Failed to add security group rule: " + securityGroupId + ", cause:" + e.awsErrorDetails().errorMessage(), e);
        }
    }
}
