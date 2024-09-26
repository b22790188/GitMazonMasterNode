package org.example.gitmazonmasternode.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.example.gitmazonmasternode.dto.RegisterServiceRequestDTO;
import org.example.gitmazonmasternode.dto.ServiceInfoResponseDTO;
import org.example.gitmazonmasternode.model.User;
import org.example.gitmazonmasternode.model.WorkerNode;
import org.example.gitmazonmasternode.repository.ServiceRepository;
import org.example.gitmazonmasternode.repository.UserRepository;
import org.example.gitmazonmasternode.repository.WorkerNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Autowired
    private WorkerNodeRepository workerNodeRepository;

    //todo: refactor to environment variable
    private final String securityGroupId = "sg-07fee68e6775cab2f";

    private final AtomicInteger currentWorkerNode = new AtomicInteger(0);

    @PostConstruct
    public void initWorkerNodes() {
        // Initialize worker node with 1 cpu and 8G RAM,
        // and leave 80% of resources for user service usage
        for (String nodeIp : workerNodes) {
            if (workerNodeRepository.findByWorkerNodeIp(nodeIp) == null) {
                WorkerNode workerNode = new WorkerNode();
                workerNode.setWorkerNodeIp(nodeIp);
                workerNode.setCpu(1.0f);
                workerNode.setMemory(8.0f);
                workerNode.setAvailableCpu(1.0f * 0.8f);
                workerNode.setAvailableMemory(8.0f * 0.8f);
                workerNodeRepository.save(workerNode);
            }
        }
    }

    private final String[] workerNodes = {
        "18.182.42.57",
        "18.176.54.151",
        "13.115.128.94"
    };

    public Map<String, String> getInstanceInfo(String username, String repoName) {

        org.example.gitmazonmasternode.model.Service service = serviceRepository.findByUserUsernameAndRepoName(username, repoName);
        if (service == null) {
            return Map.of("error", "Service not found");
        }

        WorkerNode workerNode = service.getWorkerNode();
        if (workerNode == null) {
            return Map.of("error", "Worker node not found");
        }

        Map<String, String> instanceInfo = new HashMap<>();
        instanceInfo.put("podIP", workerNode.getWorkerNodeIp());
        instanceInfo.put("container", service.getContainerName());
        instanceInfo.put("port", service.getPort().toString());
        instanceInfo.put("cpu", service.getCpu().toString());
        instanceInfo.put("memory", service.getMemory().toString());

        return instanceInfo;
    }

    public List<ServiceInfoResponseDTO> getServiceInfoByUserName(String username) {
        return serviceRepository.findServiceInfoByUserName(username);
    }

    public Map<String, String> registerService(RegisterServiceRequestDTO registerServiceRequestDTO, String accessToken) {

        // Get user from db, if not found, create it.
        User user = userRepository.findByUsername(registerServiceRequestDTO.getUsername());
        if (user == null) {
            user = new User();
            user.setUsername(registerServiceRequestDTO.getUsername());
            userRepository.save(user);
        }

        // concat user service endpoint
        String repoUrl = registerServiceRequestDTO.getRepoUrl();
        String serviceName = registerServiceRequestDTO.getServiceName();
        String serviceUrl = "https://stylish.monster/" + registerServiceRequestDTO.getUsername()
            + "/" + registerServiceRequestDTO.getServiceName();

        String repoOwner = extractOwnerFromRepoUrl(repoUrl);
        String repoName = extractRepoNameFromRepoUrl(repoUrl);
        String containerName = extractContainerNameFromRepoUrl(repoUrl);

        //todo: check existing worker node: sprint 3

        //todo: handle duplicate service registration: sprint 4

        setGithubWebhook(repoOwner, repoName, accessToken);


        // Get worker node instance ip
        Float serviceReqCpu = registerServiceRequestDTO.getCpu();
        Float serviceReqMemory = registerServiceRequestDTO.getMemory();
        WorkerNode workerNode = assignWorkerNode(serviceReqCpu, serviceReqMemory);
        String instanceIp = workerNode.getWorkerNodeIp();

        // Call api to check available port on worker node
        String availablePortUrl = "http://" + instanceIp + ":8081/availablePort";
        ResponseEntity<Map> responseEntity = restTemplate.getForEntity(availablePortUrl, Map.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            return Map.of("error", "Failed to fetch available port");
        }

        Integer availablePort = (Integer) responseEntity.getBody().get("availablePort");


        // Create service and associate with user
        org.example.gitmazonmasternode.model.Service service = new org.example.gitmazonmasternode.model.Service();
        service.setUser(user);
        service.setWorkerNode(workerNode);
        service.setRepoUrl(repoUrl);
        service.setRepoName(repoName);
        service.setServiceName(serviceName);
        service.setEndpoint(serviceUrl);
        service.setWorkerNodeIp(instanceIp);
        service.setContainerName(containerName);
        service.setPort(availablePort);
        service.setCpu(serviceReqCpu);
        service.setMemory(serviceReqMemory);

        user.addService(service);
        serviceRepository.save(service);

        //todo: remove
        log.info("service assign succeed");

        addSecurityGroupRule(securityGroupId, availablePort);

        // Notify webhook server to build image upon registration
        notifyWebhookServer(repoUrl);

        // Register endpoint
        registerEndpoint(registerServiceRequestDTO.getUsername(), serviceName, instanceIp, availablePort);

        // todo: move add SG rule from above to here

        Map<String, String> serviceUrlResponse = new HashMap<>();
        serviceUrlResponse.put("serviceUrl", serviceUrl);
        serviceUrlResponse.put("message", "您的服務網址是" + serviceUrl + "，將會在幾分鐘之內啟動");

        return serviceUrlResponse;

    }

    private void notifyWebhookServer(String repoUrl) {
        //todo: refactor image builder server ip to environment variable
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

    private WorkerNode assignWorkerNode(float requiredCpu, float requiredMemory) {
        //check if cpu and memory are available for assign service
        // if not, go to next worker node, until all worker nodes are unavailable.

        int startIndex = currentWorkerNode.get();
        int workerNodeCount = workerNodes.length;

        for (int i = 0; i < workerNodeCount; i++) {
            int currentIndex = (startIndex + i) % workerNodeCount;
            String instanceIp = workerNodes[currentIndex];
            WorkerNode workerNode = workerNodeRepository.findByWorkerNodeIp(instanceIp);

            if (workerNode != null && workerNode.getAvailableCpu() >= requiredCpu &&
                workerNode.getAvailableMemory() >= requiredMemory) {
                workerNode.setAvailableCpu(workerNode.getAvailableCpu() - requiredCpu);
                workerNode.setAvailableMemory(workerNode.getAvailableMemory() - requiredMemory);
                workerNodeRepository.save(workerNode);

                currentWorkerNode.set((currentIndex) + 1 % workerNodeCount);
                return workerNode;
            }
        }

        throw new IllegalStateException("No available worker node");
//        int currentIndex = currentWorkerNode.getAndUpdate(i -> (i + 1) % workerNodes.length);
//        return workerNodes[currentIndex];
    }

    private void setGithubWebhook(String repoOwner, String repoName, String accessToken) {
        String setWebhookUrl = "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/hooks";

        //todo: refactor webhookUrl to environment variable
        String webhookUrl = "http://54.168.192.186:8080/deploy";
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "web");
        Map<String, Object> config = new HashMap<>();
        config.put("url", webhookUrl);
        config.put("content_type", "json");
        payload.put("config", config);
        payload.put("events", List.of("push"));
        payload.put("active", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        log.info(accessToken);

        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(setWebhookUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("GitHub Webhook set successfully.");
            } else {
                log.error("Failed to set GitHub Webhook, status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            if (e.getMessage().contains("422")) {
                log.warn("Webhook already exists, continuing with the process.");
            } else {
                log.error("Failed to set GitHub Webhook, error: " + e.getMessage(), e);
                throw e;
            }
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

}
