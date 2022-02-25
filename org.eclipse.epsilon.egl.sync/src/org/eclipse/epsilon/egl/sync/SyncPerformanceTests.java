package org.eclipse.epsilon.egl.sync;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.epsilon.egl.output.OutputBuffer;
import org.eclipse.epsilon.egl.sync.SyncScalablityTests.NumberOfSyncs;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.execute.introspection.IPropertyGetter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

// System.out.println("Working Directory = " + System.getProperty("user.dir"));

public class SyncPerformanceTests {
	

//	enum TestStatus {
//		Successful, MergeConflict, LineLost, ChangeNotDetected, ConflictNotDetected, MergeFailure,
//		// When 
//	}
//
//	private static String FolderPath(int numberOfFiles, NumberOfSyncs numberOfSync) {
//		//return "/Users/sultanalmutairi/new_git/org.eclipse.epsilon/plugins/org.eclipse.epsilon.egl.engine/PreformancTests/boiler-To-Generate-"
//		//		+ numberOfFiles + "-Files/TheGeneratedFiles-" + numberOfFiles;
//		
//		return "/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Performance-Part4/" + numberOfSync + "/gen-" + numberOfFiles +"-components";
//		}


	enum NumberOfSyncs {
		OneSync, TwoSyncs, ThreeSyncs,
	}
	
	// For the model...not sure if I need it 
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
	// to find syncs and update model
	public static void addAndUpdateModel(String folderPath, EmfModel model, Map<String, String> attrToBehaviour, NumberOfSyncs numberOfSync)throws IOException {
	// today change... Map in a bove method to List as shown in below line
//	public static void addAndUpdateModel(String folderPath, EmfModel model, List<String> behaviours, NumberOfSyncs numberOfSync) throws IOException {
		Map<String, String> attrToId = new HashMap<>();
		File[] files = new File(folderPath).listFiles();
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
		new File(resultsFilePath).delete();
	}
	
	@After
	public void end() {
		System.out.println("Test " + name.getMethodName() + " took " + (System.currentTimeMillis() - start) + " ms");
	}
	
	// today change 2/2
	static // with one different value
	String resultsFilePath = "DataForPerformanceTests.csv";
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
		File originalFile = new File(String.format("/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Performance-Part4/" + numberOfSync + "/Models/" + numberOfSync + "-comps-html-%1$d-comps.model", numberOfFiles));
		model = new EmfModel();
		model.setName(numberOfSync.name());
		//System.out.println("The name of model is " + model);
		model.setMetamodelFile("/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Performance-Part4/comps.ecore");
		model.setModelFile(originalFile.getAbsolutePath());
		model.setReadOnLoad(true);
		try {
			model.load();
		} catch (EolModelLoadingException e2) {
			e2.printStackTrace();
		}

		try {
			String folderPath = String.format("/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Performance-Part4/" + numberOfSync +"/gen-%1$d-components", numberOfFiles);
			addAndUpdateModel(folderPath, model, attrToBehaviour, numberOfSync);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * One different value from the one in the models
	 */
	
	public  void oneDifferentValue(int numberOfFiles, NumberOfSyncs numberOfSync) {
		Map<String, String> attrToBehaviour = new HashMap<>();

		switch (numberOfSync) {			
		case OneSync:
			attrToBehaviour.put("name", Instant.now().toString());		
			break;
		case TwoSyncs:
			attrToBehaviour.put("name",Instant.now().toString());
//			attrToBehaviour.put("htmlBehaviour", Instant.now().toString());
			break;
		case ThreeSyncs:
			attrToBehaviour.put("name", Instant.now().toString());
//			attrToBehaviour.put("htmlBehaviour", Instant.now().toString());
//			attrToBehaviour.put("authorName", Instant.now().toString());
			break;
		default:
			break;
		}
		testValues(attrToBehaviour, numberOfFiles, numberOfSync);
	}
	
	/*
	 * Scenario 1, if we have a 10 files
	 */

	@Test
	public void test10Files() throws IOException {
		doTestNTimes(1, 10);
	}
	
	@Test
	public void test100Files() throws IOException {
		doTestNTimes(1, 100);
	}
	
	@Test
	public void test1000Files() throws IOException {
		doTestNTimes(1, 1000);
	}
	
	@Test
	public void test10000Files() throws IOException {
		doTestNTimes(1, 10000);
	}
}























































////---------------------------------------------- Start of testing run method
//// Very important point
//public IEolModule createModule() {
//try {
//	EglFileGeneratingTemplateFactory templateFactory = new EglFileGeneratingTemplateFactory();
//	// 10/2/2022..the below works and run the transformation in other workspace
//	templateFactory.setOutputRoot("/Users/sultanalmutairi/Documents/Workspaces/EpsilonPluginsAndEglSync25/10/runtime-New_configuration/A_SyncRegions_Approach/Scalability/OneSync/");
//
//	return new EgxModule(templateFactory);
//} catch (Exception ex) {
//	throw new RuntimeException(ex);
//}
//}
//
//@Test
//public void runTheGenerator() {
//	// 10/2/2022..the below works and run the transformation in other workspace
//	File originalFile = new File("/Users/sultanalmutairi/Documents/Workspaces/EpsilonPluginsAndEglSync25/10/runtime-New_configuration/A_SyncRegions_Approach/Scalability/OneSync/Models/OneSync-comps-html-200-comps.model");
//	model = new EmfModel();
//	model.setName("M");
//	// 10/2/2022..the below works and run the transformation in other workspace
//	model.setMetamodelFile("/Users/sultanalmutairi/Documents/Workspaces/EpsilonPluginsAndEglSync25/10/runtime-New_configuration/A_SyncRegions_Approach/Scalability/comps.ecore");
//	model.setModelFile(originalFile.getAbsolutePath());
//	model.setReadOnLoad(true);
//	try {
//		model.load();
//	} catch (EolModelLoadingException e2) {
//		e2.printStackTrace();
//	}
//	
//	IEolModule module = createModule(); // The createModule() method follows
//	module.getContext().getModelRepository().addModel(model); // The model parameter is the EmfModel you already
//	try {
//		// 10/2/2022..the below works and run the transformation in other workspace
////		module.parse(new File ("/Users/sultanalmutairi/Documents/Workspaces/EpsilonPluginsAndEglSync25/10/runtime-New_configuration/A_SyncRegions_Approach/Scalability/OneSync/OneSync-200-Html-Files.egx"));
//
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
//	try {
//		module.execute();
//	} catch (EolRuntimeException e) {
//		e.printStackTrace();
//	}
//}
//
////---------------------------------------------- End of testing run method
//
