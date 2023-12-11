package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Event("hearing.events.enrich-associated-hearings-with-indicated-plea")
@SuppressWarnings({"squid:S2384", "pmd:BeanMembersShouldSerialize", "pmd:BeanMembersShouldSerialize"})
public class EnrichAssociatedHearingsWithIndicatedPlea implements Serializable {

    private static final long serialVersionUID = 1L;

    private IndicatedPlea indicatedPlea;

    private List<UUID> hearingIds;

    @JsonCreator
    public EnrichAssociatedHearingsWithIndicatedPlea(@JsonProperty("hearingIds") final List<UUID> hearingIds,
                                                     @JsonProperty("indicatedPlea") final IndicatedPlea indicatedPlea) {
        this.indicatedPlea = indicatedPlea;
        this.hearingIds = new ArrayList<>(hearingIds);
    }

    public List<UUID> getHearingIds() {
        return hearingIds;
    }

    public IndicatedPlea getIndicatedPlea() {
        return indicatedPlea;
    }
}
