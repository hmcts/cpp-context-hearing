package uk.gov.moj.cpp.hearing.event.listener;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceDeleted;
import uk.gov.moj.cpp.hearing.domain.event.OffenceUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;
import uk.gov.moj.cpp.hearing.repository.LegalCaseRepository;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

@ServiceComponent(EVENT_LISTENER)
public class CaseDefendantOffencesChangedEventListener {

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private OffenceRepository offenceRepository;

    @Inject
    private DefendantRepository defendantRepository;

    @Inject
    private LegalCaseRepository legalCaseRepository;

    @Transactional
    @Handles("hearing.events.offence-added")
    public void addOffence(final JsonEnvelope envelope) {

        final OffenceAdded offenceToBeAdded = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), OffenceAdded.class);

        final Defendant defendant = defendantRepository.findBy(new HearingSnapshotKey(offenceToBeAdded.getDefendantId(), offenceToBeAdded.getHearingId()));

        final LegalCase legalCase = legalCaseRepository.findById(offenceToBeAdded.getCaseId());

        final Offence offence = Offence.builder()
                .withId(new HearingSnapshotKey(offenceToBeAdded.getId(), offenceToBeAdded.getHearingId()))
                .withDefendant(defendant)
                .withCase(legalCase)
                .withCode(offenceToBeAdded.getOffenceCode())
                .withWording(offenceToBeAdded.getWording())
                .withStartDate(offenceToBeAdded.getStartDate())
                .withEndDate(offenceToBeAdded.getEndDate())
                .withCount(offenceToBeAdded.getCount())
                .withConvictionDate(offenceToBeAdded.getConvictionDate())
                .build();

        offenceRepository.saveAndFlush(offence);
    }

    @Transactional
    @Handles("hearing.events.offence-updated")
    public void updateOffence(final JsonEnvelope envelope) {

        final OffenceUpdated offenceToBeUpdated = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), OffenceUpdated.class);

        final Offence offence = offenceRepository.findBy(new HearingSnapshotKey(offenceToBeUpdated.getId(), offenceToBeUpdated.getHearingId()));

        offence.setCode(offenceToBeUpdated.getOffenceCode());

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

        final Defendant defendant = defendantRepository.findBy(new HearingSnapshotKey(offence.getDefendantId(), offenceToBeDeleted.getHearingId()));

        final List<Offence> offencesToBeRemoved = defendant.getOffences().stream().filter(o -> o.getId().equals(offence.getId())).collect(Collectors.toList());

        defendant.getOffences().removeAll(offencesToBeRemoved);

        offenceRepository.removeAndFlush(offence);
    }

}