package uk.gov.moj.cpp.hearing.xhibit;

import static java.lang.String.format;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static javax.xml.bind.JAXBContext.newInstance;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static uk.gov.moj.cpp.hearing.XmlProducerType.PUBLIC_DISPLAY;

import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Currentcourtstatus;
import uk.gov.moj.cpp.hearing.xhibit.exception.GenerationFailedException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;


public class XmlUtils {

    private static final String PD_NAME = "currentcourtstatus";
    private final static QName PD_QNAME = new QName("", PD_NAME);
    private static final String PUBLIC_DISPLAY_XSD_PATH = "xhibit/xsd/pd/";

    public String createPublicDisplay(final Currentcourtstatus currentcourtstatus) {
        final JAXBElement<Currentcourtstatus> stringJAXBElement = new JAXBElement<>(PD_QNAME, Currentcourtstatus.class, null, currentcourtstatus);
        final String publicDisplayXmlString = convertToXml(stringJAXBElement);

        final String publicDisplayFilePath = PUBLIC_DISPLAY_XSD_PATH + PUBLIC_DISPLAY.getSchemaName();
        validate(publicDisplayXmlString, publicDisplayFilePath);

        return publicDisplayXmlString;
    }

    private String convertToXml(final JAXBElement<?> documentRoot) {

        final StringWriter sw = new StringWriter();

        try {
            final Marshaller jaxbMarshaller = getJaxbContext().createMarshaller();

            jaxbMarshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new XhibitNamespacePrefixMapper());
            jaxbMarshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(documentRoot, sw);
        } catch (final JAXBException e) {
            throw new GenerationFailedException("Could not marshal XML", e);
        }
        return sw.toString();
    }

    @SuppressWarnings({"squid:S2755"})
    private void validate(final String inputXml, final String schemaFile) {
        try {
            final URL xsd = this.getClass().getClassLoader().getResource(schemaFile);
            final Schema schema = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI).newSchema(xsd);
            final Validator validator = schema.newValidator();

            final Source source = new StreamSource(new StringReader(inputXml));

            validator.validate(source);
        } catch (final SAXException | IOException e) {
            throw new GenerationFailedException(format("Could not validate XML against schema %s : %s", schemaFile, e.getMessage()), e);
        }
    }

    private JAXBContext getJaxbContext() throws JAXBException {
        return newInstance("uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd");
    }
}
