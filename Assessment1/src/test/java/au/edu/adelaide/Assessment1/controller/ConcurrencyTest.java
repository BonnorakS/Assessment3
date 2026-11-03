package au.edu.adelaide.Assessment1.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConcurrencyTest {
	
	private static final Logger logger = LoggerFactory.getLogger(ConcurrencyTest.class);
	@LocalServerPort
	private int port;
	/**
	 * Non-functional regression test for the concurrency requirement.
	 * The test uses a real HTTP server because MockMvc would not test the embedded server's real concurrency behaviour. 
	 */
	@Test
	void handlesMoreThan200SimnultaneousBlockingRequests() throws Exception {
		
		int requestCount = 220;
		
		ExecutorService executor = Executors.newFixedThreadPool(requestCount);
		CountDownLatch ready = new CountDownLatch(requestCount);
		CountDownLatch start = new CountDownLatch(1);
		HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
		List<Future<Integer>> results = new ArrayList<>();
		for(int i = 0; i < requestCount; i++) {
			results.add(executor.submit(() -> {
				ready.countDown();
				// wait until all workers are ready.
				start.await();
				HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:" + port + "/api/v1/admin/uptime"))
						.timeout(Duration.ofSeconds(15)).GET().build();
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				return response.statusCode();
			}));
		}
		// wait until all 220 tasks are ready.
		assertTrue(ready.await(10, TimeUnit.SECONDS),"Not all requests became ready.");
		long startTime = System.nanoTime();
		// Release all requests at approximately the same time.
		start.countDown();
		int successfulRequests = 0;
		for(Future<Integer> result: results) {
			assertEquals(200, result.get());
			successfulRequests++;
		}
		
		long endTime = System.nanoTime();
		executor.shutdown();
		assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS), "Executor did not terminate.");
		// nanoTime() is measured in nanoseconds, so divide by 1 billion to convert the result to seconds.
		double elapsedSeconds = (endTime - startTime) / 1000000000.0;
		
		logger.info("Concurrency regression test");
		logger.info("Requests: {}", requestCount);
		logger.info("Successful: {}", successfulRequests);
		logger.info("Time: {} seconds", elapsedSeconds);
		
		assertEquals(requestCount, successfulRequests);
		assertTrue(elapsedSeconds < 30, "220 simultaneous requests took too long: " + elapsedSeconds + " seconds");
	}
}
