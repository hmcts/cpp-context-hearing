package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.hearing.courts.ApplicationCourtListRestriction;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.domain.event.CourtListRestricted;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_LISTENER)
public class CourtListRestrictionEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CourtListRestrictionEventListener.class.getName());

    private final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

    @Inject
    private HearingRepository hearingRepository;

    @Handles("hearing.event.court-list-restricted")
    public void processCourtListRestrictions(final Envelope<CourtListRestricted> event) throws IOException {
        final CourtListRestricted courtListRestricted = event.payload();
        LOGGER.info("CourtListRestricted for hearing id {} ", courtListRestricted.getHearingId());
        final Optional<Hearing> hearingEntity = hearingRepository.findOptionalBy(courtListRestricted.getHearingId());
        if(hearingEntity.isEmpty()){
            return;
        }
        final Hearing hearing = hearingEntity.get();
        if (hasApplicationRestriction(courtListRestricted)) {
            applyCourtApplicationRestrictions(courtListRestricted, hearing);
        }

        if (hasCaseOrDefendantRestriction(courtListRestricted)) {
            applyProsecutionCaseRestrictions(hearing.getProsecutionCases(), courtListRestricted, courtListRestricted.getRestrictCourtList());
        }

        if (isNotEmpty(courtListRestricted.getDefendantIds()) && nonNull(hearing.getCourtApplicationsJson())) {
            final Set<UUID> masterDefendantIds = collectMasterDefendantIds(
                    hearing.getProsecutionCases(), toSet(courtListRestricted.getDefendantIds()));
            if (!masterDefendantIds.isEmpty()) {
                applyApplicationRestrictionsForMatchingParties(masterDefendantIds, courtListRestricted.getRestrictCourtList(), hearing);
            }
        }

        hearingRepository.save(hearing);
    }

    private Set<UUID> collectMasterDefendantIds(final Set<ProsecutionCase> prosecutionCases, final Set<UUID> restrictedDefendantIds) {
        final Set<UUID> masterDefendantIds = new HashSet<>();
        for (final ProsecutionCase prosecutionCase : prosecutionCases) {
            for (final Defendant defendant : prosecutionCase.getDefendants()) {
                if (restrictedDefendantIds.contains(defendant.getId().getId()) && nonNull(defendant.getMasterDefendantId())) {
                    masterDefendantIds.add(defendant.getMasterDefendantId());
                }
            }
        }
        return masterDefendantIds;
    }

    private void applyApplicationRestrictionsForMatchingParties(
            final Set<UUID> masterDefendantIds,
            final Boolean restrictCourtList,
            final Hearing hearing) throws IOException {

        final List<CourtApplication> applications = parseCourtApplications(hearing.getCourtApplicationsJson());
        final List<UUID> matchingApplicantAndSubjectIds = new ArrayList<>();
        final List<UUID> matchingRespondentIds = new ArrayList<>();

        for (final CourtApplication application : applications) {
            collectMatchingPartyId(application.getSubject(), masterDefendantIds, matchingApplicantAndSubjectIds);
            collectMatchingPartyId(application.getApplicant(), masterDefendantIds, matchingApplicantAndSubjectIds);
            if (isNotEmpty(application.getRespondents())) {
                application.getRespondents().forEach(respondent ->
                        collectMatchingPartyId(respondent, masterDefendantIds, matchingRespondentIds));
            }
        }

        if (!matchingApplicantAndSubjectIds.isEmpty() || !matchingRespondentIds.isEmpty()) {
            final ApplicationCourtListRestriction existing = readExistingCourtApplicationRestriction(hearing);
            final ApplicationCourtListRestriction updated;
            if (restrictCourtList) {
                updated = ApplicationCourtListRestriction.applicationCourtListRestriction()
                        .withCourtApplicationIds(existing.getCourtApplicationIds())
                        .withCourtApplicationApplicantIds(combineLists(existing.getCourtApplicationApplicantIds(), matchingApplicantAndSubjectIds))
                        .withCourtApplicationRespondentIds(combineLists(existing.getCourtApplicationRespondentIds(), matchingRespondentIds))
                        .build();
            } else {
                updated = ApplicationCourtListRestriction.applicationCourtListRestriction()
                        .withCourtApplicationIds(existing.getCourtApplicationIds())
                        .withCourtApplicationApplicantIds(removeFromList(existing.getCourtApplicationApplicantIds(), matchingApplicantAndSubjectIds))
                        .withCourtApplicationRespondentIds(removeFromList(existing.getCourtApplicationRespondentIds(), matchingRespondentIds))
                        .build();
            }
            hearing.setRestrictCourtListJson(mapper.writeValueAsString(updated));
        }
    }

    private void collectMatchingPartyId(final CourtApplicationParty party, final Set<UUID> masterDefendantIds, final List<UUID> collector) {
        if (nonNull(party) && nonNull(party.getMasterDefendant())
                && masterDefendantIds.contains(party.getMasterDefendant().getMasterDefendantId())) {
            collector.add(party.getId());
        }
    }

    private List<CourtApplication> parseCourtApplications(final String json) throws IOException {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        final CourtApplicationsHolder holder = mapper.readValue(json, CourtApplicationsHolder.class);
        return holder.courtApplications != null ? holder.courtApplications : Collections.emptyList();
    }

    private static class CourtApplicationsHolder {
        @JsonProperty("courtApplications")
        public List<CourtApplication> courtApplications;
    }

    private void applyCourtApplicationRestrictions(final CourtListRestricted courtListRestricted, final Hearing hearing) throws IOException {
        final ApplicationCourtListRestriction existingApplicationCourtListRestriction = readExistingCourtApplicationRestriction(hearing);

        final ApplicationCourtListRestriction applicationCourtListRestriction;

        if (courtListRestricted.getRestrictCourtList()) {
            applicationCourtListRestriction = ApplicationCourtListRestriction.applicationCourtListRestriction()
                    .withCourtApplicationIds(combineLists(existingApplicationCourtListRestriction.getCourtApplicationIds(), courtListRestricted.getCourtApplicationIds()))
                    .withCourtApplicationApplicantIds(combineLists(existingApplicationCourtListRestriction.getCourtApplicationApplicantIds(), courtListRestricted.getCourtApplicationApplicantIds()))
                    .withCourtApplicationRespondentIds(combineLists(existingApplicationCourtListRestriction.getCourtApplicationRespondentIds(), courtListRestricted.getCourtApplicationRespondentIds()))
                    .withCourtApplicationSubjectIds(combineLists(existingApplicationCourtListRestriction.getCourtApplicationSubjectIds(), courtListRestricted.getCourtApplicationSubjectIds()))
                    .build();
        } else {
            applicationCourtListRestriction = ApplicationCourtListRestriction.applicationCourtListRestriction()
                    .withCourtApplicationIds(removeFromList(existingApplicationCourtListRestriction.getCourtApplicationIds(), courtListRestricted.getCourtApplicationIds()))
                    .withCourtApplicationApplicantIds(removeFromList(existingApplicationCourtListRestriction.getCourtApplicationApplicantIds(), courtListRestricted.getCourtApplicationApplicantIds()))
                    .withCourtApplicationRespondentIds(removeFromList(existingApplicationCourtListRestriction.getCourtApplicationRespondentIds(), courtListRestricted.getCourtApplicationRespondentIds()))
                    .withCourtApplicationSubjectIds(removeFromList(existingApplicationCourtListRestriction.getCourtApplicationSubjectIds(), courtListRestricted.getCourtApplicationSubjectIds()))
                    .build();
        }

        final String restrictCourtListJson = mapper.writeValueAsString(applicationCourtListRestriction);
        hearing.setRestrictCourtListJson(restrictCourtListJson);
    }

    private ApplicationCourtListRestriction readExistingCourtApplicationRestriction(final Hearing hearing) throws IOException {
        if (nonNull(hearing.getRestrictCourtListJson())) {
            return mapper.readValue(hearing.getRestrictCourtListJson(), ApplicationCourtListRestriction.class);
        }
        return ApplicationCourtListRestriction.applicationCourtListRestriction().build();
    }

    private boolean hasCaseOrDefendantRestriction(final CourtListRestricted courtListRestricted) {
        return isNotEmpty(courtListRestricted.getCaseIds()) ||
                isNotEmpty(courtListRestricted.getDefendantIds());
    }

    private boolean hasApplicationRestriction(final CourtListRestricted courtListRestricted) {
        return isNotEmpty(courtListRestricted.getCourtApplicationIds()) ||
                isNotEmpty(courtListRestricted.getCourtApplicationApplicantIds()) ||
                isNotEmpty(courtListRestricted.getCourtApplicationRespondentIds()) ||
                isNotEmpty(courtListRestricted.getCourtApplicationSubjectIds());
    }

    private List<UUID> combineLists(final List<UUID> existing, final List<UUID> toBeAdded) {
        final Set<UUID> result = new HashSet<>();

        if (nonNull(existing)) {
            result.addAll(existing);
        }

        if (nonNull(toBeAdded)) {
            result.addAll(toBeAdded);
        }

        return new ArrayList<>(result);
    }

    private List<UUID> removeFromList(final List<UUID> existing, final List<UUID> toBeRemoved) {
        final Set<UUID> result = new HashSet<>();

        if (nonNull(existing)) {
            result.addAll(existing);
        }

        if (nonNull(toBeRemoved)) {
            toBeRemoved.forEach(result::remove);
        }

        return new ArrayList<>(result);
    }

    private void applyProsecutionCaseRestrictions(final Set<ProsecutionCase> prosecutionCases, final CourtListRestricted courtListRestricted, final Boolean restrictCourtList) {
        final Set<UUID> caseIdsToBeUpdated = toSet(courtListRestricted.getCaseIds());
        final Set<UUID> defendantIdsToBeUpdated = toSet(courtListRestricted.getDefendantIds());

        for (final ProsecutionCase prosecutionCase : prosecutionCases) {

            if (caseIdsToBeUpdated.contains(prosecutionCase.getId().getId())) {
                prosecutionCase.setCourtListRestricted(restrictCourtList);
            }

            if (!defendantIdsToBeUpdated.isEmpty()) {
                applyDefendantRestrictions(prosecutionCase.getDefendants(), defendantIdsToBeUpdated, restrictCourtList);
            }
        }

    }

    private void applyDefendantRestrictions(final Set<Defendant> defendants, final Set<UUID> defendantIdsToBeUpdated, Boolean restrictCourtList) {
        for (final Defendant defendant : defendants) {
            if (defendantIdsToBeUpdated.contains(defendant.getId().getId())) {
                defendant.setCourtListRestricted(restrictCourtList);
            }
        }
    }

    private Set<UUID> toSet(final List<UUID> uuidList) {
        return nonNull(uuidList) ? new HashSet<>(uuidList) : new HashSet<>();
    }
}
