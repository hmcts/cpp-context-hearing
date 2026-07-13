package uk.gov.moj.cpp.hearing.persist.type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.junit.jupiter.api.Test;

class UnspecifiedUuidUserTypeTest {

    private final UnspecifiedUuidUserType userType = new UnspecifiedUuidUserType();

    @Test
    void shouldReportUuidReturnedClassAndVarcharSqlTypeAndBeImmutable() {
        assertThat(userType.returnedClass(), is(UUID.class));
        assertThat(userType.getSqlType(), is(Types.VARCHAR));
        assertThat(userType.isMutable(), is(false));
    }

    @Test
    void shouldReadUuidFromStringColumn() throws SQLException {
        final UUID uuid = UUID.randomUUID();
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString(1)).thenReturn(uuid.toString());

        assertThat(userType.nullSafeGet(resultSet, 1, null, null), is(uuid));
    }

    @Test
    void shouldReadNullWhenColumnIsNull() throws SQLException {
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString(1)).thenReturn(null);

        assertThat(userType.nullSafeGet(resultSet, 1, null, null), is(nullValue()));
    }

    @Test
    void shouldBindViaJdbcOtherOnPostgres() throws SQLException {
        final UUID uuid = UUID.randomUUID();
        final PreparedStatement statement = mock(PreparedStatement.class);

        userType.nullSafeSet(statement, uuid, 1, sessionWithDialect(mock(PostgreSQLDialect.class)));

        verify(statement).setObject(1, uuid.toString(), Types.OTHER);
    }

    @Test
    void shouldBindAsStringOnNonPostgres() throws SQLException {
        final UUID uuid = UUID.randomUUID();
        final PreparedStatement statement = mock(PreparedStatement.class);

        userType.nullSafeSet(statement, uuid, 1, sessionWithDialect(mock(H2Dialect.class)));

        verify(statement).setString(1, uuid.toString());
    }

    @Test
    void shouldBindNullAsOtherOnPostgres() throws SQLException {
        final PreparedStatement statement = mock(PreparedStatement.class);

        userType.nullSafeSet(statement, null, 1, sessionWithDialect(mock(PostgreSQLDialect.class)));

        verify(statement).setNull(1, Types.OTHER);
    }

    @Test
    void shouldBindNullAsVarcharOnNonPostgres() throws SQLException {
        final PreparedStatement statement = mock(PreparedStatement.class);

        userType.nullSafeSet(statement, null, 1, sessionWithDialect(mock(H2Dialect.class)));

        verify(statement).setNull(1, Types.VARCHAR);
    }

    @Test
    void shouldTreatUuidAsImmutableValue() {
        final UUID uuid = UUID.randomUUID();

        assertThat(userType.deepCopy(uuid), is(uuid));
        assertThat(userType.equals(uuid, uuid), is(true));
        assertThat(userType.equals(uuid, UUID.randomUUID()), is(false));
        assertThat(userType.disassemble(uuid), is(uuid));
        assertThat(userType.assemble(uuid, null), is(uuid));
    }

    private SharedSessionContractImplementor sessionWithDialect(final Dialect dialect) {
        final SharedSessionContractImplementor session = mock(SharedSessionContractImplementor.class);
        final JdbcServices jdbcServices = mock(JdbcServices.class);
        when(session.getJdbcServices()).thenReturn(jdbcServices);
        when(jdbcServices.getDialect()).thenReturn(dialect);
        return session;
    }
}
