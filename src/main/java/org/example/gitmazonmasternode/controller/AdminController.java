package org.example.gitmazonmasternode.controller;

import lombok.extern.log4j.Log4j2;
import org.example.gitmazonmasternode.dto.WorkerNodeResponseDTO;
import org.example.gitmazonmasternode.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Log4j2
@RestController
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/services")
    public ResponseEntity<?> getAllServices() {
        List<WorkerNodeResponseDTO> response = adminService.getAllServices();
        return ResponseEntity.ok(response);
    }
}
