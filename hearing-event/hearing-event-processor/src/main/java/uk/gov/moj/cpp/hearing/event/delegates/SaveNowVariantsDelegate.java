package uk.gov.moj.cpp.hearing.event.delegates;

import uk.gov.justice.core.courts.Now;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.SaveNowsVariantsCommand;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import javax.inject.Inject;
import java.util.List;

public class SaveNowVariantsDelegate {

    private final Enveloper enveloper;

    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private final Nows2VariantTransform nows2VariantTransform;

    @Inject
    public SaveNowVariantsDelegate(final Enveloper enveloper,
                                   final ObjectToJsonObjectConverter objectToJsonObjectConverter,
                                   final Nows2VariantTransform nows2VariantTransform) {
        this.enveloper = enveloper;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
        this.nows2VariantTransform = nows2VariantTransform;
    }

    public List<Variant> saveNowsVariants(final Sender sender, final JsonEnvelope event, final List<Now> nows, final ResultsShared resultsShared) {

        final List<Variant> newVariantDirectory = this.nows2VariantTransform.toVariants(resultsShared.getHearingId(), nows, resultsShared.getSharedTime());

        final SaveNowsVariantsCommand saveNowsVariantsCommand = SaveNowsVariantsCommand.saveNowsVariantsCommand()
                .setVariants(newVariantDirectory)
                .setHearingId(resultsShared.getHearingId());

        sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.save-nows-variants")
                .apply(this.objectToJsonObjectConverter.convert(saveNowsVariantsCommand)));

        return newVariantDirectory;
    }
}
