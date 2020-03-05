# Instructions for running the performance tests

1. The maven plugin for jmeter should handle most things and it is possible to run jmeter with:
	cd cpp.context.hearing/hearing-performance-test
	mvn clean verify -Pscheduling-performance-test
	
2. If you want to see and edit the jmeter tests you can launch Jmeter gui with:
	cd cpp.context.hearing/hearing-performance-test
	mvn jmeter:gui -Pscheduling-performance-test

3. The test results will be save in a .jtl file in ./target/jmeter/results/ and can be viewed in any of the results
   listeners within Jmeter gui. 


[INFO] Results:
[INFO]
[ERROR] Errors:
[ERROR]   PublishLatestCourtCentreHearingEventsViaSystemSchedulingIT.shouldRequestToPublishHearingList:126 » ConditionTimeout
[INFO]
[ERROR] Tests run: 111, Failures: 0, Errors: 5, Skipped: 1
[INFO]
