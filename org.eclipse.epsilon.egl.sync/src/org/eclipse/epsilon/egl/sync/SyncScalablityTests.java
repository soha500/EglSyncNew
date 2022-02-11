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
		OneSync, TwoSyncs, ThreeSyncs,
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
	// today change... Map in a bove method to List as shown in below line
//	public static void addAndUpdateModel(String folderPath, EmfModel model, List<String> behaviours, NumberOfSyncs numberOfSync) throws IOException {
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
		// Update the model with values taken from the generated file..
		FolderSync folderSync = new FolderSync();
		folderSync.getSynchronization(folderPath, model);

		model.store();
		IPropertyGetter propertyGetter = model.getPropertyGetter();
		for (String attr : attrToId.keySet()) {
			Object modelElement = model.getElementById(attrToId.get(attr));
			
			// 9/2/2022..The try below working fine, but it added only value to one sync in each file not two or third sync.
			try {
				assertEquals("test " +attr, attrToBehaviour.get(attr), (String) propertyGetter.invoke(modelElement, attr));		
			} catch (EolRuntimeException e) {
				e.printStackTrace();
			}
			
//			try {
//				switch (numberOfSync) {
//				case OneSync:
//					assertEquals("test 1 java", attrToBehaviour.get(attr), (String) propertyGetter.invoke(modelElement, "name"));	
//					break;
//				case TwoSyncs:
//					String NameBehavior =  (String) propertyGetter.invoke(modelElement, "name");
//					assertEquals("test 2 name", attrToBehaviour.get(attr), NameBehavior);
//					
//					String HTMLBehavior =  (String) propertyGetter.invoke(modelElement, "htmlBehaviour");
//					assertEquals("test 2 htmlBeheviour", attrToBehaviour.get(attr), HTMLBehavior);
//					break;
//				case ThreeSyncs:
//					assertEquals("test 3 name", attrToBehaviour.get(attr), (String) propertyGetter.invoke(modelElement, "name"));
//					assertEquals("test 3 htmlBeheviour", attrToBehaviour.get(attr), (String) propertyGetter.invoke(modelElement, "htmlBehaviour"));
//					assertEquals("test 3 autherName", attrToBehaviour.get(attr), (String) propertyGetter.invoke(modelElement, "authorName"));
//					break;
//				}
//			} catch (EolRuntimeException e) {
//				e.printStackTrace();
//			}
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
		new File(resultsFilePath).delete();

	}
	
	@After
	public void end() {
		System.out.println("Test " + name.getMethodName() + " took " + (System.currentTimeMillis() - start) + " ms");
	}
	
	// today change 2/2
	static // with one different value
	String resultsFilePath = "DataForSyncScalablityTests.csv";
	public static void writeResultsToCSvFile(int stageNumber, long takenTime, long bytesUsed, int numberOfFile, NumberOfSyncs numberOfSync) {
		try {
			boolean header = !new File(resultsFilePath).exists();
			FileWriter fw = new FileWriter(resultsFilePath, true);
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
	public void doTestNTimes(int numberOfTimes, int numberOfFile) throws IOException {
		for (int i = 0; i < numberOfTimes; i++) {
			for (int j = 0; j < 3; j++) {
				runtime.gc(); 
				NumberOfSyncs numberOfSync = j == 0 ? NumberOfSyncs.OneSync : j == 1 ? NumberOfSyncs.TwoSyncs : NumberOfSyncs.ThreeSyncs;
				long start = System.currentTimeMillis();
				oneDifferentValue(numberOfFile, numberOfSync);
//				writeResultsToCSvFile(i, System.currentTimeMillis() - start, filePath);
				long memory = runtime.totalMemory() - runtime.freeMemory();
				writeResultsToCSvFile(i, System.currentTimeMillis() - start, memory, numberOfFile, numberOfSync);
				
			}
		}
	}
	
	// today change...  
	public void testValues (Map<String, String> attrToBehaviour, int numberOfFiles, NumberOfSyncs numberOfSync) {

		// Today change 1/2/2022
//		File originalFile = new File(String.format("/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Scalability-Part2/" + folderName + "/Models/OneSync-comps-html-%1$d-Comps.model", numberOfFiles));
		File originalFile = new File(String.format("/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Scalability-Part2/" + numberOfSync + "/Models/" + numberOfSync + "-comps-html-%1$d-comps.model", numberOfFiles));
		model = new EmfModel();
		model.setName(numberOfSync.name());
		System.out.println("The name of model is " + model);
		model.setMetamodelFile("/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Scalability-Part2/comps.ecore");
		model.setModelFile(originalFile.getAbsolutePath());
		model.setReadOnLoad(true);
		try {
			model.load();
		} catch (EolModelLoadingException e2) {
			e2.printStackTrace();
		}

		try {
			// Today change.....
			String folderPath = String.format("/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Scalability-Part2/" + numberOfSync +"/gen-%1$d-components", numberOfFiles);
			addAndUpdateModel(folderPath, model, attrToBehaviour, numberOfSync);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * One different value from the one in the models
	 */
	
	// Today change 2/2
	public  void oneDifferentValue(int numberOfFiles, NumberOfSyncs numberOfSync) {
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
		default:
			break;
		}
		testValues(attrToBehaviour, numberOfFiles, numberOfSync);
	}
	
	/*
	 * Today change....runTheGenerator() method was above this test but
	 * I moved it to the end of the file. Scenario 1, if we have a 200 files
	 */

	@Test
	public void test200Files() throws IOException {
		doTestNTimes(1, 200);
	}
	
	@Test
	public void test400Files() throws IOException {
		doTestNTimes(1, 400);
	}
	@Test
	public void test600Files() throws IOException {
		doTestNTimes(1, 600);
	}
	@Test
	public void test800Files() throws IOException {
		doTestNTimes(1, 800);
	}
	@Test
	public void test1000Files() throws IOException {
		doTestNTimes(1, 1000);
	}
}
































































//				String id = regexMatch(line, "sync (.+?), name");
//				if (id != null) {
//					attrToBehaviour.put(id, behaviour);
//					newLines.add(behaviour);
//					while (!line.contains("endSync"))
//						line = original.readLine();
//					newLines.add(line);
//				
//				String id = regexMatch(line, "sync (.+?), htmlBehaviour");
//				if (id != null) {
//					attrToBehaviour.put(id, behaviour);
//					newLines.add(behaviour);
//					while (!line.contains("endSync"))
//						line = original.readLine();
//					newLines.add(line);
//				
//				String id = regexMatch(line, "sync (.+?), authorName");
//				if (id != null) {
//					attrToBehaviour.put(id, behaviour);
//					newLines.add(behaviour);
//					while (!line.contains("endSync"))
//						line = original.readLine();
//					newLines.add(line);
































//
//	/*
//	 * Scenario 2, if we have a 400 files
//	 */
//
//	@Test
//	public void test400Files() throws IOException {
//		doTestNTimes(10, 400);
//	}
//
//	/*
//	 * Scenario 3, if we have a 600 files
//	 */
//
//	@Test
//	public void test600Files() throws IOException {
//		doTestNTimes(10, 600);
//	}
//
//	/*
//	 * Scenario 4, if we have a 800 files
//	 */
//
//	@Test
//	public void test800Files() throws IOException {
//		doTestNTimes(10, 800);
//	}
//
//	/*
//	 * Scenario 5, if we have a 1000 files
//	 */
//
//	@Test
//	public void test1000Files() throws IOException {
//		doTestNTimes(10, 1000);
//	}




// old one 
//for (String className : behaviours.keySet()) {
//	if (!f.getName().contains(className))
//		continue;
//	String behaviour = behaviours.get(className);
//	BufferedReader original = new BufferedReader(new FileReader(f));
//
//	List<String> newLines = new ArrayList<String>();
//	String line;
//	while ((line = original.readLine()) != null) {
//		newLines.add(line);
//		// Today change.... to below one
//		// String id = regexMatch(line, "//sync (.+?), behaviour");
//		String id = regexMatch(line, "<!--sync (.+?), name  -->");
//		if (id != null) {
//			attrToBehaviour.put(id, behaviour);
//			newLines.add(behaviour);
//			// Today change.... to below one
//			// while (!line.contains("//endSync"))
//			while (!line.contains("<!--endSync -->"))
//				line = original.readLine();
//			newLines.add(line);
//		}
//	}
// today change...






// today change... Map in a bove method to List
//public static void addAndUpdateModel2(String folderPath, EmfModel model, List<String> behaviours, NumberOfSyncs numberOfSync) throws IOException {
//
//	Map<String, String> attrToBehaviour = new HashMap<>();
//	System.out.println(folderPath);
//	File[] files = new File(folderPath).listFiles();
//	// check if file is exists
//	for (File f : files) {
//		if (!f.isFile())
//			continue;
//		// today change...
//		for (String behaviour : behaviours) {
//			BufferedReader original = new BufferedReader(new FileReader(f));
//
//			List<String> newLines = new ArrayList<String>();
//			String line;
//			while ((line = original.readLine()) != null) {
//				newLines.add(line);
//				String id = regexMatch(line, "sync (.+?), htmlBehaviour");
//				if (id != null) {
//					attrToBehaviour.put(id, behaviour);
//					newLines.add(behaviour);
//					while (!line.contains("endSync"))
//						line = original.readLine();
//					newLines.add(line);
//				}
//			}
//			// until here
//			original.close();
//			Files.write(f.toPath(), newLines);
//			break;
//		}
//	}
//	// Update the model with values taken from the generated file..
//	FolderSync folderSync = new FolderSync();
//	folderSync.getSynchronization(folderPath, model);
//
//	model.store();
//	IPropertyGetter propertyGetter = model.getPropertyGetter();
//	for (String id : attrToBehaviour.keySet()) {
//		Object modelElement = model.getElementById(id);
//		try {
//			// Today change.... coment
//			//assertEquals("test 1 to update the model", attrToBehaviour.get(id), (String) propertyGetter.invoke(modelElement, "name"));
//			
//			switch (numberOfSync) {
//			case OneSync:
////				assertEquals("test 1 name", attrToBehaviour.get(id), (String) propertyGetter.invoke(modelElement, "name"));
//				break;
//			case TwoSyncs:
////				assertEquals("test 2 name", attrToBehaviour.get(id), (String) propertyGetter.invoke(modelElement, "name"));
//				assertEquals("test 2 htmlBeheviour", attrToBehaviour.get(id), (String) propertyGetter.invoke(modelElement, "htmlBehaviour"));
//				break;
//			case ThreeSyncs:
////				assertEquals("test 3 name", attrToBehaviour.get(id), (String) propertyGetter.invoke(modelElement, "name"));
//				assertEquals("test 3 htmlBeheviour", attrToBehaviour.get(id), (String) propertyGetter.invoke(modelElement, "htmlBehaviour"));
////				assertEquals("test 3 autherName", attrToBehaviour.get(id), (String) propertyGetter.invoke(modelElement, "authorName"));
//				break;
//			}
//		} catch (EolRuntimeException e) {
//			e.printStackTrace();
//		}
//	}
//}




//// today commented 2/2
//static // with one different value
//String resultsFilePath = "DataForSyncScalablityTests.csv";
//public static void writeResultsToCSvFile(int stageNumber, long takenTime, long bytesUsed, int numberOfFile, int nSync) {
//	try {
//		boolean header = !new File(resultsFilePath).exists();
//		FileWriter fw = new FileWriter(resultsFilePath, true);
//		BufferedWriter bw = new BufferedWriter(fw);
//		PrintWriter pw = new PrintWriter(bw);
//		if (header)
////			pw.println("Stage number, (Totel) Taken time for each run," + Clock.systemDefaultZone().instant());
//			pw.println("numberOfFiles, numberOfRegions, Stage number, (Totel) Taken time, Bytes used, Outlier, Q1 , Q3, IQR, Upper Value, Lower Value");
//		pw.println(String.format("%d,%d,%d,%d,%d", numberOfFile, nSync, stageNumber, takenTime, bytesUsed));
//		pw.flush();
//		fw.close();
//
//	} catch (Exception E) {
//		System.out.println("There is errors!!");
//	}
//}




//// today commented 2/2
//public void doTestNTimes(int numberOfTimes, int numberOfFile) throws IOException {
//	for (int i = 0; i < numberOfTimes; i++) {
//		for (int j = 0; j < 3; j++) {
//			runtime.gc(); 
//			String nSync = j == 0 ? "OneSync" : j == 1 ? "TwoSyncs" : "ThreeSyncs";
//			long start = System.currentTimeMillis();
//			oneDifferentValue(numberOfFile, nSync);
////			writeResultsToCSvFile(i, System.currentTimeMillis() - start, filePath);
//			long memory = runtime.totalMemory() - runtime.freeMemory();
//			writeResultsToCSvFile(i, System.currentTimeMillis() - start, memory, numberOfFile, j);
//			
//		}
//	}
//}



/*
 * One different value
 */

//// Today commenteded 2/2
//public  void oneDifferentValue(int numberOfFiles, String folderName) {
////	Map<String, String> behaviours = new HashMap<>();
////	behaviours.put("component", "hi");
//	
//	// today changes..
//	List<String> behaviours = new ArrayList<>();
//	behaviours.add("newComponent");
//	
//	testValues(behaviours, numberOfFiles, folderName);
//}





//@RepeatedTest(100)
//@Repeat( times = 100 )
//@Test(invocationCount = 100)

// old one
//public void testValues(Map<String, String> behaviours, int numberOfFiles, String folderName) {


//// today comentted.. 2/2  
//public void testValues (List<String> behaviours, int numberOfFiles,  String folderName) {
//
//	// Today change 1/2/2022
////	File originalFile = new File(String.format("/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Scalability-Part2/" + folderName + "/Models/OneSync-comps-html-%1$d-Comps.model", numberOfFiles));
//
//	File originalFile = new File(String.format("/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Scalability-Part2/" + folderName + "/Models/" + folderName + "-comps-html-%1$d-comps.model", numberOfFiles));
//
////	File originalFile = new File(System.getProperty("user.dir") + String.format("/SyncTests/Scalability-Part2/OneSync/BoilerController-Html-%1$d-Components.model", numberOfFiles));
//	model = new EmfModel();
//	model.setName("M");
//	// Today change 18/01/2022
//	model.setMetamodelFile("/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Scalability-Part2/comps.ecore");
//	// Today change 18/01/2022.. the below style is for short path but not sure if
//	// it works or not
////	model.setMetamodelFile(new File(System.getProperty("user.dir") + "/SyncTests/Scalability-Part2/comps.ecore").getAbsolutePath());
//	model.setModelFile(originalFile.getAbsolutePath());
//	model.setReadOnLoad(true);
//	try {
//		model.load();
//	} catch (EolModelLoadingException e2) {
//		e2.printStackTrace();
//	}
//
//	try {
//		String folderPathOneSync = String.format("/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Scalability-Part2/" + folderName +"/gen-%1$d-components", numberOfFiles);
//		addAndUpdateModel(folderPathOneSync, model, behaviours);
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
//}






/*
 * Two values but one same the one in the model
 */

//public boolean twoDifferentValues (int numberOfFiles) {
//	Map<String, String> behaviours = new HashMap<>();
//	behaviours.put("BoilerActuator", "return temperature - targetTemperature;");
//	behaviours.put("BoilerActuator", "return temperature - targetTemperature;");
//	behaviours.put("TemperatureController", "if (temperatureDifference > 0 && boilerStatus == true) { return 1; } else if (temperatureDifference < 0 && boilerStatus == false) { return 2; } else return 0;");
//	testValues(behaviours, numberOfFiles);
//	return true;
//	
//}

////@Test
//public void test10Files2() throws IOException {
//doTestNTimes(100, filePathFor10Files, () -> twoDifferentValues(10));
//}
//
//// Today change.... I commented this method to see if it is important or not Now. 	
//// createModule() 
//public IEolModule createModule() {
//	try {
//		EglFileGeneratingTemplateFactory templateFactory = new EglFileGeneratingTemplateFactory();
////		templateFactory.setOutputRoot(System.getProperty("user.dir") + "/SyncTests/GeneratedFilesFromUniversity/");
//		// other workspace
//		templateFactory.setOutputRoot("/Users/sultanalmutairi/Documents/Workspaces/runtime-EclipseApplication/org.eclipse.epsilon.examples.egl.comps/boiler-To-Generate-100-Files/boiler-To-Generate-100-Files/syncregions-100Files/");
////		templateFactory.setOutputRoot("/Users/sultanalmutairi/Documents/Workspaces/runtime-EclipseApplication/org.eclipse.epsilon.examples.egl.comps/boiler-To-Generate-100-Files/boiler-To-Generate-100-Files/syncregions-100Files/");
//
//		return new EgxModule(templateFactory);
//	} catch (Exception ex) {
//		throw new RuntimeException(ex);
//	}
//}


//String filePathFor200Files = "DataFor200FilesWith1DifferentValue.csv";
//String filePathFor400Files = "DataFor400FilesWith1DifferentValue.csv";
//String filePathFor600Files = "DataFor600FilesWith1DifferentValue.csv";
//String filePathFor800Files = "DataFor800FilesWith1DifferentValue.csv";
//String filePathFor1000Files = "DataFor1000FilesWith1DifferentValue.csv";

// with half different value
//String filePathFor200Files = "DataFor200FilesWithHalfDifferentValue.csv";
//String filePathFor400Files = "DataFor400FilesWithHalfDifferentValue.csv";
//String filePathFor600Files = "DataFor600FilesWithHalfDifferentValue.csv";
//String filePathFor800Files = "DataFor800FilesWithHalfDifferentValue.csv";
//String filePathFor1000Files = "DataFor1000FilesWithHalfDifferentValue.csv";

// Today change 19/01/2022... if I need separate path for each language, I added tests for each language below
//String filePathFor200FilesWithOneSyncRegion = "DataFor200FilesWithOneSyncRegionWith1DifferentValue.csv";
//String filePathFor200FilesWithTwoSyncRegion = "DataFor200FilesWithTwoSyncRegionWith1DifferentValue.csv";
//String filePathFor200FilesWithThreeSyncRegion = "DataFor200FilesWithThreeSyncRegionWith1DifferentValue.csv";

////Today change 19/01/2022... if I need separate path for each language
//@Test
//public void test200FilesWithOneSyncRegion() throws IOException {
//doTestNTimes(10, filePathFor200FilesWithOneSyncRegion, () -> oneDifferentValue(200));
//}
//@Test
//public void test200FilesWithTwoSyncRegion() throws IOException {
//doTestNTimes(10, filePathFor200FilesWithTwoSyncRegion, () -> oneDifferentValue(200));
//}
//@Test
//public void test200FilesWithThreeSyncRegion() throws IOException {
//doTestNTimes(10, filePathFor200FilesWithThreeSyncRegion, () -> oneDifferentValue(200));
//}





	/*
	 * 
	 * the below tests are for 50 times ran
	 * 
	 */

//	/*
//	 * Scenario 1, if we have a 100 files
//	 */
//	
//	@Test
//	public void test100Files() throws IOException {
//		doTestNTimes(50, filePathFor100Files, () -> oneDifferentValue(100));
//	}
//	
//	/*
//	 * Scenario 2, if we have a 200 files
//	 */
//	
//	@Test
//	public void test200Files() throws IOException {
//		doTestNTimes(50, filePathFor200Files, () -> oneDifferentValue(200));
//	}
//	
//	/*
//	 * Scenario 3, if we have a 300 files
//	 */
//	
//	@Test
//	public void test300Files() throws IOException {
//		doTestNTimes(50, filePathFor300Files, () -> oneDifferentValue(300));
//	}
//	
//	/*
//	 * Scenario 4, if we have a 400 files
//	 */
//	
//	@Test
//	public void test400Files() throws IOException {
//		doTestNTimes(50, filePathFor400Files, () -> oneDifferentValue(400));
//	}
//	
//	/*
//	 * Scenario 5, if we have a 500 files
//	 */
//	
//	@Test
//	public void test500Files() throws IOException {
//		doTestNTimes(50, filePathFor500Files, () -> oneDifferentValue(500));
//	}
//	
//	/*
//	 * Scenario 6, if we have a 600 files
//	 */
//	
//	@Test
//	public void test600Files() throws IOException {
//		doTestNTimes(50, filePathFor600Files, () -> oneDifferentValue(600));
//	}
//	
//	/*
//	 * Scenario 7, if we have a 700 files
//	 */
//	
//	@Test
//	public void test700Files() throws IOException {
//		doTestNTimes(50, filePathFor700Files, () -> oneDifferentValue(700));
//	}
//	
//	/*
//	 * Scenario 8, if we have a 800 files
//	 */
//	
//	@Test
//	public void test800Files() throws IOException {
//		doTestNTimes(50, filePathFor800Files, () -> oneDifferentValue(800));
//	}
//
//	/*
//	 * Scenario 9, if we have a 900 files
//	 */
//	
//	@Test
//	public void test900Files() throws IOException {
//		doTestNTimes(50, filePathFor900Files, () -> oneDifferentValue(900));
//	}
//
//	/*
//	 * Scenario 10, if we have a 1000 files
//	 */
//	
//	@Test
//	public void test1000Files() throws IOException {
//		doTestNTimes(50, filePathFor1000Files, () -> oneDifferentValue(1000));
//	}

//}



// I put it here 18/01/2022, I can retern it when I need it.
//@Test
//public void runTheGenerator() {
//	
//	File originalFile = new File(System.getProperty("user.dir") + String.format("/boiler-To-Generate-%1$d-Files/BoilerController-%1$d-Components.model", 100));
//	model = new EmfModel();
//	model.setName("M");
//	model.setMetamodelFile(new File(System.getProperty("user.dir") + "/boiler-Ecore/comps.ecore").getAbsolutePath());
//	model.setModelFile(originalFile.getAbsolutePath());
//	model.setReadOnLoad(true);
//	try {
//		model.load();
//	} catch (EolModelLoadingException e2) {
//		e2.printStackTrace();
//	}
//	
//	
//	
//	
//	IEolModule module = createModule(); // The createModule() method follows
//	module.getContext().getModelRepository().addModel(model); // The model parameter is the EmfModel you already
//	try {
//		module.parse(new File ("user.dir") + String.format("/boiler-To-Generate-100-Files/sync-regions100.egx"));
//		// other workspace
//		module.parse(new File ("/Users/sultanalmutairi/git/Epsilon-Source/org.eclipse.epsilon/examples/org.eclipse.epsilon.examples.egl.comps/boiler-To-Generate-100-Files/sync-regions100.egx"));
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
//	try {
//		module.execute();
//	} catch (EolRuntimeException e) {
//		e.printStackTrace();
//	}
//}

// I removed this part for the temp model because I do not need it for these test as each test not depends on others..
//
//@Rule
//public TemporaryFolder tempFolder = new TemporaryFolder();
//
//@Before
//public void init() throws IOException {
//
//	File orginalFile = new File(System.getProperty("user.dir") + "/boiler-To-Generate-10-Files/BoilerController-10-Components.model");
////	File orginalFile = new File(System.getProperty("/Users/sultanalmutairi/git/Epsilon-Source/org.eclipse.epsilon/examples/org.eclipse.epsilon.examples.egl.comps/boiler-To-Generate-10-Files/BoilerController-10-Components.model"));
//	File tempFile = tempFolder.newFile("tempUni.model");
//	try {
//		Files.copy(orginalFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
//
//	tempModel = new EmfModel();
//	tempModel.setName("M");
//	tempModel.setMetamodelFile(new File(System.getProperty("user.dir") + "/boiler-Ecore/comps.ecore").getAbsolutePath());
////	tempModel.setMetamodelFile(new File(System.getProperty("/Users/sultanalmutairi/git/EglSync/org.eclipse.epsilon.egl.sync/boiler-To-Generate-10-Files/comps.ecore")).getAbsolutePath());
//	tempModel.setModelFile(tempFile.getAbsolutePath());
//	tempModel.setReadOnLoad(true);
//
//	try {
//		tempModel.load();
//	} catch (EolModelLoadingException e2) {
//		e2.printStackTrace();
//	}
//	tempFile.deleteOnExit();
//
//}
