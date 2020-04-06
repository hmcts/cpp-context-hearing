package uk.gov.moj.cpp.hearing.event.relist.metadata;

import static java.util.Arrays.stream;

public enum NextHearingPromptReference {
    HDATE, HTIME, HTYPE, HEST, HCHOUSE, HCROOM;

      public static boolean isPresent(final String value){
          return stream(values()).anyMatch(v -> v.name().equals(value));
      }
}
