package uk.gov.moj.cpp.hearing.utils;

import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLine;

import java.util.ArrayList;
import java.util.List;

public class ShareResults {

    private List<SharedResultsCommandResultLine> resultLines = new ArrayList<>();

    public List<SharedResultsCommandResultLine> getResultLines() {
        return resultLines;
    }

    public void setResultLines(final List<SharedResultsCommandResultLine> resultLines) {
        this.resultLines = resultLines;
    }

}

