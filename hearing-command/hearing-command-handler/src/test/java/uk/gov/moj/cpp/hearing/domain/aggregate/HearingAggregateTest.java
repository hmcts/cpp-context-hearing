package uk.gov.moj.cpp.hearing.domain.aggregate;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Before;
import org.junit.Test;
import uk.gov.moj.cpp.hearing.domain.HearingTypeEnum;
import uk.gov.moj.cpp.hearing.domain.command.ListHearing;
import uk.gov.moj.cpp.hearing.domain.command.VacateHearing;
import uk.gov.moj.cpp.hearing.domain.event.HearingListed;
import uk.gov.moj.cpp.hearing.domain.event.HearingVacated;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.values;

public class HearingAggregateTest {

    private HearingAggregate hearingAggregate;

    @Before
    public void setUP() {
        hearingAggregate = new HearingAggregate();
    }

    @Test
    public void shouldNotListHearingForAlreadyListed() {
        // given
        UUID hearingId = randomUUID();
        // and
        HearingListed hearingListed = createHearingListed(hearingId);
        // and
        hearingAggregate.apply(hearingListed);
        // and
        ListHearing listHearing = createListHearing(hearingId);

        // when
        final Stream<Object> stream = hearingAggregate.listHearing(listHearing);

        // then
        assertThat(stream.findAny().isPresent(), is(false));
    }

    @Test
    public void shouldNotListHearingForAlreadyVacated() {
        // given
        UUID hearingId = randomUUID();
        // and
        HearingVacated hearingVacated = createHearingVacated(hearingId);
        // and
        hearingAggregate.apply(hearingVacated);
        // and
        ListHearing listHearing = createListHearing(hearingId);

        // when
        final Stream<Object> stream = hearingAggregate.listHearing(listHearing);

        // then
        assertThat(stream.findAny().isPresent(), is(false));
    }

    @Test
    public void shouldListHearingFromNoState() {
        // given
        UUID hearingId = randomUUID();
        // and
        ListHearing listHearing = createListHearing(hearingId);

        // when
        final Stream<Object> stream = hearingAggregate.listHearing(listHearing);


        // then
        Optional<Object> optional = stream.findFirst();
        assertThat(optional.isPresent(), is(true));
        // and
        HearingListed hearingListed = (HearingListed) optional.get();
        // and
        assertThat(hearingListed, isFrom(listHearing));
    }

    @Test
    public void shouldNotVacateHearingForNotListedHearing() {
        // given
        UUID hearingId = randomUUID();
        // and
        HearingVacated hearingVacated = createHearingVacated(hearingId);
        // and
        hearingAggregate.apply(hearingVacated);
        // and
        VacateHearing vacateHearing = createVacateHearing(hearingId);

        // when
        final Stream<Object> stream = hearingAggregate.vacateHearing(vacateHearing);

        // then
        assertThat(stream.findAny().isPresent(), is(false));
    }

    @Test
    public void shouldVacateHearingForListedHearing() {
        // given
        UUID hearingId = randomUUID();
        // and
        HearingListed hearingListed = createHearingListed(hearingId);
        // and
        hearingAggregate.apply(hearingListed);
        // and
        VacateHearing vacateHearing = createVacateHearing(hearingId);

        // when
        final Stream<Object> stream = hearingAggregate.vacateHearing(vacateHearing);

        // then
        Optional<Object> optional = stream.findFirst();
        assertThat(optional.isPresent(), is(true));
        // and
        HearingVacated hearingVacated = (HearingVacated) optional.get();
        // and
        assertThat(hearingVacated, isFrom(vacateHearing));

    }
    private VacateHearing createVacateHearing(UUID hearingId) {
        return new VacateHearing(hearingId);
    }

    private HearingVacated createHearingVacated(UUID hearingId) {
        return new HearingVacated(hearingId);
    }

    private HearingListed createHearingListed(UUID hearingId) {
        UUID caseId = randomUUID();
        HearingTypeEnum hearingType = values(HearingTypeEnum.values()).next();
        String courtCentreName = STRING.next();
        LocalDate startDateOfHearing = PAST_LOCAL_DATE.next();
        Integer duration = INTEGER.next();
        return new HearingListed(hearingId, caseId, hearingType, courtCentreName, startDateOfHearing, duration);
    }

    private ListHearing createListHearing(UUID hearingId) {
        UUID caseId = randomUUID();
        HearingTypeEnum hearingType = values(HearingTypeEnum.values()).next();
        String courtCentreName = STRING.next();
        LocalDate startDateOfHearing = PAST_LOCAL_DATE.next();
        Integer duration = INTEGER.next();
        return new ListHearing(hearingId, caseId, hearingType, courtCentreName, startDateOfHearing, duration);
    }


    private Matcher<HearingListed> isFrom(final ListHearing listHearing) {
        return new TypeSafeDiagnosingMatcher<HearingListed>() {

            @Override
            public void describeTo(Description description) {
                description.appendText(listHearing.toString());
            }

            @Override
            protected boolean matchesSafely(HearingListed hearingListed, Description description) {
                boolean returnStatus = true;

                if (!Objects.equals(listHearing.getHearingId(), hearingListed.getHearingId())) {
                    description.appendText(format("HearingId Mismatch:listHearing:%s, hearingListed%s",
                            listHearing.getHearingId(), hearingListed.getHearingId()));
                    returnStatus = false;
                }

                if (!Objects.equals(listHearing.getCaseId(), hearingListed.getCaseId())) {
                    description.appendText(format("CaseId Mismatch:listHearing:%s, hearingListed%s",
                            listHearing.getCaseId(), hearingListed.getCaseId()));
                    returnStatus = false;
                }

                if (!Objects.equals(listHearing.getCourtCentreName(), hearingListed.getCourtCentreName())) {
                    description.appendText(format("CourtCentreName Mismatch:listHearing:%s, hearingListed%s",
                            listHearing.getCourtCentreName(), hearingListed.getCourtCentreName()));
                    returnStatus = false;
                }

                if (!Objects.equals(listHearing.getDuration(), hearingListed.getDuration())) {
                    description.appendText(format("Duration Mismatch:listHearing:%s, hearingListed%s",
                            listHearing.getDuration(), hearingListed.getDuration()));
                    returnStatus = false;
                }


                if (!Objects.equals(listHearing.getHearingType(), hearingListed.getHearingType())) {
                    description.appendText(format("HearingType Mismatch:listHearing:%s, hearingListed%s",
                            listHearing.getHearingType(), hearingListed.getHearingType()));
                    returnStatus = false;
                }

                if (!Objects.equals(listHearing.getStartDateOfHearing(), hearingListed.getStartDateOfHearing())) {
                    description.appendText(format("StartDateOfHearing Mismatch:listHearing:%s, hearingListed%s",
                            listHearing.getStartDateOfHearing(), hearingListed.getStartDateOfHearing()));
                    returnStatus = false;
                }

                return returnStatus;
            }
        };
    }

    private Matcher<HearingVacated> isFrom(final VacateHearing vacateHearing) {
        return new TypeSafeDiagnosingMatcher<HearingVacated>() {

            @Override
            public void describeTo(Description description) {
                description.appendText(vacateHearing.toString());
            }

            @Override
            protected boolean matchesSafely(HearingVacated hearingVacated, Description description) {
                boolean returnStatus = true;

                if (!Objects.equals(vacateHearing.getHearingId(), hearingVacated.getHearingId())) {
                    description.appendText(format("HearingId Mismatch:vacateHearing:%s, hearingVacated%s",
                            vacateHearing.getHearingId(), hearingVacated.getHearingId()));
                    returnStatus = false;
                }

                return returnStatus;
            }
        };
    }
}