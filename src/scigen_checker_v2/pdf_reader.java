package scigen_checker_v2;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

import java.awt.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;

import org.apache.pdfbox.PDFBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.bouncycastle.util.test.Test;
import org.omg.CORBA.PRIVATE_MEMBER;

public class pdf_reader {
	// this is where i store the path to the sample file along with it index; it
	// should be loaded automatic
	private HashMap<String, HashMap<String, Integer>> samples = new HashMap<String, HashMap<String, Integer>>();
	private HashMap<String, HashMap<String, Integer>> tests = new HashMap<String, HashMap<String, Integer>>();
	private HashMap<String, HashMap<String, Double>> distant = new HashMap<String, HashMap<String, Double>>();
	private HashMap<String, String> errorlog = new HashMap<String, String>();
private static String testpath ="";
	
	public void readfolder(String foldername) throws IOException {
		// should try to read the index file here; if its already counted then
		// skip

		PDFTextStripper stripper = new PDFTextStripper();
		PDDocument pd;
		BufferedWriter wr;
		File folder = new File(foldername);
		File[] listOfFile = folder.listFiles();

		for (int j = 0; j < listOfFile.length; j++) {
			// System.out.println(listOfFile[j].getName());
			// read subfolders
			if (listOfFile[j].isDirectory()) {
				readfolder(listOfFile[j].getPath());
			}
			// if its a pdf file; in future can make another elseif for xml file
			else if (listOfFile[j].getName().endsWith(".pdf")) {
				// System.out.println(listOfFile[j].getPath());
				// find if there is already index for it
				String name = "INDEX-"
						+ listOfFile[j].getName().substring(0,
								listOfFile[j].getName().lastIndexOf("."))
						+ ".txt";
				if (Arrays.asList(listOfFile).toString().contains(name)) {
					// System.out.println("lets read from index file");
					readindexfile(listOfFile[j].getParent() + "/" + name);
				}
				// TODO else convert pdf like what is doing here
				else {
					System.out
							.println("converting: " + listOfFile[j].getPath());
					File totxt = new File(listOfFile[j].getPath()
							.substring(0,listOfFile[j].getPath().lastIndexOf('.'))+".txt");
					try {
						pd = PDDocument.load(listOfFile[j].getPath());
						// System.out.println(listOfFile[j].getPath().substring(0,
						// listOfFile[j].getPath().lastIndexOf('.'))+".txt");
					
							wr = new BufferedWriter(new OutputStreamWriter(
									new FileOutputStream(totxt)));
							stripper.writeText(pd, wr);
							if (pd != null) {
								pd.close();
							}
							// I use close() to flush the stream.
							wr.close();
					} catch (Exception e) {
						// TODO: handle exception
					}
					//this is faster but it seems like the app server does not support pdftotext
					//commandexecutor cm = new commandexecutor();
					//cm.execute("pdftotext "+ listOfFile[j].getPath());
					
					// ok now I have the txt file; lets normalize it
					normalize(totxt.getPath());
					
					
				}

			}
		}

	}

	private void readindexfile(String path) throws IOException {
		File index = new File(path);
		BufferedReader br;
		br = new BufferedReader(new FileReader(index));
		String line;
		HashMap<String, Integer> a = new HashMap<String, Integer>();
		while ((line = br.readLine()) != null) {
			String[] b = line.split(" ");
			a.put(b[0], Integer.parseInt(b[1]));
		}
		br.close();
		if (path.contains("/samples")) {
			samples.put(path, a);
		} else {
			tests.put(path, a);
		}

	}

	public void normalize(String pathtotxt) {
		try {
			File txt = new File(pathtotxt);
			BufferedReader br;
			br = new BufferedReader(new FileReader(txt));
			String line;
			String content="";
			while ((line = br.readLine()) != null) {
				content+=" ";
				content+= line;
				
				
			}
			br.close();
			
			content = content.toUpperCase();
	;
			content = content.replaceAll("-", " ");// parenthesis like when they
			content = content.replaceAll("[^A-Z ]", "");
			// make a new line
			content = content.replaceAll("\n", " ");
			content = content.replaceAll("\\s+", " ");// remove extra spaces
			//

			// NEED TO INVESTIGATE WHY DID IT STRIP OFF ALOT OF TEXT THAN NEEDED

			// content =
			// content.substring(0,content.lastIndexOf(" REFERENCES "));

			// NEED TO INVESTIGATE WHY DID IT STRIP OFF ALOT OF TEXT THAN NEEDED

			// write back to file and count words
		
			File output = new File(pathtotxt);
			PrintWriter out = new PrintWriter(output);
			out.println(content);
			out.close();
			// call the word counter to create the index file
			wordscount(content, pathtotxt);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void wordscount(String content, String pathtotxt)
			throws FileNotFoundException {
		File textfile = new File(pathtotxt);
		String filename = textfile.getName();
		String path = textfile.getParent();

		String[] words = content.split(" ");
		HashMap<String, Integer> counter = new HashMap<String, Integer>();
		for (int i = 0; i < words.length; i++) {
			if (!counter.containsKey(words[i])) {
				counter.put(words[i], 1);
			} else {
				counter.put(words[i], counter.get(words[i]) + 1);
			}
		}

		File indexout = new File(path + "/INDEX-" + filename);
		String filepath = (indexout.getPath());
		// lets make a map of file path and index of each file so we dont have
		// to read it back again from hdd should save time :D
		if (filepath.contains("/samples")) {
			samples.put(filepath, counter);
		} else {
			tests.put(filepath, counter);

		}

		PrintWriter out = new PrintWriter(indexout);

		for (String key : counter.keySet()) {
			out.println(key + " " + counter.get(key));
		}
		out.close();

		// TEST
		// for (String key : tests.keySet()) {
		// for (String key2 : samples.keySet()) {
		// System.out.println("distant between " + key + " and " + key2
		// + ": "
		// + cal_distant(tests.get(key), samples.get(key2)));
		// }
		//
		// }
	}

	public void Compute() {
		// So here should be the main method and it could use input as a path to a folder we need to check
		// 1st should call something to index all the samples in sample folder
		// this step can be skipped if we already index everything in the sample
		//NO, its skipped in the readfolder function
		try {
			readfolder("data/samples");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 2nd index all in a Test folder (or a folder as input of this method)

		try {
			readfolder(testpath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 3rd cal distant between test and sample folder

		for (String key : tests.keySet()) {
			HashMap<String, Double> distantto = new HashMap<String, Double>();
			for (String key2 : samples.keySet()) {
				double distanttt = cal_distant(tests.get(key),
						samples.get(key2));
				// System.out.println("distant between " + key + " and " + key2
				// + ": " + distanttt);
				distantto.put(key2, distanttt);

			}
			distant.put(key, distantto);
		}

		// 4th write those distants to file maybe
		File path = new File(testpath);
		File distantout = new File(path.getName()+"alldistant.xls");
		//File distantout = new File(testpath+"/alldistant.xls");
		PrintWriter out;
		try {
			out = new PrintWriter(distantout);

			for (String key : distant.keySet()) {
				for (String key2 : distant.get(key).keySet()) {
					out.println(getpdfpath(key) + "\t" + getpdfpath(key2) + "\t"
							+ distant.get(key).get(key2));
				}
			}
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 5th somehow classifies those in the test folder
		//the distant is a global var where I store all the distant from tests to samples
		classified(distant);

	}

	private void classified(HashMap<String, HashMap<String, Double>> distant) {
		// for each file in the test folder
		File path = new File(testpath);
		File distantout = new File(path.getName()+"result.xls");
		PrintWriter out;
		try {
			out = new PrintWriter(distantout);

			for (String key : distant.keySet()) {
				// find it nearest neighbourgh
				String NN = find_NN(distant.get(key));
				//try to get the parent of the NN (folder name should be the type of generator)
				//its might be null so lets not treat it as a file :D
				String type="";
				try {
					type = NN.substring(0, NN.lastIndexOf("/"));
					 type = type.substring(type.lastIndexOf("/")+1, type.length());
				} catch (Exception e) {
					type = ("unknown");
				}
				
				//System.out.println(type distanttoNN;
				double distanttoNN;
				try {
					 distanttoNN = distant.get(key).get(NN);
				} catch (Exception e) {
					 distanttoNN = 0.99;
				}
				//should I branch on type over here or get a common threshold for all of them
				//if I branch then how should I handle if 1 fits several cases
				if (distanttoNN < 0.48) {

					System.out.println(getpdfpath(key) + " is "+ type);
					System.out.println("with distant to :" + getpdfpath(NN) + ": "
							+ distant.get(key).get(NN));
					out.println(getpdfpath(key) + "\t" + getpdfpath(NN) + "\t" + distanttoNN
							+ "\t"+type);
				} else if (0.48 < distanttoNN && distanttoNN < 0.56) {

					System.out.println(getpdfpath(key)
							+ "  is supectec "+type+" please investigate");
					System.out.println("with distant to :" + getpdfpath(NN) + ": "
							+ distant.get(key).get(NN));
					out.println(getpdfpath(key) + "\t" + getpdfpath(NN) + "\t" + distanttoNN
							+ "\tSupected_"+type);
				}

				else if (distanttoNN > 0.56) {

					System.out.println(getpdfpath(key) + " is genuine");
					
					
					System.out.println("with distant to :" + getpdfpath(NN) + ": "
							+ distant.get(key).get(NN));
					out.println(getpdfpath(key) + "\t" + getpdfpath(NN) + "\t" + distanttoNN
							+ "\tGenuine");
				}


			}
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private String getpdfpath(String indexpath){
		File indexfile = new File(indexpath);
		String parent = indexfile.getParent();
		String indexname = indexfile.getName();
	//	System.out.println(parent);
		try {
			indexname = indexname.substring(6, indexname.lastIndexOf("."));
			indexname +=".pdf";
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		//System.out.println(indexname);
		return parent+"/"+indexname;
	}

	private String find_NN(HashMap<String, Double> distantto) {
		double minNN = 1.0;
		String NN = "";
		for (String key : distantto.keySet()) {
			if (distantto.get(key) <= minNN) {
				NN = key;
				minNN = distantto.get(key);
			}

		}
		// it returns the path to the NN
		return NN;
	}

	public double cal_distant(HashMap<String, Integer> text1,
			HashMap<String, Integer> text2) {
		double nboftoken = 0.0;
		double sum = 0.0;

		Set<String> keys1 = text1.keySet();
		Set<String> keys2 = text2.keySet();
		Set<String> allkeys = new HashSet<String>();
		allkeys.addAll(keys1);
		allkeys.addAll(keys2);
		Integer Na = 0, Nb = 0;
		// get the nb of token in each text
		for (String key : allkeys) {
			Integer Fa = 0;
			Integer Fb = 0;
			if (text1.containsKey(key))
				Fa = text1.get(key);
			if (text2.containsKey(key))
				Fb = text2.get(key);
			Na += Fa;
			Nb += Fb;
		}
		// reduce propotion for text of different lenght
		if (Na <= Nb) {
			for (String key : allkeys) {
				Integer Fa = 0;
				Integer Fb = 0;
				if (text1.containsKey(key))
					Fa = text1.get(key);
				if (text2.containsKey(key))
					Fb = text2.get(key);
				sum += Math.abs(Fa - (double) Fb * (Na / (double) Nb));
			}
			return sum / (2 * Na);
		}

		else {
			for (String key : allkeys) {
				Integer Fa = 0;
				Integer Fb = 0;
				if (text1.containsKey(key))
					Fa = text1.get(key);
				if (text2.containsKey(key))
					Fb = text2.get(key);
				sum += Math.abs(Fa * (Nb / (double) Na) - (double) Fb);
			}
			return sum / (2 * Nb);
		}
	}

	public static void main(String[] args) throws IOException {
		// PrintWriter errorwrite = new PrintWriter(errorlog);
		//String path ="";
		try {
			 testpath= args[0];
		} catch (Exception e) {
			System.out.println("incorrect number of args");
		}
		
		pdf_reader a = new pdf_reader();
		a.Compute();
	}

}
