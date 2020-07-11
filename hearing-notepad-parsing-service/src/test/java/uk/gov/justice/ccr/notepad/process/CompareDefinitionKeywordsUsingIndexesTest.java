package uk.gov.justice.ccr.notepad.process;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.ccr.notepad.shared.AbstractTest;

@RunWith(MockitoJUnitRunner.class)
public class CompareDefinitionKeywordsUsingIndexesTest extends AbstractTest {
    @InjectMocks
    CompareDefinitionKeywordsUsingIndexes target;

    @Test
    public void run() throws Exception {

        //Given
        Set<String> words = new HashSet<>(Arrays.asList("imprisonment", "suspended"));
        List<Long> resultDefinitions = findDefinitionsIndexesByKeyword.run(words, LocalDate.now());
        GroupResultByIndex groupResultByIndex = new GroupResultByIndex();
        Map<Long, Long> outPut = groupResultByIndex.run(resultDefinitions);
        Set<Long> indexes = outPut.entrySet().stream().filter(v -> v.getValue() == 2).collect(Collectors.toMap(Entry::getKey, Entry::getValue)).keySet();
        Map<Set<String>, Set<Long>> input = Maps.newHashMap();
        input.putIfAbsent(words, indexes);

        //When
        final List<ResultDefinition> definitions = target.run(input, LocalDate.now());
        ResultDefinition output = definitions.get(0);

        //Then
        assertThat(
                output.getLabel()
                , is("Suspended sentence order - imprisonment")
        );
    }

    @Test
    public void run_WhenIndexEmpty() throws Exception {

        //Given
        Set<String> words = new HashSet<>(Arrays.asList("imprisonment", "suspended"));
        List<Long> resultDefinitions = findDefinitionsIndexesByKeyword.run(words, LocalDate.now());
        GroupResultByIndex groupResultByIndex = new GroupResultByIndex();
        Map<Long, Long> outPut = groupResultByIndex.run(resultDefinitions);
        Set<Long> indexes = outPut.entrySet().stream().filter(v -> v.getValue() == 3).collect(Collectors.toMap(Entry::getKey, Entry::getValue)).keySet();
        Map<Set<String>, Set<Long>> input = Maps.newHashMap();
        input.putIfAbsent(words, indexes);

        //When
        List<ResultDefinition> output = target.run(input, LocalDate.now());

        //Then
        assertThat(
                output.size()
                , is(0)
        );
    }

    @Test
    public void run_WhenWordsEmpty() throws Exception {

        //Given
        Set<String> words = new HashSet<>(Arrays.asList("imprisonment", "suspended"));
        List<Long> resultDefinitions = findDefinitionsIndexesByKeyword.run(words, LocalDate.now());
        GroupResultByIndex groupResultByIndex = new GroupResultByIndex();
        Map<Long, Long> outPut = groupResultByIndex.run(resultDefinitions);
        Set<Long> indexes = outPut.entrySet().stream().filter(v -> v.getValue() == 3).collect(Collectors.toMap(Entry::getKey, Entry::getValue)).keySet();
        Map<Set<String>, Set<Long>> input = Maps.newHashMap();
        input.putIfAbsent(newHashSet(), indexes);

        //When
        List<ResultDefinition> output = target.run(input, LocalDate.now());

        //Then
        assertThat(
                output.size()
                , is(0)
        );
    }
}
