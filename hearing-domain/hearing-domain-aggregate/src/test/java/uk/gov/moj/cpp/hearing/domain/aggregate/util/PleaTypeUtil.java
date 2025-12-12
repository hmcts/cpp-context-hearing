package uk.gov.moj.cpp.hearing.domain.aggregate.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

public class PleaTypeUtil {
    private static final String GUILTY = "GUILTY";
    private static final String MCA_GUILTY = "MCA_GUILTY";
    private static final String AUTREFOIS_CONVICT = "AUTREFOIS_CONVICT";
    private static final String CONSENTS = "CONSENTS";
    private static final String CHANGE_TO_GUILTY_AFTER_SWORN_IN = "CHANGE_TO_GUILTY_AFTER_SWORN_IN";
    private static final String CHANGE_TO_GUILTY_NO_SWORN_IN = "CHANGE_TO_GUILTY_NO_SWORN_IN";
    private static final String CHANGE_TO_GUILTY_MAGISTRATES_COURT = "CHANGE_TO_GUILTY_MAGISTRATES_COURT";
    private static final String GUILTY_TO_A_LESSER_OFFENCE_NAMELY = "GUILTY_TO_A_LESSER_OFFENCE_NAMELY";
    private static final String GUILTY_TO_AN_ALTERNATIVE_OFFENCE_NOT_CHARGED_NAMELY = "GUILTY_TO_AN_ALTERNATIVE_OFFENCE_NOT_CHARGED_NAMELY";
    private static final String ADMITS = "ADMITS";

    private static final String NOT_GUILTY = "NOT_GUILTY";
    private static final String UNFIT_TO_PLEAD = "UNFIT_TO_PLEAD";
    private static final String CHANGE_TO_NOT_GUILTY = "CHANGE_TO_NOT_GUILTY";
    private static final String DENIES = "DENIES";
    private static final String OPPOSES = "OPPOSES";

    public static final List<String> GUILTY_PLEA_LIST = newArrayList(GUILTY,
            MCA_GUILTY,
            AUTREFOIS_CONVICT,
            CONSENTS,
            CHANGE_TO_GUILTY_AFTER_SWORN_IN,
            CHANGE_TO_GUILTY_NO_SWORN_IN,
            CHANGE_TO_GUILTY_MAGISTRATES_COURT,
            GUILTY_TO_A_LESSER_OFFENCE_NAMELY,
            GUILTY_TO_AN_ALTERNATIVE_OFFENCE_NOT_CHARGED_NAMELY,
            ADMITS);

    public static final List<String> ALL_PLEAS = newArrayList(GUILTY,
            MCA_GUILTY,
            AUTREFOIS_CONVICT,
            CONSENTS,
            CHANGE_TO_GUILTY_AFTER_SWORN_IN,
            CHANGE_TO_GUILTY_NO_SWORN_IN,
            CHANGE_TO_GUILTY_MAGISTRATES_COURT,
            GUILTY_TO_A_LESSER_OFFENCE_NAMELY,
            GUILTY_TO_AN_ALTERNATIVE_OFFENCE_NOT_CHARGED_NAMELY,
            ADMITS,
            NOT_GUILTY,
            UNFIT_TO_PLEAD,
            CHANGE_TO_NOT_GUILTY,
            DENIES,
            OPPOSES
            );


    public static Set<String> guiltyPleaTypes() {
        return new HashSet<>(GUILTY_PLEA_LIST);
    }
}
