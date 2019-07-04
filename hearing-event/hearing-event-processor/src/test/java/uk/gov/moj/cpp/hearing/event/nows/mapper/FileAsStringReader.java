package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.readFileToString;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;

public class FileAsStringReader {

    public String readFile(final String path) {
        final URL url = this.getClass().getResource(path);
        final File fileFromClasspath;
        try {
            fileFromClasspath = new File(url.toURI());
            return readFileToString(fileFromClasspath, "UTF8");
        } catch (URISyntaxException e) {
            throw new UncheckedIOException(new IOException(format("Failed to load file \'%s\' from classpath",path), e));
        } catch (IOException e) {
            throw new UncheckedIOException(format("Failed to read file at path %s", path), e);
        }
    }

    public String readFile(final String path, final Object... placeholders) {
        return format(readFile(path), placeholders);
    }
}
