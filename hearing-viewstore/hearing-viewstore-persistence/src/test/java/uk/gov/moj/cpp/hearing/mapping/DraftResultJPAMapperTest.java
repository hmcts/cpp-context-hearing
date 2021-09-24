package uk.gov.moj.cpp.hearing.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.command.result.DraftResultV2;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DraftResult;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DraftResultJPAMapperTest {

    @InjectMocks
    private DraftResultJPAMapper mapper;

    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Test
    public void testFromJPA() throws IOException {

        final String payload = "{\"hearingId\": \"f14cbcf5-4905-4329-aa3c-869cad245332\", \"relations\": [{\"ruleType\": \"standalone\", \"resultLineId\": \"18b706c2-057c-4307-a5ac-9fc0af5160c1\", \"childResultLineIds\": []}], \"hearingDay\": \"2021-03-01\", \"resultLines\": {\"18b706c2-057c-4307-a5ac-9fc0af5160c1\": {\"label\": \"No order for costs\", \"level\": \"C\", \"caseId\": \"ff4b4dd0-663d-4305-b5ac-6f4929169c69\", \"offenceId\": \"54d63af6-97e2-44f0-92d7-94e6c2dd0690\", \"shortCode\": \"NCOSTS\", \"orderedDate\": \"2021-05-04\", \"originalText\": \"NCOSTS\", \"resultLineId\": \"18b706c2-057c-4307-a5ac-9fc0af5160c1\", \"resultPrompts\": [{\"type\": \"TXT\", \"value\": \"Because yes!\", \"promptId\": \"be2a46db-709d-4e0d-9b63-aeb831564c1d\", \"promptRef\": \"reasonForNoCosts\", \"promptLabel\": \"Reason for no costs\"}], \"unresolvedParts\": [], \"masterDefendantId\": \"f11671e5-4476-4342-8cf3-a8193512bd98\", \"resultDefinitionId\": \"baf94928-04ae-4609-8e96-efc9f081b2be\"}}, \"shadowListedOffenceIds\": [\"54d63af6-97e2-44f0-92d7-94e6c2dd0690\"]}";
        final String hearingDay = "2021-03-01";
        final UUID hearingId = UUID.fromString("f14cbcf5-4905-4329-aa3c-869cad245332");
        final UUID amendedByUserId = UUID.randomUUID();
        final JsonNode jsonNode = objectMapper.readTree(payload);

        final DraftResult draftResult = new DraftResult();
        draftResult.setDraftResultPayload(jsonNode);
        draftResult.setHearingDay(hearingDay);
        draftResult.setHearingId(hearingId);
        draftResult.setAmendedByUserId(amendedByUserId);


        final DraftResultV2 draftResultV2 = mapper.fromJPA(draftResult);

        assertThat(draftResultV2.getHearingId(), is(hearingId));
        assertThat(draftResultV2.getHearingDay(), is(LocalDate.parse(hearingDay)));
        assertThat(draftResultV2.getRelations(), hasSize(1));
        assertThat(draftResultV2.getRelations().get(0).getRuleType(), is("standalone"));
        assertThat(draftResultV2.getRelations(), hasSize(1));
        assertThat(draftResultV2.getRelations(), hasSize(1));
        assertThat(draftResultV2.getShadowListedOffenceIds(), hasSize(1));
        assertThat(draftResultV2.getResultLines().values(), hasSize(1));
    }
}
