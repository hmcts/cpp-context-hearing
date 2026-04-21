## ADDED Requirements

### Requirement: Filter prosecution cases from application hearing details when application has offences
When hearing details are requested for an application hearing that has offences defined at the court application case level, the system SHALL remove all prosecution cases from the response, leaving `prosecutionCases` as null.

#### Scenario: Application hearing with court application offences returns null prosecution cases
- **WHEN** a hearing details response is for an application hearing whose court application cases contain at least one offence
- **THEN** `prosecutionCases` in the returned hearing SHALL be null

#### Scenario: Application hearing with no court application offences preserves prosecution cases
- **WHEN** a hearing details response is for an application hearing whose court application cases contain no offences
- **THEN** `prosecutionCases` in the returned hearing SHALL be unchanged (not filtered)

#### Scenario: Non-application hearing is not affected
- **WHEN** a hearing details response is for a hearing that is not an application hearing
- **THEN** the payload SHALL be returned unchanged with `prosecutionCases` intact

#### Scenario: Application hearing with null court applications is treated as having no offences
- **WHEN** a hearing details response is for an application hearing and `courtApplications` is null
- **THEN** the payload SHALL be returned unchanged (treated as no offences present)
