package uk.gov.moj.cpp.hearing.query.api.util;

import static java.nio.charset.Charset.defaultCharset;
import static javax.json.Json.createReader;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import javax.json.JsonObject;
import javax.json.JsonReader;

import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    public static String getPayload(final String path) {
        String request = null;
        try {
            request = Resources.toString(Resources.getResource(path), defaultCharset());
        } catch (final Exception e) {
            LOGGER.error("Error consuming file from location {}", path, e);
            fail("Error consuming file from location " + path);
        }
        return request;
    }
}
