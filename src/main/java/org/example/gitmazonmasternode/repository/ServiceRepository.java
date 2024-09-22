package org.example.gitmazonmasternode.repository;

import org.example.gitmazonmasternode.dto.ServiceInfoResponseDTO;
import org.example.gitmazonmasternode.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface ServiceRepository extends JpaRepository<Service, Long> {
    Service findByUserUsernameAndRepoName(String userName, String repoName);

    @Query("SELECT new org.example.gitmazonmasternode.dto.ServiceInfoResponseDTO(s.endpoint, s.repoName, s.serviceName) " +
        "FROM Service s WHERE s.user.username = :username")
    List<ServiceInfoResponseDTO> findServiceInfoByUserName(String username);
}
