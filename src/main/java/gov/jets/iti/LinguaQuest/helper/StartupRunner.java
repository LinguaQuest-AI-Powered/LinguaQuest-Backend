package gov.jets.iti.LinguaQuest.helper;

import gov.jets.iti.LinguaQuest.service.DailyMissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupRunner implements ApplicationRunner {

    private final DailyMissionService dailyMissionService;

    @Override
    public void run(ApplicationArguments args) {
        dailyMissionService.generateTodayWord();
    }
}
