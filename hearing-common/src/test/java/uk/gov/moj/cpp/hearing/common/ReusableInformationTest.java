package uk.gov.moj.cpp.hearing.common;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ReusableInformationTest {

    @InjectMocks
    private ReusableInformation reusableInformation;


    @Test
    public void shouldHaveCachableInformation() {
        final String value = "ED9 12X";
        final UUID defendantId = randomUUID();
        final String promptRef = STRING.next();

        final ReusableInformation<String> reusableInformation = new ReusableInformation.Builder<String>()
                .withValue(value)
                .withMasterDefendantId(defendantId)
                .withPromptRef(promptRef)
                .build();

        assertThat(reusableInformation.getValue(), is(value));
        assertThat(reusableInformation.getMasterDefendantId(), is(defendantId));
        assertThat(reusableInformation.getPromptRef(), is(promptRef));
    }

    @Test
    public void shouldHaveCachableInformationIfInteger() {
        final Integer value = 123;
        final UUID defendantId = randomUUID();
        final String promptRef = STRING.next();

        final ReusableInformation<Integer> reusableInformation = new ReusableInformation.Builder<Integer>()
                .withValue(value)
                .withMasterDefendantId(defendantId)
                .withPromptRef(promptRef)
                .build();

        assertThat(reusableInformation.getValue(), is(value));
        assertThat(reusableInformation.getMasterDefendantId(), is(defendantId));
        assertThat(reusableInformation.getPromptRef(), is(promptRef));
    }

    @Test
    public void shouldHaveCachableInformationIfObjectType() {
        final AddressReusableInfo addressReusableInfo = new AddressReusableInfo();
        addressReusableInfo.setParentguardiansaddressAddress1("address1");
        addressReusableInfo.setParentguardiansaddressEmailAddress1("email@example.com");
        addressReusableInfo.setParentguardiansaddressPostCode("ED9 12X");
        addressReusableInfo.setPhoneNumber(INTEGER.next());

        final UUID defendantId = randomUUID();
        final String promptRef = STRING.next();

        final ReusableInformation<AddressReusableInfo> reusableInformation = new ReusableInformation.Builder<AddressReusableInfo>()
                .withValue(addressReusableInfo)
                .withMasterDefendantId(defendantId)
                .withPromptRef(promptRef)
                .build();

        assertThat(reusableInformation.getValue(), is(addressReusableInfo));
        assertThat(reusableInformation.getMasterDefendantId(), is(defendantId));
        assertThat(reusableInformation.getPromptRef(), is(promptRef));
    }
}
