package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.entity.Translation;
import gov.jets.iti.LinguaQuest.enums.TranslatableEntityType;
import gov.jets.iti.LinguaQuest.repository.TranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TranslationResolverService {

    private final TranslationRepository translationRepository;

    public Map<Long, Map<String, String>> resolveBatch(TranslatableEntityType entityType, Collection<Long> entityIds,
            Long userLanguageId) {

        if (entityIds.isEmpty()) return Map.of();

        List<Translation> rows = translationRepository.findByEntityTypeAndEntityIdInAndLanguageId(
                entityType, entityIds, userLanguageId
        );

        return rows.stream().collect(Collectors.groupingBy(
                Translation::getEntityId,
                Collectors.toMap(Translation::getFieldName, Translation::getValue)
        ));
    }
}