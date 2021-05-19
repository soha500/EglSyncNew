//package org.eclipse.epsilon.egl.sync.output;
//
//import org.eclipse.epsilon.egl.exceptions.EglRuntimeException;
//import org.eclipse.epsilon.egl.merge.output.RegionType;
//import org.eclipse.epsilon.egl.merge.partition.CommentBlockPartitioner;
//import org.eclipse.epsilon.egl.output.OutputBuffer;
//
//import org.eclipse.epsilon.egl.output.IOutputBuffer;
//
//import java.util.LinkedList;
//import java.util.List;
//
//import org.eclipse.epsilon.common.util.StringUtil;
//import org.eclipse.epsilon.egl.exceptions.EglStoppedException;
//import org.eclipse.epsilon.egl.execute.context.IEglContext;
//import org.eclipse.epsilon.egl.formatter.Formatter;
//import org.eclipse.epsilon.egl.merge.output.RegionType;
//import org.eclipse.epsilon.egl.merge.partition.CommentBlockPartitioner;
//import org.eclipse.epsilon.egl.preprocessor.Preprocessor;
//import org.eclipse.epsilon.egl.status.Warning;
//import org.eclipse.epsilon.egl.sync.merge.partition.CommentBlockPartitionerSync;
//import org.eclipse.epsilon.egl.util.FileUtil;
//
//public class OutputBufferSync extends OutputBuffer {
//
//	protected String lastLine = null;
//	protected IEglContext context;
//
//	protected List<CommentBlockPartitionerSync> customPartitioners = new LinkedList<>();
//	protected boolean hasProtectedRegions = false;
//	protected boolean hasControlledRegions = false;
//
//	public String startSync(String startComment, String id, String property) throws EglRuntimeException {
//
//		return startLocate(startComment, id, property, RegionType.Protected);
//	}
//
//	public String startControl(String startComment, String id, String property) throws EglRuntimeException {
//
//		return startLocate(startComment, id, property, RegionType.Controlled);
//	}
//
//	public String startLocate(String startComment, String id, String property, RegionType regionType)
//			throws EglRuntimeException {
//
//		assertNoMixedRegions1(regionType);
//
//		if (lastLine != null)
//			throw new EglRuntimeException("The current region must be stopped before another region may begin.",
//					context.getModule());
//
//		// changed it from CommentBlockPartitioner to CommentBlockPartitionerSync
//
//		final CommentBlockPartitionerSync customPartitioner = new CommentBlockPartitionerSync(startComment, lastLine);
//		lastLine = customPartitioner.getLastLine(regionType);
//
//		// I have commented this two lines
//		context.getPartitioner().addPartitioner(customPartitioner);
//		customPartitioners.add(customPartitioner);
//
//		return customPartitioner.getFirstLine(id, property, regionType);
//	}
//
//	protected void assertNoMixedRegions1(RegionType regionType) throws EglRuntimeException {
//		if (regionType == RegionType.Controlled)
//			hasControlledRegions = true;
//		else if (regionType == RegionType.Protected)
//			hasProtectedRegions = true;
//
//		if (hasControlledRegions && hasProtectedRegions) {
//			throw new EglRuntimeException("Templates cannot contain both protected and controlled regions",
//					context.getModule());
//		}
//	}
//
//	// Four-Parameters
//
//	public String startSync(String startComment, String endComment, String id, String property)
//			throws EglRuntimeException {
//
//		return startLocate(startComment, endComment, id, property, RegionType.Protected);
//	}
//
//	public String startControl(String startComment, String endComment, String id, String property)
//			throws EglRuntimeException {
//
//		return startLocate(startComment, endComment, id, property, RegionType.Controlled);
//	}
//
//	public String startLocate(String startComment, String endComment, String id, String property, RegionType regionType)
//			throws EglRuntimeException {
//
//		assertNoMixedSyncRegions(regionType);
//
//		if (lastLine != null)
//			throw new EglRuntimeException("The current sync region must be stopped before another region may begin.",
//					context.getModule());
//
//		// changed it from CommentBlockPartitioner to CommentBlockPartitionerSync
//		final CommentBlockPartitionerSync customPartitioner = new CommentBlockPartitionerSync(startComment, endComment);
//		lastLine = customPartitioner.getLastLine(regionType);
//
//		// I have commented this two lines
//		context.getPartitioner().addPartitioner(customPartitioner);
//		customPartitioners.add(customPartitioner);
//
//		return customPartitioner.getFirstLine(id, property, regionType);
//	}
//
//	protected void assertNoMixedSyncRegions(RegionType regionType) throws EglRuntimeException {
//		if (regionType == RegionType.Controlled)
//			hasControlledRegions = true;
//		else if (regionType == RegionType.Protected)
//			hasProtectedRegions = true;
//
//		if (hasControlledRegions && hasProtectedRegions) {
//			throw new EglRuntimeException("Templates cannot contain both protected and controlled regions",
//					context.getModule());
//		}
//	}
//
//	public String endSync() throws EglRuntimeException {
//		return super.stopLocate();
//	}
//
//}
