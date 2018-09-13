package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class AssociatedPersonJPAMapper {

    private PersonJPAMapper personJPAMapper;

    @Inject
    public AssociatedPersonJPAMapper(final PersonJPAMapper personJPAMapper) {
        this.personJPAMapper = personJPAMapper;
    }

    public AssociatedPersonJPAMapper() {
        //TO keep cdi tester happy
    }

    AssociatedPerson toJPA(final Hearing hearing, final Defendant defendant, final uk.gov.justice.json.schemas.core.AssociatedPerson pojo) {
        final AssociatedPerson associatedPerson = new AssociatedPerson();
        associatedPerson.setPerson(personJPAMapper.toJPA(pojo.getPerson()));
        associatedPerson.setId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()));
        associatedPerson.setRole(pojo.getRole());
        associatedPerson.setDefendant(defendant);
        associatedPerson.setDefendantId(defendant.getId().getId());
        return associatedPerson;
    }

    public List<AssociatedPerson> toJPA(final Hearing hearing, final Defendant defendant, final List<uk.gov.justice.json.schemas.core.AssociatedPerson> pojos) {
        if (null == pojos) {
            return new ArrayList<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, defendant, pojo)).collect(Collectors.toList());
    }

    uk.gov.justice.json.schemas.core.AssociatedPerson fromJPA(final AssociatedPerson entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.AssociatedPerson.associatedPerson()
                .withPerson(personJPAMapper.fromJPA(entity.getPerson()))
                .withRole(entity.getRole())
                .build();
    }

    public List<uk.gov.justice.json.schemas.core.AssociatedPerson> fromJPA(List<AssociatedPerson> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).collect(Collectors.toList());
    }
}