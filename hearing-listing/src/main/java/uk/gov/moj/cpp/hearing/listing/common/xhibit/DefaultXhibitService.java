package uk.gov.moj.cpp.hearing.listing.common.xhibit;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

@ApplicationScoped
public class DefaultXhibitService implements XhibitService {

    @SuppressWarnings("squid:S1312")
    @Inject
    private Logger logger;

    @Inject
    private XhibitSessionFactory xhibitSessionFactory;

    public void sendToXhibit(final InputStream inputStream, final String fileName) throws ExportFailedException {
        send(inputStream, fileName, null);
    }

    @SuppressWarnings({"squid:S1166", "squid:S1162", "squid:S2139", "squid:S2629"})
    private void send(final InputStream inputStream, final String fileName, final UUID fileId) throws ExportFailedException {
        logger.info(format("Listing: Sending file '%s' to Xhibit", fileName));
        try (final XhibitSession xhibitSession = xhibitSessionFactory.createSession()) {
            xhibitSession.exportFile(fileName, inputStream);
            logger.info(format("Listing: File '%s' successfully exported to Xhibit", fileName));
        } catch (final ExportFailedException | IOException e) {
            final String message = fileId != null ?
                    format("Failed to send to Xhibit,%s, for document with id: %s and name: %s", e.getMessage(), fileId, fileName) :
                    format("Failed to send to Xhibit,%s, for document with name: %s", e.getMessage(), fileName);
            logger.error(message);
            throw new ExportFailedException(message, e.getCause());
        }
    }
}
