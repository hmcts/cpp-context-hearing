package uk.gov.moj.cpp.hearing.listing.common.xhibit;

import static com.github.sardine.SardineFactory.begin;

import com.github.sardine.Sardine;

public class SardineClientFactory {

    public Sardine createSardineClient(final String username, final String password) {
        return begin(username, password);
    }
}
