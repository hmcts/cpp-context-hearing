package uk.gov.moj.cpp.hearing.event.nows;

import static java.util.stream.Collectors.toSet;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.ResultLineReference;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateVariantDecisionMaker {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateVariantDecisionMaker.class.getName());

    private List<Variant> variantDirectory;
    private Map<UUID, CompletedResultLineStatus> completedResultLineStatuses;
    private List<ResultLine> completedResultLines;

    public GenerateVariantDecisionMaker(List<Variant> variantDirectory,
                                        Map<UUID, CompletedResultLineStatus> completedResultLineStatuses,
                                        List<ResultLine> completedResultLines) {

        this.variantDirectory = new ArrayList<>(variantDirectory);
        this.completedResultLineStatuses = completedResultLineStatuses;
        this.completedResultLines = new ArrayList<>(completedResultLines);
    }

    public Decision decide(List<String> userGroups) {

        final Set<String> ug = new HashSet<>(userGroups);

        final Variant variantDirectoryEntry = variantDirectory.stream()
                .filter(v -> new HashSet<>(v.getKey().getUsergroups()).equals(ug))
                .findFirst()
                .orElse(null);

        if (variantDirectoryEntry != null) {
            final Set<ResultLineReference> oldResultLinesReferences = new HashSet<>(variantDirectoryEntry.getValue().getResultLines());

            final Set<ResultLineReference> newResultLinesReferences = completedResultLines.stream()
                    .map(l -> ResultLineReference.resultLineReference()
                            .setResultLineId(l.getResultLineId())
                            .setLastSharedTime(Optional.ofNullable(completedResultLineStatuses.get(l.getResultLineId()))
                                    .map(CompletedResultLineStatus::getLastSharedDateTime)
                                    .orElse(null)
                            ))
                    .collect(toSet());

            if (newResultLinesReferences.equals(oldResultLinesReferences)) {
                LOGGER.info("NOW variant is skipped for usergroups {} for no new data", userGroups);
                return new Decision(false, false);
            } else {
                LOGGER.info("NOW variant is generated for usergroups {} as an amendment", userGroups);
                return new Decision(true, true);
            }
        }
        LOGGER.info("NOW variant is generated for the first time for usergroups {}", userGroups);
        return new Decision(true, false);
    }


    public static class Decision {
        private boolean shouldGenerate;
        private boolean amended;

        Decision(boolean shouldGenerate, boolean amended) {
            this.shouldGenerate = shouldGenerate;
            this.amended = amended;
        }

        public boolean isShouldGenerate() {
            return shouldGenerate;
        }

        public boolean isAmended() {
            return amended;
        }
    }
}
