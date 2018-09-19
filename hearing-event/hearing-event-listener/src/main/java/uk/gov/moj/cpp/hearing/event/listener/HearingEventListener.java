package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.json.schemas.core.Target;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.mapping.TargetJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.ResultLineRepository;

import javax.inject.Inject;

@SuppressWarnings({"squid:CommentedOutCodeLine"})
@ServiceComponent(EVENT_LISTENER)
public class HearingEventListener {

    @Inject
    private ResultLineRepository resultLineRepository;

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private TargetJPAMapper targetJPAMapper;

    @Handles("hearing.draft-result-saved")
    public void draftResultSaved(final JsonEnvelope event) {

        final DraftResultSaved draftResultSaved = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), DraftResultSaved.class);

        final Target targetIn = draftResultSaved.getTarget();

        final Hearing hearing = this.hearingRepository.findBy(draftResultSaved.getTarget().getHearingId());

        hearing.getTargets().stream()
                .filter(t -> t.getId().equals(targetIn.getTargetId()))
                .findFirst()
                .ifPresent(previousTarget -> hearing.getTargets().remove(previousTarget));

        hearing.getTargets().add(targetJPAMapper.toJPA(hearing, targetIn));

        hearingRepository.save(hearing);
    }

}
