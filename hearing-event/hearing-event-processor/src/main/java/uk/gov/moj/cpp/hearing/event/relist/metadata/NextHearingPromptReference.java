package uk.gov.moj.cpp.hearing.event.relist.metadata;

import static java.util.Arrays.stream;

public enum NextHearingPromptReference {
    HDATE, HTIME, HTYPE, HEST, HCHOUSE, HCROOM, bookingReference, existingHearingId, reservedJudiciary, weekCommencing, timeOfHearing, fixedDate;

      public static boolean isPresent(final String value){
          return stream(values()).anyMatch(v -> v.name().equals(value));
      }
}
