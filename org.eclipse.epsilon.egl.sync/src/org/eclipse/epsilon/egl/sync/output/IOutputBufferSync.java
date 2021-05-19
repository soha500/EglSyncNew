//package org.eclipse.epsilon.egl.sync.output;
//
//import org.eclipse.epsilon.egl.exceptions.EglRuntimeException;
//import org.eclipse.epsilon.egl.output.IOutputBuffer;
//
//
//import org.eclipse.epsilon.egl.exceptions.EglRuntimeException;
//import org.eclipse.epsilon.egl.exceptions.EglStoppedException;
//import org.eclipse.epsilon.egl.formatter.Formatter;
////import org.eclipse.epsilon.egl.internal.EglPreprocessorModule;
//import org.eclipse.epsilon.egl.preprocessor.Preprocessor;
//
//public interface IOutputBufferSync  {
//}
////public interface IOutputBufferSync extends IOutputBuffer {
//		
//	/**
//	 * 
//	 * @param startComment - the character sequence used to denote the start of a comment for the type of output in the buffer
//	 * @param endComment - the character sequence used to denote the end of a comment for the type of output in the buffer 
//	 * @param id - a unique identifier for this sync region
//	 * @param property - a name of property for this Id 
//	 * @return
//	 * @throws EglRuntimeException
//	 */
//	public 	String startSync(String startComment, String id, String property) throws EglRuntimeException;
//	
//	public 	String startControl(String startComment, String id, String property) throws EglRuntimeException;
//
//	/**
//	 * Appends a closing tag for a sync region to the buffer.
//	 * @return
//	 * @throws EglRuntimeException
//	 */
//	
//	// i have stopped the endSync here i need to check if i need only one 
//    // public String endSync() throws EglRuntimeException;
//	
//	public 	String startSync(String startComment,String endComment, String id, String property) throws EglRuntimeException;
//
//	/**
//	 * 
//	 * @param startComment - the character sequence used to denote the start of a comment for the type of output in the buffer
//	 * @param endComment - the character sequence used to denote the end of a comment for the type of output in the buffer 
//	 * @param id - a unique identifier for this sync region
//	 * @param property - a name of property for this Id 
//	 * @return
//	 * @throws EglRuntimeException
//	 */
//	public 	String startControl(String startComment, String endComment, String id, String property) throws EglRuntimeException;
//	
//
//	/**
//	 * Appends a closing tag for a sync region to the buffer.
//	 * @return
//	 * @throws EglRuntimeException
//	 */
//	public String endSync() throws EglRuntimeException;
//
//}
