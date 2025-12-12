package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselUpdated;
import uk.gov.moj.cpp.hearing.mapping.HearingApplicantCounselJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplicantCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.HearingApplicantCounselRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S00112", "squid:CommentedOutCodeLine"})
@ServiceComponent(EVENT_LISTENER)
public class ApplicantCounselEventListener {

    public static final String HEARING_NOT_FOUND = "Hearing not found";
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicantCounselEventListener.class.getName());
    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingApplicantCounselRepository hearingApplicantCounselRepository;

    @Inject
    private HearingApplicantCounselJPAMapper hearingApplicantCounselJPAMapper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;


    @Transactional
    @Handles("hearing.applicant-counsel-added")
    public void applicantCounselAdded(final JsonEnvelope envelope) {

        final ApplicantCounselAdded applicantCounselAdded = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), ApplicantCounselAdded.class);
        final Hearing hearing = hearingRepository.findBy(applicantCounselAdded.getHearingId());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Defence counsel added for hearingId {} ", applicantCounselAdded.getHearingId());
        }
        if (hearing == null) {
            LOGGER.error("Hearing not found");
        } else {
            final HearingApplicantCounsel hearingApplicantCounsel = hearingApplicantCounselJPAMapper.toJPA(hearing, applicantCounselAdded.getApplicantCounsel());
            hearingApplicantCounsel.setId(new HearingSnapshotKey(applicantCounselAdded.getApplicantCounsel().getId(), hearing.getId()));
            hearingApplicantCounselRepository.saveAndFlush(hearingApplicantCounsel);
        }

    }

    @Transactional
    @Handles("hearing.applicant-counsel-removed")
    public void applicantCounselRemoved(final JsonEnvelope event) {
        final ApplicantCounselRemoved applicantCounselRemoved = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ApplicantCounselRemoved.class);
        final Hearing hearing = hearingRepository.findBy(applicantCounselRemoved.getHearingId());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Applicant counsel removed for hearingId {} ", applicantCounselRemoved.getHearingId());
        }
        if (hearing == null) {
            LOGGER.error("Hearing not found");
        } else {
            final Optional<HearingApplicantCounsel> hearingApplicantCounsel =
                    hearing.getApplicantCounsels().stream().filter(pc -> pc.getId().getId().equals(applicantCounselRemoved.getId()))
                            .findFirst();

            if (hearingApplicantCounsel.isPresent()) {
                final HearingApplicantCounsel softDeleteApplicationCounsel = hearingApplicantCounsel.get();
                softDeleteApplicationCounsel.setDeleted(true);
                hearingApplicantCounselRepository.saveAndFlush(softDeleteApplicationCounsel);
            }
        }
    }

    @Transactional
    @Handles("hearing.applicant-counsel-updated")
    public void applicantCounselUpdated(final JsonEnvelope envelope) {

        final ApplicantCounselUpdated applicantCounselUpdated = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), ApplicantCounselUpdated.class);
        final Hearing hearing = hearingRepository.findBy(applicantCounselUpdated.getHearingId());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Applicant counsel updated for hearingId {} ", applicantCounselUpdated.getHearingId());
        }
        if (hearing == null) {
            LOGGER.error(HEARING_NOT_FOUND);
        } else {
            final HearingApplicantCounsel hearingApplicantCounsel = hearingApplicantCounselJPAMapper.toJPA(hearing, applicantCounselUpdated.getApplicantCounsel());
            hearingApplicantCounsel.setId(new HearingSnapshotKey(applicantCounselUpdated.getApplicantCounsel().getId(), hearing.getId()));
            hearingApplicantCounselRepository.saveAndFlush(hearingApplicantCounsel);

        }

    }
}
