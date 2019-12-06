package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.justice.hearing.courts.JurisdictionType.CROWN;
import static uk.gov.moj.cpp.JudicialRoleTypeEnum.CIRCUIT_JUDGE;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail.caseDetail;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Cases.cases;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Court.court;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom.courtRoom;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtSite.courtSite;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus.currentCourtStatus;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Defendant.defendant;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Cases;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Court;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtSite;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Defendant;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


//TODO: crete test
public class HearingListXhibitResponseTransformer {

    public CurrentCourtStatus transformFrom(final List<uk.gov.justice.core.courts.Hearing> hearing) {
        return currentCourtStatus()
                .withCourt(getCourt(hearing))
                .build();
    }

    private Court getCourt(final List<Hearing> hearingList) {
        return court()
                //Logically all hearings will belong to single court centre, therefore we pick up the first one
                .withCourtName(hearingList.get(0).getCourtCentre().getName())
                .withCourtSites(getCourtSites(hearingList))
                .build();
    }

    private List<CourtSite> getCourtSites(final List<Hearing> hearingList) {
        return hearingList.stream().map(hearing -> courtSite()
                .withCourtSiteName(hearing.getCourtCentre().getName())
                .withCourtRooms(getCourtRooms(hearingList))
                .build())
                .collect(toList());
    }

    private List<CourtRoom> getCourtRooms(final List<Hearing> hearingList) {
        return hearingList.stream().map(hearing -> courtRoom()
                .withCourtRoomName(hearing.getCourtCentre().getRoomName())
                .withCases(getCases(hearing))
                .build())
                .collect(toList());
    }

    private Cases getCases(final Hearing hearing) {
        final List<CaseDetail> caseDetailsList = new ArrayList<>();

        hearing.getProsecutionCases()
                .forEach(prosecutionCase ->
                        caseDetailsList.add(caseDetail()
                                .withCppUrn(prosecutionCase.getProsecutionCaseIdentifier().getCaseURN())
                                .withCaseNumber(prosecutionCase.getId().toString()) //TODO: 8 digit case number.  Only supplied by XHIBIT cases.
                                .withCaseType(CROWN.name()) //TODO: this is wrong --> Single character case type (e.g. A – Appeal, T – Trial, S – Sentence).  Only supplied by XHIBIT cases.
                                .withHearingType(hearing.getType().getDescription())
                                .withDefendants(getDefendants(prosecutionCase))
                                .withJudgeName(getJudgeName(hearing))
                                .build()
                        ));
        return cases().withCasesDetails(caseDetailsList).build();
    }

    private String getJudgeName(final Hearing hearing) {
        final Optional<JudicialRole> judicialRole = hearing
                .getJudiciary()
                .stream()
                .filter(hearingJudicialRole -> hearingJudicialRole.getJudicialRoleType().getJudiciaryType().equals(CIRCUIT_JUDGE.name()))
                .findFirst();

        if (judicialRole.isPresent()) {
            return judicialRole.get().getTitle().concat(" ").concat(judicialRole.get().getLastName());
        }
        return EMPTY;
    }

    private List<Defendant> getDefendants(final ProsecutionCase prosecutionCase) {
        return prosecutionCase.getDefendants()
                .stream()
                .map(prosecutionCaseDefendant -> defendant()
                        .withFirstName(prosecutionCaseDefendant.getPersonDefendant().getPersonDetails().getFirstName())
                        .withMiddleName(prosecutionCaseDefendant.getPersonDefendant().getPersonDetails().getMiddleName())
                        .withLastName(prosecutionCaseDefendant.getPersonDefendant().getPersonDetails().getLastName())
                        .build())
                .collect(toList());
    }
}