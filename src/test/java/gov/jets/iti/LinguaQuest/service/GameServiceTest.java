package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.response.LevelDto;
import gov.jets.iti.LinguaQuest.dto.response.StartLevelResponse;
import gov.jets.iti.LinguaQuest.dto.response.WorldLevelsResponseDto;
import gov.jets.iti.LinguaQuest.entity.Language;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.entity.UserLanguage;
import gov.jets.iti.LinguaQuest.entity.UserLevelProgress;
import gov.jets.iti.LinguaQuest.entity.Word;
import gov.jets.iti.LinguaQuest.entity.World;
import gov.jets.iti.LinguaQuest.entity.WorldLevel;
import gov.jets.iti.LinguaQuest.enums.Difficulty;
import gov.jets.iti.LinguaQuest.enums.LevelStatus;
import gov.jets.iti.LinguaQuest.exception.language.NoActiveLanguageException;
import gov.jets.iti.LinguaQuest.exception.world.LevelAlreadyCompletedException;
import gov.jets.iti.LinguaQuest.exception.world.LevelLockedException;
import gov.jets.iti.LinguaQuest.exception.world.LevelNotFoundException;
import gov.jets.iti.LinguaQuest.exception.world.NoMoreWordsException;
import gov.jets.iti.LinguaQuest.exception.world.ProgressNotFoundException;
import gov.jets.iti.LinguaQuest.exception.world.WorldCompletedException;
import gov.jets.iti.LinguaQuest.exception.world.WorldNotFoundException;
import gov.jets.iti.LinguaQuest.repository.UserLanguageRepository;
import gov.jets.iti.LinguaQuest.repository.UserLevelProgressRepository;
import gov.jets.iti.LinguaQuest.repository.WordRepository;
import gov.jets.iti.LinguaQuest.repository.WorldLevelRepository;
import gov.jets.iti.LinguaQuest.repository.WorldRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private UserLevelProgressRepository userLevelProgressRepository;

    @Mock
    private UserLanguageRepository userLanguageRepository;

    @Mock
    private WorldRepository worldRepository;

    @Mock
    private WorldLevelRepository worldLevelRepository;

    @Mock
    private WordRepository wordRepository;

    @Mock
    private WorldService worldService;

    @Mock
    private AIService aiService;

    @InjectMocks
    private GameService gameService;

    private Long userId;
    private Long worldId;
    private Long levelId;
    private Long languageId;

    private User testUser;
    private Language testLanguage;
    private UserLanguage activeUserLanguage;
    private World testWorld;
    private WorldLevel testWorldLevel;
    private Word testWord;

    @BeforeEach
    void setUp() {
        userId = 1L;
        worldId = 10L;
        levelId = 145L;
        languageId = 2L;

        testUser = User.builder()
                .id(userId)
                .email("user@example.com")
                .username("testuser")
                .build();

        testLanguage = new Language(languageId, "Spanish", "es", "/media/es.png");

        activeUserLanguage = UserLanguage.builder()
                .id(100L)
                .user(testUser)
                .language(testLanguage)
                .isActive(true)
                .build();

        testWorld = World.builder()
                .id(worldId)
                .name("Kitchen World")
                .build();

        testWorldLevel = WorldLevel.builder()
                .id(levelId)
                .world(testWorld)
                .orderIndex(1)
                .build();

        testWord = Word.builder()
                .id(50L)
                .language(testLanguage)
                .text("Manzana")
                .build();
    }

    @Nested
    @DisplayName("startLevel tests")
    class StartLevelTests {

        @Test
        @DisplayName("Should successfully start an AVAILABLE level and assign a random unused word")
        void startLevel_Success() {
            // Arrange
            WorldLevelsResponseDto worldLevelsDto = new WorldLevelsResponseDto(
                    worldId, "Kitchen World", Difficulty.EASY,
                    List.of(new LevelDto(levelId, 1, LevelStatus.AVAILABLE, null)));

            when(userLanguageRepository.findActiveByUserIdWithLanguage(userId))
                    .thenReturn(Optional.of(activeUserLanguage));
            when(worldRepository.findById(worldId))
                    .thenReturn(Optional.of(testWorld));
            when(worldLevelRepository.findByIdAndWorldId(levelId, worldId))
                    .thenReturn(Optional.of(testWorldLevel));
            when(userLevelProgressRepository.findByUserIdAndLevelIdAndLanguageId(userId, levelId, languageId))
                    .thenReturn(Optional.empty());
            when(worldService.getWorldLevels(userId, worldId))
                    .thenReturn(worldLevelsDto);
            when(wordRepository.countUnusedWords(userId, worldId, languageId))
                    .thenReturn(1L);
            when(wordRepository.findUnusedWords(eq(userId), eq(worldId), eq(languageId), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(testWord)));

            // Act
            StartLevelResponse response = gameService.startLevel(userId, worldId, levelId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.targetWord()).isEqualTo("Manzana");

            ArgumentCaptor<UserLevelProgress> progressCaptor = ArgumentCaptor.forClass(UserLevelProgress.class);
            verify(userLevelProgressRepository).save(progressCaptor.capture());

            UserLevelProgress savedProgress = progressCaptor.getValue();
            assertThat(savedProgress.getUser()).isEqualTo(testUser);
            assertThat(savedProgress.getWorldLevel()).isEqualTo(testWorldLevel);
            assertThat(savedProgress.getStatus()).isEqualTo(LevelStatus.INPROGRESS);
            assertThat(savedProgress.getWord()).isEqualTo(testWord);
        }

        @Test
        @DisplayName("Should return existing word idempotently if level is already INPROGRESS or COMPLETED")
        void startLevel_AlreadyExists_ReturnsExistingWord() {
            // Arrange
            UserLevelProgress existingProgress = UserLevelProgress.builder()
                    .id(200L)
                    .user(testUser)
                    .worldLevel(testWorldLevel)
                    .status(LevelStatus.INPROGRESS)
                    .word(testWord)
                    .build();

            when(userLanguageRepository.findActiveByUserIdWithLanguage(userId))
                    .thenReturn(Optional.of(activeUserLanguage));
            when(worldRepository.findById(worldId))
                    .thenReturn(Optional.of(testWorld));
            when(worldLevelRepository.findByIdAndWorldId(levelId, worldId))
                    .thenReturn(Optional.of(testWorldLevel));
            when(userLevelProgressRepository.findByUserIdAndLevelIdAndLanguageId(userId, levelId, languageId))
                    .thenReturn(Optional.of(existingProgress));

            // Act
            StartLevelResponse response = gameService.startLevel(userId, worldId, levelId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.targetWord()).isEqualTo("Manzana");

            verify(worldService, never()).getWorldLevels(any(), any());
            verify(wordRepository, never()).countUnusedWords(any(), any(), any());
            verify(userLevelProgressRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NoActiveLanguageException when user has no active language")
        void startLevel_NoActiveLanguage_ThrowsException() {
            // Arrange
            when(userLanguageRepository.findActiveByUserIdWithLanguage(userId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> gameService.startLevel(userId, worldId, levelId))
                    .isInstanceOf(NoActiveLanguageException.class)
                    .hasMessageContaining("doesn't have an active language");

            verify(worldRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw WorldNotFoundException when world does not exist")
        void startLevel_WorldNotFound_ThrowsException() {
            // Arrange
            when(userLanguageRepository.findActiveByUserIdWithLanguage(userId))
                    .thenReturn(Optional.of(activeUserLanguage));
            when(worldRepository.findById(worldId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> gameService.startLevel(userId, worldId, levelId))
                    .isInstanceOf(WorldNotFoundException.class)
                    .hasMessageContaining("does not exist");

            verify(worldLevelRepository, never()).findByIdAndWorldId(any(), any());
        }

        @Test
        @DisplayName("Should throw LevelNotFoundException when level does not exist in world")
        void startLevel_LevelNotFound_ThrowsException() {
            // Arrange
            when(userLanguageRepository.findActiveByUserIdWithLanguage(userId))
                    .thenReturn(Optional.of(activeUserLanguage));
            when(worldRepository.findById(worldId))
                    .thenReturn(Optional.of(testWorld));
            when(worldLevelRepository.findByIdAndWorldId(levelId, worldId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> gameService.startLevel(userId, worldId, levelId))
                    .isInstanceOf(LevelNotFoundException.class)
                    .hasMessageContaining("does not exist in world");
        }

        @Test
        @DisplayName("Should throw LevelLockedException when level status is LOCKED")
        void startLevel_LevelLocked_ThrowsException() {
            // Arrange
            WorldLevelsResponseDto worldLevelsDto = new WorldLevelsResponseDto(
                    worldId, "Kitchen World", Difficulty.EASY,
                    List.of(new LevelDto(levelId, 2, LevelStatus.LOCKED, null)));

            when(userLanguageRepository.findActiveByUserIdWithLanguage(userId))
                    .thenReturn(Optional.of(activeUserLanguage));
            when(worldRepository.findById(worldId))
                    .thenReturn(Optional.of(testWorld));
            when(worldLevelRepository.findByIdAndWorldId(levelId, worldId))
                    .thenReturn(Optional.of(testWorldLevel));
            when(userLevelProgressRepository.findByUserIdAndLevelIdAndLanguageId(userId, levelId, languageId))
                    .thenReturn(Optional.empty());
            when(worldService.getWorldLevels(userId, worldId))
                    .thenReturn(worldLevelsDto);

            // Act & Assert
            assertThatThrownBy(() -> gameService.startLevel(userId, worldId, levelId))
                    .isInstanceOf(LevelLockedException.class)
                    .hasMessageContaining("level is locked");

            verify(wordRepository, never()).countUnusedWords(any(), any(), any());
            verify(userLevelProgressRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw WorldCompletedException when no unused words are left in the world")
        void startLevel_NoUnusedWords_ThrowsException() {
            // Arrange
            WorldLevelsResponseDto worldLevelsDto = new WorldLevelsResponseDto(
                    worldId, "Kitchen World", Difficulty.EASY,
                    List.of(new LevelDto(levelId, 1, LevelStatus.AVAILABLE, null)));

            when(userLanguageRepository.findActiveByUserIdWithLanguage(userId))
                    .thenReturn(Optional.of(activeUserLanguage));
            when(worldRepository.findById(worldId))
                    .thenReturn(Optional.of(testWorld));
            when(worldLevelRepository.findByIdAndWorldId(levelId, worldId))
                    .thenReturn(Optional.of(testWorldLevel));
            when(userLevelProgressRepository.findByUserIdAndLevelIdAndLanguageId(userId, levelId, languageId))
                    .thenReturn(Optional.empty());
            when(worldService.getWorldLevels(userId, worldId))
                    .thenReturn(worldLevelsDto);
            when(wordRepository.countUnusedWords(userId, worldId, languageId))
                    .thenReturn(0L);

            // Act & Assert
            assertThatThrownBy(() -> gameService.startLevel(userId, worldId, levelId))
                    .isInstanceOf(WorldCompletedException.class)
                    .hasMessageContaining("learned all words in this world");

            verify(userLevelProgressRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("changeWord tests")
    class ChangeWordTests {

        @Test
        @DisplayName("Should successfully change the current word for an INPROGRESS level")
        void changeWord_Success() {
            // Arrange
            UserLevelProgress existingProgress = UserLevelProgress.builder()
                    .id(200L)
                    .user(testUser)
                    .worldLevel(testWorldLevel)
                    .status(LevelStatus.INPROGRESS)
                    .word(testWord)
                    .build();

            Word newWord = Word.builder()
                    .id(51L)
                    .language(testLanguage)
                    .text("Platano")
                    .build();

            when(userLanguageRepository.findActiveByUserIdWithLanguage(userId))
                    .thenReturn(Optional.of(activeUserLanguage));
            when(worldRepository.findById(worldId))
                    .thenReturn(Optional.of(testWorld));
            when(worldLevelRepository.findByIdAndWorldId(levelId, worldId))
                    .thenReturn(Optional.of(testWorldLevel));
            when(userLevelProgressRepository.findByUserIdAndLevelIdAndLanguageId(userId, levelId, languageId))
                    .thenReturn(Optional.of(existingProgress));
            when(wordRepository.countUnusedWordsExcludingCurrent(userId, worldId, languageId, testWord.getId()))
                    .thenReturn(1L);
            when(wordRepository.findUnusedWordsExcludingCurrent(eq(userId), eq(worldId), eq(languageId), eq(testWord.getId()), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(newWord)));

            // Act
            StartLevelResponse response = gameService.changeWord(userId, worldId, levelId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.targetWord()).isEqualTo("Platano");
            assertThat(existingProgress.getWord()).isEqualTo(newWord);
        }

        @Test
        @DisplayName("Should throw ProgressNotFoundException when level has not been started")
        void changeWord_ProgressNotFound_ThrowsException() {
            // Arrange
            when(userLanguageRepository.findActiveByUserIdWithLanguage(userId))
                    .thenReturn(Optional.of(activeUserLanguage));
            when(worldRepository.findById(worldId))
                    .thenReturn(Optional.of(testWorld));
            when(worldLevelRepository.findByIdAndWorldId(levelId, worldId))
                    .thenReturn(Optional.of(testWorldLevel));
            when(userLevelProgressRepository.findByUserIdAndLevelIdAndLanguageId(userId, levelId, languageId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> gameService.changeWord(userId, worldId, levelId))
                    .isInstanceOf(ProgressNotFoundException.class)
                    .hasMessageContaining("haven't started this level yet");
        }

        @Test
        @DisplayName("Should throw LevelAlreadyCompletedException when level status is COMPLETED")
        void changeWord_CompletedLevel_ThrowsLevelAlreadyCompletedException() {
            // Arrange
            UserLevelProgress completedProgress = UserLevelProgress.builder()
                    .id(200L)
                    .user(testUser)
                    .worldLevel(testWorldLevel)
                    .status(LevelStatus.COMPLETED)
                    .word(testWord)
                    .build();

            when(userLanguageRepository.findActiveByUserIdWithLanguage(userId))
                    .thenReturn(Optional.of(activeUserLanguage));
            when(worldRepository.findById(worldId))
                    .thenReturn(Optional.of(testWorld));
            when(worldLevelRepository.findByIdAndWorldId(levelId, worldId))
                    .thenReturn(Optional.of(testWorldLevel));
            when(userLevelProgressRepository.findByUserIdAndLevelIdAndLanguageId(userId, levelId, languageId))
                    .thenReturn(Optional.of(completedProgress));

            // Act & Assert
            assertThatThrownBy(() -> gameService.changeWord(userId, worldId, levelId))
                    .isInstanceOf(LevelAlreadyCompletedException.class)
                    .hasMessageContaining("Level is already completed");

            verify(wordRepository, never()).countUnusedWordsExcludingCurrent(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should throw NoMoreWordsException when no other unused words remain")
        void changeWord_NoMoreWords_ThrowsException() {
            // Arrange
            UserLevelProgress existingProgress = UserLevelProgress.builder()
                    .id(200L)
                    .user(testUser)
                    .worldLevel(testWorldLevel)
                    .status(LevelStatus.INPROGRESS)
                    .word(testWord)
                    .build();

            when(userLanguageRepository.findActiveByUserIdWithLanguage(userId))
                    .thenReturn(Optional.of(activeUserLanguage));
            when(worldRepository.findById(worldId))
                    .thenReturn(Optional.of(testWorld));
            when(worldLevelRepository.findByIdAndWorldId(levelId, worldId))
                    .thenReturn(Optional.of(testWorldLevel));
            when(userLevelProgressRepository.findByUserIdAndLevelIdAndLanguageId(userId, levelId, languageId))
                    .thenReturn(Optional.of(existingProgress));
            when(wordRepository.countUnusedWordsExcludingCurrent(userId, worldId, languageId, testWord.getId()))
                    .thenReturn(0L);

            // Act & Assert
            assertThatThrownBy(() -> gameService.changeWord(userId, worldId, levelId))
                    .isInstanceOf(NoMoreWordsException.class)
                    .hasMessageContaining("no other new words available");
        }
    }
}
