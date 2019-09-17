package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.core.courts.InterpreterIntermediary;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryAdded;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryRemoved;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryUpdated;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class InterpreterIntermediaryDelegate implements Serializable {

    private final HearingAggregateMomento momento;

    public InterpreterIntermediaryDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleInterpreterIntermediaryAdded(final InterpreterIntermediaryAdded interpreterIntermediaryAdded) {
        final InterpreterIntermediary interpreterIntermediary = interpreterIntermediaryAdded.getInterpreterIntermediary();
        this.momento.getInterpreterIntermediary().put(interpreterIntermediary.getId(), interpreterIntermediary);
    }

    public void handleInterpreterIntermediaryRemoved(final InterpreterIntermediaryRemoved interpreterIntermediaryRemoved) {
        this.momento.getInterpreterIntermediary().remove(interpreterIntermediaryRemoved.getId());
    }

    public void handleInterpreterIntermediaryUpdated(final InterpreterIntermediaryUpdated interpreterIntermediaryUpdated) {
        final InterpreterIntermediary interpreterIntermediary = interpreterIntermediaryUpdated.getInterpreterIntermediary();
        this.momento.getInterpreterIntermediary().put(interpreterIntermediary.getId(), interpreterIntermediary);
    }

    public Stream<Object> addInterpreterIntermediary(final UUID hearingId, final InterpreterIntermediary interpreterIntermediary) {
        if (this.momento.getInterpreterIntermediary().containsKey(interpreterIntermediary.getId())) {
            return Stream.of(new InterpreterIntermediaryChangeIgnored(String.format("Provided interpreterIntermediary already exists, payload [%s]", interpreterIntermediary.toString())));
        }
        return Stream.of(new InterpreterIntermediaryAdded(interpreterIntermediary, hearingId));
    }

    public Stream<Object> removeInterpreterIntermediary(final UUID id, final UUID hearingId) {
        return Stream.of(new InterpreterIntermediaryRemoved(id, hearingId));
    }

    public Stream<Object> updateInterpreterIntermediary(final InterpreterIntermediary interpreterIntermediary, final UUID hearingId) {

        final Map<UUID, InterpreterIntermediary> intermediaryMap = this.momento.getInterpreterIntermediary();
        if (!(intermediaryMap.containsKey(interpreterIntermediary.getId()))) {
            return Stream.of(new InterpreterIntermediaryChangeIgnored(String.format("Provided interpreterIntermediary does not exists, payload [%s]", interpreterIntermediary.toString())));
        } else if (intermediaryMap.get(interpreterIntermediary.getId()).equals(interpreterIntermediary)) {
            return Stream.of(new InterpreterIntermediaryChangeIgnored(String.format("No change in provided interpreterIntermediary, payload [%s]", interpreterIntermediary.toString())));
        }
        return Stream.of(new InterpreterIntermediaryUpdated(interpreterIntermediary, hearingId));
    }
}
