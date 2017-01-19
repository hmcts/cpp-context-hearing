package uk.gov.moj.cpp.hearing.query.view.convertor;

import uk.gov.moj.cpp.hearing.persist.entity.HearingEventDefinitionEntity;
import uk.gov.moj.cpp.hearing.query.view.response.HearingEventDefinitionView;

public class HearingEventDefinitionEntityToHearingEventDefinitionView {
    public static HearingEventDefinitionView convert(HearingEventDefinitionEntity entity){
        HearingEventDefinitionView view = new HearingEventDefinitionView();
        view.setActionLabel(entity.getActionLabel());
        view.setRecordedLabel(entity.getRecordedLabel());
        return view;
    }
}
