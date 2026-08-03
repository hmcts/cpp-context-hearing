package uk.gov.moj.cpp.hearing.persist;

import java.sql.Types;

import org.hibernate.dialect.HSQLDialect;

/**
 * Test dialect for the in-memory H2 database.
 *
 * H2 2.x treats BINARY(n) as a fixed-length type and zero-pads stored values,
 * so UUID identifiers (persisted as 16-byte binary) no longer match query
 * parameters. Mapping BINARY to VARBINARY restores the exact-length storage
 * behaviour of H2 1.4 that the repository tests rely on.
 */
public class H2TestDialect extends HSQLDialect {

    public H2TestDialect() {
        super();
        registerColumnType(Types.BINARY, "varbinary($l)");
    }
}
