package uk.gov.justice.ccr.notepad.result.loader;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import uk.gov.justice.ccr.notepad.result.loader.converter.StringToResultDefinitionConverter;

import java.util.Objects;

import org.junit.Test;


public class ResourceFileReaderTest {

    private StringToResultDefinitionConverter converter = new StringToResultDefinitionConverter();

    @Test
    public void getLines() {
        assertThat(new ResourceFileReader().getLines("/file-store/result-definitions.tdf", true)
                .stream().map(converter::convert).filter(Objects::nonNull).collect(toList()).size() > 1, is(true));
    }

    @Test(expected = ResourceFileProcessingException.class)
    public void getLinesWhenNoFileFoundAtResource() {
        assertThat(new ResourceFileReader().getLines("/dummy", true)
                .stream().map(converter::convert).filter(Objects::nonNull).collect(toList()).size() > 1, is(true));
    }

}
