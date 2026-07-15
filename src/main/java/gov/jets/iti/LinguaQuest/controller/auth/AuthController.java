package gov.jets.iti.LinguaQuest.controller.auth;


import gov.jets.iti.LinguaQuest.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/{version}/auth")
@RequiredArgsConstructor
public class AuthController {
    @PostMapping(value = "/login",version = "v1")
    public ResponseEntity<Void> login() {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
