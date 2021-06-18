package uk.gov.moj.cpp.hearing.query.view.service.ctl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.query.view.service.ctl.model.CTLRemandStatus.CUSTODY_OR_REMANDED_INTO_CUSTODY;
import static uk.gov.moj.cpp.hearing.query.view.service.ctl.model.ModeOfTrial.INDICTABLE;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Plea;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Verdict;

import java.time.LocalDate;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings("squid:S2187")
@RunWith(MockitoJUnitRunner.class)
public class CTLExpiryDateValidityCheckerTest extends TestCase {

    @Mock
    private Verdict verdict;

    @Mock
    private Plea plea;

    @Mock
    private Offence offence;

    @InjectMocks
    private CTLExpiryDateValidityChecker checker;

    @Test
    public void shouldReturnFalseWhenNotAValidRemandStatus() {
        when(offence.getOffenceCode()).thenReturn(INDICTABLE.type());

        assertThat(checker.valid(offence, null), is(false));
    }

    @Test
    public void shouldReturnFalseWhenOffenceIsNull() {
        assertThat(checker.valid(null, CUSTODY_OR_REMANDED_INTO_CUSTODY), is(false));
    }

    @Test
    public void shouldReturnFalseWhenOffenceTypeIsNull() {
        when(offence.getOffenceCode()).thenReturn(null);

        assertThat(checker.valid(offence, CUSTODY_OR_REMANDED_INTO_CUSTODY), is(false));
    }

    @Test
    public void shouldReturnFalseWhenOffenceTypeIsNotSupported() {
        when(offence.getOffenceCode()).thenReturn("X");

        assertThat(checker.valid(offence, CUSTODY_OR_REMANDED_INTO_CUSTODY), is(false));
    }

    @Test
    public void shouldReturnFalseWhenThereIsConvictionDate() {
        when(offence.getOffenceCode()).thenReturn(INDICTABLE.type());
        when(offence.getConvictionDate()).thenReturn(LocalDate.now());
        when(offence.getPlea()).thenReturn(plea);
        when(plea.getPleaValue()).thenReturn("NOT_GUILTY");

        assertThat(checker.valid(offence, CUSTODY_OR_REMANDED_INTO_CUSTODY), is(false));
    }

    @Test
    public void shouldReturnFalseWhenPleadedGuilty() {
        when(offence.getOffenceCode()).thenReturn(INDICTABLE.type());
        when(offence.getConvictionDate()).thenReturn(null);
        when(offence.getPlea()).thenReturn(plea);
        when(plea.getPleaValue()).thenReturn("NOT_GUILTY");
        when(offence.isProceedingsConcluded()).thenReturn(true);

        assertThat(checker.valid(offence, CUSTODY_OR_REMANDED_INTO_CUSTODY), is(false));
    }

    @Test
    public void shouldReturnFalseWhenFinalResults() {
        when(offence.getOffenceCode()).thenReturn(INDICTABLE.type());
        when(offence.getConvictionDate()).thenReturn(null);
        when(offence.getPlea()).thenReturn(plea);
        when(plea.getPleaValue()).thenReturn("NOT_GUILTY");
        when(offence.getVerdict()).thenReturn(null);
        when(offence.isProceedingsConcluded()).thenReturn(true);

        assertThat(checker.valid(offence, CUSTODY_OR_REMANDED_INTO_CUSTODY), is(false));
    }

    @Test
    public void shouldReturnFalseWhenVerdictIsAvailable() {
        when(offence.getOffenceCode()).thenReturn(INDICTABLE.type());
        when(offence.getConvictionDate()).thenReturn(null);
        when(offence.getPlea()).thenReturn(plea);
        when(plea.getPleaValue()).thenReturn("GUILTY");
        when(offence.getVerdict()).thenReturn(verdict);

        assertThat(checker.valid(offence, CUSTODY_OR_REMANDED_INTO_CUSTODY), is(false));
    }

    @Test
    public void shouldReturnTrueWhenNoFinalResultNoVerdictNoConvictionDateNoGuiltyPleaCTLOffenceTypeAndRemandStatus() {
        when(offence.getModeOfTrial()).thenReturn(INDICTABLE.toString());
        when(offence.getConvictionDate()).thenReturn(null);
        when(offence.getPlea()).thenReturn(plea);
        when(plea.getPleaValue()).thenReturn("NOT_GUILTY");
        when(offence.getVerdict()).thenReturn(null);
        when(offence.isProceedingsConcluded()).thenReturn(false);

        assertThat(checker.valid(offence, CUSTODY_OR_REMANDED_INTO_CUSTODY), is(true));
    }
}