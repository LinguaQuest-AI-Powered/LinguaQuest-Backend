package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.request.LoginRequestDto;
import gov.jets.iti.LinguaQuest.dto.response.AuthResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.UserDto;
import gov.jets.iti.LinguaQuest.util.ApplicationConstants;
import gov.jets.iti.LinguaQuest.util.JwtUtil;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    final private AuthenticationManager authenticationManager;
    final private JwtUtil jwtUtil;

    public AuthResponseDto login(LoginRequestDto loginRequestDto) {

        var resultAuth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDto.email(),loginRequestDto.password()));

        UserPrinciple userPrinciple = (UserPrinciple) resultAuth.getPrincipal();
        String jwtToken = jwtUtil.generateToken(userPrinciple);
        UserDto userDto = mapUserPrincipleToUserDto(userPrinciple);
        return new AuthResponseDto(jwtToken,"","Barear", 86400000L,userDto);

    }

    private UserDto mapUserPrincipleToUserDto(UserPrinciple userPrinciple) {
        return new UserDto(userPrinciple.user().getId(), userPrinciple.user().getUsername(),userPrinciple.user().getPhoto()
        ,userPrinciple.user().getNativeLanguage(),userPrinciple.user().getIsVerified(),userPrinciple.user().getTargetLanguages());
    }

}
