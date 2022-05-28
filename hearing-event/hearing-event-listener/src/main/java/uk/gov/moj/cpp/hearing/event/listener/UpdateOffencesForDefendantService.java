package uk.gov.moj.cpp.hearing.event.listener;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UpdateOffencesForDefendantService {

   public Hearing removeOffencesFromExistingHearing(final Hearing hearing, final List<UUID> prosecutionCaseIds, final List<UUID> defendantIds, final List<UUID> offenceIds){
      hearing.getProsecutionCases().forEach(prosecutionCase ->
              prosecutionCase.getDefendants().forEach(defendant ->
                      defendant.getOffences().removeIf(o -> offenceIds.contains(o.getId().getId()))
              ));

      hearing.getProsecutionCases().forEach(
              prosecutionCase -> prosecutionCase.getDefendants().removeIf(defendant -> defendantIds.contains(defendant.getId().getId()))
      );

      hearing.getProsecutionCases().removeIf(prosecutionCase -> prosecutionCaseIds.contains(prosecutionCase.getId().getId()));

      return hearing;
   }
}
