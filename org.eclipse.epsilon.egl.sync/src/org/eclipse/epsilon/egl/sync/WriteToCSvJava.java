package org.eclipse.epsilon.egl.sync;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

import javax.swing.JOptionPane;

public class WriteToCSvJava {
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int fivthTime = 5;
		Double time = 0.3339;
		String filePath = "data.csv";

		
		saveRecord(fivthTime, time, filePath);
		
		
	}
	
	public static void saveRecord(int fivthTime, Double time,  String filePath) {
		try {
			FileWriter fw = new FileWriter(filePath, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			
			pw.println("case , Taken time");
			pw.println("1 , 0.0283");
			pw.println("2 , 0.1012");
			pw.println("3 , 0.3983");
			pw.println("4 , 0.9003");
			pw.println(fivthTime + "," + time);
			pw.println("6 , 0.0283");
			pw.println("7 , 0.1012");
			pw.println("8 , 0.3983");
			pw.println("9 , 0.9003");
			pw.println("10 , 0.0283");
			pw.flush();
			pw.close();
			
			JOptionPane.showMessageDialog(null, "recored saved");
			
			
		} catch (Exception E) {
			// TODO: handle exception
			JOptionPane.showMessageDialog(null, "recored not saved");

			
		}
		
	}
	

}

//
//String filePathForTenFiles = "AllDataForTenFiles.csv";
//
//public static void writeResultsToCSvFile(int stageNumber, long timeBefore, long timeAfter,  String filePath) {
//	try {
//		boolean outputHeader = !new File(filePath).exists();
//		FileWriter fw = new FileWriter(filePath, true);
//		BufferedWriter bw = new BufferedWriter(fw);
//		PrintWriter pw = new PrintWriter(bw);
//		if (outputHeader)
//			pw.println("Stage number, Time before each run, Time after each run,  (Totel) Taken time for each run");
//		pw.println(String.format("%d,%d,%d,%d", stageNumber, timeBefore, timeAfter, timeAfter - timeBefore));
//		pw.flush();
//		fw.close();
//		//JOptionPane.showMessageDialog(null, "recored saved");	
//		
//	} catch (Exception E) {
//		// TODO: handle exception
//		//JOptionPane.showMessageDialog(null, "recored not saved");
//	}
//}
//
////@RepeatedTest(5)
////@Repeat( times = 50 )
////@Test(invocationCount = 10)
//
//@Test
//public void testTenFiles() throws IOException {
////	Set<Set> set = new HashSet<Set>();
//	new File(filePathForTenFiles).delete();
//	for (int i = 0; i < 50; i++) { 
//		long start = System.currentTimeMillis();
//		File originalFile = new File(System.getProperty("user.dir")
//				+ "/boiler-To-Generate-10-Files/BoilerController-10-Components.model");
//		model = new EmfModel();
//		model.setName("M");
//		model.setMetamodelFile(
//				new File(System.getProperty("user.dir") + "/boiler-Ecore/comps.ecore").getAbsolutePath());
//		model.setModelFile(originalFile.getAbsolutePath());
//		model.setReadOnLoad(true);
//		try {
//			model.load();
//		} catch (EolModelLoadingException e2) {
//			e2.printStackTrace();
//		}
//
//		Map<String, String> behaviours = new HashMap<>();
//		behaviours.put("BoilerActuator", "1return temperature - targetTemperature;");
//		behaviours.put("TemperatureController",
//				"1if (temperatureDifference > 0 && boilerStatus == true) { return 1; } else if (temperatureDifference < 0 && boilerStatus == false) { return 2; } else return 0;");
//
//		addAndUpdateModel(FOLDER_PATH1 + "/syncregions-10Files", model, behaviours);
//
//		writeResultsToCSvFile(i, start, System.currentTimeMillis(), filePathForTenFiles);
//	}
//}
