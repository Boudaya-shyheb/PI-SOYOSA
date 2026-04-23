package soyosa.entretien.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import soyosa.entretien.Domain.Entretien;
import soyosa.entretien.service.EntretienService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/entretiens")
@CrossOrigin(origins = "http://localhost:4200")
public class EntretienController {

    private final EntretienService service;

    @Autowired
    public EntretienController(EntretienService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping
    public ResponseEntity<Entretien> createInterview(
            @RequestParam("file") MultipartFile file,
            @RequestParam("date") LocalDate date,
            @RequestParam("user") String user
    ) {

        return ResponseEntity.ok(service.createEntretien(file,date,user));
    }

    @GetMapping
    public ResponseEntity<List<Entretien>> getAllEntretiens() {
        return ResponseEntity.ok(service.getAllEntretiens());
    }

    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<Entretien> getEntretienById(@PathVariable Long id) {
        return service.getEntretienById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/user/{username}")
    public ResponseEntity<Entretien> getEntretienByUsername(@PathVariable String username) {
        return ResponseEntity.ok(service.getEntretienByUsername(username));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/schedule")
    public ResponseEntity<Entretien> schedule(
            @PathVariable Long id,
            @RequestBody Entretien entretien
    ) {
        return ResponseEntity.ok(service.updateEntretien(id,entretien));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/pass")
    public ResponseEntity<Entretien> pass(@PathVariable Long id){
        return ResponseEntity.ok(service.passJobInterview(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/fail")
    public ResponseEntity<Entretien> fail(@PathVariable Long id){
        return ResponseEntity.ok(service.failJobInterview(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntretien(@PathVariable Long id) {
        service.deleteEntretien(id);
        return ResponseEntity.noContent().build();
    }
}
