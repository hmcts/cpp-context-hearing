package uk.gov.moj.cpp.hearing.utils;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;

import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowResultDefinitionRequirement;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.LocalDate;
import java.util.Arrays;

public class StubNowsReferenceData {
    private static final String BOTH_JURISDICTIONS = "B";
    public static CommandHelpers.AllNowsReferenceDataHelper setupNowsReferenceData(final LocalDate referenceDate) {
        AllNows allnows = AllNows.allNows()
                .setNows(Arrays.asList(NowDefinition.now()
                                .setId(randomUUID())
                                .setResultDefinitions(asList(NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(randomUUID())
                                                .setMandatory(true)
                                                .setPrimary(true),
                                        NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(randomUUID())
                                                // This causes a test failure but this field is under review .setText("ResultDefinitionLevel/" + STRING.next())
                                                .setMandatory(false)
                                                .setPrimary(false)))
                                .setName(STRING.next())
                                .setText("NowLevel/" + STRING.next())
                                .setTemplateName(STRING.next())
                                .setRank(INTEGER.next())
                                .setJurisdiction("B")
                                .setRemotePrintingRequired(false),
                        NowDefinition.now()
                                .setId(randomUUID())
                                .setResultDefinitions(asList(NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(randomUUID())
                                                .setMandatory(true)
                                                .setPrimary(true),
                                        NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(randomUUID())
                                                .setMandatory(false)
                                                .setPrimary(false)
                                ))
                                .setName(STRING.next())
                                .setTemplateName(STRING.next())
                                .setRank(INTEGER.next())
                                .setJurisdiction(BOTH_JURISDICTIONS)
                                .setRemotePrintingRequired(false)
                                .setText(STRING.next())
                ));
        return setupNowsReferenceData(referenceDate, allnows);
    }

    private static CommandHelpers.AllNowsReferenceDataHelper setupNowsReferenceData(final LocalDate referenceDate, final AllNows data) {
        final CommandHelpers.AllNowsReferenceDataHelper allNows = h(data);
        ReferenceDataStub.stubGetAllNowsMetaData(referenceDate, allNows.it());
        return allNows;
    }
}
