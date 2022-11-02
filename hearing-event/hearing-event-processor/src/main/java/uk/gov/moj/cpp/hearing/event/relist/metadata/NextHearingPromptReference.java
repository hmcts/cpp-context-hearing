package uk.gov.moj.cpp.hearing.event.relist.metadata;

import static java.util.Arrays.stream;

@SuppressWarnings("squid:S00115")
public enum NextHearingPromptReference {
    HDATE, HTIME, HTYPE, HEST, HCHOUSE, hCHOUSEOrganisationName, HCROOM, bookingReference, existingHearingId, reservedJudiciary, weekCommencing, timeOfHearing, fixedDate, dateToBeFixed, firstReviewDate, totalCustodialPeriod, suspendedPeriod, probationteamtobenotifiedOrganisationName, probationteamtobenotifiedAddress1, probationteamtobenotifiedAddress2, probationteamtobenotifiedAddress3, probationteamtobenotifiedAddress4, probationteamtobenotifiedAddress5, probationteamtobenotifiedPostCode, judgeReservesReviewHearing, endDate, hmiSlots;

      public static boolean isPresent(final String value){
          return stream(values()).anyMatch(v -> v.name().equals(value));
      }
}
