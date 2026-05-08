package uk.gov.moj.cpp.hearing.mapping;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.hearing.courts.ApplicationCourtListRestriction;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ApplicationCourtRestrictionMapperTest {

    @Spy
    ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @InjectMocks
    ApplicationCourtListRestrictionMapper applicationCourtListRestrictionMapper;

    @BeforeEach
    public void setUp() {
        setField(objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void testNullField() {
        Assert.assertFalse(applicationCourtListRestrictionMapper.getCourtListRestriction(null).isPresent());
    }

    @Test
    public void testEmpty() {
        Assert.assertFalse(applicationCourtListRestrictionMapper.getCourtListRestriction(" ").isPresent());
    }

    @Test
    public void shouldRemoveApplicationWhenApplicationIdIsRestricted() {
        final UUID restrictedAppId = randomUUID();
        final UUID otherAppId = randomUUID();
        final ApplicationCourtListRestriction restrictions = ApplicationCourtListRestriction.applicationCourtListRestriction()
                .withCourtApplicationIds(singletonList(restrictedAppId))
                .build();
        final List<CourtApplication> applications = List.of(
                courtApplicationWithApplicant(restrictedAppId, randomUUID()),
                courtApplicationWithApplicant(otherAppId, randomUUID()));

        final List<CourtApplication> result = applicationCourtListRestrictionMapper.getCourtApplications(applications, restrictions);

        assertThat(result.size(), is(1));
        assertThat(result.get(0).getId(), is(otherAppId));
    }

    @Test
    public void shouldNullApplicantWhenApplicantIdIsRestricted() {
        final UUID applicantId = randomUUID();
        final ApplicationCourtListRestriction restrictions = ApplicationCourtListRestriction.applicationCourtListRestriction()
                .withCourtApplicationApplicantIds(singletonList(applicantId))
                .build();
        final List<CourtApplication> applications = List.of(courtApplicationWithApplicant(randomUUID(), applicantId));

        final List<CourtApplication> result = applicationCourtListRestrictionMapper.getCourtApplications(applications, restrictions);

        assertThat(result.size(), is(1));
        assertThat(result.get(0).getApplicant(), is(nullValue()));
    }

    @Test
    public void shouldNullSubjectWhenSubjectIdIsRestricted() {
        final UUID subjectId = randomUUID();
        final ApplicationCourtListRestriction restrictions = ApplicationCourtListRestriction.applicationCourtListRestriction()
                .withCourtApplicationSubjectIds(singletonList(subjectId))
                .build();
        final List<CourtApplication> applications = List.of(courtApplicationWithApplicantAndSubject(randomUUID(), randomUUID(), subjectId));

        final List<CourtApplication> result = applicationCourtListRestrictionMapper.getCourtApplications(applications, restrictions);

        assertThat(result.size(), is(1));
        assertThat(result.get(0).getSubject(), is(nullValue()));
        assertThat(result.get(0).getApplicant(), is(notNullValue()));
    }

    @Test
    public void shouldNullBothApplicantAndSubjectWhenBothAreRestricted() {
        final UUID applicantId = randomUUID();
        final UUID subjectId = randomUUID();
        final ApplicationCourtListRestriction restrictions = ApplicationCourtListRestriction.applicationCourtListRestriction()
                .withCourtApplicationApplicantIds(singletonList(applicantId))
                .withCourtApplicationSubjectIds(singletonList(subjectId))
                .build();
        final List<CourtApplication> applications = List.of(courtApplicationWithApplicantAndSubject(randomUUID(), applicantId, subjectId));

        final List<CourtApplication> result = applicationCourtListRestrictionMapper.getCourtApplications(applications, restrictions);

        assertThat(result.size(), is(1));
        assertThat(result.get(0).getApplicant(), is(nullValue()));
        assertThat(result.get(0).getSubject(), is(nullValue()));
    }

    @Test
    public void shouldNotAffectApplicationWithNullSubjectWhenSubjectIdsAreRestricted() {
        final UUID subjectId = randomUUID();
        final ApplicationCourtListRestriction restrictions = ApplicationCourtListRestriction.applicationCourtListRestriction()
                .withCourtApplicationSubjectIds(singletonList(subjectId))
                .build();
        final UUID applicantId = randomUUID();
        final List<CourtApplication> applications = List.of(courtApplicationWithApplicant(randomUUID(), applicantId));

        final List<CourtApplication> result = applicationCourtListRestrictionMapper.getCourtApplications(applications, restrictions);

        assertThat(result.size(), is(1));
        assertThat(result.get(0).getApplicant().getId(), is(applicantId));
        assertThat(result.get(0).getSubject(), is(nullValue()));
    }

    @Test
    public void shouldReturnTrueForIsSubjectRestrictedWhenSubjectIdIsInList() {
        final UUID subjectId = randomUUID();
        final ApplicationCourtListRestriction restrictions = ApplicationCourtListRestriction.applicationCourtListRestriction()
                .withCourtApplicationSubjectIds(singletonList(subjectId))
                .build();

        assertTrue(applicationCourtListRestrictionMapper.isSubjectRestricted(restrictions, subjectId));
    }

    @Test
    public void shouldReturnFalseForIsSubjectRestrictedWhenSubjectIdIsNotInList() {
        final ApplicationCourtListRestriction restrictions = ApplicationCourtListRestriction.applicationCourtListRestriction()
                .withCourtApplicationSubjectIds(singletonList(randomUUID()))
                .build();

        assertFalse(applicationCourtListRestrictionMapper.isSubjectRestricted(restrictions, randomUUID()));
    }

    @Test
    public void shouldReturnFalseForIsSubjectRestrictedWhenListIsEmpty() {
        final ApplicationCourtListRestriction restrictions = ApplicationCourtListRestriction.applicationCourtListRestriction()
                .build();

        assertFalse(applicationCourtListRestrictionMapper.isSubjectRestricted(restrictions, randomUUID()));
    }

    private CourtApplication courtApplicationWithApplicant(final UUID applicationId, final UUID applicantId) {
        return CourtApplication.courtApplication()
                .withId(applicationId)
                .withApplicant(CourtApplicationParty.courtApplicationParty().withId(applicantId).build())
                .build();
    }

    private CourtApplication courtApplicationWithApplicantAndSubject(final UUID applicationId, final UUID applicantId, final UUID subjectId) {
        return CourtApplication.courtApplication()
                .withId(applicationId)
                .withApplicant(CourtApplicationParty.courtApplicationParty().withId(applicantId).build())
                .withSubject(CourtApplicationParty.courtApplicationParty().withId(subjectId).build())
                .build();
    }
}
