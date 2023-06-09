package zingg.common.core.block;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BlockFunction<D,R,C,T> implements Serializable {

    public static final Log LOG = LogFactory.getLog(BlockFunction.class);
		
    Tree<Canopy<D,R,C,T>> tree;
    public BlockFunction(Tree<Canopy<D,R,C,T>> tree) {
        this.tree = tree;
    }
    
    
    public R call(R r) {
        StringBuilder bf = new StringBuilder();
        bf = applyTree(r, tree, tree.getHead(), bf);
        return createRow(r, bf); //RowFactory.create(returnList);			
    }

    public abstract List<Object> getListFromRow(R r) ;

    public abstract R getRowFromList(List<Object> lob);

    public R createRow(R r, StringBuilder bf) {
        List<Object> currentRowValues = getListFromRow(r);
        currentRowValues.add(bf.toString().hashCode());
        if (LOG.isDebugEnabled()) {
            for (Object o: currentRowValues) {
                LOG.debug("return row col is " + o );
            }
        }
        return getRowFromList(currentRowValues);
    }

    public <R> StringBuilder applyTree(R tuple, Tree<Canopy<D,R,C,T>> tree,
			Canopy<D,R,C,T>root, StringBuilder result) {
		if (root.function != null) {
			Object hash = root.function.apply(tuple, root.context.fieldName);
			
			result = result.append("|").append(hash);
			for (Canopy<D,R,C,T>c : tree.getSuccessors(root)) {
				// LOG.info("Successr hash " + c.getHash() + " and our hash "+
				// hash);
				if (c != null) {
					// //LOG.debug("c.hash " + c.getHash() + " and our hash " + hash);
					if ((c.getHash() != null)) {
						//LOG.debug("Hurdle one over ");
						if ((c.getHash().equals(hash))) {
							// //LOG.debug("Hurdle 2 start " + c);
							applyTree(tuple, tree, c, result);
							// //LOG.debug("Hurdle 2 over ");
						}
					}
				}
			}
		}
		//LOG.debug("apply first step clustering result " + result);
		return result;
	}


}
