package au.edu.adelaide.Assessment1.model;

// Instant is useful for storing a timestamp in UTC.
import java.time.Instant;

public class UptimeResponse {
	// Stores the server's start time.
	private Instant utcServerStart;
	// Stores the current time when the request is made.
	private Instant utcNow;
	// Stores the number of seconds the application has been running.
	private double serverUptimeSeconds;
	
	public UptimeResponse(Instant utcServerStart, double serverUptimeSeconds, Instant utcNow) {
		this.utcServerStart = utcServerStart;
		this.utcNow = utcNow;
		this.serverUptimeSeconds = serverUptimeSeconds;
	}
	
	public Instant getUtcServerStart() {
		return utcServerStart;
	}
	
	public double getServerUptimeSeconds() {
		return serverUptimeSeconds;
	}
	
	public Instant getUtcNow() {
		return utcNow;
	}
}
