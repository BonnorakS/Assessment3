package au.edu.adelaide.Assessment1.controller;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import au.edu.adelaide.Assessment1.model.UptimeResponse;
import au.edu.adelaide.Assessment1.model.StatsResponse;
import au.edu.adelaide.Assessment1.service.ShutdownService;
import au.edu.adelaide.Assessment1.service.StatsService;
import au.edu.adelaide.Assessment1.service.UptimeService;
import au.edu.adelaide.Assessment1.controller.AdminController;

 class AdminControllerTest {

	private MockMvc mockMvc;
		
	@BeforeEach
	void setUp() {
		UptimeService uptimeService = new UptimeService();
		StatsService statsService = new StatsService();
		// Stub shutdown service so the controller has a valid dependency.
		ShutdownService shutdownService = new ShutdownService(null) {
			@Override
			public synchronized boolean requestShutdown() {
				return true;
			}
		};
		AdminController controller = new AdminController(uptimeService, statsService, shutdownService);
	    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}
	
	@Test 
	void uptimeEndpointReturnsCorrectResponse() throws Exception{
		// Verifies that the uptime API returns the required response fields.
		mockMvc.perform(get("/api/v1/admin/uptime")).andExpect(status().isOk()).andExpect(jsonPath("$.utcServerStart").exists())
		.andExpect(jsonPath("$.serverUptimeSeconds").exists()).andExpect(jsonPath("$.utcNow").exists());
	}
	
	@Test
	void globalStatsEndpointReturnsZeroInitially() throws Exception{
		// A new statsService should start with zero token usage.
		mockMvc.perform(get("/api/v1/global/stats")).andExpect(status().isOk()).andExpect(jsonPath("$.inputTokens").value(0))
		.andExpect(jsonPath("$.outputTokens").value(0));
	}
	
	@Test
	void uptimeValueIsPositive() throws Exception {
	    // Regression test: confirms the server reports a valid positive uptime.
	    mockMvc.perform(get("/api/v1/admin/uptime")).andExpect(status().isOk()).andExpect(jsonPath("$.serverUptimeSeconds").isNumber())
	            .andExpect(jsonPath("$.serverUptimeSeconds").value(org.hamcrest.Matchers.greaterThanOrEqualTo(0.0)));
	}
	
	@Test
	void globalStatsCanBeUpdatedAndReturned() throws Exception {
	    // Regression test: confirms token statistics are stored and returned correctly.
	    StatsService statsService = new StatsService();
	    statsService.addTokenUsage(100, 50);

	    AdminController controller = new AdminController(new UptimeService(),statsService,null);
	    MockMvc statsMockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	    statsMockMvc.perform(get("/api/v1/global/stats")).andExpect(status().isOk()).andExpect(jsonPath("$.inputTokens").value(100))
	            .andExpect(jsonPath("$.outputTokens").value(50));
	}
	
	@Test 
	void shutdownEndpointReturnsAccepted() throws Exception{
		ShutdownService shutdownService = new ShutdownService(null) {
		// Use a stub shutdown service so the test does not actually shut down the test JVM. It verifies the REST contact only.
			@Override
			public synchronized boolean requestShutdown() {
				return true;
			}
		};
		
		AdminController controller = new AdminController(new UptimeService(), new StatsService(), shutdownService);
		MockMvc shutdownMockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		shutdownMockMvc.perform(post("/api/v1/admin/shutdown")).andExpect(status().isAccepted()).andExpect(jsonPath("$.message")
				.value("Graceful shutdown requested."));
		
	}
	
 }
