package uk.gov.moj.cpp.hearing.command.handler.util;

import java.util.HashSet;
import java.util.Set;

public class PleaTypeUtil {
    private static final String GUILTY = "GUILTY";
    private static final String MCA_GUILTY = "MCA_GUILTY";
    private static final String AUTREFOIS_CONVICT = "AUTREFOIS_CONVICT";
    private static final String CONSENTS = "CONSENTS";
    private static final String CHANGE_TO_GUILTY_AFTER_SWORN_IN = "CHANGE_TO_GUILTY_AFTER_SWORN_IN";
    private static final String CHANGE_TO_GUILTY_NO_SWORN_IN = "CHANGE_TO_GUILTY_NO_SWORN_IN";
    private static final String CHANGE_TO_GUILTY_MAGISTRATES_COURT = "CHANGE_TO_GUILTY_MAGISTRATES_COURT";

    public static Set<String> guiltyPleaTypes() {
        final Set<String> guiltyPleaTypes = new HashSet<>();
        guiltyPleaTypes.add(GUILTY);
        guiltyPleaTypes.add(MCA_GUILTY);
        guiltyPleaTypes.add(AUTREFOIS_CONVICT);
        guiltyPleaTypes.add(CONSENTS);
        guiltyPleaTypes.add(CHANGE_TO_GUILTY_AFTER_SWORN_IN);
        guiltyPleaTypes.add(CHANGE_TO_GUILTY_NO_SWORN_IN);
        guiltyPleaTypes.add(CHANGE_TO_GUILTY_MAGISTRATES_COURT);
        return guiltyPleaTypes;
    }
}
