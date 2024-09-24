package org.example.gitmazonmasternode.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String githubAccessToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Service> services;

    public void addService(Service service) {
        // Only create list when services is null,
        // for situation when there is new user want to add service.
        if (services == null) {
            services = new ArrayList<>();
        }

        services.add(service);
        service.setUser(this);
    }

    public void removeService(Service service) {
        services.remove(service);
        service.setUser(null);
    }
}
