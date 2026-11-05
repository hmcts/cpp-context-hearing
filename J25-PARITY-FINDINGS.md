# hearing — J17 → J25 behavioural parity findings

Per the CTP parity guide (Confluence 1990371020) and the users-groups reference (PEG-3336).
J17 (`main`) is the source of truth.

## Context shape

Large JPA context (37 `@Entity`, 28 EntityManager users, ~2.8k-line DeltaSpike→JPA diff). The
migration **preserves finder contracts** and is parity-clean on the persistence BCs:

- **All 14 `getSingleResult()` call sites are `count()` / `SELECT COUNT(...)` (or
  `findEventLogCountBy…`) aggregate queries** — a COUNT always returns exactly one row, so it never
  throws `NoResultException`. **None is a throwing entity finder → no BC-01/02.**
- Entity finders use `entityManager.find(...)` and `getResultList().stream().findFirst().orElse(null)`
  (78 sites) — the same null/list contracts as the J17 DeltaSpike repositories.
- No primitive `@Version`; no JPQL `!= null` in queries.

The ~2.8k-line source diff is the mechanical DeltaSpike→JPA + `javax`→`jakarta` rewrite, not a
behavioural change. Golden test JSON is **unchanged** J17→J25 (0 files differ).

## BC catalogue disposition

| BC | Present? | Disposition |
|----|----------|-------------|
| BC-01/02 | No | N/A — all `getSingleResult` are COUNTs; entity finders are `find`/`findFirst→null` (null↔null) |
| BC-04 | No | N/A — no primitive `@Version` |
| BC-05 | No | N/A — no JPQL `!= null` |
| BC-06 | No | N/A |
| BC-07 | No | N/A — no `liquibase.hub.mode` in this context |
| BC-11 | No | N/A |
| BC-20 | **Yes — 2 kbases** | **Guarded** — `AccessControlRuleCountTest` for `COMMAND_API` (command-api) and `QUERY_API` (query-api). Both branches. |
| BC-24 | Runtime | Covered by ITs |

## Changes

- **BC-20:** two rule-count guards (`COMMAND_API`, `QUERY_API`). Both branches. (No production parity
  fix needed — the DeltaSpike→JPA migration is parity-clean.)
