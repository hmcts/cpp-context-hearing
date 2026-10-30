# Hearing — UUID-in-TEXT Column Mapping Fix (Java 25 / WildFly 40 / Hibernate 6+ upgrade)

**Prepared by:** Platform Engineering (Java 25 upgrade spike)
**Status:** ✅ Full build green (`mm`, enforcer on, all unit tests). Integration-test validation in progress.
**Decision owner:** the **hearing team** — this document exists so you can review and either ratify or replace the fix below.

---

## 1. Executive summary (for planning / non-developers)

While upgrading the hearing service to **Java 25 / WildFly 40** (which brings a newer version of Hibernate, the library
that maps Java objects to database tables), we uncovered a **pre-existing inconsistency in the hearing database schema**
that older Hibernate silently tolerated but the new Hibernate does not.

**In plain terms:** the same piece of data — a defendant's *nationality* reference id — is stored as a **different column
type in two different tables**. In one table it is stored as a proper `uuid`; in another it is stored as free `text`. The
old library quietly coped with this mismatch. The new library is stricter and refused to read/write the `text` version,
which caused a large number of hearing operations to fail during testing (defendant/hearing details came back empty or
errored).

**Important:** we did **not** create this inconsistency — it already exists in the live schema. The upgrade merely exposed it.

**What we changed:** a small, tightly-scoped **code-only** change (no database/schema change, no data migration) that teaches
the application to handle both storage formats for just these two fields. Nothing else in the schema or the other ~200
correctly-typed columns is affected.

**Why code-only:** changing the database schema at the same time as a major framework upgrade was judged too risky, so a
schema migration was explicitly ruled out for this piece of work. The fix here is reversible and isolated.

**What we need from the hearing team:** confirm you are happy to carry this code-level workaround, **or** decide to fix the
underlying schema inconsistency properly at a later date (see options in §4). Either is fine — you own that call.

---

## 2. The problem in detail

The `Person` data (name, nationality, ethnicity, etc.) is modelled once as a reusable JPA `@Embeddable` and reused in two
different tables:

| Java field (`Person`)          | Column                              | Table               | Column type in the DB |
|--------------------------------|-------------------------------------|---------------------|-----------------------|
| `nationalityId` (`UUID`)       | `nationality_id`                    | `ha_defendant`      | **`text`**            |
| `additionalNationalityId`(`UUID`)| `additional_nationality_id`       | `ha_defendant`      | **`text`**            |
| `nationalityId` (`UUID`)       | `nationality_id`                    | `associated_person` | **`uuid`**            |
| `additionalNationalityId`(`UUID`)| `additional_nationality_id`       | `associated_person` | **`uuid`**            |

This is visible in the Liquibase changelog `hearing-view-store-db-changesets/045-new-view-changes-for-r24.xml`, which
creates these columns as `TEXT` for `ha_defendant` and as `UUID` for `associated_person`. The Java field type is `UUID`
in both cases (the model is correct; the storage is inconsistent).

### Why it worked before and breaks now

- **Before (Hibernate 5, Java 17):** the UUID type mapping was lenient — it would read a `text` column into a `UUID` and
  write a `UUID` into a `text` column without complaint.
- **After (Hibernate 6/7, Java 25 / WildFly 40):** UUID mapping is strict. By default it reads/writes UUID columns using
  the JDBC "native uuid" path (`getObject`/`setObject` expecting a `java.util.UUID`). A PostgreSQL `text` column returns a
  `String`, so:
  - **Read** fails with `ClassCastException: Cannot cast java.lang.String to java.util.UUID`.
  - **Write** (if you force string handling) fails with `column … is of type uuid but expression is of type character varying`.

### Observed impact (integration tests)

On the first full integration run after the upgrade, this single root cause produced **~2,300 low-level errors** and **181
integration-test failures**: reading a defendant/hearing initialised the `Person` embeddable, hit the `text`
`nationality_id`, threw, and the hearing/defendant projections came back empty (HTTP 500 / empty payloads), which cascaded
into query timeouts across the suite.

### How we pinned down the exact scope

Rather than guess from the schema files (some columns are altered across changelogs), we queried the **live view-store
database** for every `text`/`varchar` column that actually holds UUID-shaped values. The result was exactly **two**
columns: `ha_defendant.nationality_id` and `ha_defendant.additional_nationality_id`. Every other UUID column in hearing is
a native `uuid` and needs no change. (`ethnicity_id` is `text` in `ha_defendant` too, but it is **not** mapped as a `UUID`
field, so it is unaffected.)

---

## 3. Options considered

| # | Option | Verdict |
|---|--------|---------|
| A | **Normalise the schema** — `ALTER TABLE ha_defendant ALTER COLUMN … TYPE uuid USING …::uuid` so both tables use `uuid`. | **Rejected for now.** It is the cleanest long-term fix (TEXT is the anomaly), but it is a data migration on the live view-store and was explicitly ruled out alongside the framework upgrade. Left as a future option for the hearing team. |
| B | **Global Hibernate setting** `hibernate.type.preferred_uuid_jdbc_type = CHAR/VARCHAR`. | **Rejected.** Too broad — it changes handling for *all* ~200 native `uuid` columns and breaks writes to genuine `uuid` columns (binding a string to a `uuid` column fails unless the whole datasource runs in `stringtype=unspecified`). |
| C | **Datasource** `stringtype=unspecified` on the JDBC URL. | **Rejected.** A global connection-level behaviour change (infra config, all queries) to fix two fields — disproportionate and risky. |
| D | **Split the embeddable** into two variants (one for `text` tables, one for `uuid` tables). | **Rejected.** `Person` is reused widely; duplicating it is invasive and hard to maintain. |
| E | **Scoped custom Hibernate `UserType`** on just the two affected fields. | **✅ Chosen.** Code-only, no schema change, isolated to the two fields; the other columns keep Hibernate's default mapping. |

---

## 4. The fix we implemented (for developers)

A single small `UserType` applied to only the two affected fields.

**New class:** `uk.gov.moj.cpp.hearing.persist.type.UnspecifiedUuidUserType`
(`hearing-viewstore/hearing-viewstore-persistence/.../persist/type/UnspecifiedUuidUserType.java`)

**Applied via** `@Type(UnspecifiedUuidUserType.class)` on:
- `Person.nationalityId`
- `Person.additionalNationalityId`

### How it works

- **Read** (`nullSafeGet`): `resultSet.getString(...)` then `UUID.fromString(...)`. `getString` returns the canonical UUID
  string from **both** a `uuid` column and a `text` column, so reads work regardless of the underlying type.
- **Write** (`nullSafeSet`): **dialect-aware**
  - On **PostgreSQL** (runtime / integration tests / production): `setObject(index, uuid.toString(), java.sql.Types.OTHER)`.
    Binding as JDBC `OTHER` sends the value with an *unspecified* type, so **the database server infers the target column
    type** — it casts the string to `uuid` for the `uuid` column and stores it as `text` for the `text` column. This is the
    per-parameter equivalent of `stringtype=unspecified`, but scoped to these fields only.
  - On **H2** (used by the module's in-memory unit tests, where JDBC `OTHER` means "serialised Java object"): plain
    `setString(...)`.
- **`getSqlType()` returns `VARCHAR`**: this only influences Hibernate's schema-generation (`hbm2ddl`), which is used by the
  H2 unit tests. `VARCHAR` maps cleanly on both H2 and PostgreSQL. Production and integration tests do **not** generate the
  schema from entities (it comes from Liquibase), so this value has no effect on the real schema. (An earlier attempt using
  `OTHER` here broke the H2 unit tests because H2 has no DDL mapping for `OTHER`.)

### Why this is safe and minimal

- **Scoped:** only the two problem fields carry `@Type`. The ~200 native `uuid` columns are untouched and keep Hibernate's
  default (correct, most-efficient) mapping.
- **No schema/data change:** no Liquibase, no `ALTER TABLE`, no migration.
- **Reversible:** delete the annotation + class to revert. If the schema is normalised later (Option A), this workaround can
  be removed entirely.

### Files changed

- **Added:** `hearing-viewstore/hearing-viewstore-persistence/src/main/java/uk/gov/moj/cpp/hearing/persist/type/UnspecifiedUuidUserType.java`
- **Modified:** `hearing-viewstore/hearing-viewstore-persistence/src/main/java/uk/gov/moj/cpp/hearing/persist/entity/ha/Person.java`
  (imports + `@Type` on the two fields)

---

## 5. Verification

- **Build:** full reactor `mm` (enforcer on) — **BUILD SUCCESS**, all 26 modules, all unit tests (including the H2-backed
  repository unit tests that exercise this mapping).
- **Integration tests:** the pre-fix run showed the UUID `ClassCastException` as the dominant failure; after the fix those
  errors are gone. A full `runIntegrationTests.sh` (deploy to WildFly 40 + full IT suite) is the final gate — see the PR /
  build for the latest result. (Note: an unrelated environmental issue — the Artemis broker container exiting under heavy
  local load — can cause JMS-related IT timeouts that are not related to this change.)

---

## 6. Recommendation for the hearing team

- **Short term:** accept this code-only `UserType` — it is isolated, reversible, and unblocks the Java 25 / WildFly 40 upgrade
  without touching the schema.
- **Long term (optional):** normalise the two `ha_defendant` columns to `uuid` (Option A) so the schema is internally
  consistent with `associated_person` and the rest of the estate. If/when you do, remove `UnspecifiedUuidUserType` and the
  two `@Type` annotations — the fields then use Hibernate's default `uuid` mapping like everything else.
