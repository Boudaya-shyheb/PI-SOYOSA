package soyosa.userservice.Config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import soyosa.userservice.Config.jwt.JwtAuthenticationFilter;
import soyosa.userservice.Config.jwt.JwtService;
import soyosa.userservice.Domain.User.AuthProvider;
import soyosa.userservice.Domain.User.User;
import soyosa.userservice.Repository.UserRepo;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OAuth2SuccessHandler oAuth2SuccessHandler) throws Exception {
        http
            .csrf(csrf -> csrf.disable()
            )
            .authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
                .oauth2Login(oauth ->
                        oauth.successHandler(oAuth2SuccessHandler)
                )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4200")
                        .allowedMethods("*")
                        .allowedHeaders("*");
            }
        };
    }

    @Component
    public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

        private final UserRepo userRepository;
        private final JwtService jwtService;
        private final UserDetailsService userDetailsService;

        public OAuth2SuccessHandler(UserRepo userRepository, JwtService jwtService, UserDetailsService userDetailsService) {
            this.userRepository = userRepository;
            this.jwtService = jwtService;
            this.userDetailsService = userDetailsService;
        }

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request,
                                            HttpServletResponse response,
                                            Authentication authentication)
                throws IOException {

            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            String email = oAuth2User.getAttribute("email");
            String googleId = oAuth2User.getAttribute("sub");
            String name = oAuth2User.getAttribute("name");

            User user = userRepository.findByUsername(email)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setUsername(email);
                        newUser.setVerified(true); // Google already verified
                        newUser.setProvider(AuthProvider.GOOGLE);
                        newUser.setProviderId(googleId);
                        return userRepository.save(newUser);
                    });

            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String token = jwtService.generateToken(userDetails);

            String redirectUrl =
                    "http://localhost:4200/user/login?token=" + token;

            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        }
    }

}
