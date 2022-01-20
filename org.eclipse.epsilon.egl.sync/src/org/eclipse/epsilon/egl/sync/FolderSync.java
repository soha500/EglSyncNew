package org.eclipse.epsilon.egl.sync;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.introspection.IPropertyGetter;
import org.eclipse.epsilon.eol.execute.introspection.IPropertySetter;
import org.eclipse.epsilon.eol.models.IModel;

import org.eclipse.epsilon.egl.output.OutputBuffer;
import org.eclipse.epsilon.egl.sync.diff_match_patch.Diff;
import org.eclipse.epsilon.egl.sync.diff_match_patch.Operation;

import com.sun.glass.ui.CommonDialogs.Type;

public class FolderSync {
	
	public Map<String, String> getFolderFileNamesAndContents(String folder) {
		Path folderPath = Paths.get(folder);
		List<String> fileNames = new ArrayList<>();
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folderPath)) {
			for (Path path : directoryStream) {
				fileNames.add(path.toString());
			}
		} catch (IOException ex) {
			System.err.println("Error reading files");
		}

		Map<String, String> namesAndContents = new TreeMap<String, String>();

		for (String file : fileNames) {
			try {
				String content = new String(Files.readAllBytes(Paths.get(file)));
				namesAndContents.put(file, content);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return namesAndContents;
	}

	public boolean checkFolderForChanges2(String folder) {
		Map<String, String> namesAndContents = getFolderFileNamesAndContents(folder);
		boolean hasChanges = false;
		for (String fileName : namesAndContents.keySet()) //
			hasChanges |= areChangesOutsideRegions2(namesAndContents.get(fileName));
		return hasChanges;
	}

	// To check if any change happened outside the regions..
	public static boolean areChangesOutsideRegions2(String fileContent) {
		return false;
//		String hashLine;
//		List<String> lines;
//		// looking for the last two lines and remove them
//		{
//			String[] linesArr = fileContent.split("\n");
//			//The below line was the old and the new is below it 
////			hashLine = linesArr[linesArr.length - 1].substring(2);
//			
//			// Change it today 27/10/2021
//			hashLine = linesArr[linesArr.length - 2];
//			lines = new ArrayList<String>(Arrays.asList(linesArr));
//			// This is the old one 
////			lines.remove(lines.size() - 1); // remove hashes first
////			lines.remove(lines.size() - 1); // remove comment second
////			
//
//			// Here the new one: I tried to remove the last three lines for the hashes, but still does not work.
//			lines.remove(lines.size() - 1); // remove end las comment at the bottom of the file
//			lines.remove(lines.size() - 1); // remove hashes between comments
//			lines.remove(lines.size() - 1); // remove end first comment at the bottom of the file
//		}
//
//		String recreatedContent = String.join("\n", lines); // take all lines without the last two lines
//		String contentWithoutAnyRegions = OutputBuffer.contentWithoutRegions(recreatedContent);
//		String[] linesWithoutRegions = contentWithoutAnyRegions.split("\n");
//		String oldHashDoc = makeHashDoc(hashLine);
//		String newHashDoc = makeHashDoc(OutputBuffer.makeHashLine(recreatedContent));
//		List<Diff> diffs = lineDiffs(oldHashDoc, newHashDoc);
//		for (Diff d : diffs)
//			d.text = d.text.replace("\n", ""); // remove new line
//		boolean hasChanged = false;
//		// To track the line number..
//		int l = 0; // Line index for lines without regions
//		int o = 0; // Offset 
//		boolean isChange = false; // keep track if there is change rather deletion or insertion
//		for (int d = 0; d < diffs.size(); ++d) {
//			Diff diff = diffs.get(d);
//			int dLines = diff.text.length() / 3;
//			if (dLines == 0)
//				continue;
//
//			if (diff.operation == Operation.DELETE)
//				if (d + 1 < diffs.size() && diffs.get(d + 1).operation == Operation.INSERT) {
//					int delOffset = calculateRegionLength(lines, dLines, o + l);
//					int insOffset = calculateRegionLength(lines, 1, o + l);
//					isChange = l + o + delOffset == l + o + insOffset;
//					if (isChange) {
//						o += delOffset;
//						continue;
//					}
//				}
//
//			switch (diff.operation) {
//			case DELETE:
//				o += calculateRegionLength(lines, dLines, o + l);
//				if (diff.text == OutputBuffer.makeHashLine("(a protected region)")) break; // Be silent if it was a deletion of our marker 
//				System.out.println(dLines + " DELETION" + (dLines > 1 ? "S starting" : "") + " on line " + (l + o + 1) + "."); 
//				hasChanged = true;
//				break;
//			case EQUAL:
//				o += calculateRegionLength(lines, dLines, o + l);
//				l += dLines;
//				break;
//				// the problem is here: it count a new insertion why!!!!
//			case INSERT:
//				o += calculateRegionLength(lines, 1, o + l);
//				System.out.print(dLines + (isChange ? " CHANGE" : " INSERTION"));
//				isChange = false;
//				System.out.println((dLines > 1 ? "S starting" : "") + " on line " + (l + o + 1) + ":");
//				for (int i = 0; i < diff.text.length() / 3; ++i) {
//					System.out.println(" " + (l + o + 1) + ": " + linesWithoutRegions[l]);
//					++l;
//					o += calculateRegionLength(lines, 1, o + l);
//				}
//				hasChanged = true;
//				break;
//			}
//		}
//		return hasChanged;
	}

	// Take it from https://github.com/google/diff-match-patch/wiki/Line-or-Word-Diffs
	public static List<Diff> lineDiffs (String text1, String text2) {
		diff_match_patch dmp = new diff_match_patch();
		// diff_linesToChars() -- each line is represented by a single Unicode character
		diff_match_patch.LinesToCharsResult a = dmp.diff_linesToChars(text1, text2); //Split two texts into a list of strings
		String lineText1 = a.chars1;
		String lineText2 = a.chars2;
		List<String> lineArray = a.lineArray;
		/*
		 * - Find the differences between two texts.
		 * - By comparing character by character but each character is the whole lines
		 */
		List<Diff> diffs = dmp.diff_main(lineText1, lineText2, false); 
		// diff_charsToLines() -- to replace the Unicode characters with the original lines.
		dmp.diff_charsToLines(diffs, lineArray); 

		return diffs;
	}
	
	public static int calculateRegionLength (List<String> lines, int numCheck, int startFrom) {
		int o = 0;
		boolean inRegion = false;
		int numChecked = 0;
		for (int i = startFrom; i < lines.size() && numChecked < numCheck; ++i) {
			if (!inRegion && OutputBuffer.isRegionStart(lines.get(i))) {
                inRegion = true;
			    ++i;
			    //++o; //I used to do this but now a region has a length of one= (a protected region)
		    }
			if (inRegion) {
				++o;
				inRegion = !OutputBuffer.isRegionEnd(lines.get(i));
			} else
				++numChecked;
		}
		return o;
	}
	
	public static String makeHashDoc (String hashLine) {
		return String.join("\n", hashLine.split("(?<=\\G...)"));
	}
    // -------------------------- Until here
	
	public List<Synchronization> getAllTheSyncsRegionsOfTheFolder(String folder) {
		// create data structure for all files's names and contents in the folder
		Map<String, String> namesAndContents = getFolderFileNamesAndContents(folder);
		List<Synchronization> allTheSyncRegionsInTheFolder = new ArrayList<Synchronization>();

		for (String file : namesAndContents.keySet()) {
			FileSync fileSync = new FileSync(file);
			List<Synchronization> syncRegionsOfThisFile = fileSync.getAllTheSyncRegionsOfTheFile();
			if (syncRegionsOfThisFile == null) {
				return null;
			}
			allTheSyncRegionsInTheFolder.addAll(syncRegionsOfThisFile);
		}
		return allTheSyncRegionsInTheFolder;
	}

	public void checkSyncs(IModel model, List<Synchronization> allTheSyncsRegionOfTheFolder) {
		// create a data structure
		Map<String, Set<String>> map = new HashMap<String, Set<String>>();
		IPropertyGetter propertyGetter = model.getPropertyGetter();

		for (Synchronization sync : allTheSyncsRegionOfTheFolder) {

			if (model.getElementById(sync.getId()) == null) {
				System.err.println("Sorry! There's no respictive id in the model: " + sync.getId());
				System.exit(0);
			}
			String valueOfAttributeInSyncRegion = (String) sync.getContent();
			// new array without duplicated values
			Set<String> valuesInSyncRegionWithoutDuplactie = new HashSet<>();

			// Concatenation Id and attribute in model to have one key
			String key = sync.getId() + "." + sync.getAttribute();

			valuesInSyncRegionWithoutDuplactie.add(valueOfAttributeInSyncRegion);

			if (map.containsKey(key))
				map.get(key).add(valueOfAttributeInSyncRegion);
			else
				map.put(key, valuesInSyncRegionWithoutDuplactie);
		}

		for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
			String key = entry.getKey();
			String[] id_attr = (String[]) key.split("\\.");
			String id = id_attr[0];
			String attribute = id_attr[1];

			ArrayList<String> values = new ArrayList<String>(entry.getValue());

			int type = 0;

			Object modelElement = model.getElementById(id);
			Object valueOfAttributeInTheModel = null;
			try {
				Object property = propertyGetter.invoke(modelElement, attribute);
				String str = propertyGetter.invoke(modelElement, attribute).toString();
				if (property instanceof String) {
					valueOfAttributeInTheModel = str;
					type = 0;
				} else if (property instanceof Integer) {
					valueOfAttributeInTheModel = Integer.parseInt(str);
					type = 1;
				} else if (property instanceof Double) {
					valueOfAttributeInTheModel = Double.parseDouble(str);
					type = 2;
				} else if (property instanceof Float) {
					valueOfAttributeInTheModel = Float.parseFloat(str);
					type = 3;
				} else if (property instanceof Boolean) {
					valueOfAttributeInTheModel = Boolean.parseBoolean(str);
					type = 4;
				} else if (property instanceof Long) {
					valueOfAttributeInTheModel = Long.parseLong(str);
					type = 5;
				} else if (property instanceof Short) {
					valueOfAttributeInTheModel = Short.parseShort(str);
					type = 6;
				} else if (property instanceof Byte) {
					valueOfAttributeInTheModel = Byte.parseByte(str);
					type = 7;
				} else {
					System.err.println("Other wrong type");
				}

				// First condition
				if (values.size() == 1) {
					// Case 1-a:
					if ((valueOfAttributeInTheModel.toString()).equals(values.get(0))) {
						System.out.println("size 1, same value in the model: " + valueOfAttributeInTheModel);
					// Case 1-a:
					} else {
						System.out.println("size 1, but differnt value from the one that in the model: " + valueOfAttributeInTheModel);

						DataTypes.getModelValue(model, id, attribute, type, values, 0);

					}
					// Second condition
				} else if (values.size() == 2) {
					// Case 2-a:
					if ((valueOfAttributeInTheModel.toString()).equals(values.get(0)) && !(valueOfAttributeInTheModel.toString()).equals(values.get(1))) {
						System.out.println("Size 2, two different values but one of them same the one that in the model: " + valueOfAttributeInTheModel);

						DataTypes.getModelValue(model, id, attribute, type, values, 1);

					// Case 2-b:
					} else if ((valueOfAttributeInTheModel.toString()).equals(values.get(1)) && !(valueOfAttributeInTheModel.toString()).equals(values.get(0))) {
						System.out.println("Size 2, two different values but one of them same the one that in the model : " + valueOfAttributeInTheModel);

						DataTypes.getModelValue(model, id, attribute, type, values, 0);
					// Case 3:
					} else {
						System.err.println("Size 2, two different values from the one in the model.");
						System.exit(0);
					}

				} else {
					System.err.println("Sorry! two or more different values from the one in the model.");
					System.exit(0);
				}
			} catch (EolRuntimeException e1) {
				e1.printStackTrace();
			}
		}
	}

	// -------------------------------------- check model method
	public String checkModelAgainstEachSyncRegion(IModel model, List<Synchronization> allTheSyncsRegionsOfTheFolder) {

		Map<String, Set<String>> map = new HashMap<String, Set<String>>();
		DataTypes dt = new DataTypes();
		IPropertyGetter propertyGetter = model.getPropertyGetter();

		for (Synchronization sync : allTheSyncsRegionsOfTheFolder) {
			String valueOfAttributeInTheModel;
			// a.The respective element is found
			if ((model.getElementById(sync.id) != null)) {

//				System.out.println("The respective element is found");
				// b. The value of the region is checked against the type of the respective property
				try {
					valueOfAttributeInTheModel = propertyGetter
							.invoke(model.getElementById(sync.id), sync.getAttribute()).toString();
				} catch (EolRuntimeException e1) {
					System.err.println("Sorry! There's no respictive attribute in the model: " + sync.getAttribute());
					return "The respective attribute is not found";
				}
				// for all types
				if (dt.isCompatibale(sync.content, valueOfAttributeInTheModel)) {
//					System.out.println("type is compatible");
				} else {
					System.err.println(" Sorry! The value's types are not compatible ");
					return "Incompatible type";
				}
			} else {
				System.err.println("The respective element not found");
				return "The respictive element not found";
			}
		}
		return "finish";
	}

	public String updateTheModel(IModel model, List<Synchronization> allTheSyncsRegionOfTheFolder) {

		String stepCheck = "finish";
		stepCheck = checkModelAgainstEachSyncRegion(model, allTheSyncsRegionOfTheFolder);
		if (!stepCheck.equals("finish"))
			return stepCheck;

		checkSyncs(model, allTheSyncsRegionOfTheFolder);
			System.out.println("All sync regions are without conflicts or errors. Thus, model has been updated.");
		return stepCheck;
	}

	public String getSynchronization(String folder, IModel model) {
		if (checkFolderForChanges2(folder)) {
			System.err.println("\n Sorry, the content of at least one file has been changed or modified!!");
			System.exit(0);
			return "";
		}

		List<Synchronization> allTheSyncRegionsInTheFolder = new ArrayList<Synchronization>();

		allTheSyncRegionsInTheFolder = getAllTheSyncsRegionsOfTheFolder(folder);

		if (allTheSyncRegionsInTheFolder == null)
			return "Misformated or incompleted";
		String result = updateTheModel(model, allTheSyncRegionsInTheFolder);
		return result;
		
	}

}




/*
 * 
 * with old code the tests run fine
 * 
 * 
 */

//package org.eclipse.epsilon.egl.sync;
//
//import java.io.IOException;
//import java.lang.reflect.Array;
//import java.nio.file.DirectoryStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.TreeMap;
//
//import org.eclipse.epsilon.emc.emf.EmfModel;
//import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
//import org.eclipse.epsilon.eol.execute.introspection.IPropertyGetter;
//import org.eclipse.epsilon.eol.execute.introspection.IPropertySetter;
//import org.eclipse.epsilon.eol.models.IModel;
//
//import com.sun.glass.ui.CommonDialogs.Type;
//
//public class FolderSync {
//
//	public List<Synchronization> getAllTheSyncsRegionsOfTheFolder(String folder) {
//
//		Path folderPath = Paths.get(folder);
//		List<String> fileNames = new ArrayList<>();
//
//		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folderPath)) {
//			for (Path path : directoryStream) {
//				fileNames.add(path.toString());
//			}
//		} catch (IOException ex) {
//			System.err.println("Error reading files");
//		}
//
//		// create data structure for all files's names and contents in the folder
//		Map<String, List<String>> namesAndContents = new TreeMap<String, List<String>>();
//
//		List<Synchronization> allTheSyncRegionsInTheFolder = new ArrayList<Synchronization>();
//
//		for (String file : fileNames) {
//
//			try {
//				List<String> content = Files.readAllLines(Paths.get(file));
//
//				namesAndContents.put(file, content);
//				FileSync fileSync = new FileSync(file);
//				List<Synchronization> syncRegionsOfThisFile = fileSync.getAllTheSyncRegionsOfTheFile();
//				if (syncRegionsOfThisFile == null) {
//					return null;
//				}
//				allTheSyncRegionsInTheFolder.addAll(syncRegionsOfThisFile);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//
//		}
//		return allTheSyncRegionsInTheFolder;
//
//	}
//
//	public void checkSyncs(IModel model, List<Synchronization> allTheSyncsRegionOfTheFolder) {
//		// create a data structure
//		Map<String, Set<String>> map = new HashMap<String, Set<String>>();
//		IPropertyGetter propertyGetter = model.getPropertyGetter();
//
//		for (Synchronization sync : allTheSyncsRegionOfTheFolder) {
//
//			if (model.getElementById(sync.getId()) == null) {
//				System.err.println("Sorry! There's no respictive id in the model: " + sync.getId());
//				System.exit(0);
//			}
//			String valueOfAttributeInSyncRegion = (String) sync.getContent();
//			// new array without duplicated values
//			Set<String> valuesInSyncRegionWithoutDuplactie = new HashSet<>();
//
//			// Concatenation Id and attribute in model to have one key
//			String key = sync.getId() + "." + sync.getAttribute();
//
//			valuesInSyncRegionWithoutDuplactie.add(valueOfAttributeInSyncRegion);
//
//			if (map.containsKey(key))
//				map.get(key).add(valueOfAttributeInSyncRegion);
//			else
//				map.put(key, valuesInSyncRegionWithoutDuplactie);
//		}
//
//		for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
//			String key = entry.getKey();
//			String[] id_attr = (String[]) key.split("\\.");
//			String id = id_attr[0];
//			String attribute = id_attr[1];
//
//			ArrayList<String> values = new ArrayList<String>(entry.getValue());
//
//			int type = 0;
//
//			Object modelElement = model.getElementById(id);
//			Object valueOfAttributeInTheModel = null;
//			try {
//				Object property = propertyGetter.invoke(modelElement, attribute);
//				String str = propertyGetter.invoke(modelElement, attribute).toString();
//				if (property instanceof String) {
//					valueOfAttributeInTheModel = str;
//					type = 0;
//				} else if (property instanceof Integer) {
//					valueOfAttributeInTheModel = Integer.parseInt(str);
//					type = 1;
//				} else if (property instanceof Double) {
//					valueOfAttributeInTheModel = Double.parseDouble(str);
//					type = 2;
//				} else if (property instanceof Float) {
//					valueOfAttributeInTheModel = Float.parseFloat(str);
//					type = 3;
//				} else if (property instanceof Boolean) {
//					valueOfAttributeInTheModel = Boolean.parseBoolean(str);
//					type = 4;
//				} else if (property instanceof Long) {
//					valueOfAttributeInTheModel = Long.parseLong(str);
//					type = 5;
//				} else if (property instanceof Short) {
//					valueOfAttributeInTheModel = Short.parseShort(str);
//					type = 6;
//				} else if (property instanceof Byte) {
//					valueOfAttributeInTheModel = Byte.parseByte(str);
//					type = 7;
//				} else {
//					System.err.println("Other wrong type");
//				}
//
//				// First condition
//				if (values.size() == 1) {
//					// Case 1-a:
//					if ((valueOfAttributeInTheModel.toString()).equals(values.get(0))) {
//						System.out.println("size 1, same value in the model: " + valueOfAttributeInTheModel);
//					// Case 1-a:
//					} else {
//						System.out.println("size 1, but differnt value from the one that in the model: " + valueOfAttributeInTheModel);
//
//						DataTypes.getModelValue(model, id, attribute, type, values, 0);
//
//					}
//					// Second condition
//				} else if (values.size() == 2) {
//					// Case 2-a:
//					if ((valueOfAttributeInTheModel.toString()).equals(values.get(0)) && !(valueOfAttributeInTheModel.toString()).equals(values.get(1))) {
//						System.out.println("Size 2, two different values but one of them same the one that in the model: " + valueOfAttributeInTheModel);
//
//						DataTypes.getModelValue(model, id, attribute, type, values, 1);
//
//					// Case 2-b:
//					} else if ((valueOfAttributeInTheModel.toString()).equals(values.get(1)) && !(valueOfAttributeInTheModel.toString()).equals(values.get(0))) {
//						System.out.println("Size 2, two different values but one of them same the one that in the model : " + valueOfAttributeInTheModel);
//
//						DataTypes.getModelValue(model, id, attribute, type, values, 0);
//					// Case 3:
//					} else {
//						System.err.println("Size 2, two different values from the one in the model.");
//						System.exit(0);
//					}
//
//				} else {
//					System.err.println("Sorry! two or more different values from the one in the model.");
//					System.exit(0);
//				}
//			} catch (EolRuntimeException e1) {
//				e1.printStackTrace();
//			}
//		}
//	}
//
//	// -------------------------------------- check model method
//	public String checkModelAgainstEachSyncRegion(IModel model, List<Synchronization> allTheSyncsRegionsOfTheFolder) {
//
//		Map<String, Set<String>> map = new HashMap<String, Set<String>>();
//		DataTypes dt = new DataTypes();
//		IPropertyGetter propertyGetter = model.getPropertyGetter();
//
//		for (Synchronization sync : allTheSyncsRegionsOfTheFolder) {
//
//			String valueOfAttributeInTheModel;
//
//			// a.The respective element is found
//			if ((model.getElementById(sync.id) != null)) {
//
//				System.out.println("The respective element is found");
//				// b. The value of the region is checked against the type of the respective
//				// property
//				try {
//					valueOfAttributeInTheModel = propertyGetter
//							.invoke(model.getElementById(sync.id), sync.getAttribute()).toString();
//				} catch (EolRuntimeException e1) {
//					System.err.println("Sorry! There's no respictive attribute in the model: " + sync.getAttribute());
//					return "The respective attribute is not found";
//				}
//				// for all types
//				if (dt.isCompatibale(sync.content, valueOfAttributeInTheModel)) {
//					System.out.println("type is compatible");
//				} else {
//					System.err.println(" Sorry! The value's types are not compatible ");
//					return "Incompatible type";
//				}
//			} else {
//				System.err.println("The respective element not found");
//				return "The respictive element not found";
//			}
//		}
//		return "finish";
//	}
//
//	public String updateTheModel(IModel model, List<Synchronization> allTheSyncsRegionOfTheFolder) {
//
//		String stepCheck = "finish";
//		stepCheck = checkModelAgainstEachSyncRegion(model, allTheSyncsRegionOfTheFolder);
//		if (!stepCheck.equals("finish"))
//			return stepCheck;
//
//		checkSyncs(model, allTheSyncsRegionOfTheFolder);
//		System.out.println("All sync regions are without conflicts or errors. Thus, model has been updated.");
//		return stepCheck;
//	}
//
//	public String getSynchronization(String folder, IModel model) {
//
//		List<Synchronization> allTheSyncRegionsInTheFolder = new ArrayList<Synchronization>();
//
//		allTheSyncRegionsInTheFolder = getAllTheSyncsRegionsOfTheFolder(folder);
//
//		if (allTheSyncRegionsInTheFolder == null)
//			return "Misformated or incompleted";
//		String result = updateTheModel(model, allTheSyncRegionsInTheFolder);
//		return result;
//	}
//
//}