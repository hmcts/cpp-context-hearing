package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.joining;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.moj.cpp.external.domain.progression.prosecutioncases.LinkedApplicationsSummary;
import uk.gov.moj.cpp.external.domain.progression.prosecutioncases.ProsecutionCase;
import uk.gov.moj.cpp.hearing.xhibit.ProgressionCaseRetriever;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class ComplexTypeDataProcessor {
    @Inject
    private ProgressionCaseRetriever progressionCaseRetriever;

    public String getGetDefenceCouncilFullName(final DefenceCounsel defenceCounsel) {
        return Stream.of(defenceCounsel.getTitle(), defenceCounsel.getFirstName(),
                defenceCounsel.getMiddleName(), defenceCounsel.getLastName())
                .filter(value -> !StringUtils.isEmpty(value))
                .collect(joining(" "));
    }

    public Optional<String> getAppellantDisplayName(final List<UUID> linkedCaseIds) {
        for (final UUID caseId : linkedCaseIds) {
            final ProsecutionCase prosecutionCase = progressionCaseRetriever.getProsecutionCaseDetails(caseId);
            if (prosecutionCase != null && CollectionUtils.isNotEmpty(prosecutionCase.getLinkedApplicationsSummary())) {
                final Optional<LinkedApplicationsSummary> linkedApplicationsSummary =
                        prosecutionCase.getLinkedApplicationsSummary()
                                .stream()
                                .filter(las -> Boolean.TRUE.equals(las.getIsAppeal()) && CollectionUtils.isNotEmpty(las.getRespondentDisplayNames()))
                                .findFirst();
                if (linkedApplicationsSummary.isPresent()) {
                    return Optional.of(linkedApplicationsSummary.get().getApplicantDisplayName());
                }
            }
        }
        return empty();
    }
}
