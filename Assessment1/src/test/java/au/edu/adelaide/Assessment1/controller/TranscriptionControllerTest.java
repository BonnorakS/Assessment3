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
	void setUp() throws Exception {
		// CHANGED: The test service now returns JsonNode
        // instead of String, matching the updated TranscriptionService.
		TranscriptionService stubService = new TranscriptionService(new StatsService()) {
			
			@Override
			public String transcribe(byte[] audioData, String filename) {
				try {
					// ADDED: Mock JSON response containing subtitle timeStamps.
					// This avoids calling the real OpenAI API during testing.
					return ("""
                        {
                            "text": "This is a stub transcription for testing.",
                            "segments": [
                                {
                                    "start": 0.0,
                                    "end": 3.0,
                                    "text": "This is a stub transcription for testing.",
                                    "no_speech_prob": 0.0,
                                    "avg_logprob": -0.2
                                }
                            ],
                            "words": []
                        }
                        """);
				}catch (Exception e) {
                    throw new RuntimeException(e);
                }
			}
		};
		
		TranscriptionController controller = new TranscriptionController(stubService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}
	  // CHANGED: Test the new /api/v1/subtitles endPoint.
	@Test
	void transcribeEndpointReturnsStubTranscription() throws Exception{
		// Use a stub instead of the external transcription API.
		// This makes the regression test deterministic and repeatable.
		MockMultipartFile audioFile = new MockMultipartFile("file", "test.webm", "audio/webm", "fake audio data".getBytes());
		mockMvc.perform(multipart("/api/v1/subtitles").file(audioFile)).andExpect(status().isOk()).andExpect(jsonPath("$.text")
				.value("This is a stub transcription for testing."))
				// ADDED: Check that the segment timeStamp is returned.
				.andExpect(jsonPath("$.segments[0].start").value(0.0));
	}

}
