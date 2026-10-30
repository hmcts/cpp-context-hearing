package uk.gov.justice.api.resource;

import uk.gov.justice.services.common.http.HeaderConstants;

import java.io.IOException;
import java.util.UUID;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("hearings/event-log/extract")
public interface QueryApiHearingsEventLogExtractResource {
  @GET
  @Produces("application/vnd.hearing.get-hearing-event-log-extract-for-documents+json")
  Response getHearingsEventLogExtract(@QueryParam("caseId") String caseId,
                                      @QueryParam("hearingId") String hearingId,
                                      @QueryParam("applicationId") String applicationId,
                                      @QueryParam("hearingDate") String hearingDate,
                                      @HeaderParam(HeaderConstants.USER_ID) UUID userId) throws IOException;
}
