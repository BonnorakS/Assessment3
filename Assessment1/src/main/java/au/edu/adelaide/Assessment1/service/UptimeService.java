package au.edu.adelaide.Assessment1.service;

import java.time.Instant;
import org.springframework.stereotype.Service;
import au.edu.adelaide.Assessment1.model.UptimeResponse;

@Service
public class UptimeService {

	private final Instant serverStartTime;
	
	public UptimeService() {
		serverStartTime = Instant.now();
	}
	
	public UptimeResponse getUptime() {
		
		Instant utcNow = Instant.now();
		double serverUptimeSeconds = (utcNow.toEpochMilli() - serverStartTime.toEpochMilli()) / 1000.0;
		
		return new UptimeResponse(serverStartTime, serverUptimeSeconds, utcNow);
	}
}
