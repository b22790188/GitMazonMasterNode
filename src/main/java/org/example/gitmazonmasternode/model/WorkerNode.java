package org.example.gitmazonmasternode.model;

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

    @OneToMany(mappedBy = "workerNode", fetch = FetchType.LAZY)
    private List<Service> services;
}
