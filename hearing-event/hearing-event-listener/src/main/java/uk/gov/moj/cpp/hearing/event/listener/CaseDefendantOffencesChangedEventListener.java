package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceDeleted;
import uk.gov.moj.cpp.hearing.domain.event.OffenceUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

@ServiceComponent(EVENT_LISTENER)
public class CaseDefendantOffencesChangedEventListener {

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private OffenceRepository offenceRepository;

    @Inject
    private DefendantRepository defendantRepository;

    @Transactional
    @Handles("hearing.events.offence-added")
    public void addOffence(final JsonEnvelope envelope) {

        final OffenceAdded offenceToBeAdded = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), OffenceAdded.class);

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceToBeAdded.getId(), offenceToBeAdded.getHearingId()));
        offence.setDefendantId(offenceToBeAdded.getDefendantId());
        offence.setOffenceCode(offenceToBeAdded.getOffenceCode());
        offence.setWording(offenceToBeAdded.getWording());
        offence.setStartDate(offenceToBeAdded.getStartDate());
        offence.setEndDate(offenceToBeAdded.getEndDate());
        offence.setCount(offenceToBeAdded.getCount());
        offence.setConvictionDate(offenceToBeAdded.getConvictionDate());

        offenceRepository.saveAndFlush(offence);
    }

    @Transactional
    @Handles("hearing.events.offence-updated")
    public void updateOffence(final JsonEnvelope envelope) {

        final OffenceUpdated offenceToBeUpdated = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), OffenceUpdated.class);

        final Offence offence = offenceRepository.findBy(new HearingSnapshotKey(offenceToBeUpdated.getId(), offenceToBeUpdated.getHearingId()));

        offence.setOffenceCode(offenceToBeUpdated.getOffenceCode());

        offence.setWording(offenceToBeUpdated.getWording());

        offence.setStartDate(offenceToBeUpdated.getStartDate());

        offence.setEndDate(offenceToBeUpdated.getEndDate());

        offence.setCount(offenceToBeUpdated.getCount());

        offence.setConvictionDate(offenceToBeUpdated.getConvictionDate());

        offenceRepository.saveAndFlush(offence);

    }

    @Transactional
    @Handles("hearing.events.offence-deleted")
    public void deleteOffence(final JsonEnvelope envelope) {

        final OffenceDeleted offenceToBeDeleted = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), OffenceDeleted.class);

        final Offence offence = offenceRepository.findBy(new HearingSnapshotKey(offenceToBeDeleted.getId(), offenceToBeDeleted.getHearingId()));

        offence.getDefendant().getOffences().removeIf(o -> o.getId().getId().equals(offenceToBeDeleted.getId()));

        defendantRepository.save(offence.getDefendant());
    }

}