package uk.gov.moj.cpp.hearing.event.listener;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;
import uk.gov.moj.cpp.hearing.persist.NowsMaterialRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ex.NowsMaterial;
import uk.gov.moj.cpp.hearing.persist.entity.ex.NowsMaterialStatus;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

@ServiceComponent(EVENT_LISTENER)
public class NowsRequestedEventListener {

    @Inject
    JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private NowsMaterialRepository nowsMaterialRepository;


    @Transactional
    @Handles("hearing.events.nows-requested")
    public void nowsRequested(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final NowsRequested nowsRequested = jsonObjectToObjectConverter.convert(payload, NowsRequested.class);

        UUID hearingId = fromString(nowsRequested.getHearing().getId());

        List<NowsMaterial> nowsMaterials = nowsMaterialRepository.findByHearingId(hearingId);
        if (nowsMaterials != null) {
            nowsMaterials.forEach(nowsMaterial -> nowsMaterialRepository.remove(nowsMaterial));
        }
        nowsRequested.getHearing().getNows().forEach(now -> {
            UUID defendantId = fromString(now.getDefendantId());
            now.getMaterial().forEach(material -> {
                List<String> stringList = material.getUserGroups().stream().map(materialUserGroup -> materialUserGroup.getGroup()).collect(Collectors.toList());
                nowsMaterialRepository.save(NowsMaterial.builder().withUserGroups(stringList).withStatus(NowsMaterialStatus.REQUESTED).withId(fromString(material.getId())).withDefendantId(defendantId).withHearingId(hearingId).build());
            });
        });


    }
}
