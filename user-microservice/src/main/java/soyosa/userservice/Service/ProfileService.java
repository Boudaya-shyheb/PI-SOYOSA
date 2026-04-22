package soyosa.userservice.Service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import soyosa.userservice.Config.MailConfig.EmailService;
import soyosa.userservice.Domain.Profile.Profile;
import soyosa.userservice.Repository.ProfileRepo;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ProfileService {


    private final ProfileRepo profileRepo;
    private final ImageUploadService imageUploadService;
    private final EmailService mailService;

    public ProfileService(ProfileRepo profileRepo, ImageUploadService imageUploadService, EmailService mailService) {
        this.profileRepo = profileRepo;
        this.imageUploadService = imageUploadService;
        this.mailService = mailService;
    }

    public ResponseEntity<Profile> addProfileImage(String username, MultipartFile image) throws IOException {
        Profile profile = profileRepo.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Profile not found for user: " + username));

        String image_url = imageUploadService.uploadImage(image);
        profile.setImage(image_url);
        Profile updatedProfile = profileRepo.save(profile);
        return ResponseEntity.ok(updatedProfile);
    }

    public ResponseEntity<Profile> getProfileByUsername(String username){
        try{
            Optional<Profile> profile = profileRepo.findByUserUsername(username);
            if (profile.isPresent()){
                return ResponseEntity.ok(profile.get());
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            return ResponseEntity.status(500).build();
        }
    }

    public ResponseEntity<Profile> addProfile(Profile profile){

            Profile savedProfile = profileRepo.save(profile);
            return ResponseEntity.ok(savedProfile);

    }




    public ResponseEntity<Profile> updateProfileImage(Long profileId, MultipartFile image) throws IOException {
        try {
            Profile profile = profileRepo.findById(profileId)
                    .orElseThrow(() -> new RuntimeException("Profile not found"));

            String image_url = imageUploadService.uploadImage(image);
            profile.setImage(image_url);

            Profile updatedProfile = profileRepo.save(profile);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    public ResponseEntity<List<Profile>> getAllProfiles() {
        try {
            List<Profile> profiles = profileRepo.findAll();
            return ResponseEntity.ok(profiles);
        } catch (Exception e) {
            System.err.println("Error fetching all profiles: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    public ResponseEntity<Profile> getProfileById(Long id) {
        return profileRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<Profile> updateProfile(Long id, Profile profileDetails) {
        return profileRepo.findById(id)
                .map(profile -> {
                    profile.setFirstName(profileDetails.getFirstName());
                    profile.setLastName(profileDetails.getLastName());
                    profile.setPhoneNumber(profileDetails.getPhoneNumber());
                    profile.setAddress(profileDetails.getAddress());
                    profile.setMail(profileDetails.getMail());
                    profile.setLevel(profileDetails.getLevel());
                    // Image is usually updated via updateProfileImage, but we can update URL here too if provided
                    if (profileDetails.getImage() != null) {
                        profile.setImage(profileDetails.getImage());
                    }
                    Profile updatedProfile = profileRepo.save(profile);
                    return ResponseEntity.ok(updatedProfile);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<Void> deleteProfile(Long id) {
        if (profileRepo.existsById(id)) {
            profileRepo.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    

}
