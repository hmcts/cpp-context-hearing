package uk.gov.justice.ccr.notepad.view;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.when;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DURATION;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.FIXL;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.INT;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.TXT;
import static uk.gov.justice.ccr.notepad.view.ResultPromptViewBuilder.ONEOF;

import uk.gov.justice.ccr.notepad.process.Knowledge;
import uk.gov.justice.ccr.notepad.process.Processor;
import uk.gov.justice.ccr.notepad.process.ProcessorTest;
import uk.gov.justice.ccr.notepad.result.cache.CacheFactory;
import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;
import uk.gov.justice.ccr.notepad.result.loader.FileResultLoader;
import uk.gov.justice.ccr.notepad.result.loader.ResultLoader;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.LoadingCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.mortbay.log.Log;

@RunWith(MockitoJUnitRunner.class)
public class ResultPromptViewBuilderTest {
    @Spy
    private final ResultLoader resultLoader = new FileResultLoader();
    Processor processor;
    @InjectMocks
    ResultPromptViewBuilder testObj;
    @Spy
    @InjectMocks
    ResultCache resultCache = new ResultCache();
    @InjectMocks // this is VERY hacky but won't have time to refactor for 2.3 release
            ProcessorTest processorTest = new ProcessorTest();
    @Mock
    private CacheFactory cacheFactory;
    @Mock
    private LoadingCache<String, Object> cache;

    @Before
    public void setup() throws ExecutionException {
        when(cacheFactory.build()).thenReturn(cache);
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);
        processorTest.init();
        processor = processorTest.getProcessor();
    }

    @Test
    public void buildFromKnowledge() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("parp");
        final Knowledge knowledge = processor.processParts(parts.stream().map(Part::getValueAsString).collect(Collectors.toList()), LocalDate.now());
        final ResultDefinitionView resultDefinitionView = new ResultDefinitionViewBuilder().buildFromKnowledge(parts, knowledge, new ArrayList<>(),true);

        final ResultPromptView result = testObj.buildFromKnowledge(processor.processResultPrompt(resultDefinitionView.getResultCode(), LocalDate.now()));

        assertThat(result.getPromptChoices().size()
                , is(2)
        );
        final PromptChoice p1 = result.getPromptChoices().get(0);
        final PromptChoice p2 = result.getPromptChoices().get(1);
        assertThat(Arrays.asList(p1.getCode().length(), p1.getLabel(), p1.getType(), p1.getRequired())
                , containsInAnyOrder(Arrays.asList(36, "Prohibited activities", TXT, Boolean.TRUE).toArray()));
        assertThat(Arrays.asList(p2.getCode().length(), p2.getLabel(), p2.getType(), p2.getRequired())
                , containsInAnyOrder(Arrays.asList(36, "Period of prohibition", DURATION, Boolean.TRUE).toArray()));
        final List<Children> childrenList = p2.getChildren();
        final Children c1 = childrenList.get(0);
        final Children c2 = childrenList.get(1);
        final Children c3 = childrenList.get(2);
        assertThat(Arrays.asList(c1.getType(), c1.getLabel())
                , containsInAnyOrder(Arrays.asList(INT, "Years").toArray()));
        assertThat(Arrays.asList(c2.getType(), c2.getLabel())
                , containsInAnyOrder(Arrays.asList(INT, "Months").toArray()));
        assertThat(Arrays.asList(c3.getType(), c3.getLabel())
                , containsInAnyOrder(Arrays.asList(INT, "Weeks").toArray()));
    }

    @Test
    public void testBuildKnowledgeWithOneOf() {

        final Knowledge knowledge = new Knowledge();
        final PromptChoice promptChoice = new PromptChoice();
        final Children children1 =  new Children("Fixed Date", ResultType.DATE, promptChoice.getFixedList(), promptChoice.getChildren(), "aea2ee79-47b4-4023-9a95-1b327e6e03d5");
        final Children children2 =  new Children("Week Commencing", ResultType.DATE, promptChoice.getFixedList(), promptChoice.getChildren(), "3e7ae7bd-f736-4729-ab06-82bf966bc72f");
        promptChoice.addChildren(children1);
        promptChoice.addChildren(children2);

        final List<PromptChoice> promptChoices = new ArrayList<>();
        promptChoice.setLabel("Fixed Date");
        promptChoice.setType(ResultType.DATE);
        promptChoice.setCode(UUID.randomUUID().toString());
        promptChoice.setComponentType(ONEOF);
        promptChoices.add(promptChoice);
        knowledge.setPromptChoices(promptChoices);
        final ResultPromptView result = testObj.buildFromKnowledge(knowledge);

        result.getPromptChoices().forEach(pc -> {
            pc.getChildren().forEach(c -> {
                assertThat(c.getCode(), is(notNullValue()));
                assertThat(c.getLabel(), is(notNullValue()));
                assertThat(c.getType(), is(notNullValue()));
            });
        });
    }

    @Test
    public void buildFromKnowledgeWithOneOf() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("ATRNR");
        final Knowledge knowledge = processor.processParts(parts.stream().map(Part::getValueAsString).collect(Collectors.toList()), LocalDate.now());
        final ResultDefinitionView resultDefinitionView = new ResultDefinitionViewBuilder().buildFromKnowledge(parts, knowledge, new ArrayList<>(),true);

        final ResultPromptView result = testObj.buildFromKnowledge(processor.processResultPrompt(resultDefinitionView.getResultCode(), LocalDate.now()));

        final ObjectMapper objectMapper = new ObjectMapper();
        final String JsonResponse = objectMapper.writeValueAsString(result);
        Log.info("Response : " + JsonResponse);

        assertThat(result.getPromptChoices().size()
                , is(5)
        );
        final PromptChoice p1 = result.getPromptChoices().get(0);
        final PromptChoice p2 = result.getPromptChoices().get(1);
        final PromptChoice p3 = result.getPromptChoices().get(2);
        final PromptChoice p4 = result.getPromptChoices().get(3);
        final PromptChoice p5 = result.getPromptChoices().get(4);

        assertThat(Arrays.asList(p1.getCode().length(), p1.getLabel(), p1.getType(), p1.getRequired(), p1.getHidden())
                , containsInAnyOrder(Arrays.asList(36, "Treatment institution / place", TXT, Boolean.TRUE, Boolean.FALSE).toArray()));

        assertThat(Arrays.asList(p2.getCode().length(), p2.getLabel(), p2.getType(), p2.getRequired(), p2.getHidden())
                , containsInAnyOrder(Arrays.asList(36, "Under direction of", TXT, Boolean.TRUE, Boolean.FALSE).toArray()));

        assertThat(Arrays.asList(p3.getCode().length(), p3.getLabel(), p3.getType(), p3.getRequired(), p3.getHidden())
                , containsInAnyOrder(Arrays.asList(36, "Period of treatment", DURATION, Boolean.TRUE, Boolean.FALSE).toArray()));

        assertThat(Arrays.asList(p4.getCode().length(), p4.getLabel(), p4.getComponentType(), p4.getRequired(), p4.getHidden())
                , containsInAnyOrder(Arrays.asList(36, "Period of treatment-oneOf", "ONEOF", Boolean.TRUE, Boolean.FALSE).toArray()));

        assertThat(Arrays.asList(p5.getCode().length(), p5.getLabel(), p5.getType(), p5.getRequired(), p5.getHidden())
                , containsInAnyOrder(Arrays.asList(36, "Treatment intervals", TXT, Boolean.FALSE, Boolean.FALSE).toArray()));

        final List<Children> childrenList = p3.getChildren();
        final Children c1 = childrenList.get(0);
        final Children c2 = childrenList.get(1);
        final Children c3 = childrenList.get(2);
        final Children c4 = childrenList.get(3);
        assertThat(Arrays.asList(c1.getType(), c1.getLabel()), containsInAnyOrder(Arrays.asList(INT, "Years").toArray()));
        assertThat(Arrays.asList(c2.getType(), c2.getLabel()), containsInAnyOrder(Arrays.asList(INT, "Months").toArray()));
        assertThat(Arrays.asList(c3.getType(), c3.getLabel()), containsInAnyOrder(Arrays.asList(INT, "Weeks").toArray()));
        assertThat(Arrays.asList(c4.getType(), c4.getLabel()), containsInAnyOrder(Arrays.asList(INT, "Days").toArray()));

        final List<Children> oneOfChildrenList = p4.getChildren();
        assertThat(oneOfChildrenList.size(), is(3));
        final Children gc1 = oneOfChildrenList.get(0);
        final Children gc2 = oneOfChildrenList.get(1);
        final Children gc3 = oneOfChildrenList.get(2);

        assertThat(Arrays.asList(gc1.getType(), gc1.getLabel()), containsInAnyOrder(Arrays.asList(TXT, "Additional information").toArray()));
        assertThat(Arrays.asList(gc2.getType(), gc2.getLabel()), containsInAnyOrder(Arrays.asList(FIXL, "Conviction / acquittal").toArray()));
        assertThat(Arrays.asList(gc3.getType(), gc3.getLabel()), containsInAnyOrder(Arrays.asList(DURATION, "Period of treatment-oneOf").toArray()));

        final List<Children> oneOfGrandChildrenList = gc3.getChildrenList();
        final Children gcc1 = oneOfGrandChildrenList.get(0);
        final Children gcc2 = oneOfGrandChildrenList.get(1);
        final Children gcc3 = oneOfGrandChildrenList.get(2);
        final Children gcc4 = oneOfGrandChildrenList.get(3);
        assertThat(Arrays.asList(gcc1.getType(), gcc1.getLabel()), containsInAnyOrder(Arrays.asList(INT, "Years").toArray()));
        assertThat(Arrays.asList(gcc2.getType(), gcc2.getLabel()), containsInAnyOrder(Arrays.asList(INT, "Months").toArray()));
        assertThat(Arrays.asList(gcc3.getType(), gcc3.getLabel()), containsInAnyOrder(Arrays.asList(INT, "Weeks").toArray()));
        assertThat(Arrays.asList(gcc4.getType(), gcc4.getLabel()), containsInAnyOrder(Arrays.asList(INT, "Days").toArray()));
    }

}