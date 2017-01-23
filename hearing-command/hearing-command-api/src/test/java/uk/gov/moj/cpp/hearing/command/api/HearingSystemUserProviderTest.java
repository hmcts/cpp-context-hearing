package uk.gov.moj.cpp.hearing.command.api;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Test;


public class HearingSystemUserProviderTest {
    @Test
    public void getContextSystemUserId() throws Exception {
        HearingSystemUserProvider testObj = new HearingSystemUserProvider();

        assertThat(UUID.fromString("8959b8b5-92bd-4ada-96f4-7ac9d482671a"), is(testObj.getContextSystemUserId().get()));
    }

}