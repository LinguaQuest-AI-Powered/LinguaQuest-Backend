package gov.jets.iti.LinguaQuest.security;

import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.exception.auth.EmailNotFoundException;
import gov.jets.iti.LinguaQuest.repository.UserRepository;
import gov.jets.iti.LinguaQuest.service.UserService;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    final private UserRepository userRepository;
    final private PasswordEncoder passwordEncoder;
    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException("Invalid Email or Password"));
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(user.getRole().name()));
        if(passwordEncoder.matches(password,user.getPassword())){
            UserPrinciple userPrinciple = new UserPrinciple(user);
            return new UsernamePasswordAuthenticationToken(userPrinciple,null,authorities);
        }else {
            throw new BadCredentialsException("Invalid Credentials!");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
