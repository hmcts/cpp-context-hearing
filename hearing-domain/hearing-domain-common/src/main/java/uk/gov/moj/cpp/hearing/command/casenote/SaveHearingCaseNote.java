package uk.gov.moj.cpp.hearing.command.casenote;

import uk.gov.justice.json.schemas.core.HearingCaseNote;

import java.util.UUID;

@SuppressWarnings("squid:S00121")
public class SaveHearingCaseNote {
    private final HearingCaseNote hearingCaseNote;

    private final UUID hearingId;

    public SaveHearingCaseNote(final HearingCaseNote hearingCaseNote, final UUID hearingId) {
        this.hearingCaseNote = hearingCaseNote;
        this.hearingId = hearingId;
    }

    public HearingCaseNote getHearingCaseNote() {
        return hearingCaseNote;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public static Builder saveHearingCaseNote() {
        return new uk.gov.moj.cpp.hearing.command.casenote.SaveHearingCaseNote.Builder();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final uk.gov.moj.cpp.hearing.command.casenote.SaveHearingCaseNote that = (uk.gov.moj.cpp.hearing.command.casenote.SaveHearingCaseNote) obj;

        return java.util.Objects.equals(this.hearingCaseNote, that.hearingCaseNote) &&
                java.util.Objects.equals(this.hearingId, that.hearingId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(hearingCaseNote, hearingId);
    }

    @Override
    public String toString() {
        return "SaveHearingCaseNote{" +
                "hearingCaseNote='" + hearingCaseNote + "'," +
                "hearingId='" + hearingId + "'" +
                "}";
    }

    public static class Builder {
        private HearingCaseNote hearingCaseNote;

        private UUID hearingId;

        public Builder withHearingCaseNote(final HearingCaseNote hearingCaseNote) {
            this.hearingCaseNote = hearingCaseNote;
            return this;
        }

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public SaveHearingCaseNote build() {
            return new uk.gov.moj.cpp.hearing.command.casenote.SaveHearingCaseNote(hearingCaseNote, hearingId);
        }
    }
}
