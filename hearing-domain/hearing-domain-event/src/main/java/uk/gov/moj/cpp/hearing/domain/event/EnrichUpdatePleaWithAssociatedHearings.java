package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.json.schemas.core.Plea;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Event("hearing.events.enrich-update-plea-with-associated-hearings")
public class EnrichUpdatePleaWithAssociatedHearings implements Serializable {

    private static final long serialVersionUID = 1L;

    private Plea plea;

    private List<UUID> hearingIds;

    @JsonCreator
    public EnrichUpdatePleaWithAssociatedHearings(@JsonProperty("hearingIds") final List<UUID> hearingIds,
                                                  @JsonProperty("plea") final Plea plea) {
        this.plea = plea;
        this.hearingIds = new ArrayList<>(hearingIds);
    }

    public List<UUID> getHearingIds() {
        return hearingIds;
    }

    public Plea getPlea() {
        return plea;
    }
}