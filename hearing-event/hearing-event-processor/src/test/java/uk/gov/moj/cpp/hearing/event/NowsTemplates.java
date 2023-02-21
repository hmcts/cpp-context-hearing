package uk.gov.moj.cpp.hearing.event;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingWithApplicationTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.VariantDirectoryTemplates.standardVariantTemplate;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtOrder;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.Target2;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;

public class NowsTemplates {

    private NowsTemplates() {
    }

    public static ResultsShared resultsSharedTemplate() {

        InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());

        return buildrResultsSharedTemplate(hearingOne);
    }

    public static ResultsShared resultsSharedTemplateWithCourtApplicationCases() {

        final List<CourtApplication> applications = singletonList(CourtApplication.courtApplication()
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withMasterDefendantId(randomUUID())
                                .build())
                        .build())
                .withCourtApplicationCases(singletonList(CourtApplicationCase.courtApplicationCase()
                        .withOffences(singletonList(Offence.offence().build()))
                        .withCaseStatus("ACTIVE")
                        .build()))
                .build());
        InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingWithApplicationTemplate(applications));

        return buildrResultsSharedTemplate(hearingOne);
    }

    public static ResultsShared resultsSharedTemplateWithCourtApplicationCourtOrder() {

        final List<CourtApplication> applications = singletonList(CourtApplication.courtApplication()
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withMasterDefendantId(randomUUID())
                                .build())
                        .build())
                .withCourtOrder(CourtOrder.courtOrder()
                        .withCourtOrderOffences(singletonList(CourtOrderOffence.courtOrderOffence()
                                .withOffence(Offence.offence().build()).build())).build())
                .build());
        InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingWithApplicationTemplate(applications));

        return buildrResultsSharedTemplate(hearingOne);
    }

    private static ResultsShared buildrResultsSharedTemplate(InitiateHearingCommandHelper hearingOne) {

        UUID completedResultLineId = randomUUID();

        final List<Target> targets;
        final List<Variant> variantDirectory;
        if(hearingOne.getHearing().getProsecutionCases() != null){
            targets = new ArrayList<>(asList(
                    CoreTestTemplates.target(hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId(), hearingOne.getFirstOffenceIdForFirstDefendant(), completedResultLineId).build()
            ));
            variantDirectory = singletonList(
                    standardVariantTemplate(randomUUID(), hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId()));
        }else if (hearingOne.getHearing().getCourtApplications() != null ){
            if(hearingOne.getHearing().getCourtApplications().get(0).getCourtApplicationCases() != null) {
                targets = new ArrayList<>(asList(
                        CoreTestTemplates.target(hearingOne.getHearingId(), hearingOne.getMasterDefandantIdofFirstSubject(), hearingOne.getFirstOffenceForFirstFirstCaseForFirstApplication().getId(), completedResultLineId).build()
                ));
            }else{
                targets = new ArrayList<>(asList(
                        CoreTestTemplates.target(hearingOne.getHearingId(), hearingOne.getMasterDefandantIdofFirstSubject(), hearingOne.getFirstOffenceForFirstFirstCourtOrderForFirstApplication().getId(), completedResultLineId).build()
                ));
            }
            variantDirectory = singletonList(
                    standardVariantTemplate(randomUUID(), hearingOne.getHearingId(), hearingOne.getMasterDefandantIdofFirstSubject()));
        }else{
            targets = null;
            variantDirectory = null;
        }

        return ResultsShared.builder()
                .withHearingId(hearingOne.getHearingId())
                .withTargets(targets)
                .withSharedTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                .withHearing(hearingOne.getHearing())
                .withCourtClerk(DelegatedPowers.delegatedPowers()
                        .withUserId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .build())
                .withCompletedResultLinesStatus(ImmutableMap.of(completedResultLineId, CompletedResultLineStatus.builder()
                        .withCourtClerk(DelegatedPowers.delegatedPowers()
                                .withUserId(randomUUID())
                                .withFirstName(STRING.next())
                                .withLastName(STRING.next())
                                .build())
                        .withId(completedResultLineId)
                        .withLastSharedDateTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                        .build()
                ))
                .withVariantDirectory(variantDirectory)
                .build();
    }

    public static ResultsSharedV2 resultsSharedV2Template() {

        InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());

        UUID completedResultLineId = randomUUID();

        final List<Target> targets = new ArrayList<>(asList(
                CoreTestTemplates.target(hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId(), hearingOne.getFirstOffenceIdForFirstDefendant(), completedResultLineId).build()
        ));

        return ResultsSharedV2.builder()
                .withHearingId(hearingOne.getHearingId())
                .withTargets(targets)
                .withSharedTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                .withHearing(hearingOne.getHearing())
                .withCourtClerk(DelegatedPowers.delegatedPowers()
                        .withUserId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .build())
                .withCompletedResultLinesStatus(ImmutableMap.of(completedResultLineId, CompletedResultLineStatus.builder()
                        .withCourtClerk(DelegatedPowers.delegatedPowers()
                                .withUserId(randomUUID())
                                .withFirstName(STRING.next())
                                .withLastName(STRING.next())
                                .build())
                        .withId(completedResultLineId)
                        .withLastSharedDateTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                        .build()
                ))
                .withVariantDirectory(singletonList(
                        standardVariantTemplate(randomUUID(), hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId())
                ))
                .withHearingDay(LocalDate.now())
                .build();
    }

    public static ResultsSharedV3 resultsSharedV3Template() {

        InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());

        UUID completedResultLineId = randomUUID();

        final List<Target2> targets = new ArrayList<>(asList(
                CoreTestTemplates.target2(hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId(), hearingOne.getFirstOffenceIdForFirstDefendant(), completedResultLineId).build()
        ));

        return ResultsSharedV3.builder()
                .withHearingId(hearingOne.getHearingId())
                .withTargets(targets)
                .withSharedTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                .withHearing(hearingOne.getHearing())
                .withCourtClerk(DelegatedPowers.delegatedPowers()
                        .withUserId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .build())
                .withCompletedResultLinesStatus(ImmutableMap.of(completedResultLineId, CompletedResultLineStatus.builder()
                        .withCourtClerk(DelegatedPowers.delegatedPowers()
                                .withUserId(randomUUID())
                                .withFirstName(STRING.next())
                                .withLastName(STRING.next())
                                .build())
                        .withId(completedResultLineId)
                        .withLastSharedDateTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                        .build()
                ))
                .withVariantDirectory(singletonList(
                        standardVariantTemplate(randomUUID(), hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId())
                ))
                .withHearingDay(LocalDate.now())
                .build();
    }


    public static ResultsShared resultsSharedTemplateForDocumentIsDeleted() {

        InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());

        UUID completedResultLineId = randomUUID();

        final List<Target> targets = new ArrayList<>(asList(
                CoreTestTemplates.targetForDocumentIsDeleted(hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId(), hearingOne.getFirstOffenceIdForFirstDefendant(), completedResultLineId).build()
        ));

        return ResultsShared.builder()
                .withHearingId(hearingOne.getHearingId())
                .withTargets(targets)
                .withSharedTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                .withHearing(hearingOne.getHearing())
                .withCourtClerk(DelegatedPowers.delegatedPowers()
                        .withUserId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .build())
                .withCompletedResultLinesStatus(ImmutableMap.of(completedResultLineId, CompletedResultLineStatus.builder()
                        .withCourtClerk(DelegatedPowers.delegatedPowers()
                                .withUserId(randomUUID())
                                .withFirstName(STRING.next())
                                .withLastName(STRING.next())
                                .build())
                        .withId(completedResultLineId)
                        .withLastSharedDateTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                        .build()
                ))
                .withVariantDirectory(singletonList(
                        standardVariantTemplate(randomUUID(), hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId())
                ))
                .build();
    }

    public static ResultsShared resultsSharedTemplateForSendingResultSharedForOffence(final UUID resultDefinitionId) {
        InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());


        UUID completedResultLineId = randomUUID();

        final List<Target> targets = asList(
                CoreTestTemplates.targetForOffenceResultShared(hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId(), hearingOne.getFirstOffenceIdForFirstDefendant(), completedResultLineId, resultDefinitionId).build()
        );

        return ResultsShared.builder()
                .withHearingId(hearingOne.getHearingId())
                .withTargets(targets)
                .withSharedTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                .withHearing(hearingOne.getHearing())
                .withCourtClerk(DelegatedPowers.delegatedPowers()
                        .withUserId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .build())
                .withCompletedResultLinesStatus(ImmutableMap.of(completedResultLineId, CompletedResultLineStatus.builder()
                        .withCourtClerk(DelegatedPowers.delegatedPowers()
                                .withUserId(randomUUID())
                                .withFirstName(STRING.next())
                                .withLastName(STRING.next())
                                .build())
                        .withId(completedResultLineId)
                        .withLastSharedDateTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                        .build()
                ))
                .withVariantDirectory(singletonList(
                        standardVariantTemplate(randomUUID(), hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId())
                ))
                .build();
    }
}
