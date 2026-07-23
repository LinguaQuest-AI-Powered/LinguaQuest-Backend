package gov.jets.iti.LinguaQuest.controller;

import gov.jets.iti.LinguaQuest.dto.response.StartLevelResponse;
import gov.jets.iti.LinguaQuest.dto.response.SuccessResponse;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.enums.Role;
import gov.jets.iti.LinguaQuest.service.GameService;
import gov.jets.iti.LinguaQuest.service.WorldService;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorldControllerTest {

    @Mock
    private WorldService worldService;

    @Mock
    private GameService gameService;

    @InjectMocks
    private WorldController worldController;

    private User testUser;
    private UserPrinciple userPrinciple;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .role(Role.ROLE_USER)
                .build();

        userPrinciple = new UserPrinciple(testUser);
    }

    @Test
    @DisplayName("startLevel should delegate to gameService and return SuccessResponse with targetWord")
    void startLevel_Success() {
        // Arrange
        Long worldId = 10L;
        Long levelId = 145L;
        StartLevelResponse startLevelResponse = new StartLevelResponse("Manzana");

        when(gameService.startLevel(1L, worldId, levelId)).thenReturn(startLevelResponse);

        // Act
        ResponseEntity<SuccessResponse<StartLevelResponse>> responseEntity =
                worldController.startLevel(worldId, levelId, userPrinciple);

        // Assert
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().success()).isTrue();
        assertThat(responseEntity.getBody().data().targetWord()).isEqualTo("Manzana");

        verify(gameService).startLevel(1L, worldId, levelId);
    }

    @Test
    @DisplayName("changeWord should delegate to gameService and return SuccessResponse with new targetWord")
    void changeWord_Success() {
        // Arrange
        Long worldId = 10L;
        Long levelId = 145L;
        StartLevelResponse response = new StartLevelResponse("Platano");

        when(gameService.changeWord(1L, worldId, levelId)).thenReturn(response);

        // Act
        ResponseEntity<SuccessResponse<StartLevelResponse>> responseEntity =
                worldController.changeWord(worldId, levelId, userPrinciple);

        // Assert
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().success()).isTrue();
        assertThat(responseEntity.getBody().data().targetWord()).isEqualTo("Platano");

        verify(gameService).changeWord(1L, worldId, levelId);
    }
}
