/**
 * 
 */
package uk.gov.moj.cpp.hearing.repository;

import static java.time.ZonedDateTime.parse;
import static java.util.UUID.fromString;
import static uk.gov.moj.cpp.hearing.persist.entity.ex.AhearingTest.buildHering1;
import static uk.gov.moj.cpp.hearing.persist.entity.ex.DefenceAdvocateTest.buildDefenseAdvocate1;
import static uk.gov.moj.cpp.hearing.persist.entity.ex.DefenceAdvocateTest.buildDefenseAdvocate2;
import static uk.gov.moj.cpp.hearing.persist.entity.ex.DefendantTest.buildDefendant1;
import static uk.gov.moj.cpp.hearing.persist.entity.ex.DefendantTest.buildDefendant2;
import static uk.gov.moj.cpp.hearing.persist.entity.ex.JudgeTest.buildJudge;
import static uk.gov.moj.cpp.hearing.persist.entity.ex.OffenceTest.buildOffence1;
import static uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCaseTest.buildLegalCase1;
import static uk.gov.moj.cpp.hearing.persist.entity.ex.ProsecutionAdvocateTest.buildProsecutionAdvocate;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Judge;
import uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ex.ProsecutionAdvocate;

/**
 */
final class AhearingRepositoryTestUtils {

    public static final UUID HEARING_ID_1 = fromString("23ef34ec-63e5-422e-8071-9b3753008c10");
    public static final ZonedDateTime START_DATE_1 = parse("2018-02-22T10:30:00Z");

    /**
     * 
     */
    private AhearingRepositoryTestUtils() {
    }

    static Ahearing buildHearing() {
        return buildHearing(buildLegalCase1());
    }

    static Ahearing buildHearing(final LegalCase legalCase) {
        // 1. create the hearing object and set the attendees
        final Ahearing ahearing = buildHering1(AhearingRepositoryTestUtils.HEARING_ID_1, AhearingRepositoryTestUtils.START_DATE_1);
        final ProsecutionAdvocate prosecutionAdvocate = buildProsecutionAdvocate(ahearing);
        final DefenceAdvocate defenseAdvocate1 = buildDefenseAdvocate1(ahearing);
        final DefenceAdvocate defenseAdvocate2 = buildDefenseAdvocate2(ahearing);
        final Judge judge = buildJudge(ahearing);
        ahearing.setAttendees(Arrays.asList(judge, prosecutionAdvocate, defenseAdvocate1, defenseAdvocate2));
    
        // 2. create the defendants objects and set to hearing
        final Defendant defendant1 = buildDefendant1(ahearing, defenseAdvocate1, defenseAdvocate2);
        final Defendant defendant2 = buildDefendant2(ahearing, defenseAdvocate1, defenseAdvocate2);
        final List<Defendant> defendants = Arrays.asList(defendant1, defendant2);
        ahearing.setDefendants(defendants);
        
        // 3. link the defendants to to the defenseAdvocates
        defenseAdvocate1.setDefendants(defendants);
        defenseAdvocate2.setDefendants(defendants);
    
        // 4. create the legal case, offence and set the offence to the existent defendants
        final Offence offence1 = buildOffence1(ahearing, defendant1, legalCase);
        defendant1.setOffences(Arrays.asList(offence1));
        defendant2.setOffences(Arrays.asList(offence1));
        
        return ahearing;
    }

}
