package uk.gov.moj.cpp.hearing.event.service;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.time.LocalDate;

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
        final AllResultDefinitions allResultDefinitions = new AllResultDefinitions()
                .setResultDefinitions(asList(
                        ResultDefinition.resultDefinition()
                                .setId(randomUUID())
                ));

        final LocalDate referenceDate = LocalDate.now();

        when(nowsReferenceDataLoader.loadAllResultDefinitions(null, referenceDate)).thenReturn(allResultDefinitions);

        final ResultDefinition results = target.getResultDefinitionById(null, referenceDate, allResultDefinitions.getResultDefinitions().get(0).getId());

        assertThat(results, is(allResultDefinitions.getResultDefinitions().get(0)));
    }

    @Test
    public void getResultDefinitionById_whenResultIdIsInvalid_thenReturnNull() {
        final AllResultDefinitions allResultDefinitions = new AllResultDefinitions();

        final LocalDate referenceDate = LocalDate.now();

        when(nowsReferenceDataLoader.loadAllResultDefinitions(null, referenceDate)).thenReturn(allResultDefinitions);

        final ResultDefinition results = target.getResultDefinitionById(null, referenceDate, randomUUID());

        assertThat(results, nullValue());
    }
}
