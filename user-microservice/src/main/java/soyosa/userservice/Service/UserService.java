package soyosa.userservice.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import soyosa.userservice.Config.MailConfig.EmailService;
import soyosa.userservice.Config.jwt.JwtService;
import soyosa.userservice.Domain.Dto.AuthResponse;
import soyosa.userservice.Domain.Dto.PasswordResetToken;
import soyosa.userservice.Domain.Dto.ResetPasswordRequest;
import soyosa.userservice.Domain.Dto.VerifToken;
import soyosa.userservice.Domain.User.Status;
import soyosa.userservice.Domain.User.TypeRole;
import soyosa.userservice.Domain.User.User;
import soyosa.userservice.Repository.PasswordResetTokenRepo;
import soyosa.userservice.Repository.UserRepo;
import soyosa.userservice.Repository.VerifTokenRepo;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final EmailService mailService;
    private final VerifTokenRepo verifTokenRepo;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepo userRepo, AuthenticationManager authenticationManager, JwtService jwtService, UserDetailsService userDetailsService, EmailService mailService, VerifTokenRepo verifTokenRepo, PasswordResetTokenRepo passwordResetTokenRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.mailService = mailService;
        this.verifTokenRepo = verifTokenRepo;
        this.passwordResetTokenRepo = passwordResetTokenRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<User> registerUser(User user) {
        Optional<User> existing = userRepo.findByUsername(user.getUsername());
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        user.setVerified(false);
        switch (user.getRole()) {
            case STUDENT -> user.setStatus(Status.ACTIVE);
            case ADMIN -> user.setStatus(Status.ACTIVE);
            case TUTOR -> user.setStatus(Status.PENDING);
        }
        User saved = userRepo.save(user);

        String token = UUID.randomUUID().toString();

        VerifToken verifToken = new VerifToken();
        verifToken.setUser(saved);
        verifToken.setToken(token);
        verifToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));

        verifTokenRepo.save(verifToken);


        String url = "http://localhost:8070/api/user/verify?token=" + token;

        sendVerificationEmail(user.getUsername(),url);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public Boolean verifyUser(String token){
        VerifToken verificationToken =
                verifTokenRepo.findByToken(token)
                        .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (verificationToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        User user = verificationToken.getUser();
        user.setVerified(true);
        userRepo.save(user);

        verifTokenRepo.delete(verificationToken);
        return true;
    }

    public ResponseEntity<AuthResponse> authenticate(String username, String password) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Load real user from DB
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check verification
        if (!user.getVerified()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new AuthResponse("Please verify your email first"));
        }

        // Load UserDetails
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Add extra claims: userId and name so other services (e.g. training-service) can sync users
        Map<String, Object> extraClaims = new java.util.HashMap<>();
        extraClaims.put("userId", user.getUser_id());
        extraClaims.put("status", user.getStatus().name());
        String token = jwtService.generateToken(extraClaims, userDetails);

        return ResponseEntity.ok(new AuthResponse(token));
    }

    private void sendVerificationEmail(String email, String url) {
        try {
            mailService.sendVerificationEmail(email, url);
        }catch (Exception e) {}

    }


    public Boolean forgotPassword(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));

        passwordResetTokenRepo.save(resetToken);

        String link = "http://localhost:4200/user/reset-password?token=" + token;

        sendForgotPasswordMail(user.getUsername(), link);

        return true;
    }

    private void sendForgotPasswordMail(String email, String url) {
        try {
            mailService.forgotPasswordMail(email, url);
        }catch (Exception e) {}

    }


    public Boolean resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken =
                passwordResetTokenRepo.findByToken(request.getToken())
                        .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);

        passwordResetTokenRepo.delete(resetToken);

        return true;
    }

    public ResponseEntity<User> getUserById(String userId) {
        try {
            Long id = Long.parseLong(userId);
            Optional<User> user = userRepo.findById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    public ResponseEntity<User> updateUser(String userId, User userUpdate) {
        try {
            Long id = Long.parseLong(userId);
            Optional<User> existing = userRepo.findById(id);
            if (existing.isPresent()) {
                User user = existing.get();
                if (userUpdate.getUsername() != null) {
                    user.setUsername(userUpdate.getUsername());
                }
                if (userUpdate.getPassword() != null) {
                    user.setPassword(passwordEncoder.encode(userUpdate.getPassword()));
                }
                User updated = userRepo.save(user);
                return ResponseEntity.ok(updated);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    public ResponseEntity<String> deleteUser(String userId) {
        try {
            Long id = Long.parseLong(userId);
            Optional<User> user = userRepo.findById(id);
            if (user.isPresent()) {
                userRepo.deleteById(id);
                return ResponseEntity.ok("User deleted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    public Page<User> getAllUsersByRole(TypeRole role, String search, Pageable pageable) {
        return userRepo.findByRoleAndUsernameContainingIgnoreCase(role, search, pageable);
    }

}
