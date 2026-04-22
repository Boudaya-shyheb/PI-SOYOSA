package org.example.pi_events.controller;

import lombok.RequiredArgsConstructor;
import org.example.pi_events.DTO.ClubDto;
import org.example.pi_events.service.ClubService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ClubController {

    private final ClubService clubService;

    @PostMapping
    public ClubDto createClub(
            @RequestBody ClubDto dto,
            @RequestHeader(value = "User-Role", required = false) String role) {
        RoleSecurity.requireAnyRole(role, "ADMIN", "PRESIDENT");
        return clubService.createClub(dto);
    }

    @GetMapping
    public List<ClubDto> getAllClubs() {
        return clubService.getAllClubs();
    }

    @GetMapping("/{id}")
    public ClubDto getClubById(@PathVariable Long id) {
        return clubService.getClubById(id);
    }

    @PutMapping("/{id}")
    public ClubDto updateClub(
            @PathVariable Long id,
            @RequestBody ClubDto dto,
            @RequestHeader(value = "User-Role", required = false) String role) {
        RoleSecurity.requireAnyRole(role, "ADMIN", "PRESIDENT", "VICE_PRESIDENT");
        return clubService.updateClub(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteClub(
            @PathVariable Long id,
            @RequestHeader(value = "User-Role", required = false) String role) {
        RoleSecurity.requireAnyRole(role, "ADMIN", "PRESIDENT");
        clubService.deleteClub(id);
    }
}
