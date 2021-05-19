package org.eclipse.epsilon.egl.sync;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.execute.introspection.IPropertyGetter;
import org.eclipse.epsilon.eol.execute.introspection.IPropertySetter;
import org.eclipse.epsilon.eol.models.IModel;
import org.junit.Test;

public class SyncAppFromModel {
	
	// for the university-last project - just to test
	private static final String FOLDER_PATH = System.getProperty("user.dir") + "/University-Last-Project/gen";

	/*
	 * This path works fine, but I need to check why it only works with adding Test1 at the end. 
	 */

//	private static final String FOLDER_PATH = System.getProperty("user.dir") + "/SyncTests/GeneratedFilesFromUniversity/Test1";

	
	/*
	 *  This path for the boiler test in this workspace and it works. if I use 
	 *  System.getProperty("user.dir") to make it general (relevant path), it 
	 *  refers to sync engine I do not know why?
	 */
	// Last time I commented this path for the boiler to run the University example.
//	private static final String FOLDER_PATH = "/Users/sultanalmutairi/git/Epsilon-Source/org.eclipse.epsilon/examples/org.eclipse.epsilon.examples.egl.comps/src-gen-sync-regions/syncregions/";	

	//	private static final String FOLDER_PATH = "../git/Epsilon-Source/org.eclipse.epsilon/examples/org.eclipse.epsilon.examples.egl.comps/src-gen-sync-regions/syncregions/";

	public static void main(String[] args) throws EolModelLoadingException, IOException {

		EmfModel model = new EmfModel();
		model.setName("M");
		
		// for the university-last project - just to test
		model.setMetamodelFile(new File("University-Last-Project/University.ecore").getAbsolutePath());
		model.setModelFile(new File("University-Last-Project/University.model").getAbsolutePath());	
		
//		// for the university
//		model.setMetamodelFile(new File("SyncTests/Model-University/University.ecore").getAbsolutePath());
//		model.setModelFile(new File("SyncTests/Model-University/University.model").getAbsolutePath());	
//		
		
		// Last time I commented this path for the boiler to run the University example.
		// for the boiler test in other workspace
//		model.setMetamodelFile(new File("/Users/sultanalmutairi/git/Epsilon-Source/org.eclipse.epsilon/examples/org.eclipse.epsilon.examples.egl.comps/comps.ecore").getAbsolutePath());
//		model.setModelFile(new File("/Users/sultanalmutairi/git/Epsilon-Source/org.eclipse.epsilon/examples/org.eclipse.epsilon.examples.egl.comps/BoilerController.model").getAbsolutePath());
		
		
		
		// Tried with relevant path the boiler test in other workspace, not works
//		model.setMetamodelFile(new File("/comps.ecore").getAbsolutePath());
//		model.setModelFile(new File("/BoilerController.model").getAbsolutePath());
	
	
		model.setReadOnLoad(true);
		model.setStoredOnDisposal(true);

		try {
			model.load();
		} catch (EolModelLoadingException e2) {
			System.err.println("The model element is not found in model: " + e2.getMessage());
		} 

		FolderSync syncReader = new FolderSync();
		syncReader.getSynchronization(FOLDER_PATH, model);

	}
}

//System.out.println("Working Directory = " + System.getProperty("user.dir"));
