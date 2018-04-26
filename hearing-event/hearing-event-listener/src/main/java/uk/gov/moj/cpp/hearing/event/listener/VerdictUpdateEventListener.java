package uk.gov.moj.cpp.hearing.event.listener;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Offence;
import uk.gov.moj.cpp.hearing.repository.AhearingRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

@ServiceComponent(EVENT_LISTENER)
public class VerdictUpdateEventListener {

    @Inject
    private AhearingRepository ahearingRepository;

    @Inject
    JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Transactional
    @Handles("hearing.offence-verdict-updated")
    public void verdictUpdate(final JsonEnvelope event) {
        final VerdictUpsert verdictUpdated = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), VerdictUpsert.class);

        final Ahearing ahearing = ahearingRepository.findById(verdictUpdated.getHearingId());

        final Offence offence = ahearing.getDefendants().stream()
                .flatMap(d -> d.getOffences().stream())
                .filter(o -> o.getId().getId().equals(verdictUpdated.getOffenceId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid offence id.  Offence id is not found on hearing: " + verdictUpdated.getOffenceId().toString()));

        offence.setVerdictCode(verdictUpdated.getCode());
        offence.setVerdictCategory(verdictUpdated.getCategory());
        offence.setVerdictDescription(verdictUpdated.getDescription());
        offence.setVerdictDate(verdictUpdated.getVerdictDate());
        offence.setNumberOfJurors(verdictUpdated.getNumberOfJurors());
        offence.setNumberOfSplitJurors(verdictUpdated.getNumberOfSplitJurors());
        offence.setUnanimous(verdictUpdated.getUnanimous());

        ahearingRepository.save(ahearing);
    }
}
