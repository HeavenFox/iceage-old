package com.zhujingsi.iceage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class IceAgeApp {
	public BlockingQueue<Archive> archiveUploadQueue;
	
	private List<Archive> archives;
	
	private static IceAgeApp app;
	
	private static final int UPLOAD_QUEUE_CAPACITY = 20;
	
	private IceAgeApp() {
		archiveUploadQueue = new ArrayBlockingQueue<Archive>(UPLOAD_QUEUE_CAPACITY);
		archives = new ArrayList<Archive>();
	}
	
	public static IceAgeApp getInstance() {
		if (app == null) {
			app = new IceAgeApp();
		}
		return app;
	}
	
	/**
	 * Write a message
	 * @param m
	 */
	public void message(String m) {
		System.out.println(m);
	}
	
	
	public Iterator<Archive> getArchiveIterator() {
		return archives.iterator();
	}
	
	/**
	 * Generate a list of archives from a certain backup folder
	 * @param folder
	 */
	public void generateArchives(BackupFolder folder) {
		Iterator<File> iterator = folder.iterator();
		while (iterator.hasNext()) {
			Archive archive = new Archive();
			// TODO Archive length
			while (iterator.hasNext() && archive.getLength() < 100000) {
				File f = iterator.next();
				try {
					archive.addFile(f);
				} catch (FileNotFoundException e) {
					// Print out error message
					this.message("File not found: " + f.getPath() + ". Removed from list.");
					
					// Remove the file from the list
					iterator.remove();
				}
				this.archives.add(archive);
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
	}

}
