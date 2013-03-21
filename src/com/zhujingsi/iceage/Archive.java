package com.zhujingsi.iceage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.w3c.dom.Element;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class Archive extends InputStream {
	private class FileInArchive extends InputStream {
		private long pointer = 0;
		
		@Expose
		private String path;
		
		@Expose
		public long offset;
		
		@Expose
		public long length;
		
		@Expose
		public ArrayList<PieceChecksum> checksums;
		
		private File file;
		public FileInputStream stream;
		
		private class PieceChecksum {
			@Expose
			public long offset;
			
			@Expose
			public String checksum;
			
			public PieceChecksum(long offset, String checksum) {
				this.offset = offset;
				this.checksum = checksum;
			}
		}
		
		
		public FileInArchive(long offset, long length, File file) throws FileNotFoundException {
			this.offset = offset;
			this.length = length;
			this.file = file;
			this.path = file.getPath();
			this.stream = new FileInputStream(this.file);
			this.checksums = new ArrayList<PieceChecksum>();
		}
		
		public void addChecksum(long offset, String checksum) {
			this.checksums.add(new PieceChecksum(offset, checksum));
			
		}
		
		@Override
		public int read() throws IOException {
			int v = this.stream.read();
			if (v != -1) {
				pointer++;
			}
			return v;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int amount = this.stream.read(b, off, len);
			if (amount > 0) {
				this.pointer += amount;
			}
			return amount;
		}

		@Override
		public int read(byte[] b) throws IOException {
			int amount =  this.stream.read(b);
			if (amount > 0) {
				this.pointer += amount;
			}
			return amount;
		}

		@Override
		public synchronized void reset() throws IOException {
			this.pointer = 0;
			this.stream.reset();
		}
		
		
	}
	
	private long length;
	
	private int readPointer = 0;
	
	@Expose
	private String archiveId;
	
	@Expose
	private String treeHash;
	
	@Expose
	private ArrayList<FileInArchive> listOfFiles;
	
	public Archive() {
		this.listOfFiles = new ArrayList<FileInArchive>();
	}
	
	public void addFile(File f) throws FileNotFoundException {
		long fileLength = f.length();
		listOfFiles.add(new FileInArchive(this.length, fileLength, f));
		this.length += fileLength;
	}
	
	public long getLength() {
		return this.length;
	}
	
	public String calculateChecksum() throws IOException, GeneralSecurityException {
		final int ONE_MB = 1024 * 1024; 
		
		byte[] data = new byte[ONE_MB];
		
		int numChunks = (int)(this.length >> 20);
		if ((this.length & ((1 << 20)-1)) > 0) {
			numChunks++;
		}
		
		MessageDigest hasher = MessageDigest.getInstance("SHA-256");
		
		byte[][] chunks = new byte[numChunks][];
		
		for (int i=0;i<numChunks;i++)
		{
			int startingReadPointer = this.readPointer;
			long startingOffset = this.listOfFiles.get(startingReadPointer).pointer;
			int amount_read = 0;
			int amount_thistime;
			while (amount_read != ONE_MB && (amount_thistime = this.read(data, amount_read, ONE_MB - amount_read)) != -1) {
				amount_read += amount_thistime;	
			}
			if (amount_read == 0) {
				break;
			}
			hasher.update(data, 0, amount_read);
			
			byte[] digest = hasher.digest();
			
			this.listOfFiles.get(startingReadPointer).addChecksum(startingOffset, bytesToHex(digest));
			chunks[i] = digest;
		}
		
		// Code below come from the documentation
		byte[][] prevLvlHashes = chunks;

        while (prevLvlHashes.length > 1) {

            int len = prevLvlHashes.length / 2;
            if (prevLvlHashes.length % 2 != 0) {
                len++;
            }

            byte[][] currLvlHashes = new byte[len][];

            int j = 0;
            for (int i = 0; i < prevLvlHashes.length; i = i + 2, j++) {

                // If there are at least two elements remaining
                if (prevLvlHashes.length - i > 1) {

                    // Calculate a digest of the concatenated nodes
                    hasher.reset();
                    hasher.update(prevLvlHashes[i]);
                    hasher.update(prevLvlHashes[i + 1]);
                    currLvlHashes[j] = hasher.digest();

                } else { // Take care of remaining odd chunk
                    currLvlHashes[j] = prevLvlHashes[i];
                }
            }

            prevLvlHashes = currLvlHashes;
        }
        
        treeHash = bytesToHex(prevLvlHashes[0]);

        return treeHash;
	}
	
	/**
	 * Get the calculated checksum. Returns null if it is not calculated
	 * 
	 * @return Checksum calculated
	 */
	public String getChecksum() {
		return treeHash;
	}
	
	private static String bytesToHex(byte[] bytes) {
	    final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for (int j = 0; j < bytes.length; j++) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}

	@Override
	public int read() throws IOException {
		while (readPointer < listOfFiles.size()) {
			int val = listOfFiles.get(readPointer).read();
			if (val != -1) {
				return val;
			}
			readPointer++;
		}
		return -1;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		while (readPointer < listOfFiles.size()) {
			int val = listOfFiles.get(readPointer).read(b, off, len);
			if (val != -1) {
				return val;
			}
			readPointer++;
		}
		return -1;
	}

	@Override
	public int read(byte[] b) throws IOException {
		while (readPointer < listOfFiles.size()) {
			int val = listOfFiles.get(readPointer).read(b);
			if (val != -1) {
				return val;
			}
			readPointer++;
		}
		return -1;
	}

	@Override
	public synchronized void reset() throws IOException {
		this.readPointer = 0;
		for (FileInArchive a : listOfFiles) {
			a.reset();
		}
	}

	public String getArchiveId() {
		return archiveId;
	}

	public void setArchiveId(String archiveId) {
		this.archiveId = archiveId;
	}
	
	public String toJson() {
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		return gson.toJson(this);
	}
	
	
	
}
