package au.edu.adelaide.Assessment1.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import au.edu.adelaide.Assessment1.stt.TranscriptionService;
import au.edu.adelaide.Assessment1.model.TranscriptionResponse;

//ADDED: Import JsonNode to return the complete JSON response,
//including transcription text and timeStamps.
import com.fasterxml.jackson.databind.JsonNode;

@RestController
public class TranscriptionController {

	private final TranscriptionService transcriptionService;
	
	public TranscriptionController(TranscriptionService transcriptionService) {
		this.transcriptionService = transcriptionService;
	}
	
	// CHANGED: Endpoint from /api/v1/transcribe to /api/v1/subtitles.
	// This is the endpoint required by assessment 3.
	@PostMapping( value = "/api/v1/subtitles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
	public String transcribe(@RequestPart("file") MultipartFile file) throws IOException{
		// CHANGED: Return the complete JSON response instead of
        // extracting only the transcription text.
        // This allows the video player to access subtitle timeStamps.
		return transcriptionService.transcribe(file.getBytes(), file.getOriginalFilename());
	}
}
