package uk.gov.moj.cpp.hearing.query.api.accescontrol;

@SuppressWarnings("WeakerAccess")
public class RuleConstants {

    private static final String GROUP_DEFENCE_LAWYER_USER = "Defence Lawyers";
    private static final String GROUP_CHAMBERS_CLERK_USER = "Chambers Clerk";
    private static final String GROUP_CHAMBERS_ADMIN_USER = "Chambers Admin";
    private static final String GROUP_ADVOCATES_USER = "Advocates";

    private RuleConstants() {
    }

    public static String[] getQueryForCaseByDefendantUsersGroup() {
        return new String[]{GROUP_DEFENCE_LAWYER_USER, GROUP_CHAMBERS_CLERK_USER, GROUP_CHAMBERS_ADMIN_USER, GROUP_ADVOCATES_USER};
    }
}
