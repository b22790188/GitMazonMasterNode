package org.example.gitmazonmasternode.repository;

import org.example.gitmazonmasternode.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.githubAccessToken = :accessToken WHERE u.username = :username")
    void updateAccessTokenByUsername(String username, String accessToken);
}
