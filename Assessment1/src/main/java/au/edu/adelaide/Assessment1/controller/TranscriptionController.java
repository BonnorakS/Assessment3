package au.edu.adelaide.Assessment1.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import au.edu.adelaide.Assessment1.stt.TranscriptionService;
import au.edu.adelaide.Assessment1.model.TranscriptionResponse;

@RestController
public class TranscriptionController {

	private final TranscriptionService transcriptionService;
	
	public TranscriptionController(TranscriptionService transcriptionService) {
		this.transcriptionService = transcriptionService;
	}
	
	@PostMapping( value = "api/v1/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public TranscriptionResponse transcribe(@RequestPart("file") MultipartFile file) throws IOException{
		String text = transcriptionService.transcribe(file.getBytes(), file.getOriginalFilename());
		return new TranscriptionResponse(text);
	}
}
