package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceDeleted;
import uk.gov.moj.cpp.hearing.domain.event.OffenceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.OffencesRemovedFromExistingHearing;
import uk.gov.moj.cpp.hearing.mapping.OffenceJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;

@ServiceComponent(EVENT_LISTENER)
public class UpdateOffencesForDefendantEventListener {

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private OffenceRepository offenceRepository;

    @Inject
    private DefendantRepository defendantRepository;

    @Inject
    private OffenceJPAMapper offenceJPAMapper;

    @Inject
    private UpdateOffencesForDefendantService updateOffencesForDefendantService;

    private static final Logger LOGGER = getLogger(UpdateOffencesForDefendantEventListener.class);

    @Transactional
    @Handles("hearing.events.offence-added")
    public void addOffence(final JsonEnvelope envelope) {

        final OffenceAdded offenceAdded = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), OffenceAdded.class);
        final Hearing hearing = hearingRepository.findBy(offenceAdded.getHearingId());
        final Offence offence = offenceJPAMapper.toJPA(hearing, offenceAdded.getDefendantId(), offenceAdded.getOffence());

        offenceRepository.saveAndFlush(offence);
    }

    @Transactional
    @Handles("hearing.events.offence-updated")
    public void updateOffence(final JsonEnvelope envelope) {

        final OffenceUpdated offenceUpdated = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), OffenceUpdated.class);
        final Hearing hearing = hearingRepository.findBy(offenceUpdated.getHearingId());
        final Offence offence = offenceJPAMapper.toJPA(hearing, offenceUpdated.getDefendantId(), offenceUpdated.getOffence());

        final Defendant defendant = defendantRepository.findBy(new HearingSnapshotKey(offenceUpdated.getDefendantId(), offenceUpdated.getHearingId()));

        if (nonNull(defendant)) {
            if (defendant.getOffences().removeIf(o -> o.getId().getId().equals(offenceUpdated.getOffence().getId()))) {
                defendant.getOffences().add(offence);
            }

            defendantRepository.saveAndFlush(defendant);
        }
    }

    @Transactional
    @Handles("hearing.events.offence-deleted")
    public void deleteOffence(final JsonEnvelope envelope) {

        final OffenceDeleted offenceDeleted = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), OffenceDeleted.class);

        final Offence offence = offenceRepository.findBy(new HearingSnapshotKey(offenceDeleted.getId(), offenceDeleted.getHearingId()));
        LOGGER.info("offence.getDefendant {}", nonNull(offence.getDefendant()) ? objectToJsonObjectConverter.convert(offence.getDefendant()) : "null");
        LOGGER.info("offence.getDefendant.getOffences {}", nonNull(offence.getDefendant().getOffences()) && (!offence.getDefendant().getOffences().isEmpty()) ? objectToJsonObjectConverter.convert(offence.getDefendant().getOffences()) : "null");
        offence.getDefendant().getOffences().removeIf(o -> o.getId().getId().equals(offenceDeleted.getId()));

        LOGGER.info("offence.getDefendant before saving defendant {}", nonNull(offence.getDefendant()) ? objectToJsonObjectConverter.convert(offence.getDefendant()) : "null");
        defendantRepository.save(offence.getDefendant());
    }

    @Transactional
    @Handles("hearing.events.offences-removed-from-existing-hearing")
    public void removeOffencesFromExistingAllocatedHearing(final JsonEnvelope envelope) {

        final OffencesRemovedFromExistingHearing offencesRemovedFromExistingHearing = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), OffencesRemovedFromExistingHearing.class);

        final UUID hearingId = offencesRemovedFromExistingHearing.getHearingId();
        final List<UUID> prosecutionCaseIds = offencesRemovedFromExistingHearing.getProsecutionCaseIds();
        final List<UUID> defendantIds = offencesRemovedFromExistingHearing.getDefendantIds();
        final List<UUID> offenceIds = offencesRemovedFromExistingHearing.getOffenceIds();

        final Hearing hearing = updateOffencesForDefendantService.removeOffencesFromExistingHearing(hearingRepository.findBy(hearingId), prosecutionCaseIds, defendantIds, offenceIds);

        hearingRepository.save(hearing);

    }

}