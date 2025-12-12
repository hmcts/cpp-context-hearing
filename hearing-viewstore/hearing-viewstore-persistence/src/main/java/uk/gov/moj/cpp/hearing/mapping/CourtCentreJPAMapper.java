package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CourtCentreJPAMapper {

    public CourtCentre toJPA(final uk.gov.justice.core.courts.CourtCentre pojo) {
        if (null == pojo) {
            return null;
        }
        final CourtCentre courtCentre = new CourtCentre();
        courtCentre.setId(pojo.getId());
        courtCentre.setName(pojo.getName());
        courtCentre.setRoomId(pojo.getRoomId());
        courtCentre.setRoomName(pojo.getRoomName());
        courtCentre.setWelshName(pojo.getWelshName());
        courtCentre.setWelshRoomName(pojo.getWelshRoomName());
        return courtCentre;
    }

    public uk.gov.justice.core.courts.CourtCentre fromJPA(final CourtCentre entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.CourtCentre.courtCentre()
                .withId(entity.getId())
                .withName(entity.getName())
                .withRoomId(entity.getRoomId())
                .withRoomName(entity.getRoomName())
                .withWelshName(entity.getWelshName())
                .withWelshRoomName(entity.getWelshRoomName())
                .build();
    }
}