package uk.gov.justice.ccr.notepad.result.loader;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by satishkumar on 17/12/2018.
 */
public class ResultPromptReferenceDynamicFixListUUIDMapper {
    //court centre
    public static final String COURT_CENTRE_FIX_LIST_UUID = "9d8f6a9e-2097-4391-aded-30738893b24b";
    public static final String HCHOUSE = "HCHOUSE";
    //hearing type
    public static final String HEARING_TYPE_FIX_LIST_UUID = "5591d709-4397-452c-8533-998165d58d9c";
    public static final String HTYPE = "HTYPE";
    //Major Creditor Name
    public static final String CREDITOR_NAME_FIX_LIST_UUID = "3da131fb-6576-3d9e-8201-039f3ef7529c";
    public static final String CREDNAME = "CREDNAME";

    private ResultPromptReferenceDynamicFixListUUIDMapper() {

    }

    public static Map<String, String> getPromptReferenceDynamicFixListUuids() {
        final Map<String, String> result = new HashMap<>();
        result.put(HCHOUSE, COURT_CENTRE_FIX_LIST_UUID);
        result.put(HTYPE, HEARING_TYPE_FIX_LIST_UUID);
        result.put (CREDNAME, CREDITOR_NAME_FIX_LIST_UUID);
        return result;
    }
}
