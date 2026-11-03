package au.edu.adelaide.Assessment1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import au.edu.adelaide.Assessment1.service.UptimeService;
import au.edu.adelaide.Assessment1.model.UptimeResponse;
import au.edu.adelaide.Assessment1.service.StatsService;
import au.edu.adelaide.Assessment1.model.StatsResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import au.edu.adelaide.Assessment1.model.ShutdownResponse;
import au.edu.adelaide.Assessment1.service.ShutdownService;

@RestController
public class AdminController {

	private final UptimeService uptimeService;
	private final StatsService statsService;
	private final ShutdownService shutdownService;
	
	public AdminController(UptimeService uptimeService, StatsService statsService, ShutdownService shutdownService) {
		this.uptimeService = uptimeService;
		this.statsService = statsService;
		this.shutdownService = shutdownService;
	}
	
	@GetMapping("/api/v1/admin/uptime")
	public UptimeResponse getUptime() {
		return uptimeService.getUptime();
	}
	
	@GetMapping("/api/v1/global/stats")
	public StatsResponse getStats() {
	    return statsService.getStats();
	}
	
	@PostMapping("/api/v1/admin/shutdown")
	public ResponseEntity<ShutdownResponse> shutdown(){
		
		boolean accepted = shutdownService.requestShutdown();
		
		if(!accepted) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ShutdownResponse("Graceful shutdown requested."));
	}
}
