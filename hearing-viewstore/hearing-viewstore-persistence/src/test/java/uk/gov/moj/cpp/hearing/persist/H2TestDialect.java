package uk.gov.moj.cpp.hearing.persist;

import java.sql.Types;

import org.hibernate.dialect.HSQLDialect;

/**
 * Test dialect for the in-memory H2 database.
 *
 * H2 2.x treats BINARY(n) as a fixed-length type and zero-pads stored values,
 * so UUID identifiers (persisted as 16-byte binary) no longer match query
 * parameters. All BINARY columns in this schema back java.util.UUID fields,
 * so they are mapped to H2's native UUID column type instead: this both
 * restores exact-length storage/equality semantics and makes
 * CAST(uuidColumn AS VARCHAR) render the proper hyphenated UUID string
 * (matching production Postgres uuid columns), rather than lossily decoding
 * the raw binary bytes as text.
 */
public class H2TestDialect extends HSQLDialect {

    public H2TestDialect() {
        super();
        registerColumnType(Types.BINARY, "uuid");
    }
}
