package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.core.courts.RespondentCounsel;
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselUpdated;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class RespondentCounselDelegate implements Serializable {

    private final HearingAggregateMomento momento;

    public RespondentCounselDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleRespondentCounselAdded(final RespondentCounselAdded respondentCounselAdded) {
        final RespondentCounsel respondentCounsel = respondentCounselAdded.getRespondentCounsel();
        this.momento.getRespondentCounsels().put(respondentCounsel.getId(), respondentCounsel);
    }

    public void handleRespondentCounselRemoved(final RespondentCounselRemoved respondentCounselRemoved) {
        this.momento.getRespondentCounsels().remove(respondentCounselRemoved.getId());
    }

    public void handleRespondentCounselUpdated(final RespondentCounselUpdated respondentCounselUpdated) {
        final RespondentCounsel respondentCounsel = respondentCounselUpdated.getRespondentCounsel();
        this.momento.getRespondentCounsels().put(respondentCounsel.getId(), respondentCounsel);
    }
    public Stream<Object> addRespondentCounsel(final RespondentCounsel respondentCounsel, final UUID hearingId) {
        if (this.momento.getRespondentCounsels().containsKey(respondentCounsel.getId())) {
            return Stream.of(new RespondentCounselChangeIgnored(String.format("Provided RespondentCounsel already exists, payload [%s]", respondentCounsel.toString())));
        }
        return Stream.of(new RespondentCounselAdded(respondentCounsel, hearingId));
    }

    public Stream<Object> removeRespondentCounsel(final UUID id, final UUID hearingId) {
        return Stream.of(new RespondentCounselRemoved(id, hearingId));
    }

    public Stream<Object> updateRespondentCounsel(final RespondentCounsel respondentCounsel, final UUID hearingId) {

        final Map<UUID, RespondentCounsel> respondentCounsels = this.momento.getRespondentCounsels();
        if (!(respondentCounsels.containsKey(respondentCounsel.getId()))) {
            return Stream.of(new RespondentCounselChangeIgnored(String.format("Provided RespondentCounsel does not exists, payload [%s]", respondentCounsel.toString())));
        }else if (respondentCounsels.get(respondentCounsel.getId()).equals(respondentCounsel)){
            return Stream.of(new RespondentCounselChangeIgnored(String.format("No change in provided RespondentCounsel, payload [%s]", respondentCounsel.toString())));
        }
        return Stream.of(new RespondentCounselUpdated(respondentCounsel, hearingId));
    }
}
