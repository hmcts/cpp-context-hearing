package uk.gov.moj.cpp.hearing.event.service;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllFixedList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.FixedList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.FixedListElement;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class NowsReferenceCacheTest {

    @Mock
    NowsReferenceDataLoader nowsReferenceDataLoader;

    @InjectMocks
    NowsReferenceCache target;

    @Test
    public void loadAllNowsReference_shouldReturnValue() throws Exception {

        final AllNows allNows = new AllNows();

        final LocalDate referenceDate = LocalDate.now();

        when(nowsReferenceDataLoader.loadAllNowsReference(null, referenceDate)).thenReturn(allNows);

        final AllNows results = target.getAllNows(null, referenceDate);

        assertThat(results, is(allNows));
    }

    @Test
    public void getResultDefinitionById_shouldReturnValue() {
        Map<UUID, TreeNode<ResultDefinition>> map = new HashMap<>();
        ResultDefinition resultDefinition = buildResultDefinition();
        map.put(resultDefinition.getId(), new TreeNode(resultDefinition.getId(), resultDefinition));

        final LocalDate referenceDate = LocalDate.now();

        when(nowsReferenceDataLoader.loadAllResultDefinitionAsTree(null, referenceDate)).thenReturn(map);

        final ResultDefinition results = target.getResultDefinitionById(null, referenceDate, resultDefinition.getId()).getData();

        assertThat(results, is(resultDefinition));
        assertThat(results.getPostHearingCustodyStatus(), is("C"));
        assertThat(results.getLifeDuration(), is(true));
        assertThat(results.getPublishedAsAPrompt(), is(false));
        assertThat(results.getExcludedFromResults(), is(false));
        assertThat(results.getAlwaysPublished(), is(true));
        assertThat(results.getUrgent(), is(false));
        assertThat(results.getD20(), is(false));
        assertThat(results.getTerminatesOffenceProceedings(), is(false));
        final Optional<Prompt> promptOptional = results.getPrompts().stream().findFirst();
        assertThat(promptOptional.isPresent(), is(true));
        assertThat(promptOptional.get().getDurationSequence(), is(5));
    }

    @Test
    public void getResultDefinitionById_whenResultIdIsInvalid_thenReturnNull() {

        final LocalDate referenceDate = LocalDate.now();

        when(nowsReferenceDataLoader.loadAllResultDefinitionAsTree(null, referenceDate)).thenReturn(new HashMap<>());

        TreeNode<ResultDefinition> results = target.getResultDefinitionById(null, referenceDate, randomUUID());

        assertThat(results, nullValue());
    }

    @Test
    public void getFixedListById_shouldReturnValue() {
        final AllFixedList getAllFixedList = AllFixedList.allFixedList().setFixedListCollection(
                asList(FixedList.fixedList()
                        .setId(UUID.randomUUID())
                        .setStartDate(LocalDate.now())
                        .setEndDate(LocalDate.now())
                        .setElements(asList(FixedListElement.fixedListElement()
                                .setCode("code")
                                .setValue("value")
                                .setWelshValue("welshCode")
                                .setCjsQualifier("cjsQualifiers")
                        ))

                )

        );
        final LocalDate referenceDate = LocalDate.now();

        when(nowsReferenceDataLoader.loadAllFixedList(null, referenceDate)).thenReturn(getAllFixedList);

        final AllFixedList results = target.getAllFixedList(null, referenceDate);

        assertThat(results, is(getAllFixedList));
        assertThat(results.getFixedListCollection().get(0).getId(), is(getAllFixedList.getFixedListCollection().get(0).getId()));
    }

    private ResultDefinition buildResultDefinition() {
        final ResultDefinition resultDefinition = new ResultDefinition();
        resultDefinition.setId(randomUUID());
        resultDefinition.setPostHearingCustodyStatus("C");
        resultDefinition.setLifeDuration(true);
        resultDefinition.setPublishedAsAPrompt(false);
        resultDefinition.setTerminatesOffenceProceedings(false);
        resultDefinition.setExcludedFromResults(false);
        resultDefinition.setAlwaysPublished(true);
        resultDefinition.setUrgent(false);
        resultDefinition.setD20(false);
        final Prompt prompt = new Prompt();
        prompt.setDurationSequence(5);
        resultDefinition.setPrompts(of(prompt));
        return resultDefinition;
    }
}
