package uk.gov.moj.cpp.hearing.listing.common.xhibit;

import java.io.InputStream;

public interface XhibitService {

    void sendToXhibit(final InputStream inputStream, final String fileName) throws ExportFailedException;

}
