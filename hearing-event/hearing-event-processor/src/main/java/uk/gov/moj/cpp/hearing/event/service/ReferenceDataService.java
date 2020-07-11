package uk.gov.moj.cpp.hearing.event.service;

import uk.gov.justice.core.courts.LjaDetails;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.justice.hearing.courts.referencedata.FixedListResult;
import uk.gov.justice.hearing.courts.referencedata.LocalJusticeAreas;
import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;
import uk.gov.justice.hearing.courts.referencedata.Prosecutor;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.alcohollevel.AlcoholLevelMethod;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.bailstatus.BailStatus;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllFixedList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReferenceDataService {

    ResultDefinition getResultDefinitionById(JsonEnvelope context, LocalDate referenceDate, UUID id);

    LjaDetails getLjaDetails(JsonEnvelope context, UUID courtCentreId);

    LocalJusticeAreas getLJAByNationalCourtCode(JsonEnvelope context, final String nationalCourtCode);

    FixedListResult getAllFixedLists(final JsonEnvelope context);

    TreeNode<ResultDefinition> getResultDefinitionTreeNodeById(JsonEnvelope context, LocalDate referenceDate, UUID id);

    List<BailStatus> getBailStatuses(JsonEnvelope context);

    AllFixedList getAllFixedList(JsonEnvelope context, LocalDate referenceDate);

    Prosecutor getProsecutorById(JsonEnvelope context, final UUID id);

    OrganisationalUnit getOrganisationUnitById(JsonEnvelope context, final UUID organisationUnitId);

    List<VerdictType> getVerdictTypes(final JsonEnvelope context);

    List<AlcoholLevelMethod> getAlcoholLevelMethods(final JsonEnvelope context);

}
