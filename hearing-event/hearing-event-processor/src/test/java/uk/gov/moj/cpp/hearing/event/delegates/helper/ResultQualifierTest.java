package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static com.google.common.collect.ImmutableList.of;
import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.JudicialResultPrompt.judicialResultPrompt;
import static uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllFixedList.allFixedList;

import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.FixedList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.FixedListElement;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class
)
public class ResultQualifierTest {

    JsonEnvelope commandJsonEnvelope;
    @Mock
    private ReferenceDataService referenceDataService;


    @Test
    public void testPopulateWhenResultQualifierAndResultPromptQualifiersArePresent() {
        final String qualifier = "abcd";
        final List<JudicialResultPrompt> judicialResultPromptList = of(judicialResultPrompt().withQualifier("efgh").build(), judicialResultPrompt().withQualifier("ijkl").build());
        final Optional<String> qualifierList = new ResultQualifier().populate(qualifier, judicialResultPromptList, this.referenceDataService, commandJsonEnvelope, now());
        assertThat(qualifierList.isPresent(), is(true));
        assertThat(qualifierList.get(), is("abcd,efgh,ijkl"));
    }

    @Test
    public void testPopulateWhenResultOnlyResultPromptQualifiersArePresent() {
        final String qualifier = "";
        final List<JudicialResultPrompt> judicialResultPromptList = of(judicialResultPrompt().withQualifier("efgh").build(), judicialResultPrompt().withQualifier("ijkl").build());
        final Optional<String> qualifierList = new ResultQualifier().populate(qualifier, judicialResultPromptList, this.referenceDataService, commandJsonEnvelope, now());
        assertThat(qualifierList.isPresent(), is(true));
        assertThat(qualifierList.get(), is("efgh,ijkl"));
    }

    @Test
    public void testPopulateWhenResultQualifierIsPresent() {
        final String qualifier = "abcd";
        final List<JudicialResultPrompt> judicialResultPromptList = of(judicialResultPrompt().build(), judicialResultPrompt().build());
        final Optional<String> qualifierList = new ResultQualifier().populate(qualifier, judicialResultPromptList, this.referenceDataService, commandJsonEnvelope, now());
        assertThat(qualifierList.isPresent(), is(true));
        assertThat(qualifierList.get(), is("abcd"));
    }

    @Test
    public void testPopulateWhenResultQualifierAndResultPromptQualifiersAreNotPresent() {
        final String qualifier = null;
        final List<JudicialResultPrompt> judicialResultPromptList = of(judicialResultPrompt().build(), judicialResultPrompt().build());
        final Optional<String> qualifierList = new ResultQualifier().populate(qualifier, judicialResultPromptList, this.referenceDataService, commandJsonEnvelope, now());
        assertThat(qualifierList.isPresent(), is(false));
    }

    @Test
    public void testPopulateResultQualifierWhenResultPromptsTypeIsFIXL() {
        when(referenceDataService.getAllFixedList(anyObject(), anyObject())).thenReturn(allFixedList().setFixedListCollection(
                asList(FixedList.fixedList()
                        .setId(UUID.randomUUID())
                        .setStartDate(LocalDate.now())
                        .setEndDate(LocalDate.now())
                        .setElements(asList(FixedListElement.fixedListElement()
                                .setCode("code")
                                .setValue("value")
                                .setWelshValue("welshCode")
                                .setCjsQualifier("cjsQualifiersFIXL")
                        ))

                )

        ));
        final String qualifier = null;
        final List<JudicialResultPrompt> judicialResultPromptList = of(judicialResultPrompt().withType("FIXLM").withValue("value").build(), judicialResultPrompt().build());
        final Optional<String> qualifierList = new ResultQualifier().populate(qualifier, judicialResultPromptList, this.referenceDataService, commandJsonEnvelope, now());
        assertThat(qualifierList.isPresent(), is(true));
        assertThat(qualifierList.get(), is("cjsQualifiersFIXL"));
    }

    @Test
    public void testPopulateResultQualifierWhenResultPromptsTypeIsFIXLM() {
        when(referenceDataService.getAllFixedList(anyObject(), anyObject())).thenReturn(allFixedList().setFixedListCollection(
                asList(FixedList.fixedList()
                        .setId(UUID.randomUUID())
                        .setStartDate(LocalDate.now())
                        .setEndDate(LocalDate.now())
                        .setElements(asList(FixedListElement.fixedListElement()
                                        .setCode("code")
                                        .setValue("value")
                                        .setWelshValue("welshCode"),
                                FixedListElement.fixedListElement()
                                        .setCode("code")
                                        .setValue("newValue")
                                        .setWelshValue("welshCode")
                                        .setCjsQualifier("cjsQualifiersFIXLM")
                        ))

                )

        ));
        final String qualifier = null;
        final List<JudicialResultPrompt> judicialResultPromptList = of(judicialResultPrompt().withType("FIXL").withValue("newValue").build(), judicialResultPrompt().build());
        final Optional<String> qualifierList = new ResultQualifier().populate(qualifier, judicialResultPromptList, this.referenceDataService, commandJsonEnvelope, now());
        assertThat(qualifierList.isPresent(), is(true));
        assertThat(qualifierList.get(), is("cjsQualifiersFIXLM"));
    }

    @Test
    public void shouldConcatenateQualifiers() {
        when(referenceDataService.getAllFixedList(anyObject(), anyObject())).thenReturn(allFixedList().setFixedListCollection(
                asList(FixedList.fixedList()
                                .setId(UUID.randomUUID())
                                .setStartDate(LocalDate.now())
                                .setEndDate(LocalDate.now())
                                .setElements(asList(FixedListElement.fixedListElement()
                                        .setCode("code")
                                        .setValue("newValue1")
                                        .setWelshValue("welshCode")
                                        .setCjsQualifier("cjsQualifiersFIXL")
                                )),
                        FixedList.fixedList()
                                .setId(UUID.randomUUID())
                                .setStartDate(LocalDate.now())
                                .setEndDate(LocalDate.now())
                                .setElements(asList(FixedListElement.fixedListElement()
                                        .setCode("code")
                                        .setValue("newValue2")
                                        .setWelshValue("welshCode")
                                        .setCjsQualifier("cjsQualifiersFIXLM")
                                ))

                )

        ));
        final String qualifier = null;
        final List<JudicialResultPrompt> judicialResultPromptList = of(judicialResultPrompt().withType("FIXL").withValue("newValue1").build(), judicialResultPrompt().withType("FIXLM").withValue("newValue2").build());
        final Optional<String> qualifierList = new ResultQualifier().populate(qualifier, judicialResultPromptList, this.referenceDataService, commandJsonEnvelope, now());
        assertThat(qualifierList.isPresent(), is(true));
        assertThat(qualifierList.get(), is("cjsQualifiersFIXL,cjsQualifiersFIXLM"));
    }


    @Test
    public void shouldConcatenateQualifiersWhenForMultipleValues() {
        when(referenceDataService.getAllFixedList(anyObject(), anyObject())).thenReturn(allFixedList().setFixedListCollection(
                asList(FixedList.fixedList()
                                .setId(UUID.randomUUID())
                                .setStartDate(LocalDate.now())
                                .setEndDate(LocalDate.now())
                                .setElements(asList(FixedListElement.fixedListElement()
                                                .setCode("code")
                                                .setValue("value1")
                                                .setWelshValue("welshCode")
                                                .setCjsQualifier("cjsQualifierValue1")
                                        , FixedListElement.fixedListElement()
                                                .setCode("code")
                                                .setValue("value2")
                                                .setWelshValue("welshCode")
                                                .setCjsQualifier("cjsQualifierValue2")))
                        ,
                        FixedList.fixedList()
                                .setId(UUID.randomUUID())
                                .setStartDate(LocalDate.now())
                                .setEndDate(LocalDate.now())
                                .setElements(asList(FixedListElement.fixedListElement()
                                        .setCode("code")
                                        .setValue("value3")
                                        .setWelshValue("welshCode")
                                        .setCjsQualifier("cjsQualifierValue3")
                                )))

                )

        );
        final String qualifier = null;
        final List<JudicialResultPrompt> judicialResultPromptList = of(judicialResultPrompt().withType("FIXL").withValue("value1###value2###value3").build());
        final Optional<String> qualifierList = new ResultQualifier().populate(qualifier, judicialResultPromptList, this.referenceDataService, commandJsonEnvelope, now());
        assertThat(qualifierList.isPresent(), is(true));
        assertThat(qualifierList.get(), is("cjsQualifierValue1,cjsQualifierValue2,cjsQualifierValue3"));
    }
}