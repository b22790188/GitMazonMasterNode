package org.example.gitmazonmasternode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterServiceRequestDTO {
    public String username;
    public String serviceName;
    public String repoUrl;
}
