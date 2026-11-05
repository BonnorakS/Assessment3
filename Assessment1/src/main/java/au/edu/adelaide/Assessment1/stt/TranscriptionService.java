package au.edu.adelaide.Assessment1.stt;

import java.io.IOException;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import au.edu.adelaide.Assessment1.service.StatsService;

@Service
public class TranscriptionService {
	
	private static final String TRANSCRIPTION_URL = "https://api.openai.com/v1/audio/transcriptions";
	
	private final String apiKey;
	private final RestClient restClient = RestClient.builder().build();
	private final ObjectMapper objectMapper = new ObjectMapper();
	 private final StatsService statsService;
	
	public TranscriptionService(StatsService statsService) {
		this.statsService = statsService;
		this.apiKey = System.getenv("OPENAI_API_KEY");
	}
	// CHANGED: Return JsonNode to provide the complete Whisper response.
	public String transcribe(byte[] audioData, String filename) throws IOException{
		
		// ADDED: Check that the API key is configured.
		if (apiKey == null || apiKey.isBlank()) {
			throw new IllegalStateException("OPENAI_API_KEY is not configured.");
		}
		
		// ADDED: Record when the request starts.
		statsService.requestStarted();
		
		// ADDED: Start measuring transcription processing time.
		long startTime = System.nanoTime();
		
		try {
		HttpHeaders headers = new HttpHeaders();
		
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.setBearerAuth(apiKey);
		
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		
		ByteArrayResource audioResource = new ByteArrayResource(audioData) {
			@Override
			public String getFilename() {
				return filename;
			}
		};
		body.add("file", audioResource);
		body.add("model", "whisper-1");
		// ADDED: Request detailed JSON transcription.
		body.add("response_format", "verbose_json");
		// ADDED: Request word-level timestamps.
		body.add("timestamp_granularities[]", "word");
		// ADDED: Request segment-level timestamps.
		body.add("timestamp_granularities[]", "segment");
		// Send audio to the OpenAI transcription API.
		String response = restClient.post().uri(TRANSCRIPTION_URL).headers(h -> h.addAll(headers)).body(body).retrieve().body(String.class);
		// Convert the API response into JSON.
		JsonNode json = objectMapper.readTree(response);
		
		// ADDED: Remove unreliable segments from the transcription.
		JsonNode segments = json.path("segments");
		
		if (segments.isArray()) {
			com.fasterxml.jackson.databind.node.ArrayNode filteredSegments = objectMapper.createArrayNode();
			
			for (JsonNode segment : segments) {
				// Read Whipser's confidence values.
				double noSpeechProb = segment.path("no_speech_prob").asDouble(0.0);
				double avgLogprob = segment.path("avg_logprob").asDouble(0.0);
				
				// Remove a segment only when BOTH conditions are true.
				if (noSpeechProb > 0.6 && avgLogprob < -1.0) {
					continue;
				}
				filteredSegments.add(segment);
			}
			 // Replace the original segments with the filtered segments.
			((com.fasterxml.jackson.databind.node.ObjectNode) json).set("segments", filteredSegments);
			
			// ADDED: Keep only words that belong to the remaining segments.
			JsonNode words = json.path("words");
			
			if(words.isArray()) {
				com.fasterxml.jackson.databind.node.ArrayNode filteredWords = objectMapper.createArrayNode();
				
				for (JsonNode word : words) {
					double wordStart = word.path("start").asDouble(0.0);
					double wordEnd = word.path("end").asDouble(0.0);
					
					// check whether this word overlaps a retained segment.
					boolean belongsToSegment = false;
					
					for (JsonNode segment : filteredSegments) {
						double segmentStart = segment.path("start").asDouble(0.0);
						double segmentEnd = segment.path("end").asDouble(0.0);
						
						if(wordStart < segmentEnd && wordEnd > segmentStart) {
							belongsToSegment = true;
							break;
						}
					}
					// Keep the word only if its segment was retained.
					if(belongsToSegment) {
						filteredWords.add(word);
					}
				}
				// Replace the original words with the filtered words.
				((com.fasterxml.jackson.databind.node.ObjectNode) json).set("words", filteredWords);
		}
		
	}
		
		// ADDED: Read the original audio duration in seconds.
		double audioSeconds = json.path("duration").asDouble(0.0);
		
		// ADDED: calculate how long the transcription request took.
		double elapsedSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0;
		
		// ADDED: record successful request and usage information.
		statsService.requestSucceeded(audioSeconds, elapsedSeconds);
		
		// Return the complete JSON, including timestamps.
		return objectMapper.writeValueAsString(json);
		
		}catch (IOException | RuntimeException e) {
			// ADDED: Record failed transcription requests.
			statsService.requestFailed();
			throw e;
		}
	}
}
