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
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.execute.introspection.IPropertyGetter;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class ScalablityTests {

//	private static final String FOLDER_PATH1 = System.getProperty("user.dir") + "/boiler-To-Generate-10-Files/boiler-To-Generate-10-Files/";
//	private static final String FOLDER_PATH2 = System.getProperty("user.dir") + "/boiler-To-Generate-100-Files/boiler-To-Generate-100-Files/";
//	private static final String FOLDER_PATH3 = System.getProperty("user.dir") + "/boiler-To-Generate-1000-Files/boiler-To-Generate-1000-Files/";

	EmfModel model;
	FolderSync syncReader;
	EmfModel tempModel;
	static List<String> orginalNewLines;

	public static String regexMatch(String subject, String expression) {
		Pattern p = Pattern.compile(expression); 
		Matcher m = p.matcher(subject.trim());
		if (!m.find()) return null;
		return m.group(1);
	}

	public static void addAndUpdateModel(String folderPath, EmfModel model, Map<String, String> behaviours) throws IOException {
		Map<String, String> idToBehaviour = new HashMap<>();
		File[] files = new File(folderPath).listFiles();
		// check if file is exists
		for (File f : files) {
			if (!f.isFile())
				continue;
			for (String className : behaviours.keySet()) {
				if (!f.getName().contains(className))
					continue;
				String behaviour = behaviours.get(className);

				BufferedReader original = new BufferedReader(new FileReader(f));

				List<String> newLines = new ArrayList<String>();
				String line;
				while ((line = original.readLine()) != null) {
					newLines.add(line);
					String id = regexMatch(line, "//sync (.+?), behaviour");
					if (id != null) {
						idToBehaviour.put(id, behaviour);
						newLines.add(behaviour);
						while (!line.contains("//endSync"))
							line = original.readLine();
						newLines.add(line);
					}
				}
				original.close();
				Files.write(f.toPath(), newLines);
				break;
			}
		}
		
		// Update the model with values taken from the generated file..
		FolderSync folderSync = new FolderSync();
		folderSync.getSynchronization(folderPath, model);

		model.store();
		IPropertyGetter propertyGetter = model.getPropertyGetter();
		for (String id : idToBehaviour.keySet()) {
			Object modelElement = model.getElementById(id);
			try {
				assertEquals("test 1", idToBehaviour.get(id), (String) propertyGetter.invoke(modelElement, "behaviour"));
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

    @After
    public void end() {
        System.out.println("Test " + name.getMethodName() + " took " + (System.currentTimeMillis() - start) + " ms");
    }

    // with one different value
//    String filePathFor100Files = "DataFor100FilesWith1DifferentValue.csv";
//    String filePathFor200Files = "DataFor200FilesWith1DifferentValue.csv";
//    String filePathFor300Files = "DataFor300FilesWith1DifferentValue.csv";
//    String filePathFor400Files = "DataFor400FilesWith1DifferentValue.csv";
//    String filePathFor500Files = "DataFor500FilesWith1DifferentValue.csv";
//    String filePathFor600Files = "DataFor600FilesWith1DifferentValue.csv";
//    String filePathFor700Files = "DataFor700FilesWith1DifferentValue.csv";
//    String filePathFor800Files = "DataFor800FilesWith1DifferentValue.csv";
//    String filePathFor900Files = "DataFor900FilesWith1DifferentValue.csv";
//    String filePathFor1000Files = "DataFor1000FilesWith1DifferentValue.csv";
    
//    String filePathFor2000Files = "DataFor2000FilesWithTheSameValue.csv";
//    String filePathFor4000Files = "DataFor4000FilesWithTheSameValue.csv";
//    String filePathFor6000Files = "DataFor6000FilesWithTheSameValue.csv";
//    String filePathFor8000Files = "DataFor8000FilesWithTheSameValue.csv";
//    String filePathFor10000Files = "DataFor10000FilesWithTheSameValue.csv";
//    String filePathFor12000Files = "DataFor12000FilesWithTheSameValue.csv";
    
//    String filePathFor2000Files = "DataFor2000FilesWith1DifferentValue.csv";
//    String filePathFor4000Files = "DataFor4000FilesWith1DifferentValue.csv";
//    String filePathFor6000Files = "DataFor6000FilesWith1DifferentValue.csv";
//    String filePathFor8000Files = "DataFor8000FilesWith1DifferentValue.csv";
//    String filePathFor10000Files = "DataFor10000FilesWith1DifferentValue.csv";
//    String filePathFor900Files = "DataFor900FilesWith1DifferentValue.csv";
    
    String filePathFor2000Files = "DataFor2000FilesWithHalfDifferentValue.csv";
    String filePathFor4000Files = "DataFor4000FilesWithHalfDifferentValue.csv";
    String filePathFor6000Files = "DataFor6000FilesWithHalfDifferentValue.csv";
    String filePathFor8000Files = "DataFor8000FilesWithHalfDifferentValue.csv";
    String filePathFor10000Files = "DataFor10000FilesWithHalfDifferentValue.csv";
    String filePathFor900Files = "DataFor900FilesWithHalfDifferentValue.csv";

	public static void writeResultsToCSvFile(int stageNumber, long takenTime, long bytesUsed, String filePath) {
		try {
			boolean header = !new File(filePath).exists();
			FileWriter fw = new FileWriter(filePath, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			if (header)
//				pw.println("Stage number, (Totel) Taken time for each run," + Clock.systemDefaultZone().instant());
				pw.println("Stage number, (Totel) Taken time, Bytes used, Outlier, Q1 , Q3, IQR, Upper Value, Lower Value");
			pw.println(String.format("%d,%d,%d", stageNumber, takenTime, bytesUsed));
			pw.flush();
			fw.close();

		} catch (Exception E) {
			System.out.println("There is errors!!");
		}
	}

	public static void doTestNTimes(int numberOfTimes, String filePath, Supplier<Boolean> theTest) throws IOException {
		new File(filePath).delete();
		for (int i = 0; i < numberOfTimes; i++) {
			long start = System.currentTimeMillis();
			Runtime runtime = Runtime.getRuntime();
			runtime.gc();
			theTest.get();
//			writeResultsToCSvFile(i, System.currentTimeMillis() - start, filePath);
            long memory = runtime.totalMemory() - runtime.freeMemory();
			writeResultsToCSvFile(i, System.currentTimeMillis() - start, memory,  filePath);
		}
	}
	
//	@RepeatedTest(100)
//	@Repeat( times = 100 )
//  @Test(invocationCount = 100)
	
	public void testValues (Map<String, String> behaviours, int numberOfFiles) {
		File originalFile = new File(System.getProperty("user.dir") + String.format("/boiler-To-Generate-%1$d-Files/BoilerController-%1$d-Components.model", numberOfFiles));
		model = new EmfModel();
		model.setName("M");
		model.setMetamodelFile(new File(System.getProperty("user.dir") + "/boiler-Ecore/comps.ecore").getAbsolutePath());
		model.setModelFile(originalFile.getAbsolutePath());
		model.setReadOnLoad(true);
		try {
			model.load();
		} catch (EolModelLoadingException e2) {
			e2.printStackTrace();
		}
		
		try {
			String folderPath = String.format(System.getProperty("user.dir") + "/boiler-To-Generate-%1$d-Files/boiler-To-Generate-%1$d-Files/syncregions-%1$dFiles", numberOfFiles);
			addAndUpdateModel(folderPath, model, behaviours);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * One different value
	 */
	
	public boolean oneDifferentValue (int numberOfFiles)  {
		Map<String, String> behaviours = new HashMap<>();
		behaviours.put("BoilerActuator", "Half Change - return temperature - targetTemperature;");
		behaviours.put("TemperatureController", "1-if(temperatureDifference > 0 && boilerStatus == true) { return 1; } else if (temperatureDifference < 0 && boilerStatus == false) { return 2; } else return 0;");
		testValues(behaviours, numberOfFiles);
		return true;
	}
	
//	/*
//	 * Two values, but one same the one in the model
//	 */
//	public boolean twoDifferentValues (int numberOfFiles) {
//		Map<String, String> behaviours = new HashMap<>();
//		behaviours.put("BoilerActuator", "return temperature - targetTemperature;");
//		behaviours.put("BoilerActuator", "return temperature - targetTemperature;");
//		behaviours.put("TemperatureController", "if (temperatureDifference > 0 && boilerStatus == true) { return 1; } else if (temperatureDifference < 0 && boilerStatus == false) { return 2; } else return 0;");
//		testValues(behaviours, numberOfFiles);
//		return true;
//		
//	}
	
	
////@Test
//public void test10Files2() throws IOException {
//	doTestNTimes(100, filePathFor10Files, () -> twoDifferentValues(10));
//}
//	System.out.println("Working Directory = " + System.getProperty("user.dir"));

	// createModule()
	public IEolModule createModule() {
		try {
			EglFileGeneratingTemplateFactory templateFactory = new EglFileGeneratingTemplateFactory();
//			templateFactory.setOutputRoot(System.getProperty("user.dir") + "/SyncTests/GeneratedFilesFromUniversity/");
			// other workspace
			templateFactory.setOutputRoot("/Users/sultanalmutairi/Documents/Workspaces/runtime-EclipseApplication/org.eclipse.epsilon.examples.egl.comps/boiler-To-Generate-100-Files/boiler-To-Generate-100-Files/syncregions-100Files/");
			
			return new EgxModule(templateFactory);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

//	@Test
//	public void runTheGenerator() {
//		
//		File originalFile = new File(System.getProperty("user.dir") + String.format("/boiler-To-Generate-%1$d-Files/BoilerController-%1$d-Components.model", 100));
//		model = new EmfModel();
//		model.setName("M");
//		model.setMetamodelFile(new File(System.getProperty("user.dir") + "/boiler-Ecore/comps.ecore").getAbsolutePath());
//		model.setModelFile(originalFile.getAbsolutePath());
//		model.setReadOnLoad(true);
//		try {
//			model.load();
//		} catch (EolModelLoadingException e2) {
//			e2.printStackTrace();
//		}
//		
//		
//		
//		
//		IEolModule module = createModule(); // The createModule() method follows
//		module.getContext().getModelRepository().addModel(model); // The model parameter is the EmfModel you already
//		try {
//			module.parse(new File ("user.dir") + String.format("/boiler-To-Generate-100-Files/sync-regions100.egx"));
//			// other workspace
//			module.parse(new File ("/Users/sultanalmutairi/git/Epsilon-Source/org.eclipse.epsilon/examples/org.eclipse.epsilon.examples.egl.comps/boiler-To-Generate-100-Files/sync-regions100.egx"));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		try {
//			module.execute();
//		} catch (EolRuntimeException e) {
//			e.printStackTrace();
//		}
//	}

	
	/*
	 * Scenario 1, if we have a 200 files
	 */
	
	@Test
	public void test2000Files() throws IOException {
		doTestNTimes(100, filePathFor2000Files, () -> oneDifferentValue(200));
	}
	
	/*
	 * Scenario 2, if we have a 400 files
	 */
	
	@Test
	public void test4000Files() throws IOException {
		doTestNTimes(100, filePathFor4000Files, () -> oneDifferentValue(400));
	}
	
	/*
	 * Scenario 3, if we have a 600 files
	 */
	
	@Test
	public void test6000Files() throws IOException {
		doTestNTimes(100, filePathFor6000Files, () -> oneDifferentValue(600));
	}

	/*
	 * Scenario 4, if we have a 800 files
	 */
	
	@Test
	public void test8000Files() throws IOException {
		doTestNTimes(100, filePathFor8000Files, () -> oneDifferentValue(800));
	}

	/*
	 * Scenario 5, if we have a 1000 files
	 */
	
	@Test
	public void test10000Files() throws IOException {
		doTestNTimes(100, filePathFor10000Files, () -> oneDifferentValue(1000));
	}

	/*
	 * Scenario 6, if we have a 1000 files
	 */
//	
//	@Test
//	public void test900Files() throws IOException {
//		doTestNTimes(100, filePathFor900Files, () -> oneDifferentValue(900));
//	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
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
		
}










































































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





