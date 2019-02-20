package uk.gov.moj.cpp.hearing.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DefendantAliasesJPAMapper {

    public String toJPA(final Collection<String> aliases) {
        if (null == aliases) {
            return null;
        }
        return String.join(", ", aliases);
    }

    public List<String> fromJPA(final String aliases) {
        if (null == aliases) {
            return new ArrayList<>();
        }
        return Stream.of(aliases.split(",")).map(String::trim).collect(Collectors.toList());
    }
}