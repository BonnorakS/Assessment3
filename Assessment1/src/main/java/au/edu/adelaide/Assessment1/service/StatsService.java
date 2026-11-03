package au.edu.adelaide.Assessment1.service;

import org.springframework.stereotype.Service;
import au.edu.adelaide.Assessment1.model.StatsResponse;

@Service
public class StatsService {
	
	 private int totalRequests;
	 private int successfulRequests;
	 private int failedRequests;
	 private int activeRequests;

	 private double totalAudioSeconds;
	 private double totalTranscriptionTimeSeconds;
	 
	 private long inputTokens;
	 private long outputTokens;
	 
	 public synchronized void requestStarted() {
		 totalRequests++;
		 activeRequests++;
	 }
	 
	 public synchronized void requestSucceeded(double audioSeconds, double transcriptionTimeSeconds) {
		 
		 successfulRequests++;
		 
		 totalAudioSeconds += audioSeconds;
		 totalTranscriptionTimeSeconds += transcriptionTimeSeconds;
		 
		 activeRequests--;
	 }
	 
	 public synchronized void requestFailed() {
		  failedRequests++;
		  activeRequests--;
		}
	 
	 public synchronized void addTokenUsage(long input, long output) {
		    inputTokens += input;
		    outputTokens += output;
		}
	 
	 public synchronized StatsResponse getStats() {
		 
		 return new StatsResponse(inputTokens,outputTokens);
	 }
}
