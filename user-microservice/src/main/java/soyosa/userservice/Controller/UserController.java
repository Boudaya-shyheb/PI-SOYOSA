package soyosa.userservice.Controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import soyosa.userservice.Domain.Dto.AuthResponse;
import soyosa.userservice.Domain.Dto.PasswordResetToken;
import soyosa.userservice.Domain.Dto.ResetPasswordRequest;
import soyosa.userservice.Domain.Dto.VerifToken;
import soyosa.userservice.Domain.Profile.Profile;
import soyosa.userservice.Domain.User.TypeRole;
import soyosa.userservice.Domain.User.User;
import soyosa.userservice.Repository.UserRepo;
import soyosa.userservice.Repository.VerifTokenRepo;
import soyosa.userservice.Service.ProfileService;
import soyosa.userservice.Service.UserService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Gestion user")
@RestController
@RequestMapping("/user")
public class UserController {

    private final ProfileService profileService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;


    public UserController(ProfileService profileService, UserService userService, PasswordEncoder passwordEncoder) {
        this.profileService = profileService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody Profile profile) {
        User user= profile.getUser();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userService.registerUser(user).getBody();

        profile.setUser(savedUser);
        profileService.addProfile(profile);
        return ResponseEntity.ok(savedUser);

    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody User user) {
        return userService.authenticate(user.getUsername(), user.getPassword());
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String token) {

        if (userService.verifyUser(token)){
            return ResponseEntity.ok("Account verified successfully");
        }else {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }


    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {

        if (userService.forgotPassword(email)){
            return ResponseEntity.ok("FP mail sent successfully");
        }else {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }


    }

    @PostMapping("/reset-password")
    public ResponseEntity resetPassword(
            @RequestBody ResetPasswordRequest request) {

        if (userService.resetPassword(request)){
            return ResponseEntity.ok().build();
        }else {
            return ResponseEntity.badRequest().build();
        }

    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable String userId) {
        return userService.getUserById(userId);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(
            @PathVariable String userId,
            @RequestBody User userUpdate) {
        return userService.updateUser(userId, userUpdate);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        return userService.deleteUser(userId);
    }

    @GetMapping("/all/{role}")
    public ResponseEntity<Page<User>> getAllUsersByRole(
            @PathVariable TypeRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search) {
        return ResponseEntity.ok(userService.getAllUsersByRole(role, search, PageRequest.of(page, size)));
    }

}
