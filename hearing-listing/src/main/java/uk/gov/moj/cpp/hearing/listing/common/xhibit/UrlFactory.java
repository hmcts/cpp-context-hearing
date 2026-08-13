package uk.gov.moj.cpp.hearing.listing.common.xhibit;

import static java.lang.String.format;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlFactory {

    public URL create(final String spec) {
        try {
            return new URL(spec);
        } catch (final MalformedURLException e) {
            throw new UrlCreationFailedException(format("Failed to create url from '%s'", spec), e);
        }
    }

    public String toUrl(final URL url, final String filename) {
        final String baseUrl = url.toExternalForm();

        final StringBuilder newUrl = new StringBuilder(baseUrl);
        if (!baseUrl.endsWith("/")) {
            newUrl.append("/");
        }

        return newUrl.append(filename).toString();
    }
}
