package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.domain.event.DefenceWitnessAdded;
import uk.gov.moj.cpp.hearing.domain.event.InitiateHearingDefenceWitnessEnriched;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonObject;
@SuppressWarnings({"squid:S00107","squid:S1948"})
public class DefendantAggregate implements Aggregate {

    private static final long serialVersionUID = 1L;
    
    private final List<DefenceWitnessAdded> defenceWitnessAdded = new ArrayList<>();

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                        when(DefenceWitnessAdded.class)
                                        .apply((witnessAdded) -> this.defenceWitnessAdded
                                                        .add(witnessAdded)),
                otherwiseDoNothing()
        );
    }

    public List<DefenceWitnessAdded> getWitness() {
        return defenceWitnessAdded;
    }

    public Stream<Object> addWitness(final UUID witnessId, final UUID hearingId, final UUID defendantId, final String type, final String classification, final String title, final String firstName, final String lastName) {
        final List<UUID> defendantIds = new ArrayList<>();
        defendantIds.add(defendantId);
        return apply(Stream.of(new DefenceWitnessAdded(witnessId, defendantId, hearingId, type, classification, title, firstName, lastName)));
    }


    public Stream<Object> initiateHearingDefenceWitness(final JsonObject payload) {

        final String hearingId = payload.getString("hearingId");
        final String defendantId = payload.getString("defendantId");
        final Stream.Builder<Object> streamBuilder = Stream.builder();
        defenceWitnessAdded.forEach(witness -> 
            streamBuilder.add(new InitiateHearingDefenceWitnessEnriched(
                            witness.getWitnessId().toString(), hearingId, witness.getType(),
                            witness.getClassification(), witness.getTitle(), witness.getFirstName(),
                        witness.getLastName(), defendantId)));
        return apply(streamBuilder.build());
    }
}
