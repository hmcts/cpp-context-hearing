package uk.gov.moj.cpp.hearing.query.api;

import static java.util.UUID.fromString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.UUID;

import org.junit.Test;

public class HearingSystemUserProviderTest {

    private static final UUID SYSTEM_USER_ID = fromString("8959b8b5-92bd-4ada-96f4-7ac9d482671a");

    @Test
    public void ShouldGetContextSystemUserId() {
        final HearingSystemUserProvider userProvider = new HearingSystemUserProvider();

        assertThat(userProvider.getContextSystemUserId().isPresent(), is(true));
        assertThat(userProvider.getContextSystemUserId().get(), is(SYSTEM_USER_ID));
    }

}