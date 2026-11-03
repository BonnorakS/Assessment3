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
	
	public String transcribe(byte[] audioData, String filename) throws IOException{
		
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
		body.add("model", "gpt-4o-mini-transcribe");
		
		String response = restClient.post().uri(TRANSCRIPTION_URL).headers(h -> h.addAll(headers)).body(body).retrieve().body(String.class);
		
		JsonNode json = objectMapper.readTree(response);
		System.out.println("OPENAI RESPONSE:");
		System.out.println(response);
		// Read token usage if it is provide by the API response
		JsonNode usage = json.get("usage");
		if(usage != null) {
			long input = usage.path("input_tokens").asLong(0);
			long output = usage.path("output_tokens").asLong(0);
			
			statsService.addTokenUsage(input, output);
		}
		return json.get("text").asText();
		
	}
}
