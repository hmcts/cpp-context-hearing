package uk.gov.moj.cpp.hearing.mapping;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.justice.services.messaging.JsonObjects.createReader;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.hearing.courts.ApplicationCourtListRestriction;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import javax.json.JsonObject;
import javax.json.JsonReader;

public class ApplicationCourtListRestrictionMapper {

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private JsonObject jsonFromString(String jsonObjectStr) {
        try (JsonReader jsonReader = createReader(new StringReader(jsonObjectStr))) {
            return jsonReader.readObject();
        }
    }

    public Optional<ApplicationCourtListRestriction> getCourtListRestriction(final String json) {
        if (json == null || json.trim().length() == 0) {
            return empty();
        }
        final JsonObject jsonObject = jsonFromString(json);
        return of(jsonObjectToObjectConverter.convert(jsonObject, ApplicationCourtListRestriction.class));
    }

    public boolean isApplicationRestricted(final ApplicationCourtListRestriction applicationCourtListRestriction, final UUID applicationId) {
        return isNotEmpty(applicationCourtListRestriction.getCourtApplicationIds()) && applicationCourtListRestriction.getCourtApplicationIds().contains(applicationId);
    }

    public boolean isApplicantRestricted(final ApplicationCourtListRestriction applicationCourtListRestriction, final UUID applicantId) {
        return isNotEmpty(applicationCourtListRestriction.getCourtApplicationApplicantIds()) && applicationCourtListRestriction.getCourtApplicationApplicantIds().contains(applicantId);
    }

    public boolean isSubjectRestricted(final ApplicationCourtListRestriction applicationCourtListRestriction, final UUID subjectId) {
        return isNotEmpty(applicationCourtListRestriction.getCourtApplicationSubjectIds()) && applicationCourtListRestriction.getCourtApplicationSubjectIds().contains(subjectId);
    }

    public List<CourtApplication> getCourtApplications(final List<CourtApplication> courtApplications, final ApplicationCourtListRestriction restrictions) {
        return courtApplications.stream()
                .filter(ca -> !isApplicationRestricted(restrictions, ca.getId()))
                .map(ca -> {
                    final boolean applicantRestricted = isApplicantRestricted(restrictions, ca.getApplicant().getId());
                    final boolean subjectRestricted = ca.getSubject() != null && isSubjectRestricted(restrictions, ca.getSubject().getId());
                    if (applicantRestricted || subjectRestricted) {
                        return (CourtApplication.courtApplication()
                                .withValuesFrom(ca)
                                .withApplicant(applicantRestricted ? null : ca.getApplicant())
                                .withSubject(subjectRestricted ? null : ca.getSubject())
                                .build());
                    }
                    return ca;
                }).collect(Collectors.toList());
    }
}
