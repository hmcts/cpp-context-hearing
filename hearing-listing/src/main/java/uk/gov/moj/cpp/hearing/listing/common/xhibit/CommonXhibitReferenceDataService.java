package uk.gov.moj.cpp.hearing.listing.common.xhibit;

import static java.lang.String.format;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.moj.cpp.hearing.listing.common.xhibit.exception.InvalidReferenceDataException;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtMapping;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtRoomMapping;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtRoomMappingsList;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.HearingType;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.Judiciary;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.OrganisationUnit;
import uk.gov.moj.cpp.hearing.listing.domain.xhibit.CourtLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.json.JsonObject;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonXhibitReferenceDataService {

    private static final String UNMAPPED_COURT_ROOM_NAME = "Court -99";
    private static final String CREST_COURT_SITE_CODE = "crestCourtSiteCode";
    private static final String CREST_COURT_SITE_ID = "crestCourtSiteId";
    private static final String MAGISTRATES_COURT_TYPE = "MAGISTRATES_COURT";

    private final ConcurrentMap<String, List<JsonObject>> crestCourtSitesCache = new ConcurrentHashMap<>();


    private final XhibitReferenceDataValidator xhibitReferenceDataValidator = new XhibitReferenceDataValidator();
    private static final String COURT_DETAILS_NOT_FOUND = "Cannot find court details with courtCentre %s";
    private static final String COURT_MAPPING_NOT_FOUND_FOR_TYPE =
            "Cannot find court mapping for courtCentre %s with court type %s";

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonXhibitReferenceDataService.class);

    @Inject
    private ReferenceDataCache referenceDataCache;

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    public List<UUID> getCrownCourtCentreIdsForCrestId(final String crownCourtCrestId) {
        final List<CourtLocation> courtLocations = referenceDataCache.getCrownCourtLocationsCache(crownCourtCrestId);
        courtLocations.forEach(courtLocation -> xhibitReferenceDataValidator.validate(CREST_COURT_SITE_ID, courtLocation.getCrestCourtSiteId()));

        return courtLocations.stream()
                .map(courtLocation -> getOrganisationUnitIdByOuCode(courtLocation.getOuCode()))
                .distinct()
                .collect(Collectors.toList());
    }


    public List<UUID> getMagsCourtCentreIdsForCrestId(final String magsCourtCrestId) {
        final Optional<List<CourtMapping>> courtMappingsOptional = referenceDataCache.getMagsCourtMappingsMapCache(UUID.fromString(magsCourtCrestId));
        courtMappingsOptional.ifPresent(courtMappings ->
            courtMappings.forEach(courtMapping -> xhibitReferenceDataValidator.validate(CREST_COURT_SITE_ID, courtMapping.getCrestCourtSiteId()))
        );
        return courtMappingsOptional.map(courtMappings -> courtMappings.stream()
                .map(courtMapping -> getOrganisationUnitIdByOuCode(courtMapping.getOucode()))
                .distinct()
                .collect(Collectors.toList())).orElseGet(ImmutableList::of);
    }

    public String getDefaultCrestCourtSiteCode(UUID courtCentreId) {
        List<JsonObject> courtSites = getCrestCourtSitesForCrownCourtCentre(courtCentreId);
        if (courtSites.isEmpty()) {
            throw new IllegalStateException("No court sites found for courtCentreId: " + courtCentreId);
        }
        return courtSites.get(0).getString(CREST_COURT_SITE_CODE);
    }


    public CourtLocation getCrownCourtDetails(final UUID courtCentreId) {

        final CourtMapping court = referenceDataCache.getCrownCourtMappingsMapCache(courtCentreId)
                .orElseThrow(() -> new InvalidReferenceDataException(format(COURT_DETAILS_NOT_FOUND, courtCentreId)))
                .stream()
                .findFirst()
                .orElseThrow(() -> new InvalidReferenceDataException(format(COURT_DETAILS_NOT_FOUND, courtCentreId)));

        return createCrownCourtLocation(court);
    }

    /**
     * Xhibit court location for a committing court (magistrates or crown). Resolution uses
     * {@link ReferenceDataCache#getCpXhibitCourtMappingsMapCache(UUID)}: mags
     * ({@code referencedata.query.cp-xhibit-mags-court-mapping}, including empty {@code {}}), else
     * {@code referencedata.query.cp-xhibit-court-mappings}.
     * <p>
     * When {@code courtHouseType} is set (e.g. from offence committing court), the mapping with that
     * {@link CourtMapping#getCourtType()} is used; otherwise the first mapping from the cache is used.
     */
    public CourtLocation getCriminalCourtDetails(final UUID courtCentreId, final String courtHouseType) {

        final List<CourtMapping> mappings = referenceDataCache.getCpXhibitCourtMappingsMapCache(courtCentreId)
                .orElseThrow(() -> new InvalidReferenceDataException(format(COURT_DETAILS_NOT_FOUND, courtCentreId)));
        if (mappings.isEmpty()) {
            throw new InvalidReferenceDataException(format(COURT_DETAILS_NOT_FOUND, courtCentreId));
        }

        final CourtMapping court;
        if (courtHouseType != null && !courtHouseType.isBlank()) {
            court = mappings.stream()
                    .filter(m -> courtHouseType.equals(m.getCourtType()))
                    .findFirst()
                    .orElseThrow(() -> new InvalidReferenceDataException(
                            format(COURT_MAPPING_NOT_FOUND_FOR_TYPE, courtCentreId, courtHouseType)));
        } else {
            court = mappings.stream()
                    .findFirst()
                    .orElseThrow(() -> new InvalidReferenceDataException(format(COURT_DETAILS_NOT_FOUND, courtCentreId)));
        }

        return createCourtLocationFromCourtMapping(court);
    }

    /**
     * Same as {@link #getCriminalCourtDetails(UUID, String)} with no {@code courtHouseType} (first combined mapping).
     */
    public CourtLocation getCriminalCourtDetails(final UUID courtCentreId) {
        return getCriminalCourtDetails(courtCentreId, null);
    }



    public List<JsonObject> getCrestCourtSitesForCrownCourtCentre(UUID courtCentreId) {
        return crestCourtSitesCache.computeIfAbsent(courtCentreId.toString(), id -> fetchCrestCourtSitesFromDatabase(courtCentreId));
    }



    public Optional<CourtRoomMapping> getCourtRoom(final UUID courtCentreId, final UUID courtRoomUUID) {
        final JsonObject cpCourtRoom = getCpCourtRoom(courtCentreId, courtRoomUUID);

        final Integer courtRoomId = cpCourtRoom.getJsonNumber("courtroomId").intValue();

        final Optional<CourtRoomMappingsList> courtRoomMappingsListOptional = getCourtRoomMappingsList(courtCentreId);

        return courtRoomMappingsListOptional.isPresent() ? courtRoomMappingsListOptional.get().getCpXhibitCourtRoomMappings()
                .stream()
                .filter(courtRoomMapping -> courtRoomMapping.getCourtRoomId().equals(courtRoomId))
                .findFirst()
                : Optional.empty();
    }

    public HearingType getXhibitHearingType(final UUID cppHearingTypeId) {
        return referenceDataCache.getHearingTypeCache(cppHearingTypeId)
                .orElseThrow(() -> new InvalidReferenceDataException(format("Cannot find hearing type %s", cppHearingTypeId)));
    }

    public int getCourtRoomNumber(final UUID courtCentreId, final UUID courtRoomId) {
        final CourtRoomMapping courtRoomMapping = getCourtRoomMappingBy(courtCentreId, courtRoomId);
        return courtRoomNumberForCourtRoom(courtRoomMapping.getCrestCourtRoomName());
    }

    public JsonObject getJudiciary(final UUID judiciaryId) {
        return objectToJsonObjectConverter.convert(getJudiciaryAsObject(judiciaryId));
    }

    private Judiciary getJudiciaryAsObject(final UUID judiciaryId) {

        return referenceDataCache.getJudiciariesMapCache(judiciaryId)
                .orElseThrow(() -> new InvalidReferenceDataException(format("Cannot find judiciary with id %s", judiciaryId)));
    }

    public CourtRoomMapping getCourtRoomMappingBy(final UUID courtCentreId, final UUID courtRoomId) {
        return referenceDataCache.getCourtRoomMappingByCourtCentreAndCourtRoom(courtCentreId, courtRoomId)
                .orElse(new CourtRoomMapping(UNMAPPED_COURT_ROOM_NAME));
    }

    public Optional<String> getCourtRoomDisplayName(final UUID courtCentreId, final UUID courtRoomUUID) {
        try {
            return Optional.ofNullable(getCpCourtRoom(courtCentreId, courtRoomUUID).getString("courtroomName", null));
        } catch (final Exception e) {
            LOGGER.warn("Cannot find court room display name for courtCentreId={}, courtRoomUUID={}", courtCentreId, courtRoomUUID);
            return Optional.empty();
        }
    }

    public Optional<String> getWelshCourtRoomDisplayName(final UUID courtCentreId, final UUID courtRoomUUID) {
        try {
            return Optional.ofNullable(getCpCourtRoom(courtCentreId, courtRoomUUID).getString("welshCourtroomName", null));
        } catch (final Exception e) {
            LOGGER.warn("Cannot find Welsh court room display name for courtCentreId={}, courtRoomUUID={}", courtCentreId, courtRoomUUID);
            return Optional.empty();
        }
    }

    private UUID getOrganisationUnitIdByOuCode(final String ouCode) {
        final OrganisationUnit organisationUnit = referenceDataCache.getOrganisationUnitMapCache(ouCode)
                .orElseThrow(() -> new InvalidReferenceDataException(format("Cannot find organisation unit with code %s", ouCode)));

        return organisationUnit.getId();
    }

    private List<JsonObject> fetchCrestCourtSitesFromDatabase(UUID courtCentreId) {
        final List<JsonObject> crownCourtMappingJsonObjectList = new ArrayList<>();

        final Optional<List<CourtMapping>> cownCourtMappingList = referenceDataCache.getCrownCourtMappingsMapCache(courtCentreId);

        cownCourtMappingList.ifPresent(courtMappings -> courtMappings.forEach(courtMapping ->
                crownCourtMappingJsonObjectList.add(objectToJsonObjectConverter.convert(courtMapping)))
        );

        return crownCourtMappingJsonObjectList;
    }

    private JsonObject getCpCourtRoom(final UUID courtCentreId, final UUID courtRoomUUID) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("'Getting CPS court room for court Center Id {}", courtCentreId.toString());
        }
        return referenceDataCache.getCpCourtRoomCache(courtCentreId)
                .stream()
                .filter(cpCourtRoom -> UUID.fromString(cpCourtRoom.getString("id")).equals(courtRoomUUID))
                .findFirst()
                .orElseThrow(() -> new InvalidReferenceDataException("Cannot find court room uuid : " +courtRoomUUID + " for Court Center Id : " + courtCentreId));
    }

    public Optional<CourtRoomMappingsList> getCourtRoomMappingsList(final UUID courtCentreId) {

        return Optional.of(new CourtRoomMappingsList(referenceDataCache.getCourtRoomMappingsMapCache(courtCentreId).getCpXhibitCourtRoomMappings()));
    }

    private int courtRoomNumberForCourtRoom(final String courtRoomName) {
        final String[] splitName = courtRoomName.split(" ");

        return Integer.parseInt(splitName[splitName.length-1]);
    }

    private CourtLocation createCrownCourtLocation(final CourtMapping courtMapping) {
        xhibitReferenceDataValidator.validate(CREST_COURT_SITE_ID, courtMapping.getCrestCourtSiteId());

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

    private CourtLocation createMagsCourtLocation(final CourtMapping courtMapping) {
        xhibitReferenceDataValidator.validate(CREST_COURT_SITE_CODE, courtMapping.getCrestCourtSiteCode());

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

    private CourtLocation createCourtLocationFromCourtMapping(final CourtMapping courtMapping) {
        if (MAGISTRATES_COURT_TYPE.equals(courtMapping.getCourtType())) {
            return createMagsCourtLocation(courtMapping);
        }
        return createCrownCourtLocation(courtMapping);
    }
}
