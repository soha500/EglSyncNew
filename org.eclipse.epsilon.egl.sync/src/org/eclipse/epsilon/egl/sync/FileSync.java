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
		String line = null;

		while (true) {
			try {
				line = this.bRead.readLine();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			if (line == null) {
				break;
			}

			if (line.contains("//sync")) {
				String[] idAndAttribute1 = null;
				Pattern p = Pattern.compile("\\/\\/\\s*sync\\s+(.+\\s*,\\s*\\w+)"); // Ok with sync word
				//(\/\/\s*sync\s+.+\s*,\s*\w+) check the whole line 
				Matcher m = p.matcher(line.trim());
				// if syntax is wrong
				if (!m.find()) {
					System.err.println("Sorry! there is incomplete or misformated sync regions: id or attribute");
					//System.exit(0);
					return null;
				} else {
					idAndAttribute1 = (String[]) (m.group(1)).split(", ");
					Synchronization sync = new Synchronization(idAndAttribute1[0].trim(), idAndAttribute1[1].trim());

					try {
						while ((line = this.bRead.readLine()) != null && !line.contains("//endSync") && !line.contains("//sync"))
							sync.addContent(line.trim());

						if (line == null) {
							System.err.println("Sorry! Couldn't find the endSync before the end of the file!");
							//System.exit(0);
							return null;
						} else if (line.contains("//sync")) {
							System.err.println("Sorry! this region must stop before other //sync start");
							//System.exit(0);
							return null;
						}

					} catch (IOException e1) {
						e1.printStackTrace();
					}
					allTheSyncRegionsInTheFile.add(sync);
				}
			//----------------------------------------------------- Until here	
			// for /*sync		
			} else if (line.contains("/*sync")) {
				// /*sync with extend 4 groups
				if (line.contains("extends")) {
					// One group without sync and endsync
					Pattern p = Pattern.compile("\\s(.+\\s*,\\s*\\w+)\\s*\\*\\/\\s(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/\\s+\\w+\\s+\\/\\*\\s*sync\\s+(.+\\s*,\\s*\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/");

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
					// old and work
					final String regex = "\\/\\*\\s*sync\\s+(.+)\\s*,\\s*(\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/";

					// final String regex =
					// "\\s+(.+\\s*),(\\s*\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/";

					final Pattern pattern = Pattern.compile(regex);
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
			else if (line.contains("//sync") && !line.contains(", ")) {
				//(\/\/\s*sync\s+.+\s*,\s*\w+) check the whole line 
				Pattern p = Pattern.compile("(\\/\\/\\s*sync\\s+.+\\s*,\\s*\\w+)"); // Ok with sync word
				Matcher m = p.matcher(line.trim());
				// if syntax is wrong
				if (!m.find()) {
					System.err.println("Sorry! there is incomplete or misformated sync regions");
					//System.exit(0);
					return null;
				}
			}

		}
		return allTheSyncRegionsInTheFile;
	}
}





































































































// today 14/8/2020 before I try solve that detect the changes in generated file - orignal code

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
//
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
//			if (line.contains("//sync")) {
//
//				String[] idAndAttribute1 = null;
//
//				Pattern p = Pattern.compile("\\/\\/\\s*sync\\s+(.+\\s*,\\s*\\w+)"); // Ok with sync word
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
//						while ((line = this.bRead.readLine()) != null && !line.contains("//endSync") && !line.contains("//sync"))
//							sync.addContent(line.trim());
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
//
//				// Until here
//				
//			} else if (line.contains("/*sync")) {
//				// /*sync with extend 4 groups
//				if (line.contains("extends")) {
//					// One group without sync and endsync
//					Pattern p = Pattern.compile("\\s(.+\\s*,\\s*\\w+)\\s*\\*\\/\\s(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/\\s+\\w+\\s+\\/\\*\\s*sync\\s+(.+\\s*,\\s*\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/");
//
//					Matcher m = p.matcher(line.trim());
//
//					if (m.find()) {
//
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
//
//				}
//
//				// /*sync without extend 3 groups
//				else {
//					// old and work
//					final String regex = "\\/\\*\\s*sync\\s+(.+)\\s*,\\s*(\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/";
//
//					// final String regex =
//					// "\\s+(.+\\s*),(\\s*\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/";
//
//					final Pattern pattern = Pattern.compile(regex);
//					final Matcher matcher = pattern.matcher(line);
//
//					if (matcher.find()) {
//
//						String id = matcher.group(1).trim();
//						String attribute = matcher.group(2).trim();
//						String content = matcher.group(3).trim();
//
//						Synchronization sync = new Synchronization(id, attribute, content);
//
//						allTheSyncRegionsInTheFile.add(sync);
//					}
//				}
//			}
//			else if (line.contains("//sync") && !line.contains(", ")) {
//	//				System.err.println("there are misformatted or incompleted sync regions");
//	//				System.exit(0);
//	//
//	//			}
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
// until here - the orginal code 






















//Pattern p = Pattern.compile("(\\s\\w+.\\s\\w+.)"); // Ok
//Pattern p = Pattern.compile("(\\w+.\\w+,\\s+\\w+)"); // Ok
//Pattern p = Pattern.compile("\\/\\/\\s*sync\\s+(.+\\s*,\\s*\\w+)\\s+(\\s+\\w+\\s*)\\s+\\/\\/\\s*endSync"); // Ok






























// works fine and return message if misformatted happen
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
//
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
//			if (line.contains("//sync")) {
//
//				String[] idAndAttribute1 = null;
//
//				//Pattern p = Pattern.compile("(\\s\\w+.\\s\\w+.)"); // Ok
//				//Pattern p = Pattern.compile("(\\w+.\\w+,\\s+\\w+)"); // Ok
//				Pattern p = Pattern.compile("\\/\\/\\s*sync\\s+(.+\\s*,\\s*\\w+)"); // Ok with sync word
//				//Pattern p = Pattern.compile("\\/\\/\\s*sync\\s+(.+\\s*,\\s*\\w+)\\s+(\\s+\\w+\\s*)\\s+\\/\\/\\s*endSync"); // Ok
//
//				Matcher m = p.matcher(line.trim());
//				
//				// if the syntax are wrong
//				if (!m.find())
//				{
//					System.err.println("Sorry! there is wrong in the sync format it should be like //sync id, attribute");
//
//				}
//				else
//				{
//					idAndAttribute1 = (String[]) (m.group(1)).split(", ");
//					Synchronization sync = new Synchronization(idAndAttribute1[0].trim(), idAndAttribute1[1].trim());
//	
//					try {
//						while ((line = this.bRead.readLine()) != null && !line.contains("//endSync") && !line.contains("//sync"))
//							sync.addContent(line.trim());
//						
//						if (line == null)
//						{
//							System.err.println("Sorry! Couldn't find the endSync before the end of the file!");
//						}
//						else if (line.contains("//sync"))
//						{
//							System.err.println("Sorry! this region must stop before other //sync start");
//						}
//	
//					} catch (IOException e1) {
//						e1.printStackTrace();
//					}
//					
//					allTheSyncRegionsInTheFile.add(sync);
//				}
//			} else if (line.contains("/*sync")) {
//
//				// /*sync with extend 4 groups
//				if (line.contains("extends")) {
//
//					Pattern p = Pattern.compile("\\s(.+\\s*,\\s*\\w+)\\s*\\*\\/\\s(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/\\s+\\w+\\s+\\/\\*\\s*sync\\s+(.+\\s*,\\s*\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/"); // One group without sync and endsync
//
//					Matcher m = p.matcher(line.trim());
//
//					if (m.find()) {
//
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
//
//				}
//				// /*sync without extend 3 groups
//				else {
//					// old and work
//					final String regex = "\\/\\*\\s*sync\\s+(.+)\\s*,\\s*(\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/";
//
//					//final String regex = "\\s+(.+\\s*),(\\s*\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/";
//
//					final Pattern pattern = Pattern.compile(regex);
//					final Matcher matcher = pattern.matcher(line);
//
//					if (matcher.find()) {
//
//						String id = matcher.group(1).trim();
//						String attribute = matcher.group(2).trim();
//						String content = matcher.group(3).trim();
//						
//	
//						Synchronization sync = new Synchronization(id, attribute, content);
//
//						allTheSyncRegionsInTheFile.add(sync);
//					}
//				}
//			}
//
//		}
//		return allTheSyncRegionsInTheFile;
//	}
//}



