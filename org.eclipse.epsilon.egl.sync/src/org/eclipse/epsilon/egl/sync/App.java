package org.eclipse.epsilon.egl.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
//import java.util.Scanner;

import org.eclipse.epsilon.egl.EglFileGeneratingTemplateFactory;
import org.eclipse.epsilon.egl.EgxModule;
import org.eclipse.epsilon.egl.output.IOutputBuffer;
//import org.eclipse.epsilon.egl.output.IOutputBufferFactory;
import org.eclipse.epsilon.emc.emf.EmfModel;

public class App {
	
	public static void main(String[] args) throws Exception {
		EglFileGeneratingTemplateFactory factory = new EglFileGeneratingTemplateFactory();
		factory.setOutputRoot(new File("gen").getAbsolutePath());
		EgxModule module = new EgxModule(factory);
		module.parse(new File("statemachine2java.egx"));
		
		/*
		module.getContext().setOutputBufferFactory(new IOutputBufferFactory() {
			
			@Override
			public IOutputBuffer create() {
				return new OutputBufferSync();
			}
		});*/
		
		EmfModel model = new EmfModel();
		model.setName("M");
		/*
		 * this works and automatically generates the files without need to all URL and with updating
		 */
		
		//University-Last-Project    
//		model.setMetamodelFile(new File("SimpleExample/Model-University/University.ecore").getAbsolutePath());
//		model.setModelFile(new File("SimpleExample/Model-University/University.model").getAbsolutePath());
		
//		// for the boiler test in other workspace
		model.setMetamodelFile(new File("/Users/sultanalmutairi/git/Epsilon-Source/org.eclipse.epsilon/examples/org.eclipse.epsilon.examples.egl.comps/comps.ecore").getAbsolutePath());
		model.setModelFile(new File("/Users/sultanalmutairi/git/Epsilon-Source/org.eclipse.epsilon/examples/org.eclipse.epsilon.examples.egl.comps/BoilerController.model").getAbsolutePath());

		model.setReadOnLoad(true);
		model.setStoredOnDisposal(false);
		model.load();
		
		module.getContext().getModelRepository().addModel(model);
		
		module.execute();
	}
	
}
