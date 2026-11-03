**COMP3011 Assignment 1 – Speech-to-Text Application**

**1\. Project Overview**

This project implements a Speech-to-Text web application for COMP3011 Assignment 1.

The application allows a user to:

- Record audio using the browser microphone.
- Stop the recording.
- Send the recorded audio to the transcription REST API.
- Display the returned transcription.
- Monitor the application state through clear status messages.

The backend also provides administrative and statistics REST APIs, including:

- Server uptime.
- Global token usage statistics.
- Graceful server shutdown.
- Speech transcription.

The application has been developed with a focus on correctness, reliability, accessibility, concurrency and regression testing.

**2\. Testing Strategy**

The project uses automated regression testing to verify that important system requirements continue to work after changes are made to the application.

The test suite covers both:

**Functional requirements**

Functional tests verify that the application and REST APIs perform their required operations correctly.

These include:

- Returning server uptime information.
- Returning global token statistics.
- Updating and returning token statistics.
- Gracefully requesting server shutdown.
- Accepting an uploaded audio file and returning a transcription.

**Non-functional requirements**

Non-functional tests verify system qualities such as:

- Concurrency.
- Performance under simultaneous requests.
- Thread safety.
- Reliability of shared statistics.
- Deterministic and repeatable testing.

The tests are designed as regression tests so that failures in previously implemented functionality can be detected when the system is modified.

**3\. Running the Tests**

The complete test suite can be executed using Maven:

mvn test

The tests can also be executed individually through the IDE using JUnit.

A successful test run should report that all tests have passed.

**4\. Regression Test Suite**

**4.1 AdminControllerTest**

AdminControllerTest verifies the main administrative REST endpoints using Spring MockMvc.

The controller is tested independently from the real web server so that individual endpoint behaviour can be checked quickly and deterministically.

**Test 1 – uptimeEndpointReturnsCorrectResponse**

**Purpose:**

Verifies that the /api/v1/admin/uptime endpoint returns a successful HTTP response and contains the required uptime fields.

**Expected result:**

- HTTP status is 200 OK.
- utcServerStart exists.
- serverUptimeSeconds exists.
- utcNow exists.

**Assurance provided:**

This regression test ensures that changes to the administration controller do not accidentally remove or change the required uptime response fields.

**Test 2 – globalStatsEndpointReturnsZeroInitially**

**Purpose:**

Verifies that a newly created statistics service starts with zero token usage.

**Expected result:**

- HTTP status is 200 OK.
- inputTokens is 0.
- outputTokens is 0.

**Assurance provided:**

This ensures that the initial state of the global statistics API remains correct and that statistics are not unintentionally initialised with incorrect values.

**Test 3 – uptimeValueIsPositive**

**Purpose:**

Provides an additional regression check that the uptime value returned by the server is numeric and valid.

**Expected result:**

- HTTP status is 200 OK.
- serverUptimeSeconds is a number.
- The value is greater than or equal to zero.

**Assurance provided:**

This protects against future changes that could cause invalid, negative or incorrectly formatted uptime values.

**Test 4 – globalStatsCanBeUpdatedAndReturned**

**Purpose:**

Verifies that token usage can be added to the statistics service and subsequently returned through the REST API.

The test adds:

- 100 input tokens.
- 50 output tokens.

**Expected result:**

- HTTP status is 200 OK.
- inputTokens equals 100.
- outputTokens equals 50.

**Assurance provided:**

This verifies that statistics are not only initialised correctly but can also be updated and retrieved correctly.

**Test 5 – shutdownEndpointReturnsAccepted**

**Purpose:**

Verifies that the graceful shutdown REST endpoint communicates correctly with the shutdown service.

A stub shutdown service is used so that running the test does not actually shut down the test JVM.

**Expected result:**

- HTTP status is 202 Accepted.
- The response message is:

Graceful shutdown requested.

**Assurance provided:**

This confirms that the REST controller correctly handles a shutdown request without requiring the real application server to terminate during testing.

**5\. TranscriptionControllerTest**

**Test – transcribeEndpointReturnsStubTranscription**

**Purpose:**

Verifies that the /api/v1/transcribe endpoint accepts an uploaded WebM audio file and returns the transcription correctly.

The test uses a stub TranscriptionService rather than the external transcription API.

**Why a stub is used:**

Using an external transcription service during automated regression testing could make the test:

- Dependent on network availability.
- Slower.
- Less predictable.
- Dependent on external service availability.
- Difficult to reproduce consistently.

The stub provides a deterministic response:

This is a stub transcription for testing.

**Expected result:**

- The multipart audio upload is accepted.
- HTTP status is 200 OK.
- The returned text field contains the expected transcription.

**Assurance provided:**

This confirms that the REST controller correctly receives an audio file, invokes the transcription service and returns the transcription response.

The test uses the expected WebM media type:

audio/webm

This provides regression coverage for the client/server transcription integration.

**6\. ConcurrencyTest**

**Test – handlesMoreThan200SimnultaneousBlockingRequests**

This is a **non-functional regression test** for the application's concurrency requirement.

**Purpose**

The test starts the application on a random HTTP port and sends **220 simultaneous HTTP requests** to:

/api/v1/admin/uptime

A CountDownLatch is used to ensure that the requests are released at approximately the same time.

The test uses a real HTTP server instead of MockMvc because the purpose is to test the behaviour of the actual embedded server under concurrent HTTP load.

**Expected result**

All 220 requests should:

- Complete successfully.
- Return HTTP status 200.
- Complete within the configured time limit.

The test also checks that:

successfulRequests == requestCount

and that the elapsed time is less than 30 seconds.

**Assurance provided**

This test provides regression coverage for the application's ability to handle more than 200 simultaneous requests.

It helps detect future changes that could introduce:

- Threading problems.
- Blocking behaviour.
- Server configuration problems.
- Performance regressions.
- Failed concurrent requests.

The test also records useful information through the application's logging framework, including the number of requests, successful requests and elapsed time.

**7\. StatsServiceRaceConditionTest**

**Test – concurrentTokenUpdatesAreNotLost**

This is a **concurrency and thread-safety regression test** for the statistics service.

**Purpose**

The test creates:

- 100 worker threads.
- 100 updates per thread.

Each update adds:

10 input tokens

5 output tokens

Therefore, the expected totals are:

Input tokens:

100 × 100 × 10 = 100000

Output tokens:

100 × 100 × 5 = 50000

A CountDownLatch is used to make the worker threads start at approximately the same time.

**Expected result**

After all worker threads complete:

Actual input tokens = 100000

Actual output tokens = 50000

The test uses assertions to compare the expected and actual values.

**Assurance provided**

This test is designed to detect race conditions where simultaneous updates to shared statistics could be lost.

It provides regression protection against future modifications that could make token statistics unsafe when accessed concurrently.

The test also logs:

- Number of threads.
- Updates per thread.
- Expected input tokens.
- Actual input tokens.
- Expected output tokens.
- Actual output tokens.

This logging makes failures easier to diagnose.

**8\. Test Coverage Summary**

The current regression suite covers the following areas:

| **Test Class**                | **Main Area**                  | **Requirement Type** |
| ----------------------------- | ------------------------------ | -------------------- |
| AdminControllerTest           | Uptime API                     | Functional           |
| AdminControllerTest           | Initial global statistics      | Functional           |
| AdminControllerTest           | Valid uptime value             | Functional           |
| AdminControllerTest           | Statistics update              | Functional           |
| AdminControllerTest           | Graceful shutdown              | Functional           |
| TranscriptionControllerTest   | Audio transcription API        | Functional           |
| ConcurrencyTest               | 220 simultaneous HTTP requests | Non-functional       |
| StatsServiceRaceConditionTest | Concurrent statistics updates  | Non-functional       |

The suite therefore provides regression coverage across the application's major REST functionality as well as important non-functional requirements.

**9\. Test Design and Expected Results**

The tests were developed based on the application's functional and non-functional requirements.

Each regression test has a specific purpose:

1. **Uptime tests** ensure that server uptime information remains available and valid.
2. **Statistics tests** ensure that token usage starts correctly and can be updated and retrieved.
3. **Shutdown testing** ensures that the graceful shutdown REST contract remains correct.
4. **Transcription testing** ensures that uploaded audio is processed through the controller correctly.
5. **Concurrency testing** ensures that the server can handle more than 200 simultaneous requests.
6. **Race-condition testing** ensures that concurrent token updates are not lost.

The expected results are explicitly checked using JUnit assertions and Spring MockMvc response assertions.

**10\. Use of Stubs**

Stubs are used where calling the real service would make regression tests unreliable or potentially destructive.

For example, TranscriptionControllerTest uses a stub transcription service.

The stub returns a known response:

This is a stub transcription for testing.

This allows the controller behaviour to be tested independently from the external transcription provider.

Similarly, the shutdown controller test uses a stub shutdown service so that the test verifies the REST behaviour without shutting down the test JVM.

This makes the regression tests repeatable, deterministic and safe to run.

**11\. Logging**

SLF4J logging is used in the regression tests where diagnostic information is useful.

Logging is particularly important for the non-functional tests.

**ConcurrencyTest logs**

The test records:

Concurrency regression test

Requests: ...

Successful: ...

Time: ... seconds

**StatsServiceRaceConditionTest logs**

The test records:

Race-condition regression test

Threads: ...

Updates per thread: ...

Expected input tokens: ...

Actual input tokens: ...

Expected output tokens: ...

Actual output tokens: ...

This logging provides useful diagnostic information when a regression test fails.

For example, if expected and actual token counts differ, the log can help identify a possible race condition.

**12\. Comments and Documentation**

Comments have been included in the test code to explain the purpose of important tests and testing decisions.

Examples include comments explaining:

- Why latches are used.
- Why a real HTTP server is used for the concurrency test.
- Why a stub is used for transcription testing.
- Why a stub is used for shutdown testing.
- Why expected token values are calculated.
- What each regression test is intended to verify.

The comments are intended to explain the reasoning behind the tests rather than simply restating the code.

**13\. Regression Assurance**

The regression suite provides assurance that previously implemented functionality continues to work after future code changes.

A future developer can run:

mvn test

and quickly identify whether changes have caused existing requirements to fail.

The suite covers both:

- **Functional correctness**, through REST controller and service tests.
- **Non-functional behaviour**, through concurrency and thread-safety tests.

Together, these tests reduce the risk of introducing regressions while the application is developed further.

**14\. Conclusion**

The testing strategy combines unit-style controller tests, service-level concurrency testing and real HTTP integration testing.

The tests are designed to be:

- Automated.
- Repeatable.
- Deterministic where external services are involved.
- Safe to execute.
- Focused on specific requirements.
- Supported by logging for diagnosis.

This regression suite provides evidence that the application's key functional and non-functional requirements continue to operate correctly after code changes.