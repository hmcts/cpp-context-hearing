package uk.gov.justice.progression.events;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.CrownCourtHearing;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing;

@Event("public.progression.events.sending-sheet-completed")
public class SendingSheetCompleted {
  private CrownCourtHearing crownCourtHearing;

  private Hearing hearing;

  public SendingSheetCompleted() {

  }

  public SendingSheetCompleted(final CrownCourtHearing crownCourtHearing, final Hearing hearing) {
    this.crownCourtHearing = crownCourtHearing;
    this.hearing = hearing;
  }

  public CrownCourtHearing getCrownCourtHearing() {
    return crownCourtHearing;
  }

  public Hearing getHearing() {
    return hearing;
  }

  public static Builder sendingSheetCompleted() {
    return new Builder();
  }

  public static class Builder {
    private CrownCourtHearing crownCourtHearing;

    private Hearing hearing;

    public Builder withCrownCourtHearing(final CrownCourtHearing crownCourtHearing) {
      this.crownCourtHearing = crownCourtHearing;
      return this;
    }

    public Builder withHearing(final Hearing hearing) {
      this.hearing = hearing;
      return this;
    }

    public SendingSheetCompleted build() {
      return new SendingSheetCompleted(crownCourtHearing, hearing);
    }
  }
}
