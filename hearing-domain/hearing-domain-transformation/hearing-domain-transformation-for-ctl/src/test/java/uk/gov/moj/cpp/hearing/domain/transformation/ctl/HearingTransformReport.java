package uk.gov.moj.cpp.hearing.domain.transformation.ctl;

import uk.gov.moj.cpp.coredomain.transform.TransformQuery;
import uk.gov.moj.cpp.coredomain.transform.TransformReport;

import java.io.IOException;
import java.util.Arrays;

public class HearingTransformReport {


    public final static String[] EVENT_PACKAGES =  new String [] {
            "uk.gov.moj.cpp.hearing.domain.event",
            "uk.gov.moj.cpp.hearing.nows.events",
            "uk.gov.justice.hearing.courts",
            "uk.gov.moj.cpp.hearing.subscription.events"} ;
    public final static String MASTER_PACKAGE_PREFIX="hearingmaster";

    public TransformReport query() throws IOException {
        return query("../../..");
    }

    public TransformReport query(String projectRoot) throws IOException {
        TransformReport report = (new TransformQuery()).compare(projectRoot,
                Arrays.asList(
                        "/hearing-command/hearing-command-handler/src/raml/hearing-private-event.messaging.raml",
                         "/hearing-event/hearing-event-processor/src/raml/hearing-event-processor.messaging.raml"),
                EVENT_PACKAGES, MASTER_PACKAGE_PREFIX);
        System.out.println("\r\n\r\n*****************results::");
        report.printOut();
        return report;

    }

    public static void main(String[] args) throws IOException{
        (new HearingTransformReport()).query(".");
    }

}
