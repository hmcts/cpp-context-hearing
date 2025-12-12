package uk.gov.moj.cpp.hearing.event.judiciary;


import static java.time.ZonedDateTime.now;
import static java.util.Objects.nonNull;
import static javax.json.Json.createObjectBuilder;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole;
import uk.gov.moj.cpp.hearing.repository.JudicialRoleRepository;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class JudiciaryUpdatedUserAddedProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JudiciaryUpdatedUserAddedProcessor.class);


    @Inject
    private JudicialRoleRepository judicialRoleRepository;

    @Inject
    private Sender sender;

    @Handles("public.referencedata.event.user-associated-with-judiciary")
    public void userAssociatedWithJudiciary(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("public.referencedata.event.user-associated-with-judiciary event received {}", event.toObfuscatedDebugString());
        }

        final String judiciaryId = event.payloadAsJsonObject().getString("judiciaryId");
        final String emailId = event.payloadAsJsonObject().getString("emailId");
        final String cpUserId = event.payloadAsJsonObject().getString("cpUserId");

        final List<JudicialRole> judicialRoles = judicialRoleRepository.findByJudicialId(UUID.fromString(judiciaryId));

        judicialRoles.stream()
                .filter(e -> !nonNull(e.getUserId()))
                .filter(judicialRole -> judicialRole.getHearing().getHearingDays().stream()
                                    .anyMatch(hearingDay -> now().isBefore(hearingDay.getDateTime()))
                )
                .forEach(judicialRole -> {
                    final JsonObject judicialRoleJsonObject = createObjectBuilder()
                    .add("judiciaryId", judiciaryId)
                    .add("emailId", emailId)
                    .add("cpUserId", cpUserId)
                    .add("hearingId", judicialRole.getId().getHearingId().toString())
                    .add("id", judicialRole.getId().getId().toString())
                    .build();
                    sender.send(Enveloper.envelop(judicialRoleJsonObject)
                            .withName("hearing.command.user-attached-to-judiciary")
                            .withMetadataFrom(event));

        });
    }
}