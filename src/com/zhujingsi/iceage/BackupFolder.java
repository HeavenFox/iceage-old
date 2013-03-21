package com.zhujingsi.iceage;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class BackupFolder implements Iterable<File> {
	
	private List<File> files;

	@Override
	public Iterator<File> iterator() {
		// TODO Auto-generated method stub
		return files.iterator();
	}
	

}
