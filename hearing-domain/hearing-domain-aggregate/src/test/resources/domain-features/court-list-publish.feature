Feature: Publish Court list

  Scenario: Publish court list requested
    Given no previous events
    When you recordCourtListRequested to a CourtListAggregate using a record court list requested
    Then publish court list requested

  Scenario: Publish court list export was successful
    Given publish court list requested
    When you recordCourtListExportSuccessful to a CourtListAggregate with a record court list export successful
    Then publish court list export successful

  Scenario: Publish court list export failed
    Given publish court list requested
    When you recordCourtListExportFailed to a CourtListAggregate with a record court list export failed
    Then publish court list export failed
