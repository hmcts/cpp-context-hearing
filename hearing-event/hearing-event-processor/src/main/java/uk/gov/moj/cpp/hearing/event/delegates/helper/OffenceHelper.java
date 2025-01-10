package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.IndicatedPleaValue;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.LjaDetails;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.OffenceFacts;
import uk.gov.justice.core.courts.Source;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.common.ReferenceDataLoader;
import uk.gov.moj.cpp.hearing.event.delegates.PublishResultUtil;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.alcohollevel.AlcoholLevelMethod;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.google.common.collect.Maps;

public class OffenceHelper {

    private final ReferenceDataService referenceDataService;

    private final ReferenceDataLoader referenceDataLoader;

    @Inject
    public OffenceHelper(final ReferenceDataService referenceDataService,
                         final ReferenceDataLoader referenceDataLoader) {
        this.referenceDataService = referenceDataService;
        this.referenceDataLoader = referenceDataLoader;
    }

    public void enrichOffence(final JsonEnvelope context, final Hearing hearing) {

        final List<VerdictType> verdictTypes = referenceDataService.getVerdictTypes(context);
        final List<AlcoholLevelMethod> alcoholLevelMethods = referenceDataService.getAlcoholLevelMethods(context);

        ofNullable(hearing.getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .forEach(prosecutionCase -> prosecutionCase.getDefendants().
                        forEach(defendant -> defendant.getOffences().forEach(offence -> enrichOffence(offence, verdictTypes, alcoholLevelMethods, hearing))));

        ofNullable(hearing.getCourtApplications()).orElse(emptyList()).stream()
                .flatMap(ca -> ofNullable(ca.getCourtApplicationCases()).orElse(emptyList()).stream())
                .flatMap(c -> ofNullable(c.getOffences()).map(Collection::stream).orElseGet(Stream::empty))
                .filter(Objects::nonNull)
                .forEach(offence -> enrichOffence(offence, verdictTypes, alcoholLevelMethods, hearing));

        ofNullable(hearing.getCourtApplications()).orElse(emptyList()).stream()
                .map(CourtApplication::getCourtOrder)
                .filter(Objects::nonNull)
                .flatMap(o -> o.getCourtOrderOffences().stream())
                .map(CourtOrderOffence::getOffence)
                .forEach(offence -> enrichOffence(offence, verdictTypes, alcoholLevelMethods, hearing));
    }

    private void enrichOffence(final Offence offence, final List<VerdictType> verdictTypes, final List<AlcoholLevelMethod> alcoholLevelMethods, final Hearing hearing) {

        if (nonNull(offence.getConvictionDate())) {
            populateConvictingCourt(offence, hearing);
        }

        if (nonNull(offence.getVerdict())) {
            populateFullVerdictTypeData(offence, verdictTypes);
        }

        if (PublishResultUtil.needsIndicatedPleaSetFor(offence)) {
            populateIndicatedPlea(offence);
        }

        if (nonNull(offence.getOffenceFacts())) {
            populateAlcoholLevelMethodData(offence, alcoholLevelMethods);
        }
    }

    private void populateConvictingCourt(final Offence offence, final Hearing hearing) {

        final Map.Entry<UUID, UUID> courtCentreInfo = hearing.getHearingDays().stream()
                .filter(hearingDay -> hearingDay.getSittingDay().toLocalDate().equals(offence.getConvictionDate()))
                .filter(hearingDay -> hearingDay.getCourtCentreId() != null)
                .findFirst()
                .map(hd -> Maps.immutableEntry(hd.getCourtCentreId(), hd.getCourtRoomId()))
                .orElse(Maps.immutableEntry(hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId()));

        final UUID centreId = courtCentreInfo.getKey();
        final UUID roomId = courtCentreInfo.getValue();
        final OrganisationalUnit organisationalUnit = referenceDataLoader.getOrganisationUnitById(centreId);

        final CourtCentre.Builder courtCentreBuilder = CourtCentre.courtCentre()
                .withId(centreId)
                .withRoomId(roomId)
                .withCode(organisationalUnit.getOucode())
                .withName(organisationalUnit.getOucodeL3Name());

        if (JurisdictionType.MAGISTRATES.equals(hearing.getJurisdictionType())) {
            final LjaDetails ljaDetails = referenceDataLoader.getLjaDetails(organisationalUnit);
            courtCentreBuilder.withLja(ljaDetails);
        } else {
            courtCentreBuilder.withCourtLocationCode(organisationalUnit.getCourtLocationCode());
        }

        hearing.getHearingDays().forEach(hearingDay -> {
            if (nonNull(offence.getConvictionDate()) && isNull(offence.getConvictingCourt())) {
                offence.setConvictingCourt(courtCentreBuilder.build());
            }
        });
    }

    private void populateIndicatedPlea(final Offence offence) {
        offence.setIndicatedPlea(IndicatedPlea.indicatedPlea()
                .withOriginatingHearingId(offence.getPlea().getOriginatingHearingId())
                .withIndicatedPleaDate(offence.getPlea().getPleaDate())
                .withIndicatedPleaValue(IndicatedPleaValue.INDICATED_GUILTY)
                .withOffenceId(offence.getId())
                .withSource(Source.IN_COURT)
                .build());
    }

    /**
     * Updates each verdict type with additional fields from reference data (such as verdict type
     * code).
     *
     * @param offence - the offence to be updated.
     * @param verdictTypes - the full set of verdict types from reference data.
     */
    private void populateFullVerdictTypeData(final Offence offence, final List<VerdictType> verdictTypes) {
        final Verdict originalVerdict = offence.getVerdict();
        if(nonNull(originalVerdict.getIsDeleted()) && originalVerdict.getIsDeleted()) {
            offence.setVerdict(null);
        } else {
            final Optional<VerdictType> fullVerdictType = verdictTypes.stream()
                    .filter(verdictType -> verdictType.getId().equals(originalVerdict.getVerdictType().getId()))
                    .findFirst();
            fullVerdictType.ifPresent(verdictType ->
                    offence.setVerdict(Verdict.verdict()
                            .withVerdictType(verdictType)
                            .withJurors(originalVerdict.getJurors())
                            .withOffenceId(originalVerdict.getOffenceId())
                            .withVerdictDate(originalVerdict.getVerdictDate())
                            .withLesserOrAlternativeOffence(originalVerdict.getLesserOrAlternativeOffence())
                            .withOriginatingHearingId(originalVerdict.getOriginatingHearingId())
                            .build()));
        }
    }

    /**
     * Updates each offence fact with additional fields from reference data (such as alcohol level
     * method description).
     *
     * @param offence - the offence to be updated.
     * @param alcoholLevelMethods - the full set of Alcohol Level Methods types from reference data.
     */
    private void populateAlcoholLevelMethodData(final Offence offence, final List<AlcoholLevelMethod> alcoholLevelMethods) {
        final OffenceFacts originalOffenceFacts = offence.getOffenceFacts();
        final Optional<AlcoholLevelMethod> fullAlcoholLevelMethod = alcoholLevelMethods.stream()
                .filter(alm -> alm.getMethodCode().equals(originalOffenceFacts.getAlcoholReadingMethodCode()))
                .findFirst();
        fullAlcoholLevelMethod.ifPresent(alcoholLevelMethod -> offence.setOffenceFacts(OffenceFacts.offenceFacts()
                .withAlcoholReadingMethodDescription(alcoholLevelMethod.getMethodDescription())
                .withAlcoholReadingAmount(originalOffenceFacts.getAlcoholReadingAmount())
                .withAlcoholReadingMethodCode(originalOffenceFacts.getAlcoholReadingMethodCode())
                .withVehicleCode(originalOffenceFacts.getVehicleCode())
                .withVehicleMake(originalOffenceFacts.getVehicleMake())
                .withVehicleRegistration(originalOffenceFacts.getVehicleRegistration())
                .build()));
    }
}