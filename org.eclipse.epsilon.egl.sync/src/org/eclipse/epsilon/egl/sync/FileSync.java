/*
 *  This is the old vergion of the FileSync and was working. before I made change to the regex, remove the last three lines NOT two
 */

package org.eclipse.epsilon.egl.sync;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.epsilon.eol.models.IModel;
//
public class FileSync {

	// all fields
	FileInputStream fIn;
	public BufferedReader bRead;
	public IModel model;

	// fileName Ruby looking for the last line...
	public FileSync(String fileName) {
		try {
			fIn = new FileInputStream(fileName);
			bRead = new BufferedReader(new InputStreamReader(fIn));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	
	public void reusefile() {
		try {
			fIn.getChannel().position(0);
			bRead = new BufferedReader(new InputStreamReader(fIn));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// Find all the sync regions
	public ArrayList<Synchronization> getAllTheSyncRegionsOfTheFile() {
		ArrayList<Synchronization> allTheSyncRegionsInTheFile = new ArrayList<Synchronization>();
		this.reusefile();
		
		String line = "";
		try {
			this.bRead.mark(1);
			line = this.bRead.readLine();
			this.bRead.reset();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (line == null) {
			return allTheSyncRegionsInTheFile;
		}
		
		// This regex for Java language..
		String singleLineComment = "//";
		String regexOneGroup = "\\/\\/\\s*sync\\s*(.+\\s*,\\s*\\w+)";
		// like this.. //sync _OeCHMPxQEemsbtndia47ww, description  
		// without dublcation\ ..\/\/\s*sync\s+(.+\s*,\s*\w+)
		
		String regexTwoSyncRegionsInOneLine = "\\/\\*\\s*sync\\s*(.+\\s*,\\s*\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/\\s*[\\w\\s]+?\\s*\\/\\*\\s*sync\\s*(.+\\s*,\\s*\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/";
		// like this.. /*sync _OeCHMPxQEemsbtndia47ww, name  */ MDE /*endSync */ extends /*sync _OeCHMPxQEemsbtndia47ww, name  */ Software /*endSync */ {
		// without dublcation\ .. \/\*\s*sync\s*(.+\s*,\s*\w+)\s*\*\/\s*(\w+)\s*\/\*\s*endSync\s*\*\/\s*\w+\s*\/\*\s*sync\s*(.+\s*,\s*\w+)\s*\*\/\s*(\w+)\s*\/\*\s*endSync\s*\*\/
		
		String regexMulitCommentsInOneLine = "\\/\\*\\s*sync\\s*(.+)\\s*,\\s*(\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/";
		// like this.. /*sync _OeCHMPxQEemsbtndia47ww, name  */ MDE /*endSync */ 
		// without dublcation\ ..\/\*\s*sync\s+(.+)\s*,\s*(\w+)\s*\*\/\s*(\w+)\s*\/\*\s*endSync\s*\*\/
		
		String testTwoRegionsinSameLine = ".*\\/\\*\\s*sync.+?\\/\\*\\s*sync.+";
		String testMultilinesLine = ".*\\/\\*\\s*sync.+";
		// like this.. /*sync _OeCHMPxQEemsbtndia47ww, name  */ MDE /*endSync */ extends /*sync
		// without dublcation\ ..\/\*sync.+?\/\*sync
			
		switch (line) {
		// The commen language that support Multi-Line Comment
		case "<!--HTML-->":
			singleLineComment = "<!--";
			regexOneGroup = "<!--\\s*sync\\s*(.+\\s*,\\s*\\w+)";

			regexTwoSyncRegionsInOneLine = "<!--\\s*sync\\s*(.+\\s*,\\s*\\w+)\\s*-->\\s*(\\w+)\\s*<!--\\s*endSync\\s*-->\\s*[\\w\\s]+?\\s*<!--\\s*sync\\s*(.+\\s*,\\s*\\w+)\\s*-->\\s*(\\w+)\\s*<!--\\s*endSync\\s*-->";
			regexMulitCommentsInOneLine = "<!--\\s*sync\\s*(.+)\\s*,\\s*(\\w+)\\s*-->\\s*(\\w+)\\s*<!--\\s*endSync\\s*-->";

			testTwoRegionsinSameLine = ".*<!--\\s*sync.+?<!--\\s*sync.+";
			testMultilinesLine = ".*<!--\\s*sync.+";
			break;
		case "#Ruby":
//		case "#Perl":
			singleLineComment = "#";
			regexOneGroup = "#\\s*sync\\s*(.+\\s*,\\s*\\w+)";

			regexTwoSyncRegionsInOneLine = "=begin\\s*sync\\s*(.+\\s*,\\s*\\w+)\\s*=end\\s*(\\w+)\\s*=begin\\s*endSync\\s*=end\\s*[\\w\\s]+?\\s*=begin\\s*sync\\s*(.+\\s*,\\s*\\w+)\\s*=end\\s*(\\w+)\\s*=begin\\s*endSync\\s*=end";
			regexMulitCommentsInOneLine = "=begin\\s*sync\\s*(.+)\\s*,\\s*(\\w+)\\s*=end\\s*(\\w+)\\s*=begin\\s*endSync\\s*=end";

			testTwoRegionsinSameLine = ".*=begin\\s*sync.+?=begin\\s*sync.+";
			testMultilinesLine = ".*=begin\\s*sync.+";
			break;
		case "--Haskell":
			singleLineComment = "--";
			regexOneGroup = "\\-\\-\\s*sync\\s+(.+\\s*,\\s*\\w+)";

			regexTwoSyncRegionsInOneLine = "\\{\\-\\s*sync\\s*(.+\\s*,\\s*\\w+)\\s*\\-\\}\\s*(\\w+)\\s*\\{\\-\\s*endSync\\s*\\-\\}\\s*[\\w\\s]+?\\s*\\{\\-\\s*sync\\s*(.+\\s*,\\s*\\w+)\\s*\\-\\}\\s*(\\w+)\\s*\\{\\-\\s*endSync\\s*\\-\\}";
			regexMulitCommentsInOneLine = "\\{\\-\\s*sync\\s*(.+)\\s*,\\s*(\\w+)\\s*\\-\\}\\s*(\\w+)\\s*\\{\\-\\s*endSync\\s*\\-\\}";

			testTwoRegionsinSameLine = ".*\\{\\-\\s*sync.+?\\{\\-\\s*sync.+";
			testMultilinesLine = ".*\\{\\-\\s*sync.+";
			break;
		case "#Python":
			singleLineComment = "#";
			regexOneGroup = "#\\s*sync\\s*(.+\\s*,\\s*\\w+)";

			regexTwoSyncRegionsInOneLine = "\\'\\'\\'\\s*sync\\s*(.+\\s*,\\s*\\w+)\\s*\\'\\'\\'\\s*(\\w+)\\s*\\'\\'\\'\\s*endSync\\s*\\'\\'\\'\\s*[\\w\\s]+?\\s*\\'\\'\\'\\s*sync\\s*(.+\\s*,\\s*\\w+)\\s*\\'\\'\\'\\s*(\\w+)\\s*\\'\\'\\'\\s*endSync\\s*\\'\\'\\'";
			regexMulitCommentsInOneLine = "\\'\\'\\'\\s*sync\\s*(.+)\\s*,\\s*(\\w+)\\s*\\'\\'\\'\\s*(\\w+)\\s*\\'\\'\\'\\s*endSync\\s*\\'\\'\\'";

			testTwoRegionsinSameLine = "\\'\\'\\'\\s*sync.+?\\'\\'\\'\\s*sync.+";
			testMultilinesLine = ".*\\'\\'\\'\\s*sync.+";
//		// Kotlin, CSS and Rust are other language that support in=line and multi lines as Java exactly.
		}
		while (true) {
			try {
				line = this.bRead.readLine();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			if (line == null) {
				break;
			}
				
//			if (line.trim().startsWith(singleLineComment + "sync" )) {
			if (line.trim().contains(singleLineComment + "sync" ) && !line.contains("endSync")) {

				String[] idAndAttribute1 = null;
				Pattern p = Pattern.compile(regexOneGroup); // Ok with sync word
				Matcher m = p.matcher(line.trim());
				if (!m.find()) {
					System.err.println("Sorry! there is incomplete or misformated sync regions: id or attribute");
					return null;
				} else {
					idAndAttribute1 = (String[]) (m.group(1)).split(", ");
					Synchronization sync = new Synchronization(idAndAttribute1[0].trim(), idAndAttribute1[1].trim());

					try {
//						while ((line = this.bRead.readLine()) != null && !line.trim().startsWith(singleLineComment + "endSync") && !line.trim().startsWith(singleLineComment + "sync"))
						while ((line = this.bRead.readLine()) != null && !line.trim().contains(singleLineComment + "endSync") && !line.trim().contains(singleLineComment + "sync"))
							sync.addContent(line.trim());

						if (line == null) {
							System.err.println("Sorry! Couldn't find the endSync before the end of the file!");
							return null;
//						} else if (line.trim().startsWith(singleLineComment + "sync")) {
						} else if (line.trim().contains(singleLineComment + "sync")) {
							System.err.println("Sorry! this region must stop before other //sync start");
							return null;
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					allTheSyncRegionsInTheFile.add(sync);
				}
			// I tried with contains()
			} else if (Pattern.matches(testMultilinesLine, line)) {
				
//			} else if (line.contains(multiLinesCommentStart + "sync") && !line.contains("endSync") ) {
				// /*sync with extend 4 groups
				if (Pattern.matches(testTwoRegionsinSameLine, line)) {
					Pattern p = Pattern.compile(regexTwoSyncRegionsInOneLine);
					Matcher m = p.matcher(line.trim());
					if (m.find()) {
						// matching before extend is saved in sync 1
						String[] idAndAttribute1 = (String[]) (m.group(1)).split(",");
						String content1 = m.group(2).trim();
						Synchronization sync1 = new Synchronization(idAndAttribute1[0].trim(),
								idAndAttribute1[1].trim(), content1);

						// matching after extend is saved in sync 2
						String[] idAndAttribute2 = (String[]) (m.group(3)).split(",");
						String content2 = m.group(4).trim();
						Synchronization sync2 = new Synchronization(idAndAttribute2[0].trim(),
								idAndAttribute2[1].trim(), content2);

						allTheSyncRegionsInTheFile.add(sync1);
						allTheSyncRegionsInTheFile.add(sync2);
					}
				}

				// /*sync without extend 3 groups
				else {
					// For Multi-lines comments in Java = like /*sync adcdef, name */ MDE /*endSync*/
// new = \\/\\*\\s*sync\\s+(.+)\\s*,\\s*(\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/
// old = \\/\\*\\s*sync\\s+(.+)\\s*,\\s*(\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/
					final Pattern pattern = Pattern.compile(regexMulitCommentsInOneLine);
					final Matcher matcher = pattern.matcher(line);

					if (matcher.find()) {
						String id = matcher.group(1).trim();
						String attribute = matcher.group(2).trim();
						String content = matcher.group(3).trim();

						Synchronization sync = new Synchronization(id, attribute, content);
						allTheSyncRegionsInTheFile.add(sync);
					}
				}
			}
//			else if (line.trim().startsWith(singleLineComment + "sync") && !line.contains(", ")) {
			else if (line.trim().contains(singleLineComment + "sync") && !line.contains(", ")) {

				//(\/\/\s*sync\s+.+\s*,\s*\w+) check the whole line 
				Pattern p = Pattern.compile(regexOneGroup); // Ok with sync word
				Matcher m = p.matcher(line.trim());
				if (!m.find()) {
					System.err.println("Sorry! there is incomplete or misformated sync regions");
					return null;
				}
			}
		}
		return allTheSyncRegionsInTheFile;
	}
}











































































































/*
 * This is the original code that works with syncing JAVA, 
 * whether the type of sync regions is single or multiline comment.
 * The new code above does not work with multiline comment because the sync region maybe in the midile of the line not the begining
 */
//package org.eclipse.epsilon.egl.sync;
//
//import java.io.BufferedReader;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import org.eclipse.epsilon.eol.models.IModel;
////
//public class FileSync {
//
//	// all fields
//	FileInputStream fIn;
//	public BufferedReader bRead;
//	public IModel model;
//
//	// fileName Ruby looking for the last line...
//	public FileSync(String fileName) {
//		try {
//			fIn = new FileInputStream(fileName);
//			bRead = new BufferedReader(new InputStreamReader(fIn));
//
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//
//	}
//
//	
//	public void reusefile() {
//		try {
//			fIn.getChannel().position(0);
//			bRead = new BufferedReader(new InputStreamReader(fIn));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//	}
//
//	// Find all the sync regions
//	public ArrayList<Synchronization> getAllTheSyncRegionsOfTheFile() {
//		ArrayList<Synchronization> allTheSyncRegionsInTheFile = new ArrayList<Synchronization>();
//		this.reusefile();
//		String line = null;
//
//		while (true) {
//			try {
//				line = this.bRead.readLine();
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
//
//			if (line == null) {
//				break;
//			}
//
//			// you need to have it not hard codeded but dyinamiclly 
//			// because you know what the start comment and it should not contain //sync, it should contain startComment + sync word.
//			
//			
//			// NEW CHANGES / 18/10/2021.. TO SUPPORT OTHER COMMENT FORMATS...
//			// This is the original one for // Java only I will add other language below this condition starting from similar one to Java maybe five languages,
//			// Then move to other languages....
//			
//			
//			if (line.contains("//sync")) {
//				String[] idAndAttribute1 = null;
//				Pattern p = Pattern.compile("\\/\\/\\s*sync\\s+(.+\\s*,\\s*\\w+)"); // Ok with sync word
//				
//				// The below without dublications...
//				// \/\/\s*sync\s+(.+\s*,\s*\w+)
//				
//				//(\/\/\s*sync\s+.+\s*,\s*\w+) check the whole line 
//				Matcher m = p.matcher(line.trim());
//				// if syntax is wrong
//				if (!m.find()) {
//					System.err.println("Sorry! there is incomplete or misformated sync regions: id or attribute");
//					//System.exit(0);
//					return null;
//				} else {
//					idAndAttribute1 = (String[]) (m.group(1)).split(", ");
//					Synchronization sync = new Synchronization(idAndAttribute1[0].trim(), idAndAttribute1[1].trim());
//
//					try {
//						while ((line = this.bRead.readLine()) != null  && !line.contains("//endSync") && !line.contains("//sync")) sync.addContent(line.trim());
//
//						if (line == null) {
//							System.err.println("Sorry! Couldn't find the endSync before the end of the file!");
//							//System.exit(0);
//							return null;
//						} else if (line.contains("//sync")) {
//							System.err.println("Sorry! this region must stop before other //sync start");
//							//System.exit(0);
//							return null;
//						}
//
//					} catch (IOException e1) {
//						e1.printStackTrace();
//					}
//					allTheSyncRegionsInTheFile.add(sync);
//				}
//			//----------------------------------------------------- Until here	
//			// For inhertance in Java using -- Multi-lines comments  /*sync adc123, name*/ Book /*sync bbb222, names */ Library /*endSync */		
//			} else if (line.contains("/*sync")) {
//				// /*sync with extend 4 groups
//				if (line.contains("extends")) {
//					// One group without sync and endsync
//					Pattern p = Pattern.compile("\\s(.+\\s*,\\s*\\w+)\\s*\\*\\/\\s(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/\\s+\\w+\\s+\\/\\*\\s*sync\\s+(.+\\s*,\\s*\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/");
//					
//					// The below without dublications...
//					// \s(.+\s*,\s*\w+)\s*\*\/\s(\w+)\s*\/\*\s*endSync\s*\*\/\s+\w+\s+\/\*\s*sync\s+(.+\s*,\s*\w+)\s*\*\/\s*(\w+)\s*\/\*\s*endSync\s*\*\/
//
//					Matcher m = p.matcher(line.trim());
//					if (m.find()) {
//						// matching before extend is saved in sync 1
//						String[] idAndAttribute1 = (String[]) (m.group(1)).split(",");
//						String content1 = m.group(2).trim();
//						Synchronization sync1 = new Synchronization(idAndAttribute1[0].trim(),
//								idAndAttribute1[1].trim(), content1);
//
//						// matching after extend is saved in sync 2
//						String[] idAndAttribute2 = (String[]) (m.group(3)).split(",");
//						String content2 = m.group(4).trim();
//						Synchronization sync2 = new Synchronization(idAndAttribute2[0].trim(),
//								idAndAttribute2[1].trim(), content2);
//
//						allTheSyncRegionsInTheFile.add(sync1);
//						allTheSyncRegionsInTheFile.add(sync2);
//					}
//				}
//
//				// /*sync without extend 3 groups
//				else {
//					// For Multi-lines comments in Java
//					final String regex = "\\/\\*\\s*sync\\s+(.+)\\s*,\\s*(\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/";
//
//					// final String regex =
//					// "\\s+(.+\\s*),(\\s*\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/";
//
//					final Pattern pattern = Pattern.compile(regex);
//					final Matcher matcher = pattern.matcher(line);
//
//					if (matcher.find()) {
//						String id = matcher.group(1).trim();
//						String attribute = matcher.group(2).trim();
//						String content = matcher.group(3).trim();
//
//						Synchronization sync = new Synchronization(id, attribute, content);
//						allTheSyncRegionsInTheFile.add(sync);
//					}
//				}
//			}
//			else if (line.contains("//sync") && !line.contains(", ")) {
//				//(\/\/\s*sync\s+.+\s*,\s*\w+) check the whole line 
//				Pattern p = Pattern.compile("(\\/\\/\\s*sync\\s+.+\\s*,\\s*\\w+)"); // Ok with sync word
//				Matcher m = p.matcher(line.trim());
//				// if syntax is wrong
//				if (!m.find()) {
//					System.err.println("Sorry! there is incomplete or misformated sync regions");
//					//System.exit(0);
//					return null;
//				}
//			}
//
//		}
//		return allTheSyncRegionsInTheFile;
//	}
//}














































































//
// // This is the NEW vergion of the FileSync to make it works with different comment stlyes and remove the last three lines.
//package org.eclipse.epsilon.egl.sync;
//
//import java.io.BufferedReader;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import org.eclipse.epsilon.eol.models.IModel;
//
//public class FileSync {
//
//	// all fields
//	FileInputStream fIn;
//	public BufferedReader bRead;
//	public IModel model;
//
//	// fileName Ruby looking for the last line...
//	public FileSync(String fileName) {
//		try {
//			fIn = new FileInputStream(fileName);
//			bRead = new BufferedReader(new InputStreamReader(fIn));
//
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void reusefile() {
//		try {
//			fIn.getChannel().position(0);
//			bRead = new BufferedReader(new InputStreamReader(fIn));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	// Find all the sync regions
//	public ArrayList<Synchronization> getAllTheSyncRegionsOfTheFile() {
//		ArrayList<Synchronization> allTheSyncRegionsInTheFile = new ArrayList<Synchronization>();
//		this.reusefile();
//
//		String line = null;
//		try {
//			line = this.bRead.readLine();
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//
//		if (line == null) {
//			return allTheSyncRegionsInTheFile;
//		}
////------------------------- From here 
//
//		
////		String regex = "\\/[*\\/]sync\\s*(.+?),\\s*([^\\s]+)\\s*(?:\\*\\/|\\n)([\\w\\W]+?)(?:\\/\\*endSync\\s*\\*\\/|\\/\\/endSync)";
////		
////		switch (line) {
////		case "<!--HTML-->":
////			// Works fine
////			regex = "<!--\\s*sync\\s*(.+?),\\s*([^\\s]+)\\s*-->([\\w\\W]+?)<!--\\s*endSync\\s*-->";
////			break;
////
////		case "#Python":
////			// Only works with single line comments and this considers as a limitation.
////			regex = "#\\s*sync\\s*(.+?),\\s*([^\\s]+)\\s*(?:\\*\\/|\\n)([\\w\\W]+?)#\\s*endSync";
////			break;
////		// Perl has similar comment to Ruby.
////		case "#Perl":
////		case "#Ruby":
////			// Not works with single comment = #|=begin sync\s*(.+?),\s*([^\s]+)\s*(?:=end|\n)([\w\W]+?)(?:#|=begin\s*endSync\s*=end)
////			regex = "";
////			break;
////		case "- -Haskell":
////			// Not works with single comment = --|{-\s* sync\s*(.+?),\s*([^\s]+)\s*(?:-}|\n)([\w\W]+?)(?:--|{-\s*endSync\s*-})
////			regex = "";
////			break;    // Kotlin, CSS and Rust are other language that support in=line and multi lines as Java exactly.
////		}
////		
////		String document = " ";
////				
////		while (true) {
////			try {
////				line = this.bRead.readLine();
////			} catch (IOException e1) {
////				e1.printStackTrace();
////			}
////	
////			if (line == null) {
////				break;
////			}
////			document += line;
////		}
////		
////		Pattern p = Pattern.compile(regex); 
////		Matcher m = p.matcher(document);
////		while (!m.find()) {
////			allTheSyncRegionsInTheFile.add(
////				new Synchronization(m.group(1), m.group(2), m.group(3)));			
////		}
//
//
//		
//		
////-------------------------- Until here		
//
//		
//		
//		
//		
//		
////		while (true) {
////			try {
////				line = this.bRead.readLine();
////			} catch (IOException e1) {
////				e1.printStackTrace();
////			}
////
////			if (line == null) {
////				break;
////			}
//// 
////			// you need to have it not hard codeded but dyinamiclly
////			// because you know what the start comment and it should not contain //sync, it
////			// should contain startComment + sync word.
////
////			// NEW CHANGES / 18/10/2021.. TO SUPPORT OTHER COMMENT FORMATS...
////
////			// This is for supporting single line comment...
////			
////			// \/(?:\*|\/)sync\s*(.+?),\s*([^\s]+)\s*(?:\*\/|\n)([\w\W]+?)(?:\/\*endSync\s*\*\/|\/\/endSync)
////			if (line.startsWith(singleLineComment)) {
////				
////				// P-- regex= "sync\\s+(.+)\\s*,\\s*(\\w+)"
////				Pattern p = Pattern.compile("sync\\s+(.+)\\s*,\\s*(\\w+)"); // Ok with sync word
////				Matcher m = p.matcher(line.trim());
////				// if syntax is wrong
////				if (!m.find()) {
////					System.err.println("Sorry! there is incomplete or misformated sync regions: id or attribute");
////					return null;
////				} else {
////					String id = m.group(1);
////					String attribute = m.group(2);
////
////					Synchronization sync = new Synchronization(id, attribute);
////
////					try {
////						String startString = singleLineComment + "sync";
////						String endString = singleLineComment + "endSync";
////
////						// the original one
//////						while ((line = this.bRead.readLine()) != null && !line.trim().startsWith(endString)
//////								&& !line.startsWith(startString)) sync.addContent(line.trim());
////						
////						
////						// remove trim the content.. // when I debbaged it the sync regions == 0 cannot see regions in the file
////						while ((line = this.bRead.readLine()) != null && !line.startsWith(endString)
////								&& !line.startsWith(startString)) sync.addContent(line);
////						
//////						// replace startWith with contains does not work as well ..
//////						while ((line = this.bRead.readLine()) != null && !line.contains(endString)
//////								&& !line.contains(startString)) sync.addContent(line);
//////						
////						// Not work I did the opposite way 
//////						while ((line = this.bRead.readLine()) != null && !line.trim().startsWith(startString)
//////								&& !line.startsWith(endString)) sync.addContent(line.trim());
////
////						if (line == null) {
////							System.err.println("Sorry! Couldn't find the endSync before the end of the file!");
////							return null;
////						} else if (line.startsWith(startString)) {
////							System.err.println("Sorry! this region must stop before other //sync start");
////							return null;
////						}
////
////					} catch (IOException e1) {
////						e1.printStackTrace();
////					}
////					allTheSyncRegionsInTheFile.add(sync);
////				}
////
////				// This is for supporting multie line comment...
//////			} else if (line.startsWith(multiLinesCommentStart)) {
//////				Pattern p = Pattern.compile("sync\\s+(.+)\\s*,\\s*(\\w+)"); // Ok with sync word
//////				Matcher m = p.matcher(line.trim());
//////				// if syntax is wrong
//////				if (!m.find()) {
//////					System.err.println("Sorry! there is incomplete or misformated sync regions: id or attribute");
//////					return null;
//////				} else {
//////					String id = m.group(1);
//////					String attribute = m.group(2);
//////
//////					Synchronization sync = new Synchronization(id, attribute);
//////
//////					try {
//////						String startString = multiLinesCommentStart + "sync";
//////						String endString = multiLinesCommentEnd + "endSync";
//////
//////						while ((line = this.bRead.readLine()) != null && !line.trim().startsWith(endString)
//////								&& !line.startsWith(startString))
//////							sync.addContent(line.trim());
//////
//////						if (line == null) {
//////							System.err.println("Sorry! Couldn't find the endSync before the end of the file!");
//////							return null;
//////						} else if (line.contains("//sync")) {
//////							System.err.println("Sorry! this region must stop before other //sync start");
//////							return null;
//////						}
//////
//////					} catch (IOException e1) {
//////						e1.printStackTrace();
//////					}
//////					allTheSyncRegionsInTheFile.add(sync);
//////				}
////
////				// For inhertance in Java using -- Multi-lines comments /*sync adc123, name*/
////				// Book /*sync bbb222, names */ Library /*endSync */
////			} else if (line.contains(multiLinesCommentStart)) {
////				// /*sync with extend 4 groups
////				if (line.contains("extends")) {
////					// One group without sync and endsync
////					Pattern p = Pattern.compile("\\s(.+\\s*,\\s*\\w+)\\s*\\*\\/\\s(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/\\s+\\w+\\s+\\/\\*\\s*sync\\s+(.+\\s*,\\s*\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/");
////
////					// The below without dublications...
////					// \s(.+\s*,\s*\w+)\s*\*\/\s(\w+)\s*\/\*\s*endSync\s*\*\/\s+\w+\s+\/\*\s*sync\s+(.+\s*,\s*\w+)\s*\*\/\s*(\w+)\s*\/\*\s*endSync\s*\*\/
////
////					Matcher m = p.matcher(line.trim());
////					if (m.find()) {
////						// matching before extend is saved in sync 1
////						String[] idAndAttribute1 = (String[]) (m.group(1)).split(",");
////						String content1 = m.group(2).trim();
////						Synchronization sync1 = new Synchronization(idAndAttribute1[0].trim(),
////								idAndAttribute1[1].trim(), content1);
////
////						// matching after extend is saved in sync 2
////						String[] idAndAttribute2 = (String[]) (m.group(3)).split(",");
////						String content2 = m.group(4).trim();
////						Synchronization sync2 = new Synchronization(idAndAttribute2[0].trim(),
////								idAndAttribute2[1].trim(), content2);
////
////						allTheSyncRegionsInTheFile.add(sync1);
////						allTheSyncRegionsInTheFile.add(sync2);
////					}
////				}
////
////				// /*sync without extend 3 groups
////				else {
////					// For Multi-lines comments in Java
////					final String regex = "\\/\\*\\s*sync\\s+(.+)\\s*,\\s*(\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/";
////					final Pattern pattern = Pattern.compile(regex);
////					final Matcher matcher = pattern.matcher(line);
////
////					if (matcher.find()) {
////						String id = matcher.group(1).trim();
////						String attribute = matcher.group(2).trim();
////						String content = matcher.group(3).trim();
////
////						Synchronization sync = new Synchronization(id, attribute, content);
////						allTheSyncRegionsInTheFile.add(sync);
////					}
////				}
////			} else if (line.contains("//sync") && !line.contains(", ")) {
////				// (\/\/\s*sync\s+.+\s*,\s*\w+) check the whole line
////				Pattern p = Pattern.compile("(\\/\\/\\s*sync\\s+.+\\s*,\\s*\\w+)"); // Ok with sync word
////				Matcher m = p.matcher(line.trim());
////				// if syntax is wrong
////				if (!m.find()) {
////					System.err.println("Sorry! there is incomplete or misformated sync regions");
////					// System.exit(0);
////					return null;
////				}
////			}
////
////		}
//		return allTheSyncRegionsInTheFile;
//	}
//}





