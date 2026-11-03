package au.edu.adelaide.Assessment1.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import au.edu.adelaide.Assessment1.model.StatsResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class StatsServiceRaceConditionTest {
	
	private static final Logger logger = LoggerFactory.getLogger(StatsServiceRaceConditionTest.class);

	@Test
	void concurrentTokenUpdatesAreNotLost() throws Exception {
		
		
		StatsService statsService = new StatsService();
		
		int threadCount = 100;
		int updatesPerThread = 100;
		
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		
		CountDownLatch ready = new CountDownLatch(threadCount);
		CountDownLatch start = new CountDownLatch(1);
		
		for(int i = 0; i < threadCount; i++) {
			executor.submit(() -> {
				try {
					ready.countDown();
					
					// wait until all thread are ready.
					start.await();
					
					for(int j = 0; j < updatesPerThread; j++) {
						statsService.addTokenUsage(10, 5);
					}
				}catch(InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			});
		}
		
		// Make sure all threads are ready before starting.
		assertTrue(ready.await(10, TimeUnit.SECONDS), "Not all worker threads became ready.");
		// Start all workers approximately simultaneously.
		start.countDown();
		executor.shutdown();
		assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS),"Executor did not terminate.");
		
		long expectedInputTokens = (long) threadCount * updatesPerThread * 10;
		long expectedOutputTokens = (long) threadCount * updatesPerThread * 5;
		
		StatsResponse stats = statsService.getStats();
		
		logger.info("Race-condition regression test");
		logger.info("Threads: {}", threadCount);
		logger.info("Updates per thread: {}", updatesPerThread);
		logger.info("Expected input tokens: {}", expectedInputTokens);
		logger.info("Actual input tokens: {}", stats.getInputTokens());
		logger.info("Expected output tokens: {}", expectedOutputTokens);
		logger.info("Actual output tokens: {}", stats.getOutputTokens());

	     assertEquals(expectedInputTokens,stats.getInputTokens(),"Input token updates were lost due to a race condition.");
	     assertEquals(expectedOutputTokens,stats.getOutputTokens(),"Output token updates were lost due to a race condition.");
	}
}
