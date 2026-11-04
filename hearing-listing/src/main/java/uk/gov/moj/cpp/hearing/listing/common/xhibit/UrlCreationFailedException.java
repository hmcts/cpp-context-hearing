package uk.gov.moj.cpp.hearing.listing.common.xhibit;

import java.net.MalformedURLException;

public class UrlCreationFailedException extends RuntimeException {
    public UrlCreationFailedException(final String message, final MalformedURLException e) {
        super(message, e);
    }
}
