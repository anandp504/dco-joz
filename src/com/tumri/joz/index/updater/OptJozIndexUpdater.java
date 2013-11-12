package com.tumri.joz.index.updater;

import com.tumri.content.data.Product;
import com.tumri.jic.joz.IJozIndexUpdater;
import com.tumri.jic.joz.PersistantIndexLine;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.IndexUtils;
import com.tumri.utils.data.SortedArraySet;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * User: scbraun
 * Date: 10/1/13
 */
public class OptJozIndexUpdater implements IJozIndexUpdater {

	private StringBuffer debugBuffer = new StringBuffer();
	private static Logger log = Logger.getLogger(JozIndexUpdater.class);
	protected ArrayList<Long> productIds = new ArrayList<Long>();
	private static final char MULTI_VALUE_INDEX_DELIM = AppProperties.getInstance().getMultiValueDelimiter();
	private static final String PRODUCT = "Product";

	private int experienceId;
	/**
	 * Reset mode will change the ADD, NOCHANGE, UPDATE to DELETE and ignore all DELETE operations
	 */
	public OptJozIndexUpdater() {
		debugBuffer = new StringBuffer(); //need to clear buffer each time this class is constructed.
	}

	public void setProdIds(ArrayList<Long> prods) {
		productIds = prods;
	}

	public void setExperienceId(int experienceId){
		this.experienceId = experienceId;
	}

	public void reset() {
		productIds.clear();
		debugBuffer = new StringBuffer();
	}

	public Object getHandle(long pid) {
		return ProductDB.getInstance().getHandle(pid);
	}

	private SortedArraySet<Handle> productHandles = new SortedArraySet<Handle>();

	public Object createNewHandle(long pid, boolean bDebug) {
		ProductHandle p = new ProductHandle(1.0, pid);
		if (!bDebug) {
			productHandles.add(p);
		}
		return p;
	}


	/**
	 * Handle the event when a set of index details are read in
	 *
	 * @param indexName Name of the index that is being updated
	 * @param pids      Current set of products
	 */
	public void handleLine(String indexType, String indexName, ArrayList<Object> pids, PersistantIndexLine.IndexOperation operation, boolean debug, boolean hotload) {
		ArrayList<Handle> handleList = new ArrayList<Handle>(pids.size());
		for (Object o : pids) {
			handleList.add((Handle) o);
		}

		if (debug) {
			if (productIds.size() > 0) {
				handleDebug(indexType, indexName, handleList, operation, productIds);
			} else {
				handleDebug(indexType, indexName, handleList, operation);
			}
		} else {
			if (operation != PersistantIndexLine.IndexOperation.kDelete && !productHandles.isEmpty()) {
				ProductDB.getInstance().addNewProducts(productHandles);  //todo: should we create new handles?
				productHandles = new SortedArraySet<Handle>();
			}
			//Update the ProductDB
			handleUpdate(indexType, indexName, handleList, operation, hotload);
		}
	}

	/**
	 * Return the buffer that contains the details read from the index files
	 *
	 * @return
	 */
	public StringBuffer getBuffer() {
		return debugBuffer;
	}

	/**
	 * Write the index details into a StringBuffer - in the case of debug mode
	 *
	 * @param indexType
	 * @param indexVal
	 * @param pids
	 * @param operation
	 * @param ids
	 */
	protected void handleDebug(String indexType, String indexVal, ArrayList<Handle> pids, PersistantIndexLine.IndexOperation operation, ArrayList<Long> ids) {
		boolean flag = false;
		char _delim = '\t';

		String mode = "";
		switch (operation) {
			case kAdd:
				mode = "ADD";
				break;
		}
		StringBuffer line = new StringBuffer();
		String preFix = indexType + _delim + indexVal + _delim + mode + _delim;

		int count = 0;
		boolean bNewLine = true;
		for (Handle p : pids) {
			long tempId = p.getOid();               //new line
			if (ids.contains(new Long(tempId))) {     //new line
				flag = true;
				if (bNewLine) {
					line.append(preFix);
					bNewLine = false;
				}
				line.append(tempId + ",");          //changed line
				count++;
				if (count > 24) {
					line.append("\n");
					count = 0;
					bNewLine = true;
				}
			}
		}
		if (flag) {
			line.append("\n");
		}
		debugBuffer.append(line);

	}


	/**
	 * Write the current set of lines to the console. Do not update the Joz Index.
	 * The output file will be a tab delimited file of the format:
	 * <Index Name> <Index Val> <MODE>  <Comma separated product IDs>
	 */
	private void handleDebug(String indexType, String indexVal, ArrayList<Handle> pids, PersistantIndexLine.IndexOperation operation) {
		char _delim = '\t';

		String mode = "";
		switch (operation) {
			case kAdd:
				mode = "ADD";
				break;
		}
		StringBuffer line = new StringBuffer();
		String preFix = indexType + _delim + indexVal + _delim + mode + _delim;

		int count = 0;
		boolean bNewLine = true;
		for (Handle p : pids) {
			if (bNewLine) {
				line.append(preFix);
				bNewLine = false;
			}
			line.append(p.getOid() + ",");
			count++;
			if (count > 24) {
				line.append("\n");
				count = 0;
				bNewLine = true;
			}
		}
		line.append("\n");
		debugBuffer.append(line);

	}

	/**
	 * Update the index
	 */
	private void handleUpdate(String indexType, String indexName, ArrayList<Handle> pids, PersistantIndexLine.IndexOperation operation, boolean hotload) {
		if (!hotload) {
			if (operation == PersistantIndexLine.IndexOperation.kDelete
					|| operation == PersistantIndexLine.IndexOperation.kDelModified) {
				//Skip Deletes for Cold Start
				return;
			}
		} else {
			//Skip no change for Hot load
			if (operation == PersistantIndexLine.IndexOperation.kNoChange) {
				return;
			}
		}
		updateIndex(operation, IndexUtils.getAttribute(indexType), indexName, pids);

	}


	/**
	 * Update the given set of pids into the index.
	 *
	 * @param idxAttr
	 * @param pids
	 */
	private void updateIndex(PersistantIndexLine.IndexOperation operation, Product.Attribute idxAttr,
	                                String indexVal, ArrayList<Handle> pids) {
		if (indexVal == null || "".equals(indexVal)) {
			return;
		}

		if(idxAttr == Product.Attribute.kExperienceId ||
				idxAttr == Product.Attribute.kExperienceIdF1 ||
				idxAttr == Product.Attribute.kExperienceIdF2 ||
				idxAttr == Product.Attribute.kExperienceIdF3 ||
				idxAttr == Product.Attribute.kExperienceIdF4 ||
				idxAttr == Product.Attribute.kExperienceIdF5 ||
				idxAttr == Product.Attribute.kExperienceIdUT1 ||
				idxAttr == Product.Attribute.kExperienceIdUT2 ||
				idxAttr == Product.Attribute.kExperienceIdUT3 ||
				idxAttr == Product.Attribute.kExperienceIdUT4 ||
				idxAttr == Product.Attribute.kExperienceIdUT5) {
			indexVal = indexVal.toLowerCase();
			TreeMap<Integer, ArrayList<Handle>> mindex = new TreeMap<Integer, ArrayList<Handle>>();
			mindex.put(IndexUtils.getIndexIdFromDictionary(idxAttr, indexVal), pids); //todo: perhaps use same dictionary for F1/ut1/tspecf1/tspecut1

			ProductDB.getInstance().overwriteOptIndex(idxAttr, experienceId, mindex);

		}

	}

	/**
	 * Updates the double index
	 *
	 * @param idxAttr
	 * @param indexVal
	 * @param operation
	 * @param pids
	 */
	private void updateIntegerIndex(Product.Attribute idxAttr, Integer indexVal, PersistantIndexLine.IndexOperation operation, ArrayList<Handle> pids) {
		TreeMap<Integer, ArrayList<Handle>> mindex = new TreeMap<Integer, ArrayList<Handle>>();
		mindex.put(indexVal, pids);
		if (operation == PersistantIndexLine.IndexOperation.kAdd || operation == PersistantIndexLine.IndexOperation.kAddModified
				|| operation == PersistantIndexLine.IndexOperation.kNoChange) {
			ProductDB.getInstance().updateIntegerIndex(idxAttr, mindex);
		} else
		if (operation == PersistantIndexLine.IndexOperation.kDelModified || operation == PersistantIndexLine.IndexOperation.kDelete) {
			ProductDB.getInstance().deleteIntegerIndex(idxAttr, mindex);
		}
	}

}
