package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.core.courts.HearingCaseNote;

@SuppressWarnings("squid:S00121")
@Event("hearing.hearing-case-note-saved")
public class HearingCaseNoteSaved {
    private final HearingCaseNote hearingCaseNote;

    public HearingCaseNoteSaved(final HearingCaseNote hearingCaseNote) {
        this.hearingCaseNote = hearingCaseNote;
    }

    public static HearingCaseNoteSaved.Builder hearingCaseNoteSaved() {
        return new HearingCaseNoteSaved.Builder();
    }

    public HearingCaseNote getHearingCaseNote() {
        return hearingCaseNote;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final HearingCaseNoteSaved that = (HearingCaseNoteSaved) obj;

        return java.util.Objects.equals(this.hearingCaseNote, that.hearingCaseNote);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(hearingCaseNote);
    }

    @Override
    public String toString() {
        return "HearingCaseNoteSaved{" +
                "hearingCaseNote='" + hearingCaseNote + "'" +
                "}";
    }

    public static class Builder {
        private HearingCaseNote hearingCaseNote;

        public HearingCaseNoteSaved.Builder withHearingCaseNote(final HearingCaseNote hearingCaseNote) {
            this.hearingCaseNote = hearingCaseNote;
            return this;
        }

        public HearingCaseNoteSaved build() {
            return new HearingCaseNoteSaved(hearingCaseNote);
        }
    }
}
