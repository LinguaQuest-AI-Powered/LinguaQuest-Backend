package gov.jets.iti.LinguaQuest.controller;


import gov.jets.iti.LinguaQuest.dto.response.WorldsResponseDto;
import gov.jets.iti.LinguaQuest.enums.Difficulty;
import gov.jets.iti.LinguaQuest.service.WorldService;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/{version}/worlds")
@RequiredArgsConstructor
public class WorldController {

    final private WorldService worldService;
    @GetMapping(version = "v1")
    ResponseEntity<WorldsResponseDto> getAllUserWorlds(@AuthenticationPrincipal UserPrinciple userPrinciple,
                                                       @RequestParam("languageId") Long languageId, @RequestParam("difficulty") Difficulty difficulty){
        WorldsResponseDto worldsResponseDto =  worldService.getAllWorlds(userPrinciple.user().getId(), languageId,difficulty);
        return ResponseEntity.ok(worldsResponseDto);
    }
}
