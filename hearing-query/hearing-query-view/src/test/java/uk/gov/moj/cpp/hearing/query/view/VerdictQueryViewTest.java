package uk.gov.moj.cpp.hearing.query.view;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUIDAndName;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;

import uk.gov.justice.services.common.converter.ListToJsonArrayConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.VerdictHearing;
import uk.gov.moj.cpp.hearing.query.view.service.VerdictService;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VerdictQueryViewTest {
    private static final String FIELD_CASE_ID = "caseId";

    @InjectMocks
    private VerdictQueryView verdictQueryView;

    @Mock
    private JsonEnvelope query;

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Mock
    private VerdictService verdictService;

    @Spy
    private ListToJsonArrayConverter jsonConverter;

    @Before
    public void initMocks() {

        setField(this.jsonConverter, "mapper",
                new ObjectMapperProducer().objectMapper());
        setField(this.jsonConverter, "stringToJsonObjectConverter",
                new StringToJsonObjectConverter());
    }

    @Test
    public void shouldFindVerdictsByCaseId() {
        final UUID caseId = randomUUID();
        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder().add(FIELD_CASE_ID, caseId.toString()).build());
        final ArrayList<VerdictHearing> verdicts = new ArrayList<>();
        final VerdictHearing verdictOne = new VerdictHearing(randomUUID(), randomUUID(), caseId, randomUUID(), randomUUID(), randomUUID(), "GUILTY");
        final VerdictHearing verdictTwo = new VerdictHearing(randomUUID(), randomUUID(), caseId, randomUUID(), randomUUID(), randomUUID(), "NOT GUILTY");
        verdicts.add(verdictOne);
        verdicts.add(verdictTwo);
        when(verdictService.getVerdictHearingByCaseId(caseId)).thenReturn(verdicts);
        final JsonEnvelope verdictsEnvelope = verdictQueryView.getCaseVerdicts(query);
        assertThat(verdictsEnvelope, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query).withName("hearing.get.case.verdicts"),
                payloadIsJson(allOf(
                        withJsonPath("$.verdicts", hasSize(2)),

                        withJsonPath("$.verdicts[0].verdictId", equalTo(verdictOne.getVerdictId().toString())),
                        withJsonPath("$.verdicts[0].hearingId", equalTo(verdictOne.getHearingId().toString())),
                        withJsonPath("$.verdicts[0].value", equalTo(verdictOne.getValue())),
                        withJsonPath("$.verdicts[0].caseId", equalTo(verdictOne.getCaseId().toString())),
                        withJsonPath("$.verdicts[0].defendantId", equalTo(verdictOne.getDefendantId().toString())),
                        withJsonPath("$.verdicts[0].personId", equalTo(verdictOne.getPersonId().toString())),
                        withJsonPath("$.verdicts[0].offenceId", equalTo(verdictOne.getOffenceId().toString())),

                        withJsonPath("$.verdicts[1].verdictId", equalTo(verdictTwo.getVerdictId().toString())),
                        withJsonPath("$.verdicts[1].hearingId", equalTo(verdictTwo.getHearingId().toString())),
                        withJsonPath("$.verdicts[1].value", equalTo(verdictTwo.getValue())),
                        withJsonPath("$.verdicts[1].caseId", equalTo(verdictTwo.getCaseId().toString())),
                        withJsonPath("$.verdicts[1].defendantId", equalTo(verdictTwo.getDefendantId().toString())),
                        withJsonPath("$.verdicts[1].personId", equalTo(verdictTwo.getPersonId().toString())),
                        withJsonPath("$.verdicts[1].offenceId", equalTo(verdictTwo.getOffenceId().toString()))
                ))).thatMatchesSchema()
        ));
    }

}
