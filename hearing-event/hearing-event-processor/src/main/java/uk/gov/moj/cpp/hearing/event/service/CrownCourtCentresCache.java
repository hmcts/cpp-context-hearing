package uk.gov.moj.cpp.hearing.event.service;

import static java.util.UUID.fromString;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;

@ApplicationScoped
public class CrownCourtCentresCache {

    private final Map<UUID, JsonObject> CROWN_COURTCENTRES_CACHE = new HashMap<>();

    @Inject
    private ReferenceDataLoader referenceDataService;

    @PostConstruct
    public void init() {
        referenceDataService.getAllCrownCourtCentres()
                .forEach(crownCourtCentre -> CROWN_COURTCENTRES_CACHE.put(fromString(crownCourtCentre.getString("id")), crownCourtCentre));
    }

    public Set<UUID> getAllCrownCourtCentres() {
        return CROWN_COURTCENTRES_CACHE.keySet();
    }
}
