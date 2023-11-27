package uk.gov.moj.cpp.hearing.repository;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Witness;

import java.util.UUID;

public abstract class WitnessRepository extends AbstractEntityRepository<Witness, UUID>  {


}

