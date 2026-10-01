package uk.gov.moj.cpp.hearing.listing.common.xhibit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.github.sardine.Sardine;
import org.apache.http.client.HttpResponseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class XhibitSessionTest {

    @Mock
    private Sardine client;

    @Mock
    private Logger logger;

    @Mock
    private InputStream fileContents;

    private XhibitSession xhibitSession;

    private XhibitSession xhibitSessionSpy;

    private XhibitSession xhibitSessionWithTrailingSlash;

    @BeforeEach
    public void createClassAndMocks() throws Exception {
        xhibitSession = new XhibitSession(new URL("http://outbound.com"), client, logger);
        xhibitSessionSpy = spy(xhibitSession);
        xhibitSessionWithTrailingSlash = new XhibitSession(new URL("http://outbound.com"), client, logger);
    }


    @Test
    public void shouldPutFileToOutboundUrlOnArchive() throws Exception {

        final String filename = "filename";
        xhibitSession.exportFile(filename, fileContents);

        verify(client).put("http://outbound.com/filename", fileContents);
    }

    @Test
    public void shouldHandleTrailingSlashWhenPuttingFileToOutboundUrlOnArchive() throws Exception {

        final String filename = "filename";
        xhibitSession.exportFile(filename, fileContents);

        verify(client).put("http://outbound.com/filename", fileContents);
    }

    @Test
    public void shouldThrowAExportToLibraFailedExceptionIfExportingFileThrowsAnHttpResponseException() throws Exception {

        final HttpResponseException httpResponseException = new HttpResponseException(404, "Ooops");
        final String filename = "filename";

        final String expectedMessage = "CPF01: Failed to put file 'filename' " +
                "to 'http://outbound.com/filename'. " +
                "Response status: 404";

        doThrow(httpResponseException).when(client).put("http://outbound.com/filename", fileContents);

        try {
            xhibitSessionSpy.exportFile(filename, fileContents);
            fail();
        } catch (final ExportFailedException expected) {
            assertThat(expected.getCause(), is(httpResponseException));
            assertThat(expected.getMessage(), is(expectedMessage));
        }

        verify(logger).error(expectedMessage, httpResponseException.getMessage());
    }

    @Test
    public void shouldThrowAExportToLibraFailedExceptionIfExportingFileThrowsAnIOException() throws Exception {

        final IOException ioException = new IOException("Ooops");
        final String filename = "filename";

        final String expectedMessage = "CPF01: Failed to put file 'filename' to 'http://outbound.com/filename'";

        doThrow(ioException).when(client).put("http://outbound.com/filename", fileContents);

        try {
            xhibitSessionSpy.exportFile(filename, fileContents);
            fail();
        } catch (final ExportFailedException expected) {
            assertThat(expected.getCause(), is(ioException));
            assertThat(expected.getMessage(), is(expectedMessage));
        }

        verify(logger).error(expectedMessage, ioException.getMessage());
    }

    @Test
    public void shouldCloseTheClient() throws Exception {

        xhibitSession.close();

        verify(client).shutdown();
    }
}
