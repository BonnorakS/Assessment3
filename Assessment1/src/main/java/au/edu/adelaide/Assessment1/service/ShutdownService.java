package au.edu.adelaide.Assessment1.service;

import org.springframework.context.ApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;

@Service
public class ShutdownService {

	private final ApplicationContext applicationContext;
	private boolean shutdownInProgress = false;
	
	public ShutdownService(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	public synchronized boolean requestShutdown() {
		if(shutdownInProgress) {
			return false;
		}
		
		shutdownInProgress = true;
		
		Thread shutdownThread = new Thread(() -> {
			try {
				Thread.sleep(100);
			}catch (InterruptedException e){
				Thread.currentThread().interrupt();
			}
			
			int exitCode = SpringApplication.exit(applicationContext);
			System.exit(exitCode);
		});
		
		shutdownThread.setDaemon(false);
		shutdownThread.start();
		
		return true;
	}
}
