package org.example.gitmazonmasternode.repository;

import org.example.gitmazonmasternode.model.WorkerNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkerNodeRepository extends JpaRepository<WorkerNode, Long> {
    WorkerNode findByWorkerNodeIp(String workerNodeIp);
}
