package uk.gov.moj.cpp.hearing.event.nows;


import com.docmosis.SystemManager;
import com.docmosis.document.DocumentProcessor;
import com.docmosis.template.population.DataProviderBuilder;
import com.docmosis.util.Configuration;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.File;

public class DocmosisTester {

    private static final String key = "5NAD-KLWH-JAMO-DKIA-JAEE-7JLH-WKFQ-FR18-7424-6-6DBE";
    private static final String site =
            "Licensed To: Ministry Of Justice\n" +
                    "For use with single project: MoJ Common Platform Programme\n";
    private static final String officePath = "/usr/lib/libreoffice";
    private static Configuration config = new Configuration(key, site, officePath);

    static {

        config.setConverterPoolConfiguration("1");
        config.setProperty("docmosis.openoffice.location.binary.searchpath", "program");
    }

    public static void main(final String[] args) throws Exception {

        if (!new File(officePath).isDirectory() || !new File(officePath).canRead()) {
            System.err.println("\nPlease check \"officePath\" is set to the install dir for OpenOffice or LibreOffice");
            System.exit(1);
        }

        generateDocument("suspended-sentence-order");
    }

    private static void generateDocument(final String resultType) throws Exception {
        try (final JsonReader jsonReader = Json.createReader(DocmosisTester.class.getResourceAsStream("/data/" + resultType + ".json"))) {

            final JsonObject caseDetails = jsonReader.readObject();

            try {
                SystemManager.initialise(config);

                // This is a local copy of the template, as opposed to the one on the vm used by WildFly
                final File templateFile = new File(DocmosisTester.class.getResource("/NoticeOrderWarrantsTemplate.docx").toURI());
                final File outputFile = new File("pdfs/" + resultType + ".pdf");

                if (!templateFile.canRead()) {
                    System.err.println("\nCannot find '" + templateFile + "' in: " + new File("").getCanonicalPath());
                } else {
                    DocumentProcessor.renderDoc(templateFile, outputFile, new DataProviderBuilder().addJSONString(caseDetails.toString()).getDataProvider());
                    System.out.println("\nCreated: " + outputFile.getCanonicalPath());
                }
            } finally {
                SystemManager.release();
            }
        }
    }
}
