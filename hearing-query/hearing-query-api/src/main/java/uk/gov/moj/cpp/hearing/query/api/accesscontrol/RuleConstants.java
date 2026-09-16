package uk.gov.moj.cpp.hearing.query.api.accesscontrol;

@SuppressWarnings("WeakerAccess")
public class RuleConstants {

    private RuleConstants() {
    }

    /**
     * The groups that may read a hearing's tier and list type.
     *
     * <p>Anyone who can read a plea or a draft result can read these. Plea has no read endpoint
     * of its own — it rides {@code hearing.get.hearing} — so this is the union of that rule's
     * groups with those of {@code hearing.get-draft-result} and its v2, which are what contribute
     * {@code System Users} and {@code Operational Delivery Admin}.
     *
     * <p>Wider than the write list on purpose: the judicial groups read tier and list type but do
     * not record them, exactly as they read a plea without recording one. {@code System Users} is
     * required — the listing context calls this endpoint to inherit the values onto a next
     * hearing, so removing it would break PTPH inheritance.
     */
    public static String[] getUsersForPtphDetail() {
        return new String[]{"Listing Officers", "Court Clerks", "Legal Advisers", "System Users",
                "Judiciary", "Court Associate", "Deputies", "DJMC", "Judge", "Recorders",
                "Court Administrators", "Operational Delivery Admin"};
    }
}
