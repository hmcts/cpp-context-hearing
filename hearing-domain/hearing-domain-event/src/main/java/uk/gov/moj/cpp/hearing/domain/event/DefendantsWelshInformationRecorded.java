package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.defendant.DefendantWelshInfo;

import java.io.Serializable;
import java.util.List;

@Event("hearing.event.defendants-welsh-information-recorded")
@SuppressWarnings({"squid:S2384", "pmd:BeanMembersShouldSerialize", "pmd:BeanMembersShouldSerialize"})
public class DefendantsWelshInformationRecorded implements Serializable {

    private final List<DefendantWelshInfo> defendantsWelshInfoList;

    public DefendantsWelshInformationRecorded(final List<DefendantWelshInfo> defendantsWelshInfoList) {
        this.defendantsWelshInfoList = defendantsWelshInfoList;
    }

    public List<DefendantWelshInfo> getDefendantsWelshInfoList() {
        return defendantsWelshInfoList;
    }

}
