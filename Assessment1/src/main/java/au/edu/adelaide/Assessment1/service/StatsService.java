package au.edu.adelaide.Assessment1.service;

import org.springframework.stereotype.Service;
import au.edu.adelaide.Assessment1.model.StatsResponse;

//ADDED: Allows us to return usage information as JSON key-value pairs.
import java.util.Map;

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
	 
	// ADDED: Returns audio usage and transcription processing time in seconds.
	// The controller will use this method for GET /api/v1/global/usage.
	public synchronized Map<String, Double> getUsage(){
		
		return Map.of("totalAudioSeconds", totalAudioSeconds, "totalTranscriptionTimeSeconds", totalTranscriptionTimeSeconds);
	}
}
