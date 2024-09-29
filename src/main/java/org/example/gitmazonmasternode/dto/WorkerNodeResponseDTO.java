package org.example.gitmazonmasternode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class WorkerNodeResponseDTO {

    private String WorkerNodeIp;
    private List<ServiceInfoResponseDTO> services;

}
