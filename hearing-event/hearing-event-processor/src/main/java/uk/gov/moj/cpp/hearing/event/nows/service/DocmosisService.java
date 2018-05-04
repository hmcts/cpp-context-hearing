package uk.gov.moj.cpp.hearing.event.nows.service;

import com.docmosis.document.DocumentProcessor;
import com.docmosis.document.ProcessingException;
import com.docmosis.document.converter.ConversionFormat;
import com.docmosis.document.converter.ConversionInstruction;
import com.docmosis.template.TemplateStoreException;
import com.docmosis.template.population.DataProvider;
import com.docmosis.template.population.DataProviderBuilder;
import com.docmosis.template.store.TemplateContext;
import com.docmosis.template.store.TemplateIdentifier;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.configuration.Value;
import uk.gov.moj.cpp.hearing.event.nows.service.exception.DocumentGenerationException;

import javax.inject.Inject;
import javax.json.JsonObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class DocmosisService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocmosisService.class);

    @Inject
    @Value(key = "docmosisPropertiesFile")
    private String docmosisPropertiesFile;

    @Inject
    @Value(key = "docmosisConverterPoolConfig")
    private String converterPoolConfig;

    @Inject
    private NowsTemplateRegistrationService registrationService;

    public byte[] generateDocument(final JsonObject jsonData, final String templateContext, final String templateIdentifier) {
        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            registrationService.initialiseSystemManager();
            final TemplateContext context = new TemplateContext(templateContext);
            final TemplateIdentifier templateId = new TemplateIdentifier(templateIdentifier, context);
            final ConversionInstruction conversionInstruction = new ConversionInstruction();
            conversionInstruction.setConversionFormats(new ConversionFormat[]{ConversionFormat.FORMAT_PDF});
            DocumentProcessor.renderDoc(templateId, generateDataProvder(jsonData), conversionInstruction, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException | TemplateStoreException | ProcessingException | JSONException e) {
            LOGGER.error(e.getMessage());
            throw new DocumentGenerationException(e);
        }
    }

    private DataProvider generateDataProvder(final JsonObject jsonData) throws JSONException {
        return new DataProviderBuilder().addJSONString(jsonData.toString()).getDataProvider();
    }
}