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
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

@SuppressWarnings("squid:S00112")
public class NowsReferenceDataServiceImpl implements ReferenceDataService {

    private final NowsReferenceCache nowsReferenceCache;

    private final LjaReferenceDataLoader ljaReferenceDataLoader;

    private final BailStatusReferenceDataLoader bailStatusReferenceDataLoader;

    private final FixedListLookup fixedListLookup;

    private final ProsecutorDataLoader prosecutorDataLoader;

    private final OrganisationalUnitLoader organisationalUnitLoader;

    private VerdictTypesReferenceDataLoader verdictTypesReferenceDataLoader;

    private AlcoholLevelMethodsReferenceDataLoader alcoholLevelMethodsReferenceDataLoader;

    private PleaTypeReferenceDataLoader pleaTypeReferenceDataLoader;

    @Inject
    public NowsReferenceDataServiceImpl(final NowsReferenceCache nowsReferenceCache,
                                        final LjaReferenceDataLoader ljaReferenceDataLoader,
                                        final FixedListLookup fixedListLookup,
                                        final BailStatusReferenceDataLoader bailStatusReferenceDataLoader,
                                        final ProsecutorDataLoader prosecutorDataLoader,
                                        final OrganisationalUnitLoader organisationalUnitLoader,
                                        final VerdictTypesReferenceDataLoader verdictTypesReferenceDataLoader,
                                        final AlcoholLevelMethodsReferenceDataLoader alcoholLevelMethodsReferenceDataLoader,
                                        final PleaTypeReferenceDataLoader pleaTypeReferenceDataLoader) {
        this.nowsReferenceCache = nowsReferenceCache;
        this.ljaReferenceDataLoader = ljaReferenceDataLoader;
        this.bailStatusReferenceDataLoader = bailStatusReferenceDataLoader;
        this.fixedListLookup = fixedListLookup;
        this.prosecutorDataLoader = prosecutorDataLoader;
        this.organisationalUnitLoader = organisationalUnitLoader;
        this.verdictTypesReferenceDataLoader = verdictTypesReferenceDataLoader;
        this.alcoholLevelMethodsReferenceDataLoader = alcoholLevelMethodsReferenceDataLoader;
        this.pleaTypeReferenceDataLoader = pleaTypeReferenceDataLoader;
    }

    @Override
    public LjaDetails getLjaDetails(final JsonEnvelope context, final UUID courtCentreId) {
        return ljaReferenceDataLoader.getLjaDetails(context, courtCentreId);
    }

    @Override
    public List<BailStatus> getBailStatuses(final JsonEnvelope context) {
        return bailStatusReferenceDataLoader.getAllBailStatuses(context);
    }

    @Override
    public LocalJusticeAreas getLJAByNationalCourtCode(final JsonEnvelope context, final String nationalCourtCode) {
        return ljaReferenceDataLoader.getLJAByNationalCourtCode(context, nationalCourtCode);
    }

    @Override
    public FixedListResult getAllFixedLists(final JsonEnvelope context) {
        return fixedListLookup.getAllFixedLists(context);
    }

    @Override
    public ResultDefinition getResultDefinitionById(final JsonEnvelope context, final LocalDate referenceDate, final UUID id) {
        return nowsReferenceCache.getResultDefinitionById(context, referenceDate, id).getData();
    }

    @Override
    public TreeNode<ResultDefinition> getResultDefinitionTreeNodeById(JsonEnvelope context, LocalDate referenceDate, UUID id) {
        return nowsReferenceCache.getResultDefinitionById(context, referenceDate, id);
    }

    @Override
    public AllFixedList getAllFixedList(JsonEnvelope context, LocalDate referenceDate) {
        return nowsReferenceCache.getAllFixedList(context, referenceDate);
    }

    @Override
    public Prosecutor getProsecutorById(final JsonEnvelope context, final UUID id) {
        return prosecutorDataLoader.getProsecutorById(context, id);
    }

    @Override
    public OrganisationalUnit getOrganisationUnitById(final JsonEnvelope context, final UUID organisationUnitId) {
        return organisationalUnitLoader.getOrganisationUnitById(context, organisationUnitId);
    }

    @Override
    public List<VerdictType> getVerdictTypes(final JsonEnvelope context) {
        return verdictTypesReferenceDataLoader.getAllVerdictTypes(context);
    }

    @Override
    public List<AlcoholLevelMethod> getAlcoholLevelMethods(final JsonEnvelope context) {
        return alcoholLevelMethodsReferenceDataLoader.getAllAlcoholLevelMethods(context);
    }

    @Override
    public Set<String> retrieveGuiltyPleaTypes(){
        return pleaTypeReferenceDataLoader.retrieveGuiltyPleaTypes();
    }
}
