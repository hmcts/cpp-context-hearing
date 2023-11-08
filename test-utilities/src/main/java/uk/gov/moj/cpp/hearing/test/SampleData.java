package uk.gov.moj.cpp.hearing.test;

import static java.util.Arrays.asList;
import static uk.gov.moj.cpp.hearing.CreateJsonSampleUtil.createObjectMapper;

import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.hearing.courts.Applicant;
import uk.gov.justice.hearing.courts.CourtApplicationSummaries;
import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.hearing.courts.HearingSummaries;
import uk.gov.justice.hearing.courts.ProsecutionCaseSummaries;
import uk.gov.justice.hearing.courts.Respondents;
import uk.gov.moj.cpp.hearing.domain.referencedata.HearingTypes;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SampleData {

    private static HearingSummaries applicationSummary() {
        return HearingSummaries.hearingSummaries()
                .withCourtApplicationSummaries(
                        asList(CourtApplicationSummaries
                                .courtApplicationSummaries()
                                .withApplicant(
                                        Applicant.applicant()
                                                .withFirstName("John")
                                                .withLastName("Smith")
                                                .withMiddleName("Peter")
                                                .withOrganisationName("HMRC")
                                                .withSynonym("tax John")
                                                .withId(UUID.randomUUID())
                                                .build()
                                )
                                .withApplicationReference("APR678790")
                                .withId(UUID.randomUUID())
                                .withRespondents(asList(
                                        Respondents.respondents()
                                                .withFirstName("Jane")
                                                .withMiddleName("June")
                                                .withLastName("Brown")
                                                .withId(UUID.randomUUID())
                                                .withOrganisationName("Style Council")
                                                .build()
                                ))
                                .build()
                        )
                )
                .withHasSharedResults(false)
                .withHearingDays(asList(HearingDay.hearingDay()
                        .withListedDurationMinutes(30)
                        .withListingSequence(0)
                        .withSittingDay(ZonedDateTime.now())
                        .build()
                ))
                .withHearingLanguage("English")
                .withJurisdictionType(JurisdictionType.CROWN)
                .withReportingRestrictionReason("National Security")
                .withType(HearingType.hearingType()
                        .withId(UUID.randomUUID())
                        .withDescription("Application")
                        .build())
                .build();
    }

    private static HearingSummaries hearingSummary() {
        return HearingSummaries.hearingSummaries()

                .withHasSharedResults(true)
                .withHearingDays(asList(HearingDay.hearingDay()
                        .withListedDurationMinutes(60)
                        .withListingSequence(1)
                        .withSittingDay(ZonedDateTime.now().plus(1, ChronoUnit.DAYS))
                        .build()
                ))
                .withProsecutionCaseSummaries(
                        asList(ProsecutionCaseSummaries.prosecutionCaseSummaries()
                                .withProsecutionCaseIdentifier(
                                        ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                                                .withCaseURN("URN1234456").build()
                                ).build())
                )
                .withHearingLanguage("Welsh")
                .withJurisdictionType(JurisdictionType.MAGISTRATES)
                .withType(HearingType.hearingType()
                        .withId(UUID.randomUUID())
                        .withDescription("Hearing")
                        .build())
                .build();
    }


    public static GetHearings getHearings() {
        return GetHearings.getHearings()
                .withHearingSummaries(
                        asList(
                                applicationSummary(),
                                hearingSummary()
                        )
                )
                .build();


    }

    public static HearingTypes getHearingTypes() {
       final uk.gov.moj.cpp.hearing.domain.referencedata.HearingType hearingType = new
                uk.gov.moj.cpp.hearing.domain.referencedata.HearingType(UUID.randomUUID(),1,
                "hearingCode", "hearingDescription",
                "welshHearingDescription", 10,
                LocalDate.now(), LocalDate.now().plusDays(10),
                "exhibitHearingCode", "exhibitHearingDescription", true);
        final HearingTypes types = new HearingTypes();
        types.setHearingTypes(Stream.of(hearingType).collect(Collectors.toList()));
        return types;
    }

    @SuppressWarnings({"squid:S2096", "squid:S106", "squid:S1192"})
    public static void main(String[] args) throws IOException {
        final ObjectMapper objectMapper = createObjectMapper();
        File file;

        file = new File("target/hearing.get-hearings.json");
        System.out.println("writing to file " + file.getAbsolutePath());
        objectMapper.writeValue(file, getHearings());

        file = new File("target/hearing.get-hearing.standaloneApplication.json");
        System.out.println("writing to file " + file.getAbsolutePath());
        objectMapper.writeValue(file, (new HearingFactory()).createStandaloneApplicationHearing().build());

        file = new File("target/hearing.initiateHearingSample.json");
        System.out.println("writing to file " + file.getAbsolutePath());
        objectMapper.writeValue(file, TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate());




    }
}
