package org.eclipse.epsilon.egl.sync;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Supplier;

import javax.swing.JOptionPane;

import org.eclipse.epsilon.egl.EglFileGeneratingTemplateFactory;
import org.eclipse.epsilon.egl.EgxModule;
import org.eclipse.epsilon.egl.sync.SyncGenerelisabiltyTests.Language;
import org.eclipse.epsilon.egl.sync.SyncScalablityTests.TestType;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.execute.introspection.IPropertyGetter;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class SyncScalablityTests {

	enum NumberOfSyncs {
		OneSync, TwoSyncs, ThreeSyncs, FourSyncs, FiveSyncs
	}
	
	enum TestType{
		Scalablity,
		Performance,
	}
	
	EmfModel model;
	FolderSync syncReader;
	EmfModel tempModel;
	static List<String> orginalNewLines;
	
	public static String regexMatch(String subject, String expression) {
		Pattern p = Pattern.compile(expression);
		Matcher m = p.matcher(subject.trim());
		if (!m.find())
			return null;
		return m.group(1);
	}
	
	
	//old noe
	public static void addAndUpdateModel(String folderPath, EmfModel model, Map<String, String> attrToBehaviour, NumberOfSyncs numberOfSync)throws IOException {
	Map<String, String> attrToId = new HashMap<>();
//		System.out.println(folderPath);
		File[] files = new File(folderPath).listFiles();
		// check if file is exists
		for (File f : files) {
			if (!f.isFile())
				continue;
			for (String attribute : attrToBehaviour.keySet()) {
				BufferedReader original = new BufferedReader(new FileReader(f));

				List<String> newLines = new ArrayList<String>();
				String line;
				while ((line = original.readLine()) != null) {
					newLines.add(line);
					String id = regexMatch(line, "sync (.+?), " + attribute);
					if (id != null) {
						attrToId.put(attribute, id);
//						attrToId.put(id, attribute);
						newLines.add(attrToBehaviour.get(attribute));
						while (!line.contains("endSync"))
							line = original.readLine();
						newLines.add(line);
					}
				}
				original.close();
				Files.write(f.toPath(), newLines);
//				break;
			}
		}
		// Update the model with values in the generated files..
		FolderSync folderSync = new FolderSync();
		folderSync.getSynchronization(folderPath, model);

		model.store();
		IPropertyGetter propertyGetter = model.getPropertyGetter();
		for (String attr : attrToId.keySet()) {
			Object modelElement = model.getElementById(attrToId.get(attr));
			
			try {
				assertEquals("test " +attr, attrToBehaviour.get(attr), (String) propertyGetter.invoke(modelElement, attr));		
			} catch (EolRuntimeException e) {
				e.printStackTrace();
			}
		}
	}

	// how to compute the time..
	@Rule
	public TestName name = new TestName();
	private long start;

	@Before
	public void start() {
		start = System.currentTimeMillis();
	}
	
	@BeforeClass
	public static void beforeClass() {
		new File(resultsForScalablitiyFilePath).delete();
		new File(resultsForPerformanceFilePath).delete();

	}
	
	@After
	public void end() {
		System.out.println("Test " + name.getMethodName() + " took " + (System.currentTimeMillis() - start) + " ms");
	}
	
	// today change 2/2
	static String resultsForScalablitiyFilePath = "DataForOneTimeRunFor2000and10000FilesWithOneSync.csv";
	static String resultsForPerformanceFilePath = "DataForSyncPerformanceTests.csv";
	
	public static void writeResultsToCSvFile(int stageNumber, long takenTime, long bytesUsed, int numberOfFile, NumberOfSyncs numberOfSync, TestType testType) {
		try {
			String resultFilePath = testType == TestType.Scalablity? resultsForScalablitiyFilePath : resultsForPerformanceFilePath;
					
			boolean header = !new File(resultFilePath).exists();
			FileWriter fw = new FileWriter(resultFilePath, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			if (header)
//				pw.println("Stage number, (Totel) Taken time for each run," + Clock.systemDefaultZone().instant());
				pw.println("numberOfFiles, numberOfRegions, Stage number, (Totel) Taken time, Bytes used, Outlier, Q1 , Q3, IQR, Upper Value, Lower Value");
			pw.println(String.format("%d,%d,%d,%d,%d", numberOfFile, numberOfSync.ordinal() +1, stageNumber, takenTime, bytesUsed));
			pw.flush();
			fw.close();

		} catch (Exception E) {
			System.out.println("There is errors!!");
		}
	}
	
	Runtime runtime = Runtime.getRuntime();

	// today change 2/2
	public void doTestNTimes(int numberOfTimes, int numberOfFile, TestType testType) throws IOException {
		for (int i = 0; i < numberOfTimes; i++) {			
			//  if I want to test OneSync, TwoSyncs, ThreeSyncs, FourSyncs, and FiveSyncs folders
			for (int j = 0; j < 5; j++) {
				runtime.gc(); 
				NumberOfSyncs numberOfSync = j == 0 ? NumberOfSyncs.OneSync : j == 1 ? NumberOfSyncs.TwoSyncs : j== 2 ? NumberOfSyncs.ThreeSyncs : j== 3 ? NumberOfSyncs.FourSyncs : NumberOfSyncs.FiveSyncs;

				long start = System.currentTimeMillis();
				oneDifferentValue(numberOfFile, numberOfSync, testType);
				long memory = runtime.totalMemory() - runtime.freeMemory();
				writeResultsToCSvFile(i, System.currentTimeMillis() - start, memory, numberOfFile, numberOfSync, testType);	
			}
		}
	}
	
	// today change...  
	public void testValues (Map<String, String> attrToBehaviour, int numberOfFiles, NumberOfSyncs numberOfSync, TestType testType) {

		String modelFile = testType == TestType.Scalablity
            ? String.format("/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Scalability-Part2/" + numberOfSync + "/Models/" + numberOfSync + "-comps-html-%1$d-comps.model", numberOfFiles)
            : String.format("/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Performance-Part4/" + numberOfSync + "/Models/" + numberOfSync + "-comps-html-%1$d-comps.model", numberOfFiles);
		
		String foldePath= testType == TestType.Scalablity
			? String.format("/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Scalability-Part2/" + numberOfSync +"/gen-%1$d-components", numberOfFiles)
			: String.format("/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Performance-Part4/" + numberOfSync +"/gen-%1$d-components", numberOfFiles);
		
		String eccorFile = testType == TestType.Scalablity
			? "/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Scalability-Part2/comps.ecore"
			: "/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Performance-Part4/comps.ecore";
			
		model = new EmfModel();
		model.setName(numberOfSync.name());
		System.out.println("The name of model is " + model);
		model.setMetamodelFile(eccorFile);
		model.setModelFile(new File(modelFile).getAbsolutePath());
		model.setReadOnLoad(true);
		try {
			model.load();
		} catch (EolModelLoadingException e2) {
			e2.printStackTrace();
		}

		try {
			addAndUpdateModel(foldePath, model, attrToBehaviour, numberOfSync);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * One different value from the one in the models
	 */
	
	// Today change 2/2
	public  void oneDifferentValue(int numberOfFiles, NumberOfSyncs numberOfSync, TestType testType) {
		Map<String, String> attrToBehaviour = new HashMap<>();

		switch (numberOfSync) {			
		case OneSync:
			attrToBehaviour.put("name", Instant.now().toString());		
			break;
		case TwoSyncs:
			attrToBehaviour.put("name",Instant.now().toString());
			attrToBehaviour.put("htmlBehaviour", Instant.now().toString());
			break;
		case ThreeSyncs:
			attrToBehaviour.put("name", Instant.now().toString());
			attrToBehaviour.put("htmlBehaviour", Instant.now().toString());
			attrToBehaviour.put("authorName", Instant.now().toString());
			break;
		case FourSyncs:
			attrToBehaviour.put("name", Instant.now().toString());
			attrToBehaviour.put("htmlBehaviour", Instant.now().toString());
			attrToBehaviour.put("authorName", Instant.now().toString());
			attrToBehaviour.put("javaBehaviour", Instant.now().toString());
			break;
		case FiveSyncs:
			attrToBehaviour.put("name", Instant.now().toString());
			attrToBehaviour.put("htmlBehaviour", Instant.now().toString());
			attrToBehaviour.put("authorName", Instant.now().toString());
			attrToBehaviour.put("javaBehaviour", Instant.now().toString());
			attrToBehaviour.put("pythonBehaviour", Instant.now().toString());
			break;
		default:
			break;
		}
		testValues(attrToBehaviour, numberOfFiles, numberOfSync, testType);
	}
	
	/*
	 * Scalablity tests..
	 */

	public void test2000FilesScalablity() throws IOException {
		doTestNTimes(1, 2000, TestType.Scalablity);
	}
	
	@Test
	public void test4000FilesScalablity() throws IOException {
		doTestNTimes(1, 4000, TestType.Scalablity);
	}
	@Test
	public void test6000FilesScalablity() throws IOException {
		doTestNTimes(1, 6000, TestType.Scalablity);
	}
	@Test
	public void test8000FilesScalablity() throws IOException {
		doTestNTimes(1, 8000, TestType.Scalablity);
	}
	@Test
	public void test10000FilesScalablity() throws IOException {
		doTestNTimes(1, 10000, TestType.Scalablity);
	}
	//%%%%%%%%%%%%%%%%%%%%%%%%%%% Just Testing Small files %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%	
//	// Today 24/2.. just test 10 nad 20 files with 1,2, and 3 regions. to be fast
//	@Test
//	public void test10FilesScalablity() throws IOException {
//		doTestNTimes(5, 10, TestType.Scalablity);
//	}
//	
//	@Test
//	public void test20FilesScalablity() throws IOException {
//		doTestNTimes(5, 20, TestType.Scalablity);
//	}
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%	
	
}
