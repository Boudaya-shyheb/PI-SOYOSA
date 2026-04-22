package soyosa.userservice.Config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import soyosa.userservice.Domain.User.Status;
import soyosa.userservice.Domain.User.TypeRole;
import soyosa.userservice.Domain.User.User;
import soyosa.userservice.Repository.UserRepo;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            // Create default admin if not exists
            String adminUsername = "admin@esprit.tn";
            if (userRepo.findByUsername(adminUsername).isEmpty()) {
                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(TypeRole.ADMIN);
                admin.setStatus(Status.ACTIVE);
                admin.setVerified(true);
                userRepo.save(admin);
                System.out.println("Default admin user created: " + adminUsername + " / admin123");
            }

            // Create default student if not exists
            String studentUsername = "student@esprit.tn";
            if (userRepo.findByUsername(studentUsername).isEmpty()) {
                User student = new User();
                student.setUsername(studentUsername);
                student.setPassword(passwordEncoder.encode("student123"));
                student.setRole(TypeRole.STUDENT);
                student.setStatus(Status.ACTIVE);
                student.setVerified(true);
                userRepo.save(student);
                System.out.println("Default student user created: " + studentUsername + " / student123");
            }
        };
    }
}
