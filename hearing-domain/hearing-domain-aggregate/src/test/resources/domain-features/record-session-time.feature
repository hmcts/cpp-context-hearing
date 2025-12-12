Feature: Record Session Time

  Scenario: Record Session Time
    Given no previous events
    When you recordSessionTime to a SessionTimeAggregate using a record session time
    Then session time recorded

   Scenario: Record Session Time
    Given session time recorded
    When you recordSessionTime to a SessionTimeAggregate using a record session time
    Then session time recorded
