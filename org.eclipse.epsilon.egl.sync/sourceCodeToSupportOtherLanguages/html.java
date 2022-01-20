import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.epsilon.egl.sync.Synchronization;

if (line.contains("<!--sync")) {
				String[] idAndAttribute1 = null; <
				// for java
//				Pattern p = Pattern.compile("\\/\\/\\s*sync\\s+(.+\\s*,\\s*\\w+)");
				// for html
				Pattern p = Pattern.compile("<!--s*sync\\s+(.+\\s*,\\s*\\w+)\\s*-->\\s*\\w+\\s*<!--\\s*endSync\\s*-->");

				Matcher m = p.matcher(line.trim());
				// if syntax is wrong
				if (!m.find()) {
					System.err.println("Sorry! there is incomplete or misformated sync regions: id or attribute");
					return null;
				} else {
					idAndAttribute1 = (String[]) (m.group(1)).split(", ");
					Synchronization sync = new Synchronization(idAndAttribute1[0].trim(), idAndAttribute1[1].trim());

					try {
						while ((line = this.bRead.readLine()) != null && !line.contains("endSync-->") && !line.contains("<!--sync"))
							sync.addContent(line.trim());

						if (line == null) {
							System.err.println("Sorry! Couldn't find the endSync before the end of the file!");
							  return null;
						} else if (line.contains("<!--sync")) {
							System.err.println("Sorry! this region must stop before other //sync start");
							  return null;
						}

					} catch (IOException e1) {
						e1.printStackTrace();
					}
					allTheSyncRegionsInTheFile.add(sync);
				}
			} else if (line.contains("<!--sync")) {
			//} else if (line.contains("<!--sync") && line.contains("extends")) {

				// /*sync with extend 4 groups
				if (line.contains("extends")) {
					// One group without sync and endsync
					// for java
//					Pattern p = Pattern.compile("\\s(.+\\s*,\\s*\\w+)\\s*\\*\\/\\s(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/\\s+\\w+\\s+\\/\\*\\s*sync\\s+(.+\\s*,\\s*\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/");
					// for html
					Pattern p = Pattern.compile("<!--\\s*sync\\s*(.+?),\\s*([^\\s]+)\\s*-->([\\w\\W]+?)<!--\\s*endSync\\s*-->");
					
					Matcher m = p.matcher(line.trim());
					if (m.find()) {
						String[] idAndAttribute1 = (String[]) (m.group(1)).split(",");
						String content1 = m.group(2).trim();
						Synchronization sync1 = new Synchronization(idAndAttribute1[0].trim(),
								idAndAttribute1[1].trim(), content1);

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
					// for java
//					final String regex = "\\/\\*\\s*sync\\s+(.+)\\s*,\\s*(\\w+)\\s*\\*\\/\\s*(\\w+)\\s*\\/\\*\\s*endSync\\s*\\*\\/";
					// for html
					final String regex = "<!--\\s*sync\\s*(.+?),\\s*([^\\s]+)\\s*-->([\\w\\W]+?)<!--\\s*endSync\\s*-->";

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
			else if (line.contains("<!--sync") && !line.contains(", ")) {
				Pattern p = Pattern.compile("<!--s*sync\\s+(.+\\s*,\\s*\\w+)\\s*-->\\s*\\w+\\s*<!--\\s*endSync\\s*-->"); // Ok with sync word
				Matcher m = p.matcher(line.trim());
				if (!m.find()) {
					System.err.println("Sorry! there is incomplete or misformated sync regions");
					return null;
				}
			}