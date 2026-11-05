package uk.gov.moj.cpp.hearing.command.handler.service;

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
public class XhibitCrownCourtCentresCache {

    private final Map<UUID, JsonObject> crownCourtCentresCache = new HashMap<>();

    @Inject
    private ReferenceDataService referenceDataService;

    @PostConstruct
    public void init() {
        referenceDataService.getAllCrownCourtCentres()
                .forEach(crownCourtCentre -> crownCourtCentresCache.put(fromString(crownCourtCentre.getString("id")), crownCourtCentre));
    }

    public Set<UUID> getAllCrownCourtCentres() {
        return crownCourtCentresCache.keySet();
    }
}
