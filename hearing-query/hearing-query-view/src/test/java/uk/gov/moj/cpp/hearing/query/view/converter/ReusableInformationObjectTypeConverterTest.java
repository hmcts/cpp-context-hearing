package uk.gov.moj.cpp.hearing.query.view.converter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.common.ReusableInformation.IdType.APPLICATION;
import static uk.gov.moj.cpp.hearing.common.ReusableInformation.IdType.DEFENDANT;
import static java.util.UUID.randomUUID;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.common.AddressReusableInformation;
import uk.gov.moj.cpp.hearing.common.NameAddressReusableInformation;
import uk.gov.moj.cpp.hearing.common.NameAddressReusableInformationForApplication;
import uk.gov.moj.cpp.hearing.common.ReusableInformation;
import uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationObjectTypeConverter;

import java.util.UUID;

import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReusableInformationObjectTypeConverterTest {

    @InjectMocks
    private ReusableInformationObjectTypeConverter reusableInformationObjectTypeConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @BeforeEach
    public void setUp() {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldConvertToNameAddress() {
        reusableInformationObjectTypeConverter = new ReusableInformationObjectTypeConverter();
        setField(this.reusableInformationObjectTypeConverter, "objectToJsonObjectConverter", objectToJsonObjectConverter);

        final UUID defendantId = UUID.randomUUID();
        final String promptRef = STRING.next();
        final String prisonOrganisationName = "organisation name";
        final String prisonOrganisationLastName = "organisation last name";
        final String prisonOrganisationAddress1 = "organisation addresss 1";
        final String prisonOrganisationAddress2 = "organisation addresss 2";
        final String prisonOrganisationEmailAddress1 = "test@example.com";
        final String prisonOrganisationPostCode = "E16 2DD";
        final Integer cacheable = 2;
        final String cacheDataPath = STRING.next();


        final NameAddressReusableInformation nameAddressReusableInformation = new NameAddressReusableInformation.Builder()
                .withPrisonOrganisationName(prisonOrganisationName)
                .withPrisonOrganisationLastName(prisonOrganisationLastName)
                .withPrisonOrganisationAddress1(prisonOrganisationAddress1)
                .withPrisonOrganisationAddress2(prisonOrganisationAddress2)
                .withPrisonOrganisationEmailAddress1(prisonOrganisationEmailAddress1)
                .withPrisonOrganisationPostCode(prisonOrganisationPostCode)
                .build();

        final ReusableInformation reusableInformation = new ReusableInformation.Builder<NameAddressReusableInformation>()
                .withValue(nameAddressReusableInformation)
                .withIdType(DEFENDANT)
                .withId(defendantId)
                .withPromptRef(promptRef)
                .withCacheable(cacheable)
                .withCacheDataPath(cacheDataPath)
                .build();

        final JsonObject jsonObject = reusableInformationObjectTypeConverter.toJsonObject(reusableInformation, ReusableInformationConverterType.NAMEADDRESS);

        assertNotNull(jsonObject);
        assertThat(jsonObject.getString("promptRef"), is(reusableInformation.getPromptRef()));
        assertThat(jsonObject.getString("masterDefendantId"), is(reusableInformation.getMasterDefendantId().toString()));
        assertThat(jsonObject.getString("type"), is(ReusableInformationConverterType.NAMEADDRESS.name()));

        final JsonObject addressJsonObject = jsonObject.getJsonObject("value");
        assertNotNull(addressJsonObject);

        assertThat(addressJsonObject.getString("prisonOrganisationName"), is(nameAddressReusableInformation.getPrisonOrganisationName()));
        assertThat(addressJsonObject.getString("prisonOrganisationLastName"), is(nameAddressReusableInformation.getPrisonOrganisationLastName()));
        assertThat(addressJsonObject.getString("prisonOrganisationAddress1"), is(nameAddressReusableInformation.getPrisonOrganisationAddress1()));
        assertThat(addressJsonObject.getString("prisonOrganisationAddress2"), is(nameAddressReusableInformation.getPrisonOrganisationAddress2()));
        assertThat(addressJsonObject.getString("prisonOrganisationPostCode"), is(nameAddressReusableInformation.getPrisonOrganisationPostCode()));
        assertThat(addressJsonObject.getString("prisonOrganisationEmailAddress1"), is(nameAddressReusableInformation.getPrisonOrganisationEmailAddress1()));

        assertNull(addressJsonObject.get("prisonOrganisationMiddleName"));
        assertNull(addressJsonObject.get("prisonOrganisationAddress3"));
        assertNull(addressJsonObject.get("prisonOrganisationAddress4"));
        assertNull(addressJsonObject.get("prisonOrganisationAddress5"));
        assertNull(addressJsonObject.get("prisonOrganisationEmailAddress2"));
    }

    @Test
    public void shouldConvertToAddress() {
        reusableInformationObjectTypeConverter = new ReusableInformationObjectTypeConverter();
        setField(this.reusableInformationObjectTypeConverter, "objectToJsonObjectConverter", objectToJsonObjectConverter);

        final UUID defendantId = UUID.randomUUID();
        final String promptRef = STRING.next();
        final String parentGuardiansAddress1 = "parent addresss 1";
        final String parentGuardiansAddress2 = "parent addresss 2";
        final String parentGuardiansAddressEmailAddress1 = "test@example.com";
        final String parentGuardiansAddressPostCode = "E16 2DD";
        final Integer cacheable = 2;
        final String cacheDataPath = STRING.next();

        final AddressReusableInformation addressReusableInformation = new AddressReusableInformation.Builder()
                .withParentguardiansaddressAddress1(parentGuardiansAddress1)
                .withParentguardiansaddressAddress2(parentGuardiansAddress2)
                .withParentguardiansaddressEmailAddress1(parentGuardiansAddressEmailAddress1)
                .withParentguardiansaddressPostCode(parentGuardiansAddressPostCode)
                .build();

        final ReusableInformation reusableInformation = new ReusableInformation.Builder<AddressReusableInformation>()
                .withValue(addressReusableInformation)
                .withIdType(DEFENDANT)
                .withId(defendantId)
                .withPromptRef(promptRef)
                .withCacheable(cacheable)
                .withCacheDataPath(cacheDataPath)
                .build();

        final JsonObject jsonObject = reusableInformationObjectTypeConverter.toJsonObject(reusableInformation, ReusableInformationConverterType.ADDRESS);

        assertNotNull(jsonObject);
        assertThat(jsonObject.getString("promptRef"), is(reusableInformation.getPromptRef()));
        assertThat(jsonObject.getString("masterDefendantId"), is(reusableInformation.getMasterDefendantId().toString()));
        assertThat(jsonObject.getString("type"), is(ReusableInformationConverterType.ADDRESS.name()));

        final JsonObject addressJsonObject = jsonObject.getJsonObject("value");
        assertNotNull(addressJsonObject);

        assertThat(addressJsonObject.getString("parentguardiansaddressAddress1"), is(addressReusableInformation.getParentguardiansaddressAddress1()));
        assertThat(addressJsonObject.getString("parentguardiansaddressAddress2"), is(addressReusableInformation.getParentguardiansaddressAddress2()));
        assertThat(addressJsonObject.getString("parentguardiansaddressEmailAddress1"), is(addressReusableInformation.getParentguardiansaddressEmailAddress1()));
        assertThat(addressJsonObject.getString("parentguardiansaddressPostCode"), is(addressReusableInformation.getParentguardiansaddressPostCode()));

        assertNull(addressJsonObject.get("parentguardiansaddressAddress3"));
        assertNull(addressJsonObject.get("parentguardiansaddressAddress4"));
        assertNull(addressJsonObject.get("parentguardiansaddressAddress5"));
        assertNull(addressJsonObject.get("parentguardiansaddressEmailAddress2"));
    }

    @Test
    public void shouldReturnNullObjectWhenInputIsNull() {
        final JsonObject jsonObject = reusableInformationObjectTypeConverter.toJsonObject(null, null);
        assertNull(jsonObject);

    }

    @Test
    public void shouldConvertToNameAddressForApplication() {
        reusableInformationObjectTypeConverter = new ReusableInformationObjectTypeConverter();
        setField(this.reusableInformationObjectTypeConverter, "objectToJsonObjectConverter", objectToJsonObjectConverter);

        final UUID applicationId = randomUUID();
        final String promptRef = "prosecutortobenotified";

        final NameAddressReusableInformationForApplication nameAddressReusableInformationForApplication = new NameAddressReusableInformationForApplication.Builder()
                .withOrganisationName(STRING.next())
                .withAddress1(STRING.next())
                .withAddress2(STRING.next())
                .withAddress3(STRING.next())
                .withAddress4(STRING.next())
                .withAddress5(STRING.next())
                .withPostCode("CB53XA")
                .withPrimaryEmail("Mark.Taylor@gmail.com")
                .withSecondaryEmail("Mark.Taylor@yahoo.com")
                .build();

        final ReusableInformation reusableInformation = new ReusableInformation.Builder<NameAddressReusableInformationForApplication>()
                .withValue(nameAddressReusableInformationForApplication)
                .withIdType(APPLICATION)
                .withId(applicationId)
                .withPromptRef(promptRef)
                .withCacheable(2)
                .withCacheDataPath(STRING.next())
                .build();

        final JsonObject jsonObject = reusableInformationObjectTypeConverter.toJsonObject(reusableInformation, ReusableInformationConverterType.NAMEADDRESS);

        assertNotNull(jsonObject);
        assertThat(jsonObject.getString("promptRef"), is(reusableInformation.getPromptRef()));
        assertThat(jsonObject.getString("type"), is(ReusableInformationConverterType.NAMEADDRESS.name()));

        final JsonObject nameAddressJsonObject = jsonObject.getJsonObject("value");
        assertNotNull(nameAddressJsonObject);

        assertThat(nameAddressJsonObject.getString("organisationName"), is(nameAddressReusableInformationForApplication.getOrganisationName()));
        assertThat(nameAddressJsonObject.getString("address1"), is(nameAddressReusableInformationForApplication.getAddress1()));
        assertThat(nameAddressJsonObject.getString("address2"), is(nameAddressReusableInformationForApplication.getAddress2()));
        assertThat(nameAddressJsonObject.getString("address3"), is(nameAddressReusableInformationForApplication.getAddress3()));
        assertThat(nameAddressJsonObject.getString("address4"), is(nameAddressReusableInformationForApplication.getAddress4()));
        assertThat(nameAddressJsonObject.getString("address5"), is(nameAddressReusableInformationForApplication.getAddress5()));
        assertThat(nameAddressJsonObject.getString("postCode"), is(nameAddressReusableInformationForApplication.getPostCode()));
        assertThat(nameAddressJsonObject.getString("primaryEmail"), is(nameAddressReusableInformationForApplication.getPrimaryEmail()));
        assertThat(nameAddressJsonObject.getString("secondaryEmail"), is(nameAddressReusableInformationForApplication.getSecondaryEmail()));
    }
}
