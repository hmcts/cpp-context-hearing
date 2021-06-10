package uk.gov.moj.cpp.hearing.event.helper;


import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static javax.json.Json.createArrayBuilder;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.event.helper.HearingHelper.getOffencesFromHearing;

import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultCategory;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV2;

import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

public class ResultsSharedHelper {

    /**
     * If any of the JudicialResults have a Category of Final , then set the corresponding Offence's
     * isDisposed Flag to true.
     *
     * @param resultsShared
     * @return void
     */
    public void setIsDisposedFlagOnOffence(final ResultsShared resultsShared) {
        final List<Offence> offencesList = getOffencesFromHearing(resultsShared.getHearing());
        setDisposed(offencesList);
    }

    /**
     *  If any of the JudicialResults have a Category of Final , then set the corresponding Offence's isDisposed Flag to true.
     *
     * @param resultsShared
     * @return void
     *
     */
    public void setIsDisposedFlagOnOffence(final ResultsSharedV2 resultsShared) {
        final List<Offence> offencesList = getOffencesFromHearing(resultsShared.getHearing());
        setDisposed(offencesList);
    }

    private void setDisposed(final List<Offence> offencesList) {

        if (isNotEmpty(offencesList)) {
            for (final Offence offence : offencesList) {

                final List<JudicialResult> judicialResults = offence.getJudicialResults();

                if (nonNull(judicialResults) && isCategoryTypeFinalPresentInJudicialResult(judicialResults)) {
                    offence.setIsDisposed(Boolean.TRUE);
                } else {
                    offence.setIsDisposed(Boolean.FALSE);
                }
            }
        }

    }

    private boolean isCategoryTypeFinalPresentInJudicialResult(final List<JudicialResult> judicialResultsList) {

        if (isNotEmpty(judicialResultsList)) {
            return judicialResultsList
                    .stream()
                    .filter(judicialResult -> nonNull(judicialResult.getCategory()))
                    .anyMatch(judicialResult -> judicialResult.getCategory().equals(JudicialResultCategory.FINAL));
        }
        return false;

    }

    public void cancelFutureHearingDays(final JsonEnvelope context, final Sender sender, final ResultsShared resultsShared, final ObjectToJsonObjectConverter objectToJsonObjectConverter) {
        final Hearing sharedHearing = resultsShared.getHearing();
        final List<HearingDay> hearingDayList = sharedHearing.getHearingDays();
        final CrackedIneffectiveTrial crackedIneffectiveTrial = sharedHearing.getCrackedIneffectiveTrial();

        if (hearingDayList.size() > 1 && nonNull(crackedIneffectiveTrial)) {
            for (final HearingDay hearingDay : hearingDayList) {
                if (hearingDay.getSittingDay().isAfter(resultsShared.getSharedTime())) {
                    hearingDay.setIsCancelled(true);
                }
            }

            if (hearingDayList.stream().anyMatch(hearingDay -> ofNullable(hearingDay.getIsCancelled()).orElse(false))) {
                cancelHearingDays(context, sender, resultsShared.getHearingId(), hearingDayList, objectToJsonObjectConverter);
            }
        }
    }

    public void cancelFutureHearingDays(final JsonEnvelope context, final Sender sender, final ResultsSharedV2 resultsShared, final ObjectToJsonObjectConverter objectToJsonObjectConverter) {
        final Hearing sharedHearing = resultsShared.getHearing();
        final List<HearingDay> hearingDayList = sharedHearing.getHearingDays();
        final CrackedIneffectiveTrial crackedIneffectiveTrial = sharedHearing.getCrackedIneffectiveTrial();

        if (hearingDayList.size() > 1 && nonNull(crackedIneffectiveTrial)) {
            for (final HearingDay hearingDay : hearingDayList) {
                if(hearingDay.getSittingDay().isAfter(resultsShared.getSharedTime())) {
                    hearingDay.setIsCancelled(true);
                }
            }

            if (hearingDayList.stream().anyMatch(hearingDay -> ofNullable(hearingDay.getIsCancelled()).orElse(false))) {
                cancelHearingDays(context, sender, resultsShared.getHearingId(), hearingDayList, objectToJsonObjectConverter);
            }
        }
    }

    private void cancelHearingDays(final JsonEnvelope context, final Sender sender, final UUID hearingId, final List<HearingDay> hearingDaysList, final ObjectToJsonObjectConverter objectToJsonObjectConverter) {
        final JsonArrayBuilder arrayBuilder = createArrayBuilder();
        for (final HearingDay hearingDay : hearingDaysList) {
            arrayBuilder.add(createObjectBuilder(objectToJsonObjectConverter.convert(hearingDay)).build());
        }
        final JsonObject payload = Json.createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("hearingDays", arrayBuilder)
                .build();

        sender.send(envelop(payload)
                .withName("hearing.command.cancel-hearing-days")
                .withMetadataFrom(context));
    }
}
