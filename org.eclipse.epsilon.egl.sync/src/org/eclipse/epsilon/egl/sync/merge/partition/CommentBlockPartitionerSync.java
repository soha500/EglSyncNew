//package org.eclipse.epsilon.egl.sync.merge.partition;
//
//
//import org.eclipse.epsilon.egl.merge.output.Output;
//import org.eclipse.epsilon.egl.merge.output.RegionType;
//import org.eclipse.epsilon.egl.merge.partition.CommentBlockPartitioner;
//import org.eclipse.epsilon.egl.merge.partition.Partitioner;
//
//
////package org.eclipse.epsilon.egl.merge.partition;
//
//import java.util.LinkedList;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import org.eclipse.epsilon.egl.merge.output.LocatedRegion;
//import org.eclipse.epsilon.egl.merge.output.Region;
//import org.eclipse.epsilon.egl.util.FileUtil;
//
//public class CommentBlockPartitionerSync extends CommentBlockPartitioner {
// 	
//	private final String startComment;
//	private final String endComment;
//	
//	private String firstLine;
//	private String contents = "(.*^[\\s]*)";
//	private String lastLine;
//	
//	private static String escape(String text) {
//		String escaped;
//		
//		escaped = text.replaceAll("\\*", "\\\\*");
//		
//		return escaped;
//	}
//	
//	public CommentBlockPartitionerSync(String startComment, String endComment) {
//	   super(startComment, endComment);
//	this.startComment = startComment == null ? "" : startComment;
//	this.endComment   = endComment   == null ? "" : endComment;
//	//init();
//	}
//
//	/*
//	 * getFirstLine() 
//	 */
//
//	public String getFirstLine(String id, String property, RegionType regionType) {
//		final StringBuilder builder = new StringBuilder();
//		
//		// Build starting comment
//		if (startComment.length() > 0) {
//			builder.append(startComment);
//		}
//		// I have added the // to sync in order to make it print as //sync this is new changes 
//		builder.append("sync ");
////		builder.append(regionTypeToString(regionType) + " sync region ");
//		builder.append(id);
//		builder.append(", ");
//		builder.append(property);
//		builder.append(" ");
////		builder.append(contents ? "on" : "off");
////		builder.append(" begin");
//		if (endComment.length() > 0) {
//			builder.append(' ');
//			builder.append(endComment);
//		}
//		
//		return builder.toString();
//	}
//	/*
//	 * start-getLastLine()
//	 */
//	
//	public String getLastLine(RegionType regionType) {
//		final StringBuilder builder = new StringBuilder();
//		
//		// Build ending comment
//		if (startComment.length() > 0) {
//			builder.append(startComment);
//		}
//		builder.append("endSync");
////		builder.append(regionTypeToString(regionType) + " sync region ");
////		builder.append(id);
////		builder.append(" end end of sync region ");
//		if (endComment.length() > 0) {
//			builder.append(' ');
//			builder.append(endComment);
//		}
//		
//		return builder.toString();
//	}
//	
//}
//
