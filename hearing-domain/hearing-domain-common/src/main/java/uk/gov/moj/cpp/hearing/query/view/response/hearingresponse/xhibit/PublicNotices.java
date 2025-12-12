package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PublicNotices implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> publicNotice;

    public PublicNotices(final List<String> publicNotice) {
        this.publicNotice = publicNotice;
    }

    public static PublicNotices.Builder publicNotices() {
        return new PublicNotices.Builder();
    }

    public List<String> getPublicNotice() {
        return publicNotice;
    }

    public void setPublicNotice(final List<String> publicNotice) {
        if(Objects.nonNull(publicNotice) && !publicNotice.isEmpty()){
            this.publicNotice = publicNotice.stream().collect(Collectors.toList());
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj){
            return true;
        }
        if (obj == null || getClass() != obj.getClass()){
            return false;
        }
        final PublicNotices that = (PublicNotices) obj;

        return java.util.Objects.equals(this.publicNotice, that.publicNotice);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(publicNotice);
    }

    @Override
    public String toString() {
        return "PublicNotices{" +
                "publicNotice='" + publicNotice + "'" +
                "}";
    }

    public static class Builder {
        private List<String> publicNotice;

        public PublicNotices.Builder withPublicNotice(final List<String> publicNotice) {
            this.publicNotice = publicNotice;
            return this;
        }

        public PublicNotices build() {
            return new PublicNotices(publicNotice);
        }
    }
}