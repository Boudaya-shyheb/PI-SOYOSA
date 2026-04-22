package soyosa.userservice.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/test")
@RestController
public class TestController {

    @GetMapping("/test-success")
    public ResponseEntity<String> success(@RequestParam String token) {
        return ResponseEntity.ok("JWT Generated: " + token);
    }

}