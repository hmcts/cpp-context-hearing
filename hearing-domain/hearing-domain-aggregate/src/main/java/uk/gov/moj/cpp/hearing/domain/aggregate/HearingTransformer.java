package uk.gov.moj.cpp.hearing.domain.aggregate;

import uk.gov.moj.cpp.hearing.domain.event.MagsCourtHearingRecorded;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Defendant;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Offence;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.PleaValue;


import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class HearingTransformer {

    public static final String DEFAULT_HEARING_TYPE="Magistrate Court Hearing";

    private Map<LocalDate, Map<Defendant, List<Offence>>  > extractPleaDate2DefendantOffence(Hearing originatingHearing) {
        final Map<LocalDate, Map<Defendant, List<Offence>>  > pleaDate2DefendantOffence = new HashMap<>();
        originatingHearing.getDefendants().forEach(
                defendants ->
                        defendants.getOffences().forEach(
                                offence -> {
                                    if (offence.getPlea() != null && offence.getPlea().getValue().equals(PleaValue.GUILTY)) {
                                        final LocalDate pleaDate = offence.getPlea().getPleaDate();
                                        if (!pleaDate2DefendantOffence.containsKey(pleaDate)) {
                                            pleaDate2DefendantOffence.put(pleaDate, new HashMap<>());
                                        }
                                        final Map<Defendant, List<Offence>> defendants4pleaDate = pleaDate2DefendantOffence.get(pleaDate);

                                        if (!defendants4pleaDate.containsKey(defendants)) {
                                            defendants4pleaDate.put(defendants, new ArrayList<>());
                                        }
                                        final List<Offence> offences4Defendant = defendants4pleaDate.get(defendants);
                                        offences4Defendant.add(offence);
                                    }
                                }
                        )
        );
        return pleaDate2DefendantOffence;
    }

    private MagsCourtHearingRecorded map(Map.Entry<LocalDate, Map<Defendant, List<Offence>>> entryT, Hearing originatingHearing) {
        final List<Defendant> defendants = new ArrayList<>();
        //clone each defendant,
        entryT.getValue().entrySet().forEach(
                entry->{
                    final Defendant defendant = entry.getKey();
                    final List<Offence> filteredOffences = entry.getValue();
                    final Defendant defendantClone = (new Defendant.Builder()).withAddress(defendant.getAddress()).withBailStatus(defendant.getBailStatus()).
                            withCustodyTimeLimitDate(defendant.getCustodyTimeLimitDate()).withDateOfBirth(defendant.getDateOfBirth()).withDefenceOrganisation(defendant.getDefenceOrganisation())
                            .withFirstName(defendant.getFirstName()).withGender(defendant.getGender()).withId(defendant.getId()).withInterpreter(defendant.getInterpreter()).withLastName(defendant.getLastName()).
                                    withNationality(defendant.getNationality()).withOffences(filteredOffences).withPersonId(defendant.getPersonId()).build();
                    defendants.add(defendantClone);
                }
        );
        //default the hearing type
        final String hearingType = originatingHearing.getType()==null || originatingHearing.getType().trim().length()==0 ? HearingTransformer.DEFAULT_HEARING_TYPE : originatingHearing.getType();
        final Hearing originatingHearingClone = new Hearing(originatingHearing.getCaseId(), originatingHearing.getCaseUrn(),
                originatingHearing.getCourtCentreId(), originatingHearing.getCourtCentreName(), defendants, originatingHearing.getSendingCommittalDate(),                                     hearingType);
        return new MagsCourtHearingRecorded(originatingHearingClone, entryT.getKey(), UUID.randomUUID());
    }

    public List<MagsCourtHearingRecorded> transform(final Hearing originatingHearing) {

        final Map<LocalDate, Map<Defendant, List<Offence>>  > pleaDate2DefendantOffence = extractPleaDate2DefendantOffence(originatingHearing);

        final List<MagsCourtHearingRecorded> result;

        result = pleaDate2DefendantOffence.entrySet().stream().map( entry-> map(entry, originatingHearing) ).collect(Collectors.toList());

        result.sort((e1, e2) -> e1.getConvictionDate().compareTo(e2.getConvictionDate()));
        return result;
    }

}
