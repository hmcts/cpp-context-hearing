package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.json.schemas.core.publichearingresulted.SharedHearing;
import uk.gov.justice.json.schemas.core.publichearingresulted.SharedVariant;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class PublicHearingResulted {
     private SharedHearing hearing;
     private List<SharedVariant> variants = new ArrayList<>();
     private ZonedDateTime sharedTime;

     public SharedHearing getHearing() {
          return hearing;
     }

     public PublicHearingResulted setHearing(SharedHearing hearing) {
          this.hearing = hearing;
          return this;
     }

     public List<SharedVariant> getVariants() {
          return variants;
     }

     public PublicHearingResulted setVariants(List<SharedVariant> variants) {
          this.variants = variants;
          return this;
     }

     public ZonedDateTime getSharedTime() {
          return sharedTime;
     }

     public PublicHearingResulted setSharedTime(ZonedDateTime sharedTime) {
          this.sharedTime = sharedTime;
          return this;
     }

     public static PublicHearingResulted publicHearingResulted() {
          return new PublicHearingResulted();
     }
}
