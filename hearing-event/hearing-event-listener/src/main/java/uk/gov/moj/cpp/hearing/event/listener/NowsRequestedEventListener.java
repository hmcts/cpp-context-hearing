package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.nows.events.MaterialUserGroup;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;
import uk.gov.moj.cpp.hearing.persist.NowsRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Nows;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterial;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsResult;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.transaction.Transactional;

@SuppressWarnings("squid:S1188")
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

            final Nows nows = Nows.builder()
                    .withId(fromString(now.getId()))
                    .withNowsTypeId(typeId)
                    .withDefendantId(fromString(now.getDefendantId()))
                    .withHearingId(hearingId)
                    .build();

            nows.setMaterial(now.getMaterials().stream()
                    .map(material -> {

                        final NowsMaterial nowsMaterial = NowsMaterial.builder()
                                .withId(fromString(material.getId()))
                                .withUserGroups(material.getUserGroups().stream()
                                        .map(MaterialUserGroup::getGroup)
                                        .collect(Collectors.toList()))
                                .withStatus("requested")
                                .withLanguage(material.getLanguage())
                                .withNows(nows)
                                .build();

                        nowsMaterial.setNowResult(material.getNowResult().stream()
                                .map(result -> NowsResult.builder()
                                        .withId(randomUUID())
                                        .withSharedResultId(fromString(result.getSharedResultId()))
                                        .withSequence(result.getSequence())
                                        .withNowsMaterial(nowsMaterial)
                                        .build()
                                )
                                .collect(Collectors.toList()));

                        return nowsMaterial;
                    })
                    .collect(Collectors.toList()));

            nowsRepository.save(nows);
        });
    }
}
