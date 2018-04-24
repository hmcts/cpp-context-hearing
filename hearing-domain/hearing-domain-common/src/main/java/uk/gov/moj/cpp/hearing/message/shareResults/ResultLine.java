package uk.gov.moj.cpp.hearing.message.shareResults;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class ResultLine {

    private UUID id;
    private ZonedDateTime sharedTime;
    private UUID caseId;
    private String urn;
    private UUID offenceId;
    private Plea plea;

    private String offenceTitle;
    private String level;
    private String resultLabel;
    private LocalDate startDate;
    private LocalDate endDate;
    private String court;
    private String courtRoom;
    private UUID clerkOfTheCourtId;
    private String clerkOfTheCourtFirstName;
    private String clerkOfTheCourtLastName;
    private List<Prompt> prompts;


    public UUID getId() {
        return id;
    }

    public ZonedDateTime getSharedTime() {
        return sharedTime;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public String getUrn() {
        return urn;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public Plea getPlea() {
        return plea;
    }

    public String getOffenceTitle() {
        return offenceTitle;
    }

    public String getLevel() {
        return level;
    }

    public String getResultLabel() {
        return resultLabel;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getCourt() {
        return court;
    }

    public String getCourtRoom() {
        return courtRoom;
    }

    public UUID getClerkOfTheCourtId() {
        return clerkOfTheCourtId;
    }

    public String getClerkOfTheCourtFirstName() {
        return clerkOfTheCourtFirstName;
    }

    public String getClerkOfTheCourtLastName() {
        return clerkOfTheCourtLastName;
    }

    public List<Prompt> getPrompts() {
        return prompts;
    }

    public ResultLine setId(UUID id) {
        this.id = id;
        return this;
    }

    public ResultLine setSharedTime(ZonedDateTime sharedTime) {
        this.sharedTime = sharedTime;
        return this;
    }

    public ResultLine setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public ResultLine setUrn(String urn) {
        this.urn = urn;
        return this;
    }

    public ResultLine setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public ResultLine setPlea(Plea plea) {
        this.plea = plea;
        return this;
    }

    public ResultLine setOffenceTitle(String offenceTitle) {
        this.offenceTitle = offenceTitle;
        return this;
    }

    public ResultLine setLevel(String level) {
        this.level = level;
        return this;
    }

    public ResultLine setResultLabel(String resultLabel) {
        this.resultLabel = resultLabel;
        return this;
    }

    public ResultLine setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public ResultLine setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public ResultLine setCourt(String court) {
        this.court = court;
        return this;
    }

    public ResultLine setCourtRoom(String courtRoom) {
        this.courtRoom = courtRoom;
        return this;
    }

    public ResultLine setClerkOfTheCourtId(UUID clerkOfTheCourtId) {
        this.clerkOfTheCourtId = clerkOfTheCourtId;
        return this;
    }

    public ResultLine setClerkOfTheCourtFirstName(String clerkOfTheCourtFirstName) {
        this.clerkOfTheCourtFirstName = clerkOfTheCourtFirstName;
        return this;
    }

    public ResultLine setClerkOfTheCourtLastName(String clerkOfTheCourtLastName) {
        this.clerkOfTheCourtLastName = clerkOfTheCourtLastName;
        return this;
    }

    public ResultLine setPrompts(List<Prompt> prompts) {
        this.prompts = prompts;
        return this;
    }

    public static ResultLine resultLine() {
        return new ResultLine();
    }
}