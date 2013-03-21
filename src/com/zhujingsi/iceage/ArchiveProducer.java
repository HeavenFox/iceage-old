package com.zhujingsi.iceage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Iterator;

public class ArchiveProducer implements Runnable {
	private IceAgeApp app;
	
	public ArchiveProducer() {
		app = IceAgeApp.getInstance();
	}

	@Override
	public void run() {
		Archive archive;
		Iterator<Archive> iterator = app.getArchiveIterator();
		while (iterator.hasNext()) {
			archive = iterator.next();
			try {
				archive.calculateChecksum();
				app.archiveUploadQueue.add(archive);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

}
