package uk.gov.moj.cpp.hearing.listing.common.xhibit;

import uk.gov.moj.cpp.hearing.listing.common.xhibit.exception.InvalidReferenceDataException;
import uk.gov.moj.cpp.hearing.listing.common.xhibit.model.CourtCentreRoomKey;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtMapping;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtRoomMapping;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtRoomMappingsList;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.HearingType;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.HearingTypesList;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.Judiciary;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.OrganisationUnit;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.OrganisationUnitList;
import uk.gov.moj.cpp.hearing.listing.domain.xhibit.CourtLocation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

@ApplicationScoped
public class ReferenceDataCache {

    @Inject
    private ReferenceDataLoader referenceDataLoader;

    private final Map<String, List<CourtLocation>> crownCourtLocationListMapCache = new ConcurrentHashMap<>();

    private final Map<String, List<CourtMapping>> crownCourtMappingListMapCache = new ConcurrentHashMap<>();

    private final Map<String, List<CourtMapping>> magsCourtMappingListMapCache = new ConcurrentHashMap<>();

    private final Map<UUID, HearingType> hearingTypesMapCache = new ConcurrentHashMap<>();

    private final Map<UUID, List<JsonObject>> cpCourtRoomCache = new ConcurrentHashMap<>();

    private final Map<String, OrganisationUnit> organisationUnitMapCache = new ConcurrentHashMap<>();

    private final Map<UUID, OrganisationUnit> organisationUnitMapByIdCache = new ConcurrentHashMap<>();

    private final Map<UUID, CourtRoomMappingsList> courtRoomMappingsMapCache = new ConcurrentHashMap<>();

    private final Map<CourtCentreRoomKey, Optional<CourtRoomMapping>> courtRoomMappingByCourtCentreRoomMap = new ConcurrentHashMap<>();

    private final Map<UUID, Judiciary> judiciariesMapCache = new ConcurrentHashMap<>();

    private final Map<String, HearingType> hearingTypesCodesMapCache = new ConcurrentHashMap<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();


    @PostConstruct
    public void initReferenceData() {
        synchronized (this) {
            initCrownCourtMappingsList();
            initAllHearingTypes();
            initOrganisationUnitList();
        }
    }

    public Optional<List<CourtMapping>> getCrownCourtMappingsMapCache(final UUID courtCentreId) {
        OrganisationUnit organisationUnit = organisationUnitMapByIdCache.get(courtCentreId);
        return organisationUnit == null
                ? Optional.empty()
                : Optional.ofNullable(crownCourtMappingListMapCache.get(organisationUnit.getOucode()));
    }

    public Optional<List<CourtMapping>> getMagsCourtMappingsMapCache(final UUID courtCentreId) {
        OrganisationUnit organisationUnit = organisationUnitMapByIdCache.get(courtCentreId);
        if (organisationUnit == null) {
            return Optional.empty();
        }

        String oucode = organisationUnit.getOucode();

        // Ensure safe lazy load
        return Optional.ofNullable(
                magsCourtMappingListMapCache.computeIfAbsent(oucode, key ->
                        referenceDataLoader.getXhibitMagsCourtMappings(oucode)
                                .map(m -> m.getCpXhibitCourtMappings())
                                .orElse(null))
        );
    }

    public Optional<Judiciary> getJudiciariesMapCache(final UUID judiciaryId) {
        return Optional.ofNullable(
                judiciariesMapCache.computeIfAbsent(judiciaryId,
                        key -> referenceDataLoader.getJudiciary(judiciaryId).orElse(null)));
    }

    public CourtRoomMappingsList getCourtRoomMappingsMapCache(final UUID courtCentreId) {
        return courtRoomMappingsMapCache.computeIfAbsent(courtCentreId,
                key -> referenceDataLoader.getCourtRoomMappingsList(courtCentreId).orElse(null));
    }

    public Optional<CourtRoomMapping> getCourtRoomMappingByCourtCentreAndCourtRoom(final UUID courtCentreId, final UUID courtRoomId) {
        return courtRoomMappingByCourtCentreRoomMap.computeIfAbsent(new CourtCentreRoomKey(courtCentreId, courtRoomId), key -> {
            return referenceDataLoader.getCourtRoomMappingsList(courtCentreId)
                    .orElseThrow(() -> new InvalidReferenceDataException("Invalid object courtRoomMappings"))
                    .getCpXhibitCourtRoomMappings()
                    .stream()
                    .filter(c -> courtRoomId.equals(c.getCourtRoomUUID()))
                    .findFirst();
        });
    }

    public Optional<OrganisationUnit> getOrganisationUnitMapCache(final String ouCode) {
        return Optional.ofNullable(
                organisationUnitMapCache.computeIfAbsent(ouCode,
                        key -> referenceDataLoader.getOrganisationUnitByOuCode(ouCode).orElse(null)));
    }

    public List<JsonObject> getCpCourtRoomCache(final UUID courtCentreId) {
        // Try to read first
        lock.readLock().lock();
        try {
            List<JsonObject> result = cpCourtRoomCache.get(courtCentreId);
            if (result != null) {
                return result; // Fast path - multiple readers can do this
            }
        } finally {
            lock.readLock().unlock();
        }
        lock.writeLock().lock();
        try {
            return cpCourtRoomCache.computeIfAbsent(courtCentreId,
                    key -> referenceDataLoader.getCpCourtRoom(key));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<HearingType> getHearingTypeCache(final UUID hearingTypeId) {
        ensureHearingTypesInitialized();
        return Optional.ofNullable(hearingTypesMapCache.get(hearingTypeId));
    }

    public Optional<HearingType> getHearingTypeCodeCache(final String hearingCode) {
        ensureHearingTypesInitialized();
        return Optional.ofNullable(hearingTypesCodesMapCache.get(hearingCode));
    }

    public List<CourtLocation> getCrownCourtLocationsCache(final String crestCourtId) {
        ensureCrownCourtMappingsInitialized();
        return crownCourtLocationListMapCache.get(crestCourtId);
    }

    private synchronized void ensureHearingTypesInitialized() {
        if (hearingTypesMapCache.isEmpty() || hearingTypesCodesMapCache.isEmpty()) {
            initAllHearingTypes();
        }
    }

    private synchronized void ensureCrownCourtMappingsInitialized() {
        if (crownCourtLocationListMapCache.isEmpty()) {
            initCrownCourtMappingsList();
        }
    }

    private void initCrownCourtMappingsList() {
        referenceDataLoader.getXhibitCrownCourtMappings().ifPresent(courtMappingsList -> {
            courtMappingsList.getCpXhibitCourtMappings().forEach(courtMapping -> {
                crownCourtLocationListMapCache
                        .computeIfAbsent(courtMapping.getCrestCourtId(), k -> new java.util.concurrent.CopyOnWriteArrayList<>())
                        .add(createCourtLocation(courtMapping));

                crownCourtMappingListMapCache
                        .computeIfAbsent(courtMapping.getOucode(), k -> new java.util.concurrent.CopyOnWriteArrayList<>())
                        .add(courtMapping);
            });
        });
    }

    public void initAllHearingTypes() {
        referenceDataLoader.getAllHearingTypesList().ifPresent(this::getHearingTypes);
    }

    public void initOrganisationUnitList() {
        referenceDataLoader.getOrganisationUnitList().ifPresent(this::getOrganisationUnits);
    }

    public CourtLocation createCourtLocation(final CourtMapping courtMapping) {
        return new CourtLocation(
                courtMapping.getOucode(),
                courtMapping.getCrestCourtId(),
                courtMapping.getCrestCourtSiteId(),
                courtMapping.getCrestCourtName(),
                courtMapping.getCrestCourtShortName(),
                courtMapping.getCrestCourtSiteName(),
                courtMapping.getCrestCourtSiteCode(),
                courtMapping.getCourtType());
    }

    private void getHearingTypes(HearingTypesList hearingTypesList) {
        hearingTypesList.getHearingTypes().forEach(hearingType -> {
            hearingTypesMapCache.putIfAbsent(hearingType.getId(), hearingType);
            hearingTypesCodesMapCache.putIfAbsent(hearingType.getHearingCode(), hearingType);
        });
    }

    private void getOrganisationUnits(OrganisationUnitList organisationUnitList) {
        organisationUnitList.getOrganisationunits().forEach(unit -> {
            organisationUnitMapByIdCache.putIfAbsent(unit.getId(), unit);
            organisationUnitMapCache.putIfAbsent(unit.getOucode(), unit);
        });
    }
}
