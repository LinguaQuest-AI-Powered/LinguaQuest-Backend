package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.world.LevelDto;
import gov.jets.iti.LinguaQuest.dto.world.StartLevelResponse;
import gov.jets.iti.LinguaQuest.dto.world.WorldLevelsResponseDto;
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
import gov.jets.iti.LinguaQuest.exception.world.InsufficientCoinsException;
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
            when(wordRepository.countUnusedWordsByDifficulty(userId, worldId, languageId, Difficulty.EASY))
                    .thenReturn(1L);
            when(wordRepository.findUnusedWordsByDifficulty(eq(userId), eq(worldId), eq(languageId), eq(Difficulty.EASY), any(Pageable.class)))
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

        @Test
        @DisplayName("Should pick MEDIUM difficulty word for second-third level in world")
        void startLevel_ProgressiveDifficulty_Medium() {
            // Arrange (3 total levels, level 2 is MEDIUM third)
            WorldLevelsResponseDto worldLevelsDto = new WorldLevelsResponseDto(
                    worldId, "Kitchen World", Difficulty.EASY,
                    List.of(
                            new LevelDto(101L, 1, LevelStatus.COMPLETED, "Word1"),
                            new LevelDto(levelId, 2, LevelStatus.AVAILABLE, null),
                            new LevelDto(103L, 3, LevelStatus.LOCKED, null)
                    ));

            Word mediumWord = Word.builder().id(55L).language(testLanguage).text("Cuchillo").difficulty(Difficulty.MEDIUM).build();

            WorldLevel mediumWorldLevel = WorldLevel.builder().id(levelId).world(testWorld).orderIndex(2).build();

            when(userLanguageRepository.findActiveByUserIdWithLanguage(userId)).thenReturn(Optional.of(activeUserLanguage));
            when(worldRepository.findById(worldId)).thenReturn(Optional.of(testWorld));
            when(worldLevelRepository.findByIdAndWorldId(levelId, worldId)).thenReturn(Optional.of(mediumWorldLevel));
            when(userLevelProgressRepository.findByUserIdAndLevelIdAndLanguageId(userId, levelId, languageId)).thenReturn(Optional.empty());
            when(worldService.getWorldLevels(userId, worldId)).thenReturn(worldLevelsDto);
            when(wordRepository.countUnusedWords(userId, worldId, languageId)).thenReturn(1L);
            when(wordRepository.countUnusedWordsByDifficulty(userId, worldId, languageId, Difficulty.MEDIUM)).thenReturn(1L);
            when(wordRepository.findUnusedWordsByDifficulty(eq(userId), eq(worldId), eq(languageId), eq(Difficulty.MEDIUM), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(mediumWord)));

            // Act
            StartLevelResponse response = gameService.startLevel(userId, worldId, levelId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.targetWord()).isEqualTo("Cuchillo");
        }

        @Test
        @DisplayName("Should fall back to any unused word if no unused word matches target difficulty")
        void startLevel_DifficultyFallback() {
            // Arrange
            WorldLevelsResponseDto worldLevelsDto = new WorldLevelsResponseDto(
                    worldId, "Kitchen World", Difficulty.EASY,
                    List.of(new LevelDto(levelId, 1, LevelStatus.AVAILABLE, null)));

            when(userLanguageRepository.findActiveByUserIdWithLanguage(userId)).thenReturn(Optional.of(activeUserLanguage));
            when(worldRepository.findById(worldId)).thenReturn(Optional.of(testWorld));
            when(worldLevelRepository.findByIdAndWorldId(levelId, worldId)).thenReturn(Optional.of(testWorldLevel));
            when(userLevelProgressRepository.findByUserIdAndLevelIdAndLanguageId(userId, levelId, languageId)).thenReturn(Optional.empty());
            when(worldService.getWorldLevels(userId, worldId)).thenReturn(worldLevelsDto);
            when(wordRepository.countUnusedWords(userId, worldId, languageId)).thenReturn(1L);
            // 0 unused words matching EASY difficulty -> triggers fallback
            when(wordRepository.countUnusedWordsByDifficulty(userId, worldId, languageId, Difficulty.EASY)).thenReturn(0L);
            when(wordRepository.findUnusedWords(eq(userId), eq(worldId), eq(languageId), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(testWord)));

            // Act
            StartLevelResponse response = gameService.startLevel(userId, worldId, levelId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.targetWord()).isEqualTo("Manzana");
        }
    }

    @Nested
    @DisplayName("changeWord tests")
    class ChangeWordTests {

        @Test
        @DisplayName("Should successfully change the current word for an INPROGRESS level")
        void changeWord_Success() {
            // Arrange
            testUser.setCoins(100);
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
            when(worldLevelRepository.countWorldLevelByWorld(testWorld))
                    .thenReturn(1L);
            when(wordRepository.countUnusedWordsExcludingCurrentByDifficulty(userId, worldId, languageId, Difficulty.EASY, testWord.getId()))
                    .thenReturn(1L);
            when(wordRepository.findUnusedWordsExcludingCurrentByDifficulty(eq(userId), eq(worldId), eq(languageId), eq(Difficulty.EASY), eq(testWord.getId()), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(newWord)));

            // Act
            StartLevelResponse response = gameService.changeWord(userId, worldId, levelId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.targetWord()).isEqualTo("Platano");
            assertThat(response.coins()).isEqualTo(50);
            assertThat(existingProgress.getWord()).isEqualTo(newWord);
            assertThat(testUser.getCoins()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should throw InsufficientCoinsException when user coins are less than 50")
        void changeWord_InsufficientCoins_ThrowsException() {
            // Arrange
            testUser.setCoins(10);
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

            // Act & Assert
            assertThatThrownBy(() -> gameService.changeWord(userId, worldId, levelId))
                    .isInstanceOf(InsufficientCoinsException.class)
                    .hasMessageContaining("Insufficient coins");
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
            testUser.setCoins(100);
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

        @Test
        @DisplayName("Should fall back to any unused word if no word matches target difficulty on change")
        void changeWord_DifficultyFallback() {
            // Arrange
            testUser.setCoins(100);
            UserLevelProgress existingProgress = UserLevelProgress.builder()
                    .id(200L)
                    .user(testUser)
                    .worldLevel(testWorldLevel)
                    .status(LevelStatus.INPROGRESS)
                    .word(testWord)
                    .build();

            Word fallbackWord = Word.builder()
                    .id(60L)
                    .language(testLanguage)
                    .text("Tenedor")
                    .difficulty(Difficulty.MEDIUM)
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
            when(worldLevelRepository.countWorldLevelByWorld(testWorld))
                    .thenReturn(1L);
            // 0 words matching EASY difficulty -> triggers fallback
            when(wordRepository.countUnusedWordsExcludingCurrentByDifficulty(userId, worldId, languageId, Difficulty.EASY, testWord.getId()))
                    .thenReturn(0L);
            when(wordRepository.findUnusedWordsExcludingCurrent(eq(userId), eq(worldId), eq(languageId), eq(testWord.getId()), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(fallbackWord)));

            // Act
            StartLevelResponse response = gameService.changeWord(userId, worldId, levelId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.targetWord()).isEqualTo("Tenedor");
            assertThat(response.coins()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should throw NoActiveLanguageException when user has no active language")
        void changeWord_NoActiveLanguage_ThrowsException() {
            // Arrange
            when(userLanguageRepository.findActiveByUserIdWithLanguage(userId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> gameService.changeWord(userId, worldId, levelId))
                    .isInstanceOf(NoActiveLanguageException.class)
                    .hasMessageContaining("doesn't have an active language");

            verify(worldRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw WorldNotFoundException when world does not exist")
        void changeWord_WorldNotFound_ThrowsException() {
            // Arrange
            when(userLanguageRepository.findActiveByUserIdWithLanguage(userId))
                    .thenReturn(Optional.of(activeUserLanguage));
            when(worldRepository.findById(worldId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> gameService.changeWord(userId, worldId, levelId))
                    .isInstanceOf(WorldNotFoundException.class)
                    .hasMessageContaining("does not exist");

            verify(worldLevelRepository, never()).findByIdAndWorldId(any(), any());
        }

        @Test
        @DisplayName("Should throw LevelNotFoundException when level does not exist in world")
        void changeWord_LevelNotFound_ThrowsException() {
            // Arrange
            when(userLanguageRepository.findActiveByUserIdWithLanguage(userId))
                    .thenReturn(Optional.of(activeUserLanguage));
            when(worldRepository.findById(worldId))
                    .thenReturn(Optional.of(testWorld));
            when(worldLevelRepository.findByIdAndWorldId(levelId, worldId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> gameService.changeWord(userId, worldId, levelId))
                    .isInstanceOf(LevelNotFoundException.class)
                    .hasMessageContaining("does not exist in world");

            verify(userLevelProgressRepository, never()).findByUserIdAndLevelIdAndLanguageId(any(), any(), any());
        }
    }
}
