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
import org.eclipse.epsilon.egl.sync.SyncGenerelisabiltyTests.Language;
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

public class SyncGenerelisabiltyTests {
	// Todat change... when I have one line added, when I have multline added
	enum TestStrategy {
		OneLineAdded,
		MultipleLinesAdded,
	}
	
	enum TestStatus {
		// When 
		MergeSuccessful,               
		ModelElementNotExists,
		ValueNotCompatable, 
		OneDifferntValue,	
		TwoDifferntValues,
	}
	
	enum Language {
		Java,
		Html,
		Python,
		Ruby,
	}
	
	// I need this as I am updating the model.
//	EmfModel model;
	FolderSync syncReader;
	EmfModel tempModel;
	static List<String> orginalNewLines;

	public static String regexMatch(String subject, String expression) {
		Pattern p = Pattern.compile(expression); 
		Matcher m = p.matcher(subject.trim());
		if (!m.find()) return null;
		return m.group(1);
	}

	public static void addAndUpdateModel(String folderPath, EmfModel model, List<String> behaviours, Language language) throws IOException {
		Map<String, String> idToBehaviour = new HashMap<>();
		File[] files = new File(folderPath).listFiles();
		// check if file is exists
		for (File f : files) {
			if (!f.isFile())
				continue;
			for (String behaviour : behaviours) {
				BufferedReader original = new BufferedReader(new FileReader(f));

				List<String> newLines = new ArrayList<String>();
				String line;
				while ((line = original.readLine()) != null) {
					newLines.add(line); 		
					String id = regexMatch(line, "sync (.+?), ");
					if (id != null) {
						idToBehaviour.put(id, behaviour);
						newLines.add(behaviour);
						while (!line.contains("endSync"))
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
// Maybe I need to have multiple asserts in a single unit test..!! or seprate them in different methods as what i did at the bottom.
					
				switch (language) {
				case Java:
					assertEquals("test 1 java", idToBehaviour.get(id), (String) propertyGetter.invoke(modelElement, "behaviour"));	
					break;
				case Html:
					assertEquals("test 2 html", idToBehaviour.get(id), (String) propertyGetter.invoke(modelElement, "htmlBehaviour"));
					break;
				case Ruby:
					assertEquals("test 4 ruby", idToBehaviour.get(id), (String) propertyGetter.invoke(modelElement, "rubyBehaviour"));
					break;
				case Python:
					assertEquals("test 3 python", idToBehaviour.get(id), (String) propertyGetter.invoke(modelElement, "pythonBehaviour"));
					break;
//				default:
//				break;
				}
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

	static // with one different value
	String resultsFilePath = "DataForSyncGenerlasabiltyTests.csv";
	public static void writeResultsToCSvFile(int stageNumber, long takenTime, long bytesUsed, int numberOfFile, Language language) {
		try {
			boolean header = !new File(resultsFilePath).exists();
			FileWriter fw = new FileWriter(resultsFilePath, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			if (header)
				pw.println("Language, Stage number, (Totel) Taken time, Bytes used, Outlier, Q1 , Q3, IQR, Upper Value, Lower Value");
			pw.println(String.format("%s,%d,%d,%d", language, stageNumber, takenTime, bytesUsed));
			pw.flush();
			fw.close();
		} catch (Exception E) {
			System.out.println("There is errors!!");
		}
	}

	Runtime runtime = Runtime.getRuntime();

	public void doTestNTimes(int numberOfTimes, int numberOfFile) throws IOException {
		for (int i = 0; i < numberOfTimes; i++) {
			for (int j = 0; j < 4; j++) {
				runtime.gc();
				/// any langauge 
				Language language =  j == 0 ? Language.Java : j == 1 ? Language.Html : j == 2 ? Language.Python : Language.Ruby;
				long start = System.currentTimeMillis();
				oneDifferentValue(numberOfFile, language);
				long memory = runtime.totalMemory() - runtime.freeMemory();
				writeResultsToCSvFile(i, System.currentTimeMillis() - start, memory, numberOfFile, language);
			}
		}
	}

	public void testValues (List<String> behaviours, int numberOfFiles, Language language) {
		// Today change 
		File originalFile = new File(String.format("/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Generalisability-Part3/" + language + "/BoilerController-" + language + "-%1$d-Components.model", numberOfFiles));
		
		EmfModel model = new EmfModel();
		model.setName(language.name());
		model.setMetamodelFile("/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Generalisability-Part3/" + language + "/comps.ecore");
		model.setModelFile(originalFile.getAbsolutePath());
		model.setReadOnLoad(true);
		try {
			model.load();
		} catch (EolModelLoadingException e2) {
			e2.printStackTrace();
		}
		
		try {
			// Today change 
			String folderPath = String.format("/Users/sultanalmutairi/git/EglSync 2/org.eclipse.epsilon.egl.sync/SyncTests/Generalisability-Part3/" + language + "/gen-%1$d-components", numberOfFiles);			
			
			// Today change
			addAndUpdateModel(folderPath, model, behaviours, language);
			
//			addAndUpdateModelJava(folderPath, model, behaviours);
//			addAndUpdateModelHtml(folderPath, model, behaviours);
//			addAndUpdateModelPython(folderPath, model, behaviours);
//			addAndUpdateModelRuby(folderPath, model, behaviours);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 *  One different value
	 */
	
// works when we have the same value with the one in the model.	
// works only with the first languaje, when we have one different value from the one in the model, And the rest must have the same value in the model.

	public boolean oneDifferentValue (int numberOfFiles, Language language)  {
		List<String> behaviours = new ArrayList<>(); 	
		// It takes the first langauge and update the model as expected, but with the another languages I need to pass the same value in the model.
		switch (language) {
		case Java:
			behaviours.add("hi3");		
			break;
		case Html:
			behaviours.add("hi3");
			break;
		case Ruby:
			behaviours.add("hi3");
			break;
		case Python:
			behaviours.add("hi23");
			break;

//		default:
//			break;
		}
		testValues(behaviours, numberOfFiles, language);
		return true;
	}

	// when I have the same values from the one in the model the model
	// when I have one different values from the one in the model
	// when I the size of the models are different -- increase by two components until 10 (now is only two)
	/*
	 * Scenario 1, if we have a 200 files
	 */
	
	@Test
	public void testOneDifferentValue() throws IOException {
		doTestNTimes(1, 200);
	}

	/*
	 * Scenario 1, if we have a 200 files
	 */
	
//	@Test
	public void testSimilarValue() throws IOException {
		doTestNTimes(2, 200);
	}
	
	
	/*
	 * Scenario 1, similar values
	 */
//	@Test
	public void testSimilarValue2Components() throws IOException {
		doTestNTimes(2, 200);
	}
//	@Test
	public void testSimilarValue4Components() throws IOException {
		doTestNTimes(2, 200);
	}
//	@Test
	public void testSimilarValue6Components() throws IOException {
		doTestNTimes(2, 200);
	}
//	@Test
	public void testSimilarValue8Components() throws IOException {
		doTestNTimes(2, 200);
	}
//	@Test
	public void testSimilarValue10Components() throws IOException {
		doTestNTimes(2, 200);
	}
	
	/*
	 * Scenario 2, different valuse
	 */
	
//	@Test
	public void testDifferentValue2Components() throws IOException {
		doTestNTimes(2, 200);
	}
//	@Test
	public void testDifferentValue4Components() throws IOException {
		doTestNTimes(2, 200);
	}
//	@Test
	public void testDifferentValue6Components() throws IOException {
		doTestNTimes(2, 200);
	}
//	@Test
	public void testDifferentValue8Components() throws IOException {
		doTestNTimes(2, 200);
	}
//	@Test
	public void testDifferentValue10Components() throws IOException {
		doTestNTimes(2, 200);
	}
	
	
	/*
	 * Maybe Scenario 2, different valuse with multiple lines not only one.
	 */
	
}	
	

























































































































	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	@Test
//	public void testOneDiffernetValue() throws IOException {
//		doTestNTimes(2, 200);
//	}
	
//	@Test
//	public void testDiffernetValue() throws IOException {
//		doTestNTimes(2, 200);
//	}
	
	/*
	 * Scenario 2, if we have a 400 files
	 */
	
//	@Test
//	public void test400Files() throws IOException {
//		doTestNTimes(10, filePathFor400Files, () -> oneDifferentValue(400));
//	}
//	
//	/*
//	 * Scenario 3, if we have a 600 files
//	 */
//	
//	@Test
//	public void test600Files() throws IOException {
//		doTestNTimes(10, filePathFor600Files, () -> oneDifferentValue(600));
//	}
//
//	/*
//	 * Scenario 4, if we have a 800 files
//	 */
//	
//	@Test
//	public void test800Files() throws IOException {
//		doTestNTimes(10, filePathFor800Files, () -> oneDifferentValue(800));
//	}
//
//	/*
//	 * Scenario 5, if we have a 1000 files
//	 */
//	
//	@Test
//	public void test1000Files() throws IOException {
//		doTestNTimes(10, filePathFor1000Files, () -> oneDifferentValue(1000));
//	}
	
//	public static void addAndUpdateModelJava(String folderPath, EmfModel model, List<String> behaviours) throws IOException {
//		Map<String, String> idToBehaviour = new HashMap<>();
//		File[] files = new File(folderPath).listFiles();
//		// check if file is exists
//		for (File f : files) {
//			if (!f.isFile())
//				continue;
//			for (String behaviour : behaviours) {
//				BufferedReader original = new BufferedReader(new FileReader(f));
//
//				List<String> newLines = new ArrayList<String>();
//				String line;
//				while ((line = original.readLine()) != null) {
//					newLines.add(line);
//					
//					// Today change.. for html, but I want to make it general for other language comment like Python 
//					String id = regexMatch(line, "sync (.+?), behaviour");
//					if (id != null) {
//						idToBehaviour.put(id, behaviour);
//						newLines.add(behaviour);
//						while (!line.contains("endSync"))
//							line = original.readLine();
//						newLines.add(line);
//					}
//				}
//				original.close();
//				Files.write(f.toPath(), newLines);
//				break;
//			}
//		}
//		
//		// Update the model with values taken from the generated file..
//		FolderSync folderSync = new FolderSync();
//		folderSync.getSynchronization(folderPath, model);
//
//		model.store();
//		IPropertyGetter propertyGetter = model.getPropertyGetter();
//		for (String id : idToBehaviour.keySet()) {
//			Object modelElement = model.getElementById(id);
//			try {
//				assertEquals("test 1 java", idToBehaviour.get(id), (String) propertyGetter.invoke(modelElement, "behaviour"));
//			} catch (EolRuntimeException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//	public static void addAndUpdateModelHtml(String folderPath, EmfModel model, List<String> behaviours) throws IOException {
//		Map<String, String> idToBehaviour = new HashMap<>();
//		File[] files = new File(folderPath).listFiles();
//		// check if file is exists
//		for (File f : files) {
//			if (!f.isFile())
//				continue;
//			for (String behaviour : behaviours) {
//				BufferedReader original = new BufferedReader(new FileReader(f));
//
//				List<String> newLines = new ArrayList<String>();
//				String line;
//				while ((line = original.readLine()) != null) {
//					newLines.add(line);
//					
//					// Today change.. for html, but I want to make it general for other language comment like Python 
//					String id = regexMatch(line, "sync (.+?), HtmlBehaviour");
//					if (id != null) {
//						idToBehaviour.put(id, behaviour);
//						newLines.add(behaviour);
//						while (!line.contains("endSync"))
//							line = original.readLine();
//						newLines.add(line);
//					}
//				}
//				original.close();
//				Files.write(f.toPath(), newLines);
//				break;
//			}
//		}
//		
//		// Update the model with values taken from the generated file..
//		FolderSync folderSync = new FolderSync();
//		folderSync.getSynchronization(folderPath, model);
//
//		model.store();
//		IPropertyGetter propertyGetter = model.getPropertyGetter();
//		for (String id : idToBehaviour.keySet()) {
//			Object modelElement = model.getElementById(id);
//			try {
//				assertEquals("test 2 html", idToBehaviour.get(id), (String) propertyGetter.invoke(modelElement, "HtmlBehaviour"));
//			} catch (EolRuntimeException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//	
//	public static void addAndUpdateModelPython(String folderPath, EmfModel model, List<String> behaviours) throws IOException {
//		Map<String, String> idToBehaviour = new HashMap<>();
//		File[] files = new File(folderPath).listFiles();
//		// check if file is exists
//		for (File f : files) {
//			if (!f.isFile())
//				continue;
//			for (String behaviour : behaviours) {
//				BufferedReader original = new BufferedReader(new FileReader(f));
//
//				List<String> newLines = new ArrayList<String>();
//				String line;
//				while ((line = original.readLine()) != null) {
//					newLines.add(line);
//					
//					// Today change.. for html, but I want to make it general for other language comment like Python 
//					String id = regexMatch(line, "sync (.+?), PythonBehaviour");
//					if (id != null) {
//						idToBehaviour.put(id, behaviour);
//						newLines.add(behaviour);
//						while (!line.contains("endSync"))
//							line = original.readLine();
//						newLines.add(line);
//					}
//				}
//				original.close();
//				Files.write(f.toPath(), newLines);
//				break;
//			}
//		}
//		
//		// Update the model with values taken from the generated file..
//		FolderSync folderSync = new FolderSync();
//		folderSync.getSynchronization(folderPath, model);
//
//		model.store();
//		IPropertyGetter propertyGetter = model.getPropertyGetter();
//		for (String id : idToBehaviour.keySet()) {
//			Object modelElement = model.getElementById(id);
//			try {
//				assertEquals("test 3 python", idToBehaviour.get(id), (String) propertyGetter.invoke(modelElement, "PythonBehaviour"));
//			} catch (EolRuntimeException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//	public static void addAndUpdateModelRuby(String folderPath, EmfModel model, List<String> behaviours) throws IOException {
//		Map<String, String> idToBehaviour = new HashMap<>();
//		File[] files = new File(folderPath).listFiles();
//		// check if file is exists
//		for (File f : files) {
//			if (!f.isFile())
//				continue;
//			for (String behaviour : behaviours) {
//				BufferedReader original = new BufferedReader(new FileReader(f));
//
//				List<String> newLines = new ArrayList<String>();
//				String line;
//				while ((line = original.readLine()) != null) {
//					newLines.add(line);
//					
//					// Today change.. for html, but I want to make it general for other language comment like Python 
//					String id = regexMatch(line, "sync (.+?), RubyBehaviour");
//					if (id != null) {
//						idToBehaviour.put(id, behaviour);
//						newLines.add(behaviour);
//						while (!line.contains("endSync"))
//							line = original.readLine();
//						newLines.add(line);
//					}
//				}
//				original.close();
//				Files.write(f.toPath(), newLines);
//				break;
//			}
//		}
//		
//		// Update the model with values taken from the generated file..
//		FolderSync folderSync = new FolderSync();
//		folderSync.getSynchronization(folderPath, model);
//
//		model.store();
//		IPropertyGetter propertyGetter = model.getPropertyGetter();
//		for (String id : idToBehaviour.keySet()) {
//			Object modelElement = model.getElementById(id);
//			try {
//				assertEquals("test 4 ruby", idToBehaviour.get(id), (String) propertyGetter.invoke(modelElement, "RubyBehaviour"));
//			} catch (EolRuntimeException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//	
//}

































//// Tody changed.. for Java, html, Python and Ruby comments..
//String id = regexMatch(line, "//sync (.+?), name");
//if (id != null) {
//	idToBehaviour.put(id, behaviour);
//	newLines.add(behaviour);
//	// Today change.... to below one
//	// while (!line.contains("//endSync"))
//	while (!line.contains("//endSync"))
//		line = original.readLine();
//	newLines.add(line);
//}
//String id = regexMatch(line, "<!--sync (.+?), name  -->");
//if (id != null) {
//	idToBehaviour.put(id, behaviour);
//	newLines.add(behaviour);
//	while (!line.contains("<!--endSync -->"))
//		line = original.readLine();
//	newLines.add(line);
//}
//String id = regexMatch(line, "#sync (.+?), pythonBehaviour");
//if (id != null) {
//	idToBehaviour.put(id, behaviour);
//	newLines.add(behaviour);
//	while (!line.contains("#endSync"))
//		line = original.readLine();
//	newLines.add(line);
//}
//
//// Tody changed.. for Ruby
//String id = regexMatch(line, "#sync (.+?), RubyBehaviour");
//if (id != null) {
//	idToBehaviour.put(id, behaviour);
//	newLines.add(behaviour);
//	while (!line.contains("#endSync"))
//		line = original.readLine();
//	newLines.add(line);
//}









//  VERY IMPORTANT METHOD..
//// createModule() This method was before the @Teat method, I will return to this later and make sure it run the transformation automatically 
//public IEolModule createModule() {
//	try {
//		EglFileGeneratingTemplateFactory templateFactory = new EglFileGeneratingTemplateFactory();
////		templateFactory.setOutputRoot(System.getProperty("user.dir") + "/SyncTests/GeneratedFilesFromUniversity/");
//		// other workspace
//		templateFactory.setOutputRoot("/Users/sultanalmutairi/Documents/Workspaces/runtime-EclipseApplication/org.eclipse.epsilon.examples.egl.comps/boiler-To-Generate-100-Files/boiler-To-Generate-100-Files/syncregions-100Files/");
//		
//		return new EgxModule(templateFactory);
//	} catch (Exception ex) {
//		throw new RuntimeException(ex);
//	}
//}