package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Collections.unmodifiableList;
import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.core.courts.Prompt;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;

public class UUIDComparator implements Comparator<Prompt> {

    static final String PROMPT_MISSING_FROM_REFERENCE_DATA = "Prompt missing from reference data";
    private static final Logger LOGGER = getLogger(UUIDComparator.class);
    private final List<UUID> referenceList;

    public UUIDComparator(final List<UUID> referenceList) {
        this.referenceList = unmodifiableList(referenceList);
    }

    @Override
    public int compare(final Prompt left, final Prompt right) {

        final Integer indexOf1 = referenceList.indexOf(left.getId());

        final Integer indexOf2 = referenceList.indexOf(right.getId());

        if (indexOf1 == -1 || indexOf2 == -1) {
            LOGGER.error(PROMPT_MISSING_FROM_REFERENCE_DATA);
            return 0;
        }

        return indexOf1.compareTo(indexOf2);
    }
}
