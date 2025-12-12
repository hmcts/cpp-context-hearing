package uk.gov.moj.cpp.hearing.xhibit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import static org.xmlunit.builder.DiffBuilder.compare;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

public class XmlTestUtils {

    private static final Logger LOGGER = getLogger(XmlTestUtils.class);

    public static void assertXmlEquals(final String actualXml, final String expectedXmlResourceName) throws IOException {

        final String expectedXml = loadResourceFile(expectedXmlResourceName);

        final Diff xmlDiff = compare(expectedXml).withTest(actualXml).build();

        final Iterator<Difference> iter = xmlDiff.getDifferences().iterator();
        int size = 0;
        while (iter.hasNext()) {
            LOGGER.info(iter.next().toString());
            size++;
        }
        assertThat("XML differences", size, is(0));
    }

    public static String loadResourceFile(final String resourceName) throws IOException {
        try (final InputStream configurationStream = XmlTestUtils.class.getClassLoader().getResourceAsStream(resourceName)) {
            return IOUtils.toString(configurationStream);
        }
    }
}
