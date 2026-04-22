package soyosa.userservice.Controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import soyosa.userservice.Domain.Profile.Profile;
import soyosa.userservice.Domain.User.User;
import soyosa.userservice.Repository.UserRepo;
import soyosa.userservice.Service.ProfileService;

/**
 * Internal API for other services to fetch user information.
 * Used for inter-service communication (e.g., courses service fetching student names).
 */
@Tag(name = "User Info (Internal)")
@RestController
@RequestMapping("/api/internal/user")
public class UserInfoController {

    private final UserRepo userRepository;
    private final ProfileService profileService;

    public UserInfoController(UserRepo userRepository, ProfileService profileService) {
        this.userRepository = userRepository;
        this.profileService = profileService;
    }

    /**
     * Get user information by userId (username)
     * @param userId User ID (username or email)
     * @return User information DTO
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoDto> getUserInfo(@PathVariable("userId") String userId) {
        try {
            // Try to find user by username
            User user = userRepository.findByUsername(userId)
                    .orElseThrow(() -> new Exception("User not found"));

            // Try to get profile for additional info
            Profile profile = null;
            try {
                ResponseEntity<Profile> profileResp = profileService.getProfileByUsername(userId);
                if (profileResp.getStatusCode().is2xxSuccessful()) {
                    profile = profileResp.getBody();
                }
            } catch (Exception e) {
                // Profile not found, continue with just user info
            }

            UserInfoDto dto = new UserInfoDto();
            dto.setUserId(user.getUser_id().toString());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getUsername());

            if (profile != null) {
                dto.setFirstName(profile.getFirstName());
                dto.setLastName(profile.getLastName());
            } else {
                dto.setFirstName(user.getUsername());
                dto.setLastName("");
            }

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * DTO for user information response
     */
    public static class UserInfoDto {
        private String userId;
        private String username;
        private String email;
        private String firstName;
        private String lastName;

        public UserInfoDto() {
        }

        public UserInfoDto(String userId, String username, String email, String firstName, String lastName) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getDisplayName() {
            if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
                return firstName + " " + lastName;
            }
            if (firstName != null && !firstName.isEmpty()) {
                return firstName;
            }
            return username;
        }
    }
}
