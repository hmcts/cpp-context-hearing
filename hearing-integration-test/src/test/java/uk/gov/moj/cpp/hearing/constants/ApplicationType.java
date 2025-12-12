package uk.gov.moj.cpp.hearing.constants;

import static java.util.UUID.fromString;

import java.util.UUID;

public enum ApplicationType {

    APPLICATION_TO_REOPEN_CASE("44c238d9-3bc2-3cf3-a2eb-a7d1437b8383"),
    APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_SJP_CASE("7375727f-30fc-3f55-99f3-36adc4f0e70e"),
    APPEARANCE_TO_MAKE_STATUTORY_DECLARATION("f3a6e917-7cc8-3c66-83dd-d958abd6a6e4"),
    APPEAL_AGAINST_SENTENCE("beb08419-0a9a-3119-b3ec-038d56c8a718"),
    APPEAL_AGAINST_CONVICTION("57810183-a5c2-3195-8748-c6b97eda1ebd");

    private UUID id;

    ApplicationType(final String id) {
        this.id = fromString(id);
    }

    public UUID getId() {
        return id;
    }
}
