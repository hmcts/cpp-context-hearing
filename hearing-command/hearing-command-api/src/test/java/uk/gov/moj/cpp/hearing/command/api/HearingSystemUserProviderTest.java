package uk.gov.moj.cpp.hearing.command.api;

import static java.util.UUID.fromString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class HearingSystemUserProviderTest {

    @Test
    public void ShouldGetContextSystemUserId() {
        final HearingSystemUserProvider userProvider = new HearingSystemUserProvider();

        assertThat(userProvider.getContextSystemUserId().isPresent(), is(true));
        assertThat(userProvider.getContextSystemUserId().get(), is(fromString("8959b8b5-92bd-4ada-96f4-7ac9d482671a")));
    }

}