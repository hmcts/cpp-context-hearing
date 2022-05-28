package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.core.courts.CompanyRepresentative;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeAdded;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeRemoved;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeUpdated;
import uk.gov.moj.cpp.hearing.mapping.HearingCompanyRepresentativeJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCompanyRepresentative;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.HearingCompanyRepresentativeRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_LISTENER)
public class CompanyRepresentativeEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyRepresentativeEventListener.class.getName());
    private static final String HEARING_NOT_FOUND = "Hearing not found";

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingCompanyRepresentativeRepository hearingCompanyRepresentativeRepository;

    @Inject
    private HearingCompanyRepresentativeJPAMapper hearingCompanyRepresentativeJPAMapper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Transactional
    @Handles("hearing.company-representative-added")
    public void companyRepresentativeAdded(final JsonEnvelope envelope) {
        final CompanyRepresentativeAdded companyRepresentativeAdded = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), CompanyRepresentativeAdded.class);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Company representative added event payload {} ", companyRepresentativeAdded);
        }
        final Hearing hearing = getHearing(companyRepresentativeAdded.getHearingId());
        saveCompanyRepresentative(hearing, companyRepresentativeAdded.getCompanyRepresentative());
    }

    @Transactional
    @Handles("hearing.company-representative-updated")
    public void companyRepresentativeUpdated(final JsonEnvelope envelope) {
        final CompanyRepresentativeUpdated companyRepresentativeUpdated = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), CompanyRepresentativeUpdated.class);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Company representative updated for hearingId {} ", companyRepresentativeUpdated.getHearingId());
        }
        final Hearing hearing = getHearing(companyRepresentativeUpdated.getHearingId());
        saveCompanyRepresentative(hearing, companyRepresentativeUpdated.getCompanyRepresentative());
    }

    @Transactional
    @Handles("hearing.company-representative-removed")
    public void companyRepresentativeRemoved(final JsonEnvelope event) {
        final CompanyRepresentativeRemoved companyRepresentativeRemoved = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), CompanyRepresentativeRemoved.class);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Company representative removed for hearingId {} ", companyRepresentativeRemoved.getHearingId());
        }
        final Hearing hearing = getHearing(companyRepresentativeRemoved.getHearingId());
        if (Objects.nonNull(hearing)) {
            final Optional<HearingCompanyRepresentative> hearingCompanyRepresentative =
                    hearing.getCompanyRepresentatives().stream()
                            .filter(cr -> cr.getId().getId().equals(companyRepresentativeRemoved.getId()))
                            .findFirst();
            if (hearingCompanyRepresentative.isPresent()) {
                final HearingCompanyRepresentative companyRepresentative = hearingCompanyRepresentative.get();
                companyRepresentative.setDeleted(true);
                hearingCompanyRepresentativeRepository.saveAndFlush(companyRepresentative);
            }
        } else {
            LOGGER.error(HEARING_NOT_FOUND);
        }
    }

    private void saveCompanyRepresentative(final Hearing hearing, final CompanyRepresentative companyRepresentative) {
        if (Objects.nonNull(hearing)) {
            final HearingCompanyRepresentative hearingCompanyRepresentative = hearingCompanyRepresentativeJPAMapper.toJPA(hearing, companyRepresentative);
            hearingCompanyRepresentative.setId(new HearingSnapshotKey(companyRepresentative.getId(), hearing.getId()));
            hearingCompanyRepresentativeRepository.saveAndFlush(hearingCompanyRepresentative);
        } else {
            LOGGER.error(HEARING_NOT_FOUND);
        }
    }

    private Hearing getHearing(final UUID hearingId) {
        return hearingRepository.findBy(hearingId);
    }
}
