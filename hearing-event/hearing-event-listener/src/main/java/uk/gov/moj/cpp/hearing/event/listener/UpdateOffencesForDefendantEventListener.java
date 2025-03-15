package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAddedV2;
import uk.gov.moj.cpp.hearing.domain.event.OffenceDeleted;
import uk.gov.moj.cpp.hearing.domain.event.OffenceDeletedV2;
import uk.gov.moj.cpp.hearing.domain.event.OffenceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.OffenceUpdatedV2;
import uk.gov.moj.cpp.hearing.domain.event.OffencesRemovedFromExistingHearing;
import uk.gov.moj.cpp.hearing.mapping.HearingDefenceCounselJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.OffenceJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;
import uk.gov.moj.cpp.hearing.repository.HearingDefenceCounselRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;

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

    @Inject
    private HearingDefenceCounselRepository hearingDefenceCounselRepository;

    @Inject
    private HearingDefenceCounselJPAMapper hearingDefenceCounselJPAMapper;


    @Transactional
    @Handles("hearing.events.offence-added")
    public void addOffence(final JsonEnvelope envelope) {

        final OffenceAdded offenceAdded = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), OffenceAdded.class);
        final Hearing hearing = hearingRepository.findBy(offenceAdded.getHearingId());
        if(isNull(hearing)){
            return;
        }

        if(Optional.ofNullable(hearing.getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .map(ProsecutionCase::getDefendants)
                .flatMap(Collection::stream)
                .map(Defendant::getId)
                .noneMatch(id -> id.getId().equals(offenceAdded.getDefendantId()))){
            return;
        }

        final Offence offence = offenceJPAMapper.toJPA(hearing, offenceAdded.getDefendantId(), offenceAdded.getOffence());

        offenceRepository.saveAndFlush(offence);
    }

    @Transactional
    @Handles("hearing.events.offence-added-v2")
    public void addOffenceV2(final JsonEnvelope envelope) {

        final OffenceAddedV2 offenceAdded = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), OffenceAddedV2.class);
        final Hearing hearing = hearingRepository.findBy(offenceAdded.getHearingId());
        if(isNull(hearing)){
            return;
        }

        if(Optional.ofNullable(hearing.getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .map(ProsecutionCase::getDefendants)
                .flatMap(Collection::stream)
                .map(Defendant::getId)
                .noneMatch(id -> id.getId().equals(offenceAdded.getDefendantId()))){
            return;
        }

        offenceAdded.getOffences().forEach(offencePojo -> {
            final Offence offence = offenceJPAMapper.toJPA(hearing, offenceAdded.getDefendantId(), offencePojo);
            offenceRepository.saveAndFlush(offence);
        });
    }

    @Transactional
    @Handles("hearing.events.offence-updated")
    public void updateOffence(final JsonEnvelope envelope) {

        final OffenceUpdated offenceUpdated = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), OffenceUpdated.class);
        final Optional<Hearing> hearing = hearingRepository.findOptionalBy(offenceUpdated.getHearingId());
        if(hearing.isEmpty()){
            return;
        }
        final Offence offence = offenceJPAMapper.toJPA(hearing.get(), offenceUpdated.getDefendantId(), offenceUpdated.getOffence());

        final Defendant defendant = defendantRepository.findBy(new HearingSnapshotKey(offenceUpdated.getDefendantId(), offenceUpdated.getHearingId()));

        if (nonNull(defendant)) {
            if (defendant.getOffences().removeIf(o -> o.getId().getId().equals(offenceUpdated.getOffence().getId()))) {
                defendant.getOffences().add(offence);
            }

            defendantRepository.saveAndFlush(defendant);
        }
    }

    @Transactional
    @Handles("hearing.events.offence-updated-v2")
    public void updateOffenceV2(final JsonEnvelope envelope) {

        final OffenceUpdatedV2 offenceUpdated = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), OffenceUpdatedV2.class);
        final Optional<Hearing> hearingEntity = hearingRepository.findOptionalBy(offenceUpdated.getHearingId());
        if(hearingEntity.isEmpty()){
            return;
        }
        final Hearing hearing = hearingEntity.get();
        final Set<Offence> offences = offenceJPAMapper.toJPA(hearing, offenceUpdated.getDefendantId(), offenceUpdated.getOffences());

        final Defendant defendant = defendantRepository.findBy(new HearingSnapshotKey(offenceUpdated.getDefendantId(), offenceUpdated.getHearingId()));

        if (nonNull(defendant)) {
            offences.forEach(offence -> {
                    if (defendant.getOffences().removeIf(o -> o.getId().getId().equals(offence.getId().getId()))) {
                                defendant.getOffences().add(offence);
                    }
                });

            defendantRepository.saveAndFlush(defendant);
        }
    }

    @Transactional
    @Handles("hearing.events.offence-deleted")
    public void deleteOffence(final JsonEnvelope envelope) {

        final OffenceDeleted offenceDeleted = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), OffenceDeleted.class);

        final Optional<Offence> offence = offenceRepository.findOptionalBy(new HearingSnapshotKey(offenceDeleted.getId(), offenceDeleted.getHearingId()));
        if(offence.isEmpty()){
            return;
        }

        offence.get().getDefendant().getOffences().removeIf(o -> o.getId().getId().equals(offenceDeleted.getId()));

        defendantRepository.save(offence.get().getDefendant());
    }

    @Transactional
    @Handles("hearing.events.offence-deleted-v2")
    public void deleteOffenceV2(final JsonEnvelope envelope) {

        final OffenceDeletedV2 offenceDeleted = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), OffenceDeletedV2.class);

        offenceDeleted.getIds().forEach(offenceId -> {
            final Optional<Offence> offence = offenceRepository.findOptionalBy(new HearingSnapshotKey(offenceId, offenceDeleted.getHearingId()));
            if(offence.isEmpty()){
                return;
            }

            offence.get().getDefendant().getOffences().removeIf(o -> o.getId().getId().equals(offenceId));

            defendantRepository.save(offence.get().getDefendant());
        });
    }

    @Transactional
    @Handles("hearing.events.offences-removed-from-existing-hearing")
    public void removeOffencesFromExistingAllocatedHearing(final JsonEnvelope envelope) {

        final OffencesRemovedFromExistingHearing offencesRemovedFromExistingHearing = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), OffencesRemovedFromExistingHearing.class);

        final UUID hearingId = offencesRemovedFromExistingHearing.getHearingId();
        final List<UUID> prosecutionCaseIds = offencesRemovedFromExistingHearing.getProsecutionCaseIds();
        final List<UUID> defendantIds = offencesRemovedFromExistingHearing.getDefendantIds();
        final List<UUID> offenceIds = offencesRemovedFromExistingHearing.getOffenceIds();
        final Set<UUID> removedDefendantIdsFromHearing = new HashSet<>();

        Optional<Hearing> existingHearing = hearingRepository.findOptionalBy(hearingId);
        if(existingHearing.isEmpty()){
            return;
        }
        final Hearing hearing = updateOffencesForDefendantService.removeOffencesFromExistingHearing(existingHearing.get(), prosecutionCaseIds, defendantIds, offenceIds, removedDefendantIdsFromHearing);

        hearingRepository.save(hearing);

        if(CollectionUtils.isNotEmpty(hearing.getDefenceCounsels()) && CollectionUtils.isNotEmpty(removedDefendantIdsFromHearing)){
            removeDefenceCounselsFromHearing(hearing, removedDefendantIdsFromHearing);
        }
    }

    private void removeDefenceCounselsFromHearing(final Hearing hearing, final Set<UUID> removedDefendantIdsFromHearing) {
        final Set<HearingDefenceCounsel> defenceCounselDBEntities = hearing.getDefenceCounsels();
        final List<DefenceCounsel> preFilterDefenceCounselList = new ArrayList<>();
        final List<DefenceCounsel> postFilterDefenceCounselList = new ArrayList<>();

        defenceCounselDBEntities.forEach(hdc -> {
            final DefenceCounsel defenceCounsel =  hearingDefenceCounselJPAMapper.fromJPA(hdc);
            if(nonNull(defenceCounsel)){
                preFilterDefenceCounselList.add(defenceCounsel);
                postFilterDefenceCounselList.add(DefenceCounsel.defenceCounsel().withValuesFrom(defenceCounsel)
                        .withDefendants(new ArrayList<>(defenceCounsel.getDefendants()))
                        .build());
            }
        });


        postFilterDefenceCounselList.forEach(dc -> dc.getDefendants().removeIf(defId-> removedDefendantIdsFromHearing.contains(defId)));
        postFilterDefenceCounselList.removeIf(dc -> dc.getDefendants().isEmpty());

        defenceCounselDBEntities.forEach(dce -> {
           final DefenceCounsel defenceCounsel = postFilterDefenceCounselList.stream().filter(dc -> dc.getId().equals(dce.getId().getId()))
                    .findFirst().orElse(null);
            if(nonNull(defenceCounsel)){
                if(!preFilterDefenceCounselList.contains(defenceCounsel)){
                    // Update defence counsels with remaining defendants
                    final HearingDefenceCounsel hearingDefenceCounsel = hearingDefenceCounselJPAMapper.toJPA(hearing, defenceCounsel);
                    hearingDefenceCounsel.setId(new HearingSnapshotKey(defenceCounsel.getId(), hearing.getId()));
                    hearingDefenceCounselRepository.saveAndFlush(hearingDefenceCounsel);
                }
            } else {
                // Remove defence counsels from DB, when there are no defendants
                dce.setDeleted(true);
                hearingDefenceCounselRepository.saveAndFlush(dce);
            }
        });
    }

}