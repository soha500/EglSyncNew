package org.eclipse.epsilon.egl.sync;

import java.io.File;
import java.io.FileReader;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.execute.introspection.IPropertySetter;

public class SyncApp {

	public static void main(String[] args) throws Exception {

		EmfModel model = new EmfModel();
		model.setName("M");
		/*
		 * this works and automatically generates the files without need to all url and with updating
		 */
	
		// for the university-last project - just to test
		model.setMetamodelFile(new File("University-Last-Project/University.ecore").getAbsolutePath());
		model.setModelFile(new File("University-Last-Project/University.model").getAbsolutePath());	
		
		
//		// University-Last-Project 
//		model.setMetamodelFile(new File("SyncTests/Model-University/University.ecore").getAbsolutePath());
//		model.setModelFile(new File("SyncTests/Model-University/University.model").getAbsolutePath());	

		// for the boiler test in other workspace
//		model.setMetamodelFile(new File("/Users/sultanalmutairi/git/Epsilon-Source/org.eclipse.epsilon/examples/org.eclipse.epsilon.examples.egl.comps/comps.ecore").getAbsolutePath());
//		model.setModelFile(new File("/Users/sultanalmutairi/git/Epsilon-Source/org.eclipse.epsilon/examples/org.eclipse.epsilon.examples.egl.comps/BoilerController.model").getAbsolutePath());

		model.setReadOnLoad(true);
		model.setStoredOnDisposal(true);
		model.load();

		// Getting an element from an ID
		Object openState = model.getElementById("_a7rXYF25EeiOVIR7pFwT6g");
		System.out.println(openState);

		IPropertySetter propertySetter = model.getPropertySetter();

		// Updating the action property of the open state
		propertySetter.setObject(openState);
		propertySetter.setProperty("action");
		propertySetter.invoke("Hello from Java");

		model.dispose();

	}
}