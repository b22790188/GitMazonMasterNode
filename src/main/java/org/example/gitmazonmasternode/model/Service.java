package org.example.gitmazonmasternode.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "service")
@Data
@NoArgsConstructor
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_node_id", nullable = false)
    private WorkerNode workerNode;

    @Column(name = "repo_url", nullable = false)
    private String repoUrl;

    @Column(name = "repo_name", nullable = false)
    private String repoName;

    @Column(name = "endpoint", nullable = false)
    private String endpoint;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "worker_node_ip", nullable = false)
    private String workerNodeIp;

    @Column(name = "container_name", nullable = false)
    private String containerName;

    @Column(name = "port", nullable = false)
    private Integer port;

    @Column(name = "cpu", nullable = false)
    private Float cpu;

    @Column(name = "memory", nullable = false)
    private Float memory;
}
