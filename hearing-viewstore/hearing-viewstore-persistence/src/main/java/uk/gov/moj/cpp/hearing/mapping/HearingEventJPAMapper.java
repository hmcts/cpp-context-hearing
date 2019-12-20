package uk.gov.moj.cpp.hearing.mapping;


public class HearingEventJPAMapper {

    private HearingEventJPAMapper(){}

    public static uk.gov.justice.core.courts.HearingEvent fromJPA(final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent hearingEventEntity) {
        if (null == hearingEventEntity) {
            return null;
        }

        return uk.gov.justice.core.courts.HearingEvent.hearingEvent()
                .withId(hearingEventEntity.getId())
                .withAlterable(hearingEventEntity.isAlterable())
                .withDefenceCounselId(hearingEventEntity.getDefenceCounselId())
                .withDeleted(hearingEventEntity.isDeleted())
                .withEventDate(hearingEventEntity.getEventDate().toString())
                .withEventTime(hearingEventEntity.getEventTime())
                .withHearingEventDefinitionId(hearingEventEntity.getHearingEventDefinitionId())
                .withHearingId(hearingEventEntity.getHearingId())
                .withRecordedLabel(hearingEventEntity.getRecordedLabel())
                .withLastModifiedTime(hearingEventEntity.getLastModifiedTime())
                .build();
    }
}
