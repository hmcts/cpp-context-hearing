package uk.gov.moj.cpp.hearing.query.view.service.ctl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.query.view.service.ctl.model.CTLRemandStatus.CUSTODY_OR_REMANDED_INTO_CUSTODY;
import static uk.gov.moj.cpp.hearing.query.view.service.ctl.model.ModeOfTrial.INDICTABLE;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Plea;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Verdict;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("squid:S2187")
@ExtendWith(MockitoExtension.class)
public class CTLExpiryDateValidityCheckerTest {

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

        assertThat(checker.valid(offence, null), is(false));
    }

    @Test
    public void shouldReturnFalseWhenOffenceIsNull() {
        assertThat(checker.valid(null, CUSTODY_OR_REMANDED_INTO_CUSTODY), is(false));
    }

    @Test
    public void shouldReturnFalseWhenOffenceTypeIsNull() {
        assertThat(checker.valid(offence, CUSTODY_OR_REMANDED_INTO_CUSTODY), is(false));
    }

    @Test
    public void shouldReturnFalseWhenOffenceTypeIsNotSupported() {
        assertThat(checker.valid(offence, CUSTODY_OR_REMANDED_INTO_CUSTODY), is(false));
    }

    @Test
    public void shouldReturnFalseWhenThereIsConvictionDate() {
        assertThat(checker.valid(offence, CUSTODY_OR_REMANDED_INTO_CUSTODY), is(false));
    }

    @Test
    public void shouldReturnFalseWhenPleadedGuilty() {
        assertThat(checker.valid(offence, CUSTODY_OR_REMANDED_INTO_CUSTODY), is(false));
    }

    @Test
    public void shouldReturnFalseWhenFinalResults() {
        assertThat(checker.valid(offence, CUSTODY_OR_REMANDED_INTO_CUSTODY), is(false));
    }

    @Test
    public void shouldReturnFalseWhenVerdictIsAvailable() {
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