package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.justice.core.courts.CourtApplication.courtApplication;
import static uk.gov.justice.core.courts.CourtApplicationParty.courtApplicationParty;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.core.courts.AssociatedDefenceOrganisation;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationOrganisationDetailsUpdatedForHearing;
import uk.gov.moj.cpp.hearing.mapping.CourtApplicationsSerializer;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

@SuppressWarnings({"squid:S3776", "pmd:NullAssignment"})
@ServiceComponent(EVENT_LISTENER)
public class ApplicationOrganisationDetailsUpdatedEventListener {

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private CourtApplicationsSerializer courtApplicationsSerializer;

    @Transactional
    @Handles("hearing.application-organisation-details-updated-for-hearing")
    public void applicationOrganisationDetailsUpdated(final JsonEnvelope envelope) {

        final ApplicationOrganisationDetailsUpdatedForHearing applicationOrganisationDetailsUpdatedForHearing = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), ApplicationOrganisationDetailsUpdatedForHearing.class);
        final UUID applicationId = applicationOrganisationDetailsUpdatedForHearing.getApplicationId();
        final UUID subjectId = applicationOrganisationDetailsUpdatedForHearing.getSubjectId();
        final AssociatedDefenceOrganisation associatedDefenceOrganisation = applicationOrganisationDetailsUpdatedForHearing.getAssociatedDefenceOrganisation();
        final UUID hearingId = applicationOrganisationDetailsUpdatedForHearing.getHearingId();

        final Hearing hearingEntity = hearingRepository.findBy(hearingId);
        final List<CourtApplication> courtApplications = courtApplicationsSerializer.courtApplications(hearingEntity.getCourtApplicationsJson());
        if (isNotEmpty(courtApplications)) {
            final List<CourtApplication> updatedCourtApplications = courtApplications.stream()
                    .filter(courtApplication -> applicationId.equals(courtApplication.getId()) &&
                            (nonNull(courtApplication.getSubject()) && subjectId.equals(courtApplication.getSubject().getId())))
                    .map(courtApplication ->
                            courtApplication()
                                    .withValuesFrom(courtApplication)
                                    .withSubject(courtApplicationParty()
                                            .withValuesFrom(courtApplication.getSubject())
                                            .withAssociatedDefenceOrganisation(associatedDefenceOrganisation)
                                            .build())
                                    .build()
                    ).toList();
            hearingEntity.setCourtApplicationsJson(courtApplicationsSerializer.json(updatedCourtApplications));
            hearingRepository.save(hearingEntity);
        }
    }
}
