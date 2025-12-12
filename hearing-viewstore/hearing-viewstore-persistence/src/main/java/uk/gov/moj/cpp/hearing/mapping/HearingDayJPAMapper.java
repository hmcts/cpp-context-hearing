package uk.gov.moj.cpp.hearing.mapping;

import static java.util.Objects.isNull;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HearingDayJPAMapper {

    HearingDay toJPA(final Hearing hearing, final uk.gov.justice.core.courts.HearingDay pojo) {
        if (isNull(pojo)) {
            return null;
        }
        final HearingDay hearingDay = new HearingDay();
        hearingDay.setId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()));
        hearingDay.setHasSharedResults(pojo.getHasSharedResults());
        hearingDay.setHearing(hearing);
        hearingDay.setDate(pojo.getSittingDay().toLocalDate());
        hearingDay.setDateTime(pojo.getSittingDay());
        hearingDay.setListedDurationMinutes(pojo.getListedDurationMinutes());
        hearingDay.setListingSequence(pojo.getListingSequence());
        hearingDay.setSittingDay(pojo.getSittingDay());
        hearingDay.setIsCancelled(pojo.getIsCancelled());
        hearingDay.setCourtCentreId(pojo.getCourtCentreId());
        hearingDay.setCourtRoomId(pojo.getCourtRoomId());
        return hearingDay;
    }

    public uk.gov.justice.core.courts.HearingDay fromJPA(final HearingDay entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.HearingDay.hearingDay()
                .withHasSharedResults(entity.getHasSharedResults())
                .withListedDurationMinutes(entity.getListedDurationMinutes())
                .withListingSequence(entity.getListingSequence())
                .withSittingDay(entity.getSittingDay())
                .withIsCancelled(entity.getIsCancelled())
                .withCourtCentreId(entity.getCourtCentreId())
                .withCourtRoomId(entity.getCourtRoomId())
                .build();
    }

    public Set<HearingDay> toJPA(Hearing hearing, List<uk.gov.justice.core.courts.HearingDay> pojos) {
        if (isNull(pojos)) {
            return new HashSet<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, pojo)).collect(Collectors.toSet());
    }

    public List<uk.gov.justice.core.courts.HearingDay> fromJPA(Set<HearingDay> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).sorted(Comparator.comparing(uk.gov.justice.core.courts.HearingDay::getSittingDay)).collect(Collectors.toList());
    }
}
