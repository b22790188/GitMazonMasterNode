package org.example.gitmazonmasternode.repository;

import org.example.gitmazonmasternode.dto.ServiceInfoResponseDTO;
import org.example.gitmazonmasternode.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface ServiceRepository extends JpaRepository<Service, Long> {
    Service findByUserUsernameAndRepoName(String userName, String repoName);

    Service findByUserUsernameAndServiceName(String userName, String serviceName);

    List<Service> findByUserUsername(String userName);

    @Query("SELECT new org.example.gitmazonmasternode.dto.ServiceInfoResponseDTO(s.user.username, s.serviceName, s.repoName, s.endpoint, s.containerName) " +
        "FROM Service s WHERE s.user.username = :username")
    List<ServiceInfoResponseDTO> findServiceInfoByUserName(String username);
//
//    @Query("SELECT s FROM Service s JOIN FETCH s.workerNode JOIN FETCH s.user")
//    List<Service> findAllServices();
}
