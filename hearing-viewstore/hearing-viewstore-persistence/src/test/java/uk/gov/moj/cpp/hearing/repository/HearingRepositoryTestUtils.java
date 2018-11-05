/**
 * 
 */
package uk.gov.moj.cpp.hearing.repository;

import static java.time.ZonedDateTime.parse;
import static java.util.UUID.fromString;
import static uk.gov.moj.cpp.hearing.persist.entity.ha.HearingTest.buildHearing1;
import static uk.gov.moj.cpp.hearing.persist.entity.ha.DefenceAdvocateTest.buildDefenseAdvocate1;
import static uk.gov.moj.cpp.hearing.persist.entity.ha.DefenceAdvocateTest.buildDefenseAdvocate2;
import static uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantTest.buildDefendant1;
import static uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantTest.buildDefendant2;
import static uk.gov.moj.cpp.hearing.persist.entity.ha.JudgeTest.buildJudge;
import static uk.gov.moj.cpp.hearing.persist.entity.ha.LegalCaseTest.buildLegalCase1;
import static uk.gov.moj.cpp.hearing.persist.entity.ha.OffenceTest.buildOffence1;
import static uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionAdvocateTest.buildProsecutionAdvocate;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Judge;
import uk.gov.moj.cpp.hearing.persist.entity.ha.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionAdvocate;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 */
final class HearingRepositoryTestUtils {

    public static final UUID HEARING_ID_1 = fromString("23ef34ec-63e5-422e-8071-9b3753008c10");
    public static final ZonedDateTime START_DATE_1 = parse("2018-02-22T10:30:00Z");

    /**
     * 
     */
    private HearingRepositoryTestUtils() {
    }

    static Hearing buildHearing() {
        return buildHearing(buildLegalCase1());
    }

    static Hearing buildHearing(final LegalCase legalCase) {
        // 1. create the hearing object and set the attendees
        final Hearing hearing = buildHearing1(HearingRepositoryTestUtils.HEARING_ID_1, HearingRepositoryTestUtils.START_DATE_1);
        final ProsecutionAdvocate prosecutionAdvocate = buildProsecutionAdvocate(hearing);
        final DefenceAdvocate defenseAdvocate1 = buildDefenseAdvocate1(hearing);
        final DefenceAdvocate defenseAdvocate2 = buildDefenseAdvocate2(hearing);
        final Judge judge = buildJudge(hearing);
        hearing.setAttendees(Arrays.asList(judge, prosecutionAdvocate, defenseAdvocate1, defenseAdvocate2));
    
        // 2. create the defendants objects and set to hearing
        final Defendant defendant1 = buildDefendant1(hearing, defenseAdvocate1, defenseAdvocate2);
        final Defendant defendant2 = buildDefendant2(hearing, defenseAdvocate1, defenseAdvocate2);
        final List<Defendant> defendants = Arrays.asList(defendant1, defendant2);
        hearing.setDefendants(defendants);
        
        // 3. link the defendants to to the defenseAdvocates
        defenseAdvocate1.setDefendants(defendants);
        defenseAdvocate2.setDefendants(defendants);
    
        // 4. create the legal case, offence and set the offence to the existent defendants
        final Offence offence1 = buildOffence1(hearing, defendant1, legalCase);
        defendant1.setOffences(Arrays.asList(offence1));
        defendant2.setOffences(Arrays.asList(offence1));

        return hearing;
    }

}
