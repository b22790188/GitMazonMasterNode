package org.example.gitmazonmasternode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServiceInfoResponseDTO {
    private String username;
    private String serviceName;
    private String repoName;
    private String endpoint;
}
