package soyosa.userservice.Controller;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import soyosa.userservice.Config.MailConfig.EmailService;
import soyosa.userservice.Domain.Profile.Profile;
import soyosa.userservice.Service.ProfileService;

import javax.xml.crypto.OctetStreamData;
import java.io.IOException;
import java.util.List;

@Tag(name = "Gestion profile")
@RestController
@RequestMapping("/profiles")
public class ProfileController {

    private final ProfileService profileService;
    private final EmailService emailService;

    public ProfileController(ProfileService profileService, EmailService emailService) {
        this.profileService = profileService;
        this.emailService = emailService;
    }

    @PostMapping("/add-profile-image/{username}")
    public ResponseEntity<Profile> addProfileImage(@PathVariable String username, @RequestPart("image") MultipartFile image) throws IOException {
        return profileService.addProfileImage(username, image);
    }

    @PutMapping("/{id}/image")
    public ResponseEntity<Profile> updateProfileImage(@PathVariable Long id,
                                                      @RequestPart("image") MultipartFile image) throws IOException {
        return profileService.updateProfileImage(id, image);
    }

    @GetMapping
    public ResponseEntity<List<Profile>> getAllProfiles() {
        return profileService.getAllProfiles();
    }

    @GetMapping("/by-username/{username}")
    public ResponseEntity<Profile> getProfileByUsername(@PathVariable String username) {
        return profileService.getProfileByUsername(username);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profile> getProfileById(@PathVariable Long id) {
        return profileService.getProfileById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Profile> updateProfile(@PathVariable Long id, @RequestBody Profile profile) {
        return profileService.updateProfile(id, profile);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        return profileService.deleteProfile(id);
    }



}
