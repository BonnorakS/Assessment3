package au.edu.adelaide.Assessment1.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import au.edu.adelaide.Assessment1.service.StatsService;
import au.edu.adelaide.Assessment1.stt.TranscriptionService;

class TranscriptionControllerTest {
	
	private MockMvc mockMvc;
	
	@BeforeEach
	void setUp() {
		
		TranscriptionService stubService = new TranscriptionService(new StatsService()) {
			
			@Override
			public String transcribe(byte[] audioData, String filename) {
				return "This is a stub transcription for testing.";
			}
		};
		
		TranscriptionController controller = new TranscriptionController(stubService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}
	
	@Test
	void transcribeEndpointReturnsStubTranscription() throws Exception{
		// Use a stub instead of the external transcription API.
		// This makes the regression test deterministic and repeatable.
		MockMultipartFile audioFile = new MockMultipartFile("file", "test.webm", "audio/webm", "fake audio data".getBytes());
		mockMvc.perform(multipart("/api/v1/transcribe").file(audioFile)).andExpect(status().isOk()).andExpect(jsonPath("$.text")
				.value("This is a stub transcription for testing."));
	}

}
