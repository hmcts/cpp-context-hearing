package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.HearingExtended;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.domain.event.InheritedVerdictAdded;
import uk.gov.moj.cpp.hearing.mapping.CourtCentreJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingDayJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.PleaJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.ProsecutionCaseJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.VerdictJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;
import uk.gov.moj.cpp.hearing.repository.ProsecutionCaseRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S2201","squid:S134"})
@ServiceComponent(EVENT_LISTENER)
public class InitiateHearingEventListener {
    private static final String GUILTY = "GUILTY";
    private static final Logger LOGGER = LoggerFactory.getLogger(InitiateHearingEventListener.class.getName());

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private ProsecutionCaseRepository prosecutionCaseRepository;

    @Inject
    private OffenceRepository offenceRepository;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private HearingJPAMapper hearingJPAMapper;

    @Inject
    private ProsecutionCaseJPAMapper prosecutionCaseJPAMapper;

    @Inject
    private PleaJPAMapper pleaJPAMapper;

    @Inject
    private VerdictJPAMapper verdictJPAMapper;

    @Inject
    private CourtCentreJPAMapper courtCentreJPAMapper;

    @Inject
    private HearingDayJPAMapper hearingDayJPAMapper;

    @Transactional
    @Handles("hearing.events.initiated")
    public void newHearingInitiated(final JsonEnvelope event) {

        final JsonObject payload = event.payloadAsJsonObject();

        LOGGER.debug("hearing.initiated event received {}", payload);

        final HearingInitiated initiated = jsonObjectToObjectConverter.convert(payload, HearingInitiated.class);

        final Hearing hearingEntity = hearingJPAMapper.toJPA(initiated.getHearing());
        hearingEntity.setHearingState(HearingState.INITIALISED);
        getOffencesForHearing(hearingEntity)
                .forEach(x -> updateOffenceForShadowListedStatus(initiated.getHearing().getShadowListedOffences(), x));

        hearingRepository.save(hearingEntity);
    }

    @Transactional
    @Handles("hearing.events.hearing-extended")
    public void hearingExtended(final JsonEnvelope event) {

        final JsonObject payload = event.payloadAsJsonObject();

        LOGGER.debug("hearing.hearingExtended event received {}", payload);

        final HearingExtended hearingExtended = jsonObjectToObjectConverter.convert(payload, HearingExtended.class);

        final Hearing hearingEntity = hearingRepository.findBy(hearingExtended.getHearingId());

        if (nonNull(hearingExtended.getCourtApplication())) {
            final String courtApplicationsJson = hearingJPAMapper.addOrUpdateCourtApplication(hearingEntity.getCourtApplicationsJson(), hearingExtended.getCourtApplication());
            hearingEntity.setCourtApplicationsJson(courtApplicationsJson);
            if(nonNull(hearingExtended.getCourtCentre())) {
                hearingEntity.setCourtCentre(courtCentreJPAMapper.toJPA(hearingExtended.getCourtCentre()));
            }
            if(nonNull(hearingExtended.getHearingDays()) && isNotEmpty(hearingExtended.getHearingDays())){
                final Set<HearingDay> existingHearingDays = hearingEntity.getHearingDays();
                existingHearingDays.clear();
                existingHearingDays.addAll(hearingDayJPAMapper.toJPA(hearingEntity, hearingExtended.getHearingDays()));
                hearingEntity.setHearingDays(existingHearingDays);
            }
            if(nonNull(hearingExtended.getCourtCentre())) {
                hearingEntity.setJurisdictionType(hearingExtended.getJurisdictionType());
            }
            hearingRepository.save(hearingEntity);
        }
        if (isNotEmpty(hearingExtended.getProsecutionCases())) {
            final List<uk.gov.justice.core.courts.ProsecutionCase> prosecutionCasesFromEntities = prosecutionCaseJPAMapper.fromJPA(hearingEntity.getProsecutionCases());
            hearingExtended.getProsecutionCases().forEach(
                    prosecutionCaseRequest -> {
                        final uk.gov.justice.core.courts.ProsecutionCase prosecutionCaseEntity = prosecutionCasesFromEntities.stream()
                                .filter(prosecutionCase -> prosecutionCase.getId().equals(prosecutionCaseRequest.getId()))
                                .findFirst().orElse(null);
                        uk.gov.justice.core.courts.ProsecutionCase prosecutionCaseToBePersisted = null;
                        if (nonNull(prosecutionCaseEntity)) {
                            prosecutionCaseToBePersisted = createProsecutionCase(prosecutionCaseRequest, prosecutionCaseEntity);
                        } else {
                            prosecutionCaseToBePersisted = prosecutionCaseRequest;
                        }
                        final ProsecutionCase prosecutionCase = prosecutionCaseJPAMapper.toJPA(hearingEntity, prosecutionCaseToBePersisted);
                        getOffencesForProsecutionCase(prosecutionCase).forEach(offence -> updateOffenceForShadowListedStatus(hearingExtended.getShadowListedOffences(), offence));
                        prosecutionCaseRepository.save(prosecutionCase);
                    }
            );
        }
    }

    @Transactional
    @Handles("hearing.events.application-detail-changed")
    public void hearingApplicationDetailChanged(final JsonEnvelope event) {

        final JsonObject payload = event.payloadAsJsonObject();

        LOGGER.debug("hearing.events.application-detail-changed event received {}", payload);

        final ApplicationDetailChanged applicationDetailChanged = jsonObjectToObjectConverter.convert(payload, ApplicationDetailChanged.class);

        final Hearing hearingEntity = hearingRepository.findBy(applicationDetailChanged.getHearingId());

        final String courtApplicationsJson = hearingJPAMapper.addOrUpdateCourtApplication(hearingEntity.getCourtApplicationsJson(), applicationDetailChanged.getCourtApplication());

        hearingEntity.setCourtApplicationsJson(courtApplicationsJson);

        hearingRepository.save(hearingEntity);
    }

    @Transactional
    @Handles("hearing.conviction-date-added")
    public void convictionDateUpdated(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.conviction-date-added event received {}", event.toObfuscatedDebugString());
        }
        final ConvictionDateAdded convictionDateAdded = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ConvictionDateAdded.class);
        updateConvictionDate(convictionDateAdded.getHearingId(), convictionDateAdded.getOffenceId(), convictionDateAdded.getCourtApplicationId(), convictionDateAdded.getConvictionDate());
    }

    @Transactional
    @Handles("hearing.conviction-date-removed")
    public void convictionDateRemoved(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.conviction-date-removed event received {}", event.toObfuscatedDebugString());
        }
        final ConvictionDateRemoved convictionDateRemoved = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ConvictionDateRemoved.class);
        updateConvictionDate(convictionDateRemoved.getHearingId(), convictionDateRemoved.getOffenceId(), convictionDateRemoved.getCourtApplicationId(), null);
    }

    @Transactional
    @Handles("hearing.events.inherited-plea")
    public void hearingInitiatedPleaData(final JsonEnvelope envelop) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.inherited-plea event received {}", envelop.toObfuscatedDebugString());
        }

        final InheritedPlea event = jsonObjectToObjectConverter.convert(envelop.payloadAsJsonObject(), InheritedPlea.class);

        final Offence offence = offenceRepository.findBy(new HearingSnapshotKey(event.getPlea().getOffenceId(), event.getHearingId()));

        if (nonNull(offence)) {

            final boolean shouldSetPlea = isNull(offence.getPlea()) || isPleaInherited(event, offence);

            if (shouldSetPlea) {
                offence.setPlea(pleaJPAMapper.toJPA(event.getPlea()));
                offence.setConvictionDate(GUILTY.equals(event.getPlea().getPleaValue()) ? event.getPlea().getPleaDate() : null);
                offenceRepository.save(offence);
            }
        }
    }

    @Transactional
    @Handles("hearing.events.inherited-verdict-added")
    public void hearingInitiatedVerdictData(final JsonEnvelope envelop) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.inherited-verdict-added event received {}", envelop.toObfuscatedDebugString());
        }

        final InheritedVerdictAdded event = jsonObjectToObjectConverter.convert(envelop.payloadAsJsonObject(), InheritedVerdictAdded.class);

        final Offence offence = offenceRepository.findBy(new HearingSnapshotKey(event.getVerdict().getOffenceId(), event.getHearingId()));

        if (nonNull(offence)) {

            final boolean shouldSetVerdict = isNull(offence.getVerdict()) || isVerdictInherited(event, offence);

            if (shouldSetVerdict) {
                offence.setVerdict(verdictJPAMapper.toJPA(event.getVerdict()));
                offenceRepository.save(offence);
            }
        }
    }

    private List<Offence> getOffencesForHearing(final Hearing hearingEntity) {
        return ofNullable(hearingEntity.getProsecutionCases()).orElse(new HashSet<>())
                .stream()
                .flatMap(x -> ofNullable(x.getDefendants()).orElse(new HashSet<>()).stream())
                .flatMap(def -> ofNullable(def.getOffences()).orElse(new HashSet<>()).stream())
                .collect(toList());
    }

    private void updateOffenceForShadowListedStatus(final List<UUID> shadowListedOffences, final Offence offence) {
        ofNullable(shadowListedOffences).orElseGet(ArrayList::new)
                .stream()
                .filter(x -> offence.getId().getId().equals(x))
                .findFirst()
                .ifPresent(x -> offence.setShadowListed(true));


    }

    private List<Offence> getOffencesForProsecutionCase(final ProsecutionCase prosecutionCase) {
        return prosecutionCase.getDefendants()
                .stream()
                .flatMap(def -> ofNullable(def.getOffences()).orElse(new HashSet<>()).stream())
                .collect(toList());
    }

    private boolean isPleaInherited(InheritedPlea event, Offence offence) {
        return !event.getHearingId().equals(offence.getPlea().getOriginatingHearingId());
    }

    private boolean isVerdictInherited(InheritedVerdictAdded event, Offence offence) {
        return !event.getHearingId().equals(offence.getVerdict().getOriginatingHearingId());
    }

    private void save(final UUID offenceId, final UUID hearingId, final Consumer<Offence> consumer) {
        ofNullable(offenceRepository.findBy(new HearingSnapshotKey(offenceId, hearingId))).map(o -> {
            consumer.accept(o);
            offenceRepository.saveAndFlush(o);
            return o;
        }).orElseThrow(() -> new RuntimeException("Offence id is not found on hearing id: " + hearingId));
    }

    private uk.gov.justice.core.courts.ProsecutionCase createProsecutionCase(final uk.gov.justice.core.courts.ProsecutionCase prosecutionCaseRequest,
                                                                             final uk.gov.justice.core.courts.ProsecutionCase prosecutionCaseEntity) {
        return uk.gov.justice.core.courts.ProsecutionCase.prosecutionCase()
                .withId(prosecutionCaseEntity.getId())
                .withDefendants(addDefendants(prosecutionCaseEntity.getDefendants(), prosecutionCaseRequest.getDefendants()))
                .withStatementOfFactsWelsh(prosecutionCaseEntity.getStatementOfFactsWelsh())
                .withStatementOfFacts(prosecutionCaseEntity.getStatementOfFacts())
                .withOriginatingOrganisation(prosecutionCaseEntity.getOriginatingOrganisation())
                .withCaseStatus(prosecutionCaseEntity.getCaseStatus())
                .withClassOfCase(prosecutionCaseEntity.getClassOfCase())
                .withProsecutionCaseIdentifier(prosecutionCaseEntity.getProsecutionCaseIdentifier())
                .withAppealProceedingsPending(prosecutionCaseEntity.getAppealProceedingsPending())
                .withBreachProceedingsPending(prosecutionCaseEntity.getBreachProceedingsPending())
                .withCaseMarkers(prosecutionCaseEntity.getCaseMarkers())
                .withPoliceOfficerInCase(prosecutionCaseEntity.getPoliceOfficerInCase())
                .withRemovalReason(prosecutionCaseEntity.getRemovalReason())
                .withInitiationCode(prosecutionCaseEntity.getInitiationCode())
                .withCpsOrganisation(prosecutionCaseEntity.getCpsOrganisation())
                .withIsCpsOrgVerifyError(prosecutionCaseEntity.getIsCpsOrgVerifyError())
                .build();
    }

    private static Defendant createDefendant(final Defendant defendant, final Defendant defendantEntity) {
        return Defendant.defendant()
                .withId(defendantEntity.getId())
                .withOffences(addOffences(defendantEntity.getOffences(), defendant.getOffences()))
                .withMasterDefendantId(defendantEntity.getMasterDefendantId())
                .withPncId(defendantEntity.getPncId())
                .withCroNumber(defendantEntity.getCroNumber())
                .withPersonDefendant(defendantEntity.getPersonDefendant())
                .withProsecutionCaseId(defendantEntity.getProsecutionCaseId())
                .withProceedingsConcluded(defendantEntity.getProceedingsConcluded())
                .withAssociatedPersons(defendantEntity.getAssociatedPersons())
                .withCourtProceedingsInitiated(defendantEntity.getCourtProceedingsInitiated())
                .withLegalEntityDefendant(defendantEntity.getLegalEntityDefendant())
                .withDefenceOrganisation(defendantEntity.getDefenceOrganisation())
                .withIsYouth(defendantEntity.getIsYouth())
                .withNumberOfPreviousConvictionsCited(defendantEntity.getNumberOfPreviousConvictionsCited())
                .withProsecutionAuthorityReference(defendantEntity.getProsecutionAuthorityReference())
                .withAssociatedDefenceOrganisation(defendantEntity.getAssociatedDefenceOrganisation())
                .withLegalAidStatus(defendantEntity.getLegalAidStatus())
                .withMitigation(defendantEntity.getMitigation())
                .withMitigationWelsh(defendantEntity.getMitigationWelsh())
                .withWitnessStatement(defendantEntity.getWitnessStatement())
                .withWitnessStatementWelsh(defendantEntity.getWitnessStatementWelsh())
                .withAliases(defendantEntity.getAliases())
                .withAssociationLockedByRepOrder(defendantEntity.getAssociationLockedByRepOrder())
                .withDefendantCaseJudicialResults(defendantEntity.getDefendantCaseJudicialResults())
                .build();
    }

    private static List<Defendant> addDefendants(final List<Defendant> defendantsEntities, final List<Defendant> defendantsRequest) {
        final List<UUID> defendantIdsInEntities = defendantsEntities.stream().map(Defendant::getId).collect(toList());
        defendantsRequest.forEach(defendant -> {
            if (defendantIdsInEntities.contains(defendant.getId())) {
                final Defendant defendantEntity = defendantsEntities.stream()
                        .filter(def -> def.getId().equals(defendant.getId()))
                        .findFirst()
                        .orElse(null);
                final Defendant defendantToBeAdded = createDefendant(defendant, defendantEntity);
                defendantsEntities.removeIf(defToBeRemoved -> defToBeRemoved.getId().equals(defendant.getId()));
                defendantsEntities.add(defendantToBeAdded);
            } else {
                defendantsEntities.add(defendant);
            }
        });
        return defendantsEntities;
    }

    private static List<uk.gov.justice.core.courts.Offence> addOffences(final List<uk.gov.justice.core.courts.Offence> offencesEntities, final List<uk.gov.justice.core.courts.Offence> offencesRequest) {
        final List<UUID> offencesIdsInEntities = offencesEntities.stream()
                .map(uk.gov.justice.core.courts.Offence::getId)
                .collect(toList());
        offencesRequest.forEach(offence -> {
            if (!offencesIdsInEntities.contains(offence.getId())) {
                offencesEntities.add(offence);
            }
        });
        return offencesEntities;
    }

    private void updateConvictionDate(final UUID hearingId, final UUID offenceId, final UUID courtApplicationID, final LocalDate convictionDate) {
        if(courtApplicationID == null) {
            save(offenceId, hearingId, o -> o.setConvictionDate(convictionDate));
        }else{
            final Hearing hearingEntity = hearingRepository.findBy(hearingId);
            final String updatedCourtApplicationJson;
            if(offenceId != null) {
                updatedCourtApplicationJson = hearingJPAMapper.updateConvictedDateOnOffencesInCourtApplication(hearingEntity.getCourtApplicationsJson(), courtApplicationID, offenceId, convictionDate);
            }else{
                final uk.gov.justice.core.courts.Hearing hearing = hearingJPAMapper.fromJPA(hearingEntity);
                final Optional<CourtApplication> courtApplication = hearing.getCourtApplications().stream()
                        .filter( ca -> ca.getId().equals(courtApplicationID))
                        .findFirst();
                if(courtApplication.isPresent()) {
                    courtApplication.get().setConvictionDate(convictionDate);
                    updatedCourtApplicationJson = hearingJPAMapper.addOrUpdateCourtApplication(hearingEntity.getCourtApplicationsJson(), courtApplication.get());
                }else{
                    updatedCourtApplicationJson =   hearingEntity.getCourtApplicationsJson();
                    if(LOGGER.isDebugEnabled()) {
                        LOGGER.debug("hearing.hearing-court-application-plea-updated / removed event application not found {}", courtApplicationID);
                    }
                }
            }
            hearingEntity.setCourtApplicationsJson(updatedCourtApplicationJson);
            hearingRepository.save(hearingEntity);
        }
    }
}
