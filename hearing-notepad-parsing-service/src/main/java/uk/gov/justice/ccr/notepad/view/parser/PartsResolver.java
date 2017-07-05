package uk.gov.justice.ccr.notepad.view.parser;


import static com.google.common.collect.Lists.newLinkedList;

import uk.gov.justice.ccr.notepad.view.Part;

import java.util.List;
import java.util.stream.Stream;

public class PartsResolver {

    private final List<Part> parts = newLinkedList();
    private final StringBuilder stringBuilder = new StringBuilder();
    private boolean ignorePartHavingSquareBracket;

    public List<Part> getParts(final String line) {
        Stream<Character> stream = amendBlankSpaceInTheEnd(line).chars().mapToObj(i -> (char) i);
        stream.map(Character::charValue).forEach(this::resolvePart);
        return parts;
    }

    private void resolvePart(final char c) {
        if ('[' == c) {
            appendStringBuilder(c);
            ignorePartHavingSquareBracket = true;
        } else if (']' == c) {
            appendStringBuilder(c);
            if (ignorePartHavingSquareBracket) {
                addPart();
                ignorePartHavingSquareBracket = false;
            }
        } else if (' ' == c) {
            if (ignorePartHavingSquareBracket) {
                appendStringBuilder(c);
            } else if (stringBuilder.length() > 0) {
                addPart();
            }
        } else {
            appendStringBuilder(c);
        }
    }

    private void addPart() {
        Part part = new Part();
        part.setValue(stringBuilder.toString().trim());
        stringBuilder.setLength(0);
        parts.add(part);
    }

    private void appendStringBuilder(final char c) {
        stringBuilder.append(c);
    }

    private String amendBlankSpaceInTheEnd(final String line) {
        return line.trim() + " ";
    }

}
