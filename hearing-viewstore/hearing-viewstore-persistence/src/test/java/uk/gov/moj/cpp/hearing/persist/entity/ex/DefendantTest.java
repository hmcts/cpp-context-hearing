package uk.gov.moj.cpp.hearing.persist.entity.ex;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static java.time.ZonedDateTime.parse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.moj.cpp.hearing.utils.TestUtils.hasInnerStaticClass;
import static uk.gov.moj.cpp.hearing.utils.TestUtils.hasParameterizedConstructor;

import java.util.Arrays;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class DefendantTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldHaveANoArgsConstructor() {
        assertThat(Defendant.class, hasValidBeanConstructor());
    }

    @Test
    public void shouldHaveABuilderInnerClass() {
        assertTrue(hasInnerStaticClass(Defendant.class, "Builder"));
    }
    
    @Test
    public void shouldHaveAParameterizedConstructor() {
        assertTrue(hasParameterizedConstructor(Defendant.class, Defendant.Builder.class));
    }

    public static Defendant buildDefendant1(final Ahearing ahearing, final DefenceAdvocate defenseAdvocate1,
            final DefenceAdvocate defenseAdvocate2) {
        return new Defendant.Builder()
                        .withId(new HearingSnapshotKey(UUID.fromString("dbd45b1d-cd60-41cf-9cde-7501128fbfe0"), ahearing.getId()))
                        .withHearing(ahearing)
                        .withPersonId(UUID.fromString("5a6e2001-91ed-4af2-99af-f30ddc9ef5af"))
                        .withFirstName("Ken")
                        .withLastName("Thompson")
                        .withDateOfBirth(parse("1943-02-04T00:00:00Z").toLocalDate())
                        .withNationality("United States")
                        .withGender("M")
                        .withAddress(new Address.Builder()
                                .withAddress1("222 Furze Road Exeter")
                                .withAddress2("Lorem Ipsum")
                                .withAddress3("Solor")
                                .withAddress4("Porro Quisquam")
                                .withPostCode("CR0 1XG")
                                .build())
                        .withWorkTelephone("02070101011")
                        .withHomeTelephone("02070101010")
                        .withMobileTelephone("07422263910")
                        .withFax("021111111")
                        .withEmail("ken.thompson@acme.me")
                        .withDefenceAdvocates(Arrays.asList(defenseAdvocate1, defenseAdvocate2))
                        .build();
    }
    
    public static Defendant buildDefendant2(final Ahearing ahearing, final DefenceAdvocate defenseAdvocate1,
            final DefenceAdvocate defenseAdvocate2) {
        return new Defendant.Builder()
                .withId(new HearingSnapshotKey(UUID.fromString("dc08d6c5-2e12-4beb-ac7a-4b5de83af657"), ahearing.getId()))
                .withHearing(ahearing)
                .withPersonId(UUID.fromString("98583be4-8d4a-4552-9252-ceccd61d32db"))
                .withFirstName("William Nelson")
                .withLastName("Joy")
                .withDateOfBirth(parse("1954-11-08T00:00:00Z").toLocalDate())
                .withNationality("United States")
                .withGender("M")
                .withAddress(new Address.Builder()
                        .withAddress1("222 Furze Road Exeter")
                        .withAddress2("Lorem Ipsum")
                        .withAddress3("Solor")
                        .withAddress4("Porro Quisquam")
                        .withPostCode("CR0 1XG")
                        .build())
                .withWorkTelephone("02070101011")
                .withHomeTelephone("02070101010")
                .withMobileTelephone("07422263910")
                .withFax("021111111")
                .withEmail("william-nelson.joy@acme.me")
                .withDefenceAdvocates(Arrays.asList(defenseAdvocate1, defenseAdvocate2))
                .build();
    }
}