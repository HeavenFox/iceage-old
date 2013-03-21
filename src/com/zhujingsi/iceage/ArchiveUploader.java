package com.zhujingsi.iceage;

import java.io.IOException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.UploadArchiveRequest;
import com.amazonaws.services.glacier.model.UploadArchiveResult;

public class ArchiveUploader implements Runnable {
	private IceAgeApp app;
	
	public ArchiveUploader() {
		app = IceAgeApp.getInstance();
	}
	
	@Override
	public void run() {
		AWSCredentials credentials;
		try {
			credentials = new PropertiesCredentials(ArchiveUploader.class.getResourceAsStream("AwsCredentials.properties"));
			AmazonGlacierClient client = new AmazonGlacierClient(credentials);
	        client.setEndpoint("https://glacier.us-east-1.amazonaws.com/");
	        
			Archive archive = app.archiveUploadQueue.take();
			
			UploadArchiveRequest request = new UploadArchiveRequest()
            .withVaultName("a")
            .withChecksum(archive.getChecksum()) 
            .withBody(archive)
            .withContentLength(archive.getLength());
			
			UploadArchiveResult uploadArchiveResult = client.uploadArchive(request);
			archive.setArchiveId(uploadArchiveResult.getArchiveId());
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
