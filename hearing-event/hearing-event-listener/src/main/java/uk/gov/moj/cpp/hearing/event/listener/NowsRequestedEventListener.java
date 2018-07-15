package uk.gov.moj.cpp.hearing.event.listener;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;
import uk.gov.moj.cpp.hearing.persist.NowsRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Nows;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterial;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsResult;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

@ServiceComponent(EVENT_LISTENER)
public class NowsRequestedEventListener {

    @Inject
    JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private NowsRepository nowsRepository;


    @Transactional
    @Handles("hearing.events.nows-requested")
    public void nowsRequested(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final NowsRequested nowsRequested = jsonObjectToObjectConverter.convert(payload, NowsRequested.class);

        UUID hearingId = fromString(nowsRequested.getHearing().getId());

        List<Nows> nowsList = nowsRepository.findByHearingId(hearingId);
        if (nowsList != null) {
            nowsList.forEach(nows -> nowsRepository.remove(nows));
        }
        nowsRequested.getHearing().getNows().forEach(now -> {
            UUID typeId = now.getNowsTypeId() == null ? randomUUID() : fromString(now.getNowsTypeId());
            Nows nows = Nows.builder().withNowsTypeId(typeId).withDefendantId(fromString(now.getDefendantId()))
                    .withHearingId(hearingId).withId(fromString(now.getId())).build();

            now.getMaterials().forEach(material -> {
                List<String> stringList = material.getUserGroups().stream().map(materialUserGroup -> materialUserGroup.getGroup()).collect(Collectors.toList());
                NowsMaterial nowsMaterial = NowsMaterial.builder().withUserGroups(stringList).withStatus("requested")
                        .withId(fromString(material.getId())).withLanguage(material.getLanguage()).withNows(nows).build();

                material.getNowResult().forEach(result -> {
                    NowsResult nowResult = NowsResult.builder().withSharedResultId(fromString(result.getSharedResultId())).withSequence(result.getSequence()).withNowsMaterial(nowsMaterial).build();
                    nowsMaterial.getNowResult().add(nowResult);
                });
                nows.getMaterial().add(nowsMaterial);
            });


            nowsRepository.save(nows);
        });


    }
}
