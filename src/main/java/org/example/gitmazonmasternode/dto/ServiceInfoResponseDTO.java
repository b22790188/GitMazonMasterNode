package org.example.gitmazonmasternode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServiceInfoResponseDTO {
    private String endpoint;
    private String repoName;
    private String serviceName;
}
