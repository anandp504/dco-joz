package com.tumri.joz.utils;

import com.tumri.joz.index.DictionaryManager;
import com.tumri.joz.index.ProductAttributeIndex;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.utils.FSUtils;
import com.tumri.utils.data.SetDifference;
import com.tumri.utils.data.SortedArraySet;
import org.junit.Test;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: scbraun
 * Date: Sep 28, 2009
 * Time: 10:15:30 AM
 */
public class IndexLoadingComparator {
	private static Logger log = Logger.getLogger(IndexLoadingComparator.class);
	private static final String FILE_SEPERATOR = "/";

	private boolean bErrorsFound = false;

	public List<String> validate(String mupDir) {
		bErrorsFound = false;
		List<String> infos = new ArrayList<String>();
		if (mupDir == null || mupDir.isEmpty()) {
			mupDir = AppProperties.getInstance().getProperty("com.tumri.content.file.sourceDir");
		}

		File f = new File(mupDir);
		ArrayList<File> files = new ArrayList<File>();
		FSUtils.findFiles(files, f, "US.*DEFAULT_provider-content.*");

		if (files.isEmpty()) {
			infos.add("No MUP files found at dir: " + f.getAbsolutePath());
			return infos;
		}

		for (File mupFile : files) {
			List<String> info = new ArrayList<String>();
			try {
				String advName = getProviderFromFileName(mupFile.getName());
				info.addAll(compareProducts(mupFile, advName));
			} catch (Throwable t) {
				info.add(t.getMessage());
			}
			infos.addAll(info);
		}
		String result = "SUCCESS";
		if (bErrorsFound) {
			result = "FAILED";
		}
		infos.add(0, result);

		return infos;

	}

	@SuppressWarnings("unchecked")
	public boolean validateForAdvertiser(String advertiser) {
		bErrorsFound = false;
		List<String> infos = new ArrayList<String>();
		ArrayList<File> mupFiles = new ArrayList<File>();
		String mupDir = AppProperties.getInstance().getProperty("com.tumri.content.file.sourceDir");
		File indexDir = new File(mupDir + "/" + advertiser.toUpperCase() + "/data");
		FSUtils.findFiles(mupFiles, indexDir, "US.*DEFAULT_provider-content.*");

		if (mupFiles.isEmpty()) {
			infos.add("No MUP files found at dir: " + indexDir.getAbsolutePath());
			return false;
		}

		for (File mupFile : mupFiles) {
			List<String> info = new ArrayList<String>();
			try {
				info.addAll(compareProducts(mupFile, advertiser));
			} catch (Throwable t) {
				info.add(t.getMessage());
			}
			infos.addAll(info);
		}


		return !bErrorsFound;

	}

	@SuppressWarnings("unchecked")
	private List<String> compareProducts(File f, String providerName) {
		List<String> retInfos = new ArrayList<String>();
		if (providerName == null) {
			retInfos.add("Invalid Provider Name: " + providerName);
			return retInfos;
		}
		ProductAttributeIndex<Integer, Handle> index = ProductDB.getInstance().getIndex(IProduct.Attribute.kProvider);
		Integer keyId = DictionaryManager.getInstance().getId(IProduct.Attribute.kProvider, providerName);
		if (index == null) {
			retInfos.add("Invalid Index");
			return retInfos;
		}
		SortedSet<Handle> results = index.get(keyId);
		SortedSet<Long> pIdsFromIndex = new SortedArraySet<Long>();
		for (Handle h : results) {
			pIdsFromIndex.add(h.getOid());
		}

		if (!f.exists()) {
			retInfos.add("File does not exist: " + f.getAbsoluteFile());
			return retInfos;
		}

		SortedSet<Long> pIds = getPIdList(f);

		SetDifference<Long> sd1 = new SetDifference<Long>(pIdsFromIndex, pIds);
		SetDifference<Long> sd2 = new SetDifference<Long>(pIds, pIdsFromIndex);
		boolean errorFound = false;

		if (!sd1.isEmpty()) {
			errorFound = true;
			StringBuilder retString = new StringBuilder();
			retString.append(providerName + " Error: Product(s) found in Index but not in MUP: ");
			for (Long s : sd1) {
				retString.append(s);
				retString.append(", ");
			}
			retInfos.add(retString.toString());
		}

		if (!sd2.isEmpty()) {
			errorFound = true;
			StringBuilder retString = new StringBuilder();
			retString.append(providerName + " Error: Products(s) found in MUP but not in Index: ");
			for (Long s : sd2) {
				retString.append(s);
				retString.append(", ");
			}
			retInfos.add(retString.toString());
		}
		if (!errorFound) {
			retInfos.add("Index and MUP match for " + providerName);
		} else {
			bErrorsFound = true;
		}
		return retInfos;

	}

	private SortedSet<Long> getPIdList(File file) {
		SortedSet<Long> ss = new SortedArraySet<Long>();
		try {
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String pId = getPIdFromMUPLine(strLine);
				Long l = Long.valueOf(pId.substring(2)); //Removes leading US
				ss.add(l);
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		return ss;
	}


	private String getPIdFromMUPLine(String line) {
		StringTokenizer st = new StringTokenizer(line, "\t");
		while (st.hasMoreTokens()) {
			return st.nextToken();
		}
		return null;
	}

	private class MupFilenameFilter implements FilenameFilter {
		Pattern myPattern = null;

		public MupFilenameFilter(String partialName) {
			myPattern = Pattern.compile(partialName);
		}

		public boolean accept(File file, String s) {
			Matcher myMatcher = myPattern.matcher(s);
			return myMatcher.matches();
		}
	}

	private String getProviderFromFileName(String fileName) {
		String providerName = "";
		if (fileName != null) {
			String[] parts = fileName.split("_");
			if (parts.length < 7) {
				return "";
			}
			for (int i = 1; i < parts.length - 5; i++) {
				String delim = "";
				if (i > 1) {
					delim = "_";
				}
				providerName = providerName + delim + parts[i];
			}
		}
		return providerName;
	}

	@Test
	public void test() {
		List<String> infos = validate("/opt/Tumri/joz/data/caa/current");
		if (infos != null) {
			for (String s : infos) {
				System.out.println(s);
			}
		}
	}
}

