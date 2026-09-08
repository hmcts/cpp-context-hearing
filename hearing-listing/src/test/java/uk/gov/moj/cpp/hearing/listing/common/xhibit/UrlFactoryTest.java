package uk.gov.moj.cpp.hearing.listing.common.xhibit;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class UrlFactoryTest {

    @InjectMocks
    private UrlFactory urlFactory;

    @Test
    public void shouldCreateAUrlFromTheUrlSpecString() throws Exception {

        final String spec = "http://www.google.com";

        final URL url = urlFactory.create(spec);

        assertThat(url.getProtocol(), is("http"));
        assertThat(url.getHost(), is("www.google.com"));
    }

    @Test
    public void shouldThrowARuntimeExceptionIfTheUrlIsMalformed() throws Exception {

        try {
            urlFactory.create("something silly");
            fail();
        } catch (final RuntimeException expected) {
            assertThat(expected.getMessage(), is("Failed to create url from 'something silly'"));
            assertThat(expected.getCause(), is(instanceOf(MalformedURLException.class)));
        }
    }

    @Test
    public void shouldCreateAUrlStringWithAppendedFilename() throws Exception {

        final URL url = new URL("http://www.google.com");

        assertThat(urlFactory.toUrl(url, "robots.txt"), is("http://www.google.com/robots.txt"));
    }

    @Test
    public void shouldAppendASlashWhenCreatingUrlFromFilenameIfRequired() throws Exception {

        final URL url = new URL("http://www.google.com/");

        assertThat(urlFactory.toUrl(url, "robots.txt"), is("http://www.google.com/robots.txt"));
    }
}
