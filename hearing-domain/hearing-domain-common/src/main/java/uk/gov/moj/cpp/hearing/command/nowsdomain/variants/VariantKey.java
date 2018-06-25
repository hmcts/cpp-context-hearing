package uk.gov.moj.cpp.hearing.command.nowsdomain.variants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("squid:S2384")
public class VariantKey implements Serializable {

    private UUID hearingId;

    private UUID defendantId;

    private UUID nowsTypeId;

    private List<String> usergroups = new ArrayList<>();

    public static VariantKey variantKey() {
        return new VariantKey();
    }

    public UUID getHearingId() {
        return this.hearingId;
    }

    public VariantKey setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public UUID getDefendantId() {
        return this.defendantId;
    }

    public VariantKey setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public UUID getNowsTypeId() {
        return this.nowsTypeId;
    }

    public VariantKey setNowsTypeId(UUID nowsTypeId) {
        this.nowsTypeId = nowsTypeId;
        return this;
    }

    public List<String> getUsergroups() {
        return this.usergroups;
    }

    public VariantKey setUsergroups(List<String> usergroups) {
        this.usergroups = usergroups;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VariantKey that = (VariantKey) o;
        return Objects.equals(hearingId, that.hearingId) &&
                Objects.equals(defendantId, that.defendantId) &&
                Objects.equals(nowsTypeId, that.nowsTypeId) &&
                Objects.equals(new HashSet<>(usergroups), new HashSet<>(that.usergroups));
    }

    @Override
    public int hashCode() {
        return Objects.hash(hearingId, defendantId, nowsTypeId, new HashSet<>(usergroups));
    }
}
