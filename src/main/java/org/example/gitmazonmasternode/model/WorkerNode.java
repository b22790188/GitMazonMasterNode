package org.example.gitmazonmasternode.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "worker_node")
@Data
@NoArgsConstructor
public class WorkerNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "worker_node_ip", nullable = false)
    private String workerNodeIp;

    @JsonIgnore
    @OneToMany(mappedBy = "workerNode", fetch = FetchType.LAZY)
    private List<Service> services;

    @Column(name = "cpu", nullable = false)
    private Float cpu;

    @Column(name = "memory", nullable = false)
    private Float memory;

    @Column(name = "availabe_cpu", nullable = false)
    private Float availableCpu;

    @Column(name = "available_memory", nullable = false)
    private Float availableMemory;
}

