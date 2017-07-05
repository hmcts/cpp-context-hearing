package uk.gov.justice.ccr.notepad.result.loader;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceFileReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceFileReader.class);

    public final List<String> getLines(final String filePath, final boolean skipHeader) {
        List<String> lines = newArrayList();
        try (InputStream fileInputStream = this.getClass().getResourceAsStream(filePath)) {
            if (fileInputStream == null) {
                throw new ResourceFileProcessingException("File does not exist : " + filePath);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));

            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }

        } catch (IOException e) {
            LOGGER.error("Error while processing the content  ", e);
        }
        return lines.stream()
                .skip(skipHeader ? 1 : 0)
                .collect(toList());
    }

}