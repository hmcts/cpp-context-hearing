package uk.gov.moj.cpp.hearing.persist.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

/**
 * Reads and binds a {@link UUID} via JDBC type {@code OTHER} so PostgreSQL infers the target column type at execution
 * time (the per-parameter equivalent of {@code stringtype=unspecified}).
 * <p>
 * This lets the SAME {@code @Embeddable} field work whether the underlying column is native {@code uuid}
 * (e.g. {@code associated_person}) or {@code text} (e.g. {@code ha_defendant}) — a legacy schema inconsistency that
 * Hibernate 6/7's strict {@code UUIDJdbcType} (getObject/setObject expecting a native uuid) can no longer bridge:
 * a {@code uuid} column returns/accepts {@link UUID}, a {@code text} column returns/accepts {@link String}.
 * <p>
 * On read, {@code getString} yields the canonical UUID string from both column types. On write,
 * {@code setObject(..., OTHER)} sends the value with an unspecified type OID so the server casts it to whichever type
 * the column actually is. Apply this only to the specific affected fields via {@code @Type}; do NOT use globally —
 * the estate's other UUID columns are native {@code uuid} and work with Hibernate's default mapping.
 */
public class UnspecifiedUuidUserType implements UserType<UUID> {

    @Override
    public int getSqlType() {
        // Portable DDL type (used only by hbm2ddl in tests; production/IT schema comes from Liquibase). VARCHAR maps on
        // both H2 and PostgreSQL. Actual bind/read is custom below so PG can still infer uuid vs text at runtime.
        return Types.VARCHAR;
    }

    @Override
    public Class<UUID> returnedClass() {
        return UUID.class;
    }

    @Override
    public boolean equals(final UUID x, final UUID y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(final UUID x) {
        return Objects.hashCode(x);
    }

    @Override
    public UUID nullSafeGet(final ResultSet rs, final int position, final SharedSessionContractImplementor session,
                            final Object owner) throws SQLException {
        final String value = rs.getString(position);
        return value == null ? null : UUID.fromString(value);
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final UUID value, final int index,
                            final SharedSessionContractImplementor session) throws SQLException {
        // On PostgreSQL bind via JDBC OTHER so the server infers the column type (uuid or text). Other databases
        // (H2 in unit tests, where OTHER means a serialized Java object) bind as a plain string against the varchar column.
        final boolean postgres = session.getJdbcServices().getDialect() instanceof PostgreSQLDialect;
        if (value == null) {
            st.setNull(index, postgres ? Types.OTHER : Types.VARCHAR);
        } else if (postgres) {
            st.setObject(index, value.toString(), Types.OTHER);
        } else {
            st.setString(index, value.toString());
        }
    }

    @Override
    public UUID deepCopy(final UUID value) {
        return value; // UUID is immutable
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(final UUID value) {
        return value; // UUID is Serializable
    }

    @Override
    public UUID assemble(final Serializable cached, final Object owner) {
        return (UUID) cached;
    }
}
