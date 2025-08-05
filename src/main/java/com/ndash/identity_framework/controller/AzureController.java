package com.ndash.identity_framework.controller;

import com.microsoft.graph.models.User;
import com.ndash.identity_framework.services.AzureADService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@RestController
//@RequestMapping("/users")
public class AzureController {

    @Autowired
    private AzureADService azureADService;

    @GetMapping
    public List<User> getAllUsers() {
        return azureADService.getAllUsers();
    }

    @PostMapping
    public User createUser(@RequestParam String displayName, @RequestParam String mail) {
        return azureADService.createUser(displayName, mail);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        azureADService.deleteUser(userId);
        return ResponseEntity.ok("User deleted");
    }
}

