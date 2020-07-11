package uk.gov.justice.ccr.notepad.view.parser;

import uk.gov.justice.ccr.notepad.view.Part;

import java.util.ArrayList;
import java.util.List;

public class PartsResolver {

    public static final char SQUARE_BRACKET_OPEN_CHAR = '[';
    public static final char SQUARE_BRACKET_CLOSE_CHAR = ']';
    public static final char SPACE_CHAR = ' ';

    public List<Part> getParts(final String line) {
        final List<Part> parts = new ArrayList<>();
        final StringBuilder stringBuilder = new StringBuilder();
        final char[] charArray = line.trim().toCharArray();
        boolean squareBracketOpened = false;
        int indexOfChar = 0;

        for (final Character character : charArray) {
            ++indexOfChar;
            if (charArray.length == indexOfChar) {
                stringBuilder.append(character);
                addPart(parts, stringBuilder);
                break;
            }
            if (character.equals(SQUARE_BRACKET_OPEN_CHAR)) {
                stringBuilder.append(character);
                squareBracketOpened = true;
            } else if (character.equals(SQUARE_BRACKET_CLOSE_CHAR)) {
                stringBuilder.append(character);
                if (squareBracketOpened) {
                    addPart(parts, stringBuilder);
                    squareBracketOpened = false;
                }
            } else if (character.equals(SPACE_CHAR)) {
                if (squareBracketOpened) {
                    stringBuilder.append(character);
                } else {
                    addPart(parts, stringBuilder);
                }
            } else {
                stringBuilder.append(character);
            }
        }

        return parts;
    }

    private void addPart(List<Part> parts, StringBuilder stringBuilder) {
        if (stringBuilder.length() > 0) {
            Part part = new Part();
            part.setValue(stringBuilder.toString());
            parts.add(part);
            stringBuilder.setLength(0);
        }
    }
}
