package uk.gov.justice.ccr.notepad.process;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;


public class CompareDefinitionKeywordsUsingIndexesTest {
    ResultCache resultCache = new ResultCache();
    FindDefinitionsIndexesByKeyword findDefinitionsIndexesByKeyword = new FindDefinitionsIndexesByKeyword();
    CompareDefinitionKeywordsUsingIndexes testObj = new CompareDefinitionKeywordsUsingIndexes();

    @Before
    public void init() throws ExecutionException {
        resultCache.loadResultCache();
        findDefinitionsIndexesByKeyword.resultCache = resultCache;
        testObj.resultCache = resultCache;
    }

    @Test
    public void run() throws Exception {

        //Given
        List<String> words = Arrays.asList("imprisonment", "suspended");
        List<Long> resultDefinitions = findDefinitionsIndexesByKeyword.run(words);
        GroupResultByIndex groupResultByIndex = new GroupResultByIndex();
        Map<Long, Long> outPut = groupResultByIndex.run(resultDefinitions);
        Set<Long> indexes = outPut.entrySet().stream().filter(v -> v.getValue() == 2).collect(Collectors.toMap(Entry::getKey, Entry::getValue)).keySet();
        Map<List<String>, Set<Long>> input = Maps.newHashMap();
        input.putIfAbsent(words, indexes);

        //When
        ResultDefinition output = testObj.run(input).get(0);

        //Then
        assertThat(
                output.getLabel()
                , is("Suspended sentence order - imprisonment")
        );
    }

    @Test
    public void run_WhenIndexEmpty() throws Exception {

        //Given
        List<String> words = Arrays.asList("imprisonment", "suspended");
        List<Long> resultDefinitions = findDefinitionsIndexesByKeyword.run(words);
        GroupResultByIndex groupResultByIndex = new GroupResultByIndex();
        Map<Long, Long> outPut = groupResultByIndex.run(resultDefinitions);
        Set<Long> indexes = outPut.entrySet().stream().filter(v -> v.getValue() == 3).collect(Collectors.toMap(Entry::getKey, Entry::getValue)).keySet();
        Map<List<String>, Set<Long>> input = Maps.newHashMap();
        input.putIfAbsent(words, indexes);

        //When
        List<ResultDefinition> output = testObj.run(input);

        //Then
        assertThat(
                output.size()
                , is(0)
        );
    }

    @Test
    public void run_WhenWordsEmpty() throws Exception {

        //Given
        List<String> words = Arrays.asList("imprisonment", "suspended");
        List<Long> resultDefinitions = findDefinitionsIndexesByKeyword.run(words);
        GroupResultByIndex groupResultByIndex = new GroupResultByIndex();
        Map<Long, Long> outPut = groupResultByIndex.run(resultDefinitions);
        Set<Long> indexes = outPut.entrySet().stream().filter(v -> v.getValue() == 3).collect(Collectors.toMap(Entry::getKey,Entry::getValue)).keySet();
        Map<List<String>, Set<Long>> input = Maps.newHashMap();
        input.putIfAbsent(new ArrayList<>(), indexes);

        //When
        List<ResultDefinition> output = testObj.run(input);

        //Then
        assertThat(
                output.size()
                , is(0)
        );
    }
}