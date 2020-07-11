package uk.gov.justice.ccr.notepad.process;

import static java.time.LocalDate.now;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.BOOLEAN;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.CURR;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DATE;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DURATION;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.INT;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.TIME;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.TXT;
import static uk.gov.justice.ccr.notepad.view.Part.State.RESOLVED;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;
import uk.gov.justice.ccr.notepad.shared.AbstractTest;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.PromptChoice;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessorTest extends AbstractTest {

    @InjectMocks
    protected Processor target = new Processor();

    @Test
    public void shouldProcessPartsAndGetCorrectPromptsRelatedToResultDefinitionId() throws Exception {
        final List<Part> upwrParts = new PartsResolver().getParts("UPWR 80 hrs");
        final List<Part> yracParts = new PartsResolver().getParts("YRAC1 80 hrs");

        List<String> values = upwrParts.stream().map(Part::getValueAsString).collect(toList());
        Knowledge knowledge = target.processParts(values, now());

        assertThat(knowledge.getResultDefinitionParts().size(), is(1));
        assertThat(knowledge.getResultDefinitionParts().get("upwr").getCode(), is("9bec5977-1796-4645-9b9e-687d4f23d37d"));
        assertThat(knowledge.getResultPromptParts().size(), is(2));
        assertThat(knowledge.getResultPromptParts().get("hrs").getCode(), is("72413aef-ecb9-4e9b-a143-31d1e9c11584"));

        values = yracParts.stream().map(Part::getValueAsString).collect(toList());
        knowledge = target.processParts(values, now());

        assertThat(knowledge.getResultDefinitionParts().size(), is(1));
        assertThat(knowledge.getResultDefinitionParts().get("yrac1").getCode(), is("27528205-a237-471b-a487-7943e9284c44"));
        assertThat(knowledge.getResultPromptParts().size(), is(2));
        assertThat(knowledge.getResultPromptParts().get("hrs").getCode(), is("f13d59b1-88a1-4ad0-972c-e86a22cdd700"));
    }

    @Test
    public void processResultDefinition1() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("Imp 2 years");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = target.processParts(values, now());

        assertThat(
                knowledge.getResultDefinitionParts().size()
                , is(1)
        );
        assertThat(
                knowledge.getResultPromptParts().size()
                , is(2)
        );

    }

    @Test
    public void processResultDefinition2() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("Pard $367 conc conc [33] m");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = target.processParts(values, now());

        assertThat(
                knowledge.getResultDefinitionParts().size()
                , is(1)
        );
        assertThat(
                knowledge.getResultPromptParts().size()
                , is(4)
        );
        assertThat(
                knowledge.getResultPromptParts().get("m").getType()
                , is(DURATION)
        );

    }

    @Test
    public void processResultDefinition3() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("sus imp 4 yr 8 mo $5666 conc Early not apply [2ewe wew[wwe] [wewe ew]");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = target.processParts(values, now());
        assertThat(knowledge.getResultDefinitionParts().entrySet(), hasSize(2));
        assertThat(knowledge.getResultPromptParts().entrySet(), hasSize(11));
    }

    @Test
    public void processResultDefinition4() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("Spec cust conc 4 yr 8 mo $5666 conc Early not apply [2ewe wew[wwe] [wewe ew]");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = target.processParts(values, now());
        assertThat(knowledge.getResultDefinitionParts().entrySet(), hasSize(4));
        assertThat(knowledge.getResultPromptParts().entrySet(), hasSize(9));
    }

    @Test
    public void processResultDefinition5() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("vs 2 yr 3m 3£3 02:25 30/11/1980");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = target.processParts(values, now());

        assertThat(
                knowledge.getResultDefinitionParts().size()
                , is(1)
        );
        assertThat(
                knowledge.getResultPromptParts().size()
                , is(6)
        );
        assertThat(
                knowledge.getResultPromptParts().get("2").getType()
                , is(INT)
        );
        assertThat(
                knowledge.getResultPromptParts().get("yr").getType()
                , is(DURATION)
        );
        assertThat(
                knowledge.getResultPromptParts().get("3m").getType()
                , is(DURATION)
        );
        assertThat(
                knowledge.getResultPromptParts().get("3£3").getType()
                , is(TXT)
        );
        assertThat(
                knowledge.getResultPromptParts().get("02:25").getType()
                , is(TIME)
        );
        assertThat(
                knowledge.getResultPromptParts().get("30/11/1980").getType()
                , is(DATE)
        );
    }

    @Test
    public void processResultDefinition6() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("life imp release 2 yr mand conc £0 £");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = target.processParts(values, now());

        assertThat(
                knowledge.getResultDefinitionParts().size()
                , is(3)
        );
        assertThat(
                knowledge.getResultPromptParts().size()
                , is(6)
        );
        assertThat(
                knowledge.getResultDefinitionParts().get("life").getState()
                , is(RESOLVED)
        );
        assertThat(
                knowledge.getResultDefinitionParts().get("mand").getState()
                , is(RESOLVED)
        );
        assertThat(
                knowledge.getResultPromptParts().get("yr").getType()
                , is(DURATION)
        );
        assertThat(
                knowledge.getResultPromptParts().get("£0").getType()
                , is(CURR)
        );
        assertThat(
                knowledge.getResultPromptParts().get("£").getType()
                , is(TXT)
        );
        assertThat(
                knowledge.getResultPromptParts().get("release").getType()
                , is(TXT)
        );
        assertThat(
                knowledge.getResultPromptParts().get("conc").getType()
                , is(BOOLEAN)
        );
    }

    @Test
    public void processResultDefinition7() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("alc req pard shope 2 yr mand conc £300 344");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = target.processParts(values, now());

        Map<String, Part> resultDefinitionParts = knowledge.getResultDefinitionParts();
        Map<String, Part> resultPromptParts = knowledge.getResultPromptParts();

        assertThat(
                resultDefinitionParts.size()
                , is(5)
        );
        assertThat(
                resultPromptParts.size()
                , is(5)
        );
        assertThat(
                resultDefinitionParts.get("pard").getResultChoices().size()
                , is(1)
        );
        assertThat(
                resultDefinitionParts.get("shope").getResultChoices().size()
                , is(1)
        );
        assertThat(
                resultDefinitionParts.get("req").getResultChoices().size()
                , is(11)
        );
        assertThat(
                resultDefinitionParts.get("alc").getResultChoices()
                , hasSize(7)
        );
        assertThat(
                resultPromptParts.get("yr").getType()
                , is(DURATION)
        );
        assertThat(
                resultPromptParts.get("£300").getType()
                , is(CURR)
        );
        assertThat(
                resultPromptParts.get("344").getType()
                , is(INT)
        );
        assertThat(
                resultPromptParts.get("conc").getType()
                , is(BOOLEAN)
        );
    }

    @Test
    public void processResultDefinition8() {
        final List<Part> parts = new PartsResolver().getParts("UPWR 80 hrs");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = target.processParts(values, now());
        Map<String, Part> resultDefinitionParts = knowledge.getResultDefinitionParts();
        Map<String, Part> resultPromptParts = knowledge.getResultPromptParts();

        assertThat(
                resultDefinitionParts.size()
                , is(1)
        );
        assertThat(
                resultPromptParts.size()
                , is(2)
        );
    }

    @Test
    public void processResultPrompt1() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("imp sus");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = target.processResultPrompt(target.processParts(values, now()).getResultDefinitionParts().get("imp").getCode(), now());

        assertThat(knowledge.getPromptChoices(), hasSize(13));
    }

    @Test
    public void processResultPrompt2() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("f");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = target.processResultPrompt(target.processParts(values, now()).getResultDefinitionParts().get("f").getCode(), now());


        assertThat(
                knowledge.getPromptChoices().size()
                , is(1)
        );
    }

    @Test
    public void processResultPrompt3() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("imp");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = target.processResultPrompt(target.processParts(values, now()).getResultDefinitionParts().get("imp").getCode(), now());


        assertThat(
                knowledge.getPromptChoices().size()
                , is(14)
        );
    }

    @Test
    public void processResultPromptWithFixedList() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("restraop conv");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = target.processResultPrompt(target.processParts(values, now()).getResultDefinitionParts().get("restraop").getCode(), now());
        final Optional<PromptChoice> pc = knowledge.getPromptChoices().stream().filter(p -> p.getLabel().equals("Conviction / acquittal")).findFirst();

        assertThat(pc.isPresent(), is(true));
        assertThat(pc.get().getType(), is(ResultType.FIXL));
        assertThat(pc.get().getFixedList(), is(Sets.newHashSet("Acquitted", "Convicted")));
    }

    @Test
    public void retrieveChildResultDefinitionDetailWhenNoResultDefinitionRules() {

        final List<Part> parts = new PartsResolver().getParts("imp sus");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());
        final String resultDefinitionId = target.processParts(values, now()).getResultDefinitionParts().get("imp").getCode();

        final ChildResultDefinitionDetail childResultDefinitionDetail = target.retrieveChildResultDefinitionDetail(resultDefinitionId, now());

        assertThat(childResultDefinitionDetail, nullValue());
    }

    @Test
    public void shouldSetComponentTypeIfPromptReferenceIsHCROOM() {
        final Knowledge knowledge = target.processResultPrompt("fbed768b-ee95-4434-87c8-e81cbc8d24c8", now());
        assertThat(knowledge.getPromptChoices().stream().filter(promptChoice -> "HCROOM".equals(promptChoice.getComponentType())).count(), is(1L));
    }

    @Test
    public void shouldSetComponentTypeIfPromptReferenceIsHCHOUSE() {
        final Knowledge knowledge = target.processResultPrompt("fbed768b-ee95-4434-87c8-e81cbc8d24c8", now());
        assertThat(knowledge.getPromptChoices().stream().filter(promptChoice -> "HCHOUSE".equals(promptChoice.getComponentType())).count(), is(1L));
    }

    @Test
    public void shouldSetComponentTypeIfResultPromptRuleIsOneOf() {
        final Knowledge knowledge = target.processResultPrompt("7f80f0f4-ae8a-4965-9cc6-ef5c2c5caef7", now());
        assertThat(knowledge.getPromptChoices().stream().filter(promptChoice -> "ONEOF".equals(promptChoice.getComponentType())).count(), is(6L));
    }

    public Processor getProcessor() {
        return target;
    }
}
