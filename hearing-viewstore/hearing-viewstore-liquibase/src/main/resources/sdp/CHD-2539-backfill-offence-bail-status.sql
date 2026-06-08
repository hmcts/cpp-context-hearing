-- SDP: CHD-2539 — Back-fill offence-level bail status for existing active cases
--
-- Purpose:
--   Before this change was released, bail status was only stored at the defendant level
--   (ha_defendant.bail_status_code/id/desc). Per-offence bail status columns
--   (ha_offence.bail_status_code/id/description) were always null.
--
--   This script seeds those columns for every ACTIVE offence (proceedings_concluded IS NOT TRUE)
--   by copying the defendant's current bail status into each of their active offences.
--
-- Idempotency:
--   The WHERE clause restricts to rows where bail_status_code IS NULL, so re-running
--   will only touch rows that have not yet been seeded (or were not seeded by a previous run).
--   Rows already written by the application after deployment are not overwritten.
--
-- Scope:
--   Only active offences (proceedings_concluded IS NOT TRUE) are back-filled.
--   Offences with proceedings_concluded = TRUE have final results and are excluded from
--   defendant remand calculation; seeding them is unnecessary.
--
-- Rollback:
--   See rollback section at the bottom of this file.
--
-- Prerequisites:
--   Run AFTER deploying the cpp-context-hearing release that includes BailStatusHelper changes.
--   Schedule in a low-traffic window. Any hearing shared after deployment will write correct
--   per-offence bail status via the application, so post-SDP results will not be affected.
--
-- Estimated impact:
--   One row updated per active offence per active case. Review row count before committing.
--   Run in a transaction and ROLLBACK if counts are unexpected.

BEGIN;

-- Preview: count of rows that will be updated
-- SELECT count(*)
-- FROM ha_offence o
-- JOIN ha_defendant d
--   ON d.id       = o.defendant_id
--  AND d.hearing_id = o.hearing_id
-- WHERE (o.proceedings_concluded IS NULL OR o.proceedings_concluded = FALSE)
--   AND o.bail_status_code IS NULL
--   AND d.bail_status_code IS NOT NULL;

UPDATE ha_offence o
SET
    bail_status_code        = d.bail_status_code,
    bail_status_id          = d.bail_status_id,
    bail_status_description = d.bail_status_desc
FROM ha_defendant d
WHERE d.id           = o.defendant_id
  AND d.hearing_id   = o.hearing_id
  AND (o.proceedings_concluded IS NULL OR o.proceedings_concluded = FALSE)
  AND o.bail_status_code IS NULL
  AND d.bail_status_code IS NOT NULL;

-- Verify: the following should return 0 after the update
-- (active offences belonging to defendants with a bail status that are still unseeded)
-- SELECT count(*)
-- FROM ha_offence o
-- JOIN ha_defendant d
--   ON d.id       = o.defendant_id
--  AND d.hearing_id = o.hearing_id
-- WHERE (o.proceedings_concluded IS NULL OR o.proceedings_concluded = FALSE)
--   AND o.bail_status_code IS NULL
--   AND d.bail_status_code IS NOT NULL;

COMMIT;

-- ============================================================
-- ROLLBACK (run this block to undo the SDP if needed)
-- ============================================================
-- Note: only safe to run if the application has NOT yet written new per-offence bail
-- status values after deployment. Once the application has run, this will erase real data.
--
-- BEGIN;
--
-- UPDATE ha_offence o
-- SET
--     bail_status_code        = NULL,
--     bail_status_id          = NULL,
--     bail_status_description = NULL
-- FROM ha_defendant d
-- WHERE d.id           = o.defendant_id
--   AND d.hearing_id   = o.hearing_id
--   AND (o.proceedings_concluded IS NULL OR o.proceedings_concluded = FALSE)
--   AND o.bail_status_code = d.bail_status_code;
--
-- COMMIT;
