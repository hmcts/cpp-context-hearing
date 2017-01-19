package uk.gov.moj.cpp.hearing.it;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIT.class);
    private static final String ENDPOINT_PROPERTIES_FILE = "endpoint.properties";

    protected static final Properties ENDPOINT_PROPERTIES = new Properties();

    protected static RequestSpecification requestSpec;

    private static String baseUri;

    static  {
        readConfig();
        setRequestSpecification();
    }

    private static void readConfig() {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try(final InputStream stream = loader.getResourceAsStream(ENDPOINT_PROPERTIES_FILE)) {
            ENDPOINT_PROPERTIES.load(stream);
        } catch (final IOException e) {
            LOGGER.warn("Error reading properties from {}", ENDPOINT_PROPERTIES_FILE, e);
        }
        String baseUriProp = System.getProperty("INTEGRATION_HOST_KEY");
        baseUri = isNotEmpty(baseUriProp) ? format("http://%s:8080", baseUriProp) : ENDPOINT_PROPERTIES.getProperty("base-uri");
    }

    private static void setRequestSpecification() {
        requestSpec = new RequestSpecBuilder().setBaseUri(baseUri).build();
    }
}
