package uk.gov.moj.cpp.hearing.listing.common.xhibit;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class XhibitServiceTest {

    @Mock
    private Logger logger;

    @Mock
    private InputStream content;

    @Mock
    private XhibitSession ftpsSession;

    @Mock
    private XhibitSessionFactory xhibitSessionFactory;

    @InjectMocks
    @Spy
    private DefaultXhibitService xhibitService;

    @Test
    public void shouldSendExportFileToXhibitWithInputStream() throws Exception {

        final String documentName = "Xhibit_000001_20181212121211.xml";

        when(xhibitSessionFactory.createSession()).thenReturn(ftpsSession);

        xhibitService.sendToXhibit(content, documentName);

        final InOrder inOrder = inOrder(ftpsSession, logger, xhibitSessionFactory);

        inOrder.verify(xhibitSessionFactory).createSession();
        inOrder.verify(ftpsSession).exportFile(documentName, content);
        inOrder.verify(logger).info("Listing: File '" + documentName + "' successfully exported to Xhibit");

    }

    @Test
    public void shouldFailExportFileToXhibitWithInputStreamWhenExhibitNotReachable() throws Exception {

        final String documentName = "Xhibit_000001_20161212121212.xml";

        when(xhibitSessionFactory.createSession()).thenReturn(ftpsSession);
        doThrow(Exception2.class).when(ftpsSession).exportFile(documentName, content);
        final InOrder inOrder = inOrder(logger);

        try {
            xhibitService.sendToXhibit(content, documentName);
            fail();
        } catch (final ExportFailedException e) {
            final String errorMessage = format("Failed to send to Xhibit,%s, for document with name: %s", "Not reachable", documentName);
            assertThat(e.getMessage(), is(errorMessage));
            inOrder.verify(logger).info("Listing: Sending file '" + documentName + "' to Xhibit");
            inOrder.verify(logger).error(errorMessage);
        }
    }

    private class Exception2 extends ExportFailedException {

        public Exception2(final String message,
                          final Throwable cause) {
            super(message, cause);
        }

        @Override
        public String getMessage() {
            return "Not reachable";
        }
    }
}
