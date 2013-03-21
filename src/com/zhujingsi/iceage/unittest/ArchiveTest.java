package com.zhujingsi.iceage.unittest;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.glacier.TreeHashGenerator;
import com.zhujingsi.iceage.Archive;

public class ArchiveTest {
	Archive archive;

	@Before
	public void setUp() throws Exception {
		archive = new Archive();
		archive.addFile(new File("test_files/test1"));
		archive.addFile(new File("test_files/test2"));
		archive.addFile(new File("test_files/test3"));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testChecksum() {
		try {
			String correct = TreeHashGenerator.calculateTreeHash(new File("test_files/combined"));
			String checksum = archive.calculateChecksum();
			assertEquals(correct, checksum);
			System.out.println(archive.toJson());
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}

}
