package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.IndicatedPleaValue;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.OffenceFacts;
import uk.gov.justice.core.courts.Source;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.delegates.PublishResultUtil;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.alcohollevel.AlcoholLevelMethod;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.inject.Inject;

public class OffenceHelper {

    private final ReferenceDataService referenceDataService;

    @Inject
    public OffenceHelper(final ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    public void enrichOffence(final JsonEnvelope context, final Hearing hearing) {

        final List<VerdictType> verdictTypes = referenceDataService.getVerdictTypes(context);
        final List<AlcoholLevelMethod> alcoholLevelMethods = referenceDataService.getAlcoholLevelMethods(context);

        ofNullable(hearing.getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .forEach(prosecutionCase -> prosecutionCase.getDefendants().
                        forEach(defendant -> defendant.getOffences().forEach(offence -> enrichOffence(offence, verdictTypes, alcoholLevelMethods))));

        ofNullable(hearing.getCourtApplications()).orElse(emptyList()).stream()
                .flatMap(ca -> ofNullable(ca.getCourtApplicationCases()).orElse(emptyList()).stream())
                .flatMap(c -> ofNullable(c.getOffences()).map(Collection::stream).orElseGet(Stream::empty))
                .filter(Objects::nonNull)
                .forEach(offence -> enrichOffence(offence, verdictTypes, alcoholLevelMethods));

        ofNullable(hearing.getCourtApplications()).orElse(emptyList()).stream()
                .map(CourtApplication::getCourtOrder)
                .filter(Objects::nonNull)
                .flatMap(o -> o.getCourtOrderOffences().stream())
                .map(CourtOrderOffence::getOffence)
                .forEach(offence -> enrichOffence(offence, verdictTypes, alcoholLevelMethods));
    }

    private void enrichOffence(Offence offence, final List<VerdictType> verdictTypes, final List<AlcoholLevelMethod> alcoholLevelMethods) {

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
        final Optional<VerdictType> fullVerdictType = verdictTypes.stream()
                .filter(verdictType -> verdictType.getId().equals(originalVerdict.getVerdictType().getId()))
                .findFirst();
        fullVerdictType.ifPresent(verdictType -> offence.setVerdict(Verdict.verdict()
                .withVerdictType(verdictType)
                .withJurors(originalVerdict.getJurors())
                .withOffenceId(originalVerdict.getOffenceId())
                .withVerdictDate(originalVerdict.getVerdictDate())
                .withLesserOrAlternativeOffence(originalVerdict.getLesserOrAlternativeOffence())
                .withOriginatingHearingId(originalVerdict.getOriginatingHearingId())
                .build()));
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