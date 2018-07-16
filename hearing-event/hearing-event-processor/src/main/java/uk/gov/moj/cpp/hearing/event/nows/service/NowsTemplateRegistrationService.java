package uk.gov.moj.cpp.hearing.event.nows.service;

import uk.gov.justice.services.common.configuration.Value;
import uk.gov.moj.cpp.hearing.event.nows.service.exception.TemplateRegistrationException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import com.docmosis.SystemManager;
import com.docmosis.document.converter.ConversionException;
import com.docmosis.document.converter.NoConvertersRunningException;
import com.docmosis.template.TemplateStoreException;
import com.docmosis.template.store.StoreHelper;
import com.docmosis.template.store.TemplateContext;
import com.docmosis.template.store.TemplateIdentifier;
import com.docmosis.template.store.TemplateStore;
import com.docmosis.template.store.TemplateStoreFactory;
import com.docmosis.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@Singleton
public class NowsTemplateRegistrationService {

    public static final String TEMPLATE_CONTEXT = "hearing";
    public static final String TEMPLATE_IDENTIFIER = "NoticeOrderWarrants";
    private static final Logger LOGGER = LoggerFactory.getLogger(NowsTemplateRegistrationService.class);
    private final Properties docmosisProperties = new Properties();

    @Inject
    @Value(key = "docmosisConverterPoolConfig")
    private String converterPoolConfig;

    @Inject
    @Value(key = "docmosisPropertiesFile")
    private String docmosisPropertiesFile;

    @PostConstruct
    public void registerNoticeOfResultsTemplate() {
        try {
            initialiseSystemManager();
            final TemplateContext templateContext = new TemplateContext(TEMPLATE_CONTEXT);
            final TemplateStore store = TemplateStoreFactory.getStore();
            final TemplateIdentifier templateId = new TemplateIdentifier(TEMPLATE_IDENTIFIER, templateContext);
            StoreHelper.storeTemplate(templateId, getClass().getClassLoader().getResourceAsStream("templates/NoticeOrderWarrantsTemplate.docx"), true, true, store);
        } catch (IOException | TemplateStoreException | ConversionException | NoConvertersRunningException e) {
            LOGGER.error("Error registering the Notice orders and warrants Results Template with Docmosis Service", e);
            throw new TemplateRegistrationException(e);
        }
    }

    public void initialiseSystemManager() throws IOException {
        final FileInputStream docmosisPropertiesInputStream = new FileInputStream(docmosisPropertiesFile);
        docmosisProperties.load(docmosisPropertiesInputStream);
        final Configuration config = new Configuration(docmosisProperties);
        config.setConverterPoolConfiguration(converterPoolConfig);
        SystemManager.initialise(config);
    }

    @PreDestroy
    void preDestroy() {
        SystemManager.release();
    }

}
