package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.nonNull;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingUserAddedToJudiciary;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole;
import uk.gov.moj.cpp.hearing.repository.JudicialRoleRepository;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:CommentedOutCodeLine", "squid:S1166", "squid:S134"})
@ServiceComponent(EVENT_LISTENER)
public class HearingJudiciaryListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(HearingJudiciaryListener.class.getName());


    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private JudicialRoleRepository judicialRoleRepository;


    @Handles("hearing.event.user-added-to-judiciary")
    public void userAddedToJudiciary(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.event.user-added-to-judiciary received {}", event.toObfuscatedDebugString());
        }
        final HearingUserAddedToJudiciary hearingUserAddedToJudiciary = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), HearingUserAddedToJudiciary.class);
        final JudicialRole judicialRole = judicialRoleRepository.findBy(new HearingSnapshotKey(hearingUserAddedToJudiciary.getId(), hearingUserAddedToJudiciary.getHearingId()));
        if (!nonNull(judicialRole.getUserId())){
            judicialRole.setUserId(hearingUserAddedToJudiciary.getCpUserId());
            judicialRoleRepository.save(judicialRole);
        }
    }



















}
