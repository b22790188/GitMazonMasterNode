package org.example.gitmazonmasternode.service;

import org.example.gitmazonmasternode.dto.ServiceInfoResponseDTO;
import org.example.gitmazonmasternode.dto.WorkerNodeResponseDTO;
import org.example.gitmazonmasternode.model.WorkerNode;
import org.example.gitmazonmasternode.repository.WorkerNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private WorkerNodeRepository workerNodeRepository;

    public List<WorkerNodeResponseDTO> getAllServices() {
        List<WorkerNode> workerNodes = workerNodeRepository.findAll();
        return workerNodes.stream()
            .map(workerNode -> {
                List<ServiceInfoResponseDTO> serviceDTOs = workerNode.getServices().stream()
                    .map(service -> new ServiceInfoResponseDTO(
                        service.getUser().getUsername(),
                        service.getServiceName(),
                        service.getRepoName(),
                        service.getEndpoint(),
                        service.getContainerName()))
                    .collect(Collectors.toList());
                return new WorkerNodeResponseDTO(workerNode.getWorkerNodeIp(), serviceDTOs);
            })
            .collect(Collectors.toList());
    }
}
