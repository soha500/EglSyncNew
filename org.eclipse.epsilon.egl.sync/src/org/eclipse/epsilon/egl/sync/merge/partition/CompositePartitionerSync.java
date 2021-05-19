//package org.eclipse.epsilon.egl.sync.merge.partition;
//package org.eclipse.epsilon.egl.merge.partition;
//
//import java.util.LinkedList;
//import java.util.List;
//
//import org.eclipse.epsilon.egl.merge.output.Output;
//import org.eclipse.epsilon.egl.merge.output.LocatedRegion;
//import org.eclipse.epsilon.egl.merge.output.Region;
////import org.eclipse.epsilon.egl.sync.merge.partition.CommentBlockPartitionerSync;
//
//public class CompositePartitionerSync implements Partitioner {
//
//	private List<CommentBlockPartitionerSync> partitioners = new LinkedList<CommentBlockPartitionerSync>();
//
//	public CompositePartitionerSync(CommentBlockPartitionerSync... partitioners) {
//		for (CommentBlockPartitionerSync partitioner : partitioners) {
//			addPartitioner(partitioner);
//		}
//	}
//	
//	
//	public void addPartitioner(CommentBlockPartitionerSync customPartitioner) {
//		if (customPartitioner == null)
//			throw new NullPointerException("partitioner cannot be null");
//		
//		if (!partitioners.contains(customPartitioner)) partitioners.add(customPartitioner);
//	}
//	
//}