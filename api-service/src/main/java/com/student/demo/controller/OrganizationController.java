package com.student.demo.controller;

import com.student.demo.entity.Organization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/orgs")
public class OrganizationController {

    // Simple stubs for Phase 8. In a real system, these would interact with an OrganizationRepository and UserService.

    @PostMapping
    public ResponseEntity<Organization> createOrganization(@RequestBody Map<String, String> payload) {
        Organization org = new Organization();
        org.setId(UUID.randomUUID());
        org.setName(payload.get("name"));
        org.setPlanTier("FREE");
        return ResponseEntity.ok(org);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getMyOrganization() {
        // Mock returning the active organization for the current user
        return ResponseEntity.ok(Map.of(
            "id", UUID.randomUUID().toString(),
            "name", "Acme Corp",
            "planTier", "PRO"
        ));
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<Map<String, String>> inviteUser(@PathVariable UUID id, @RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        return ResponseEntity.ok(Map.of("message", "Invitation sent to " + email));
    }
}
