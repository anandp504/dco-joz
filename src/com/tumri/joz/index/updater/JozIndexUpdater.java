package com.tumri.joz.index.updater;

import com.tumri.content.data.Product;
import com.tumri.jic.joz.IJozIndexUpdater;
import com.tumri.jic.joz.PersistantIndexLine;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.IndexUtils;
import com.tumri.joz.utils.ZipCodeDB;
import com.tumri.utils.Pair;
import com.tumri.utils.data.SortedArraySet;
import com.tumri.utils.strings.StringTokenizer;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Implementation class that will handle the update of Joz Indexes, or write to a buffer for inspection purposes.
 *
 * @author: nipun
 * Date: Feb 15, 2008
 * Time: 8:46:27 PM
 */
public class JozIndexUpdater implements IJozIndexUpdater {
	private StringBuffer debugBuffer = new StringBuffer();
	private static Logger log = Logger.getLogger(JozIndexUpdater.class);
	protected ArrayList<Long> productIds = new ArrayList<Long>();
	private static final char MULTI_VALUE_INDEX_DELIM = AppProperties.getInstance().getMultiValueDelimiter();
	private static final String PRODUCT = "Product";
	/**
	 * Reset mode will change the ADD, NOCHANGE, UPDATE to DELETE and ignore all DELETE operations
	 */
	private boolean m_resetMode = false;

	public JozIndexUpdater() {
		debugBuffer = new StringBuffer(); //need to clear buffer each time this class is constructed.
	}

	public JozIndexUpdater(boolean reset) {
		debugBuffer = new StringBuffer(); //need to clear buffer each time this class is constructed.
		m_resetMode = reset;
	}

	public void setProdIds(ArrayList<Long> prods) {
		productIds = prods;
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
		if (m_resetMode) {
			if (operation != PersistantIndexLine.IndexOperation.kDelete)
				operation = PersistantIndexLine.IndexOperation.kDelete;
		}
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
				ProductDB.getInstance().addNewProducts(productHandles);
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
			case kAddModified:
				mode = "ADD-MOD";
				break;
			case kDelete:
				mode = "DELETE";
				break;
			case kDelModified:
				mode = "DELETE-MOD";
				break;
			case kNoChange:
				mode = "NO-CHANGE";
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
			case kAddModified:
				mode = "ADD-MOD";
				break;
			case kDelete:
				mode = "DELETE";
				break;
			case kDelModified:
				mode = "DELETE-MOD";
				break;
			case kNoChange:
				mode = "NO-CHANGE";
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
	private static void updateIndex(PersistantIndexLine.IndexOperation operation, Product.Attribute idxAttr,
	                                String indexVal, ArrayList<Handle> pids) {
		if (indexVal == null || "".equals(indexVal)) {
			return;
		}
		//Handle the Provider Category using the same Category index - since Joz does not differentiate between these
		if (idxAttr == Product.Attribute.kProviderCategory) {
			idxAttr = Product.Attribute.kCategory;
		}

		if (idxAttr == Product.Attribute.kCPC || idxAttr == Product.Attribute.kCPO || idxAttr == Product.Attribute.kPrice) {
			//Double
			TreeMap<Double, ArrayList<Handle>> mindex = new TreeMap<Double, ArrayList<Handle>>();
			mindex.put(new Double(indexVal), pids);
			if (operation == PersistantIndexLine.IndexOperation.kAdd || operation == PersistantIndexLine.IndexOperation.kAddModified
					|| operation == PersistantIndexLine.IndexOperation.kNoChange) {
				ProductDB.getInstance().updateDoubleIndex(idxAttr, mindex);
			} else
			if (operation == PersistantIndexLine.IndexOperation.kDelModified || operation == PersistantIndexLine.IndexOperation.kDelete) {
				ProductDB.getInstance().deleteDoubleIndex(idxAttr, mindex);
			}
		} else if (idxAttr == Product.Attribute.kAge) {
			Integer val = IndexUtils.getIndexIdFromDictionary(idxAttr, indexVal.toUpperCase());
			//No need to update the index for this - just update the handle
			for (Handle h : pids) {
				((ProductHandle) h).setAge(val);
			}
		} else if (idxAttr == Product.Attribute.kGender) {
			Integer val = IndexUtils.getIndexIdFromDictionary(idxAttr, indexVal.toUpperCase());
			//No need to update the index for this - just update the handle
			for (Handle h : pids) {
				((ProductHandle) h).setGender(val);
			}
		} else if (idxAttr == Product.Attribute.kProductType) {
			if (indexVal == null || indexVal.equals("")) {
				indexVal = PRODUCT;
			}
			Integer val = IndexUtils.getIndexIdFromDictionary(idxAttr, indexVal.toUpperCase());
			//No need to update the index for this - just update the handle
			for (Handle h : pids) {
				((ProductHandle) h).setProductType(val);
			}
		} else if (idxAttr == Product.Attribute.kHHI) {
			Integer val = IndexUtils.getIndexIdFromDictionary(idxAttr, indexVal.toUpperCase());
			//No need to update the index for this - just update the handle
			for (Handle h : pids) {
				((ProductHandle) h).setHouseHoldIncome(val);
			}
		} else if (idxAttr == Product.Attribute.kCC) {
			Integer val = IndexUtils.getIndexIdFromDictionary(idxAttr, indexVal.toUpperCase());
			//No need to update the index for this - just update the handle
			for (Handle h : pids) {
				((ProductHandle) h).setChildCount(val);
			}
		} else if (idxAttr == Product.Attribute.kRank || idxAttr == Product.Attribute.kDiscount) {
			Integer val = null;
			//Rank and Discount have index to be loaded and also the handle needs to be updated.
			//Rank and Discount are expected to be integers from 1 - 100
			try {
				val = new Integer(indexVal);
				if (val < 0) {
					val = 0;
				} else if (val > 100) {
					val = 100;
				}
			} catch (Exception e) {
				val = 0;
			}
			if (val == 0) {
				return; //Skip items that have no rank or discount.
			}
			TreeMap<Integer, ArrayList<Handle>> mindex = new TreeMap<Integer, ArrayList<Handle>>();
			//add all products with a rank/discount to a single bucket
			mindex.put(0, pids);
			if (operation == PersistantIndexLine.IndexOperation.kAdd || operation == PersistantIndexLine.IndexOperation.kAddModified
					|| operation == PersistantIndexLine.IndexOperation.kNoChange) {
				ProductDB.getInstance().updateIntegerIndex(idxAttr, mindex);
			} else
			if (operation == PersistantIndexLine.IndexOperation.kDelModified || operation == PersistantIndexLine.IndexOperation.kDelete) {
				ProductDB.getInstance().deleteIntegerIndex(idxAttr, mindex);
			}
			if (idxAttr == Product.Attribute.kDiscount) {
				for (Handle h : pids) {
					((ProductHandle) h).setDiscount(val);
				}
			} else {
				for (Handle h : pids) {
					((ProductHandle) h).setRank(val);
				}
			}
		} else if (idxAttr == Product.Attribute.kCategory ||
				idxAttr == Product.Attribute.kBrand ||
				idxAttr == Product.Attribute.kSupplier ||
				idxAttr == Product.Attribute.kProvider ||
				idxAttr == Product.Attribute.kGeoEnabledFlag ||
				idxAttr == Product.Attribute.kProviderCategory ||
				idxAttr == Product.Attribute.kBT ||
				idxAttr == Product.Attribute.kGlobalId) {
			//Integer
			TreeMap<Integer, ArrayList<Handle>> mindex = new TreeMap<Integer, ArrayList<Handle>>();
			Integer val = IndexUtils.getIndexIdFromDictionary(idxAttr, indexVal);
			mindex.put(val, pids);
			if (operation == PersistantIndexLine.IndexOperation.kAdd || operation == PersistantIndexLine.IndexOperation.kAddModified
					|| operation == PersistantIndexLine.IndexOperation.kNoChange) {
				ProductDB.getInstance().updateIntegerIndex(idxAttr, mindex);
			} else
			if (operation == PersistantIndexLine.IndexOperation.kDelModified || operation == PersistantIndexLine.IndexOperation.kDelete) {
				ProductDB.getInstance().deleteIntegerIndex(idxAttr, mindex);
			}

		} else if (idxAttr == Product.Attribute.kExternalFilterField1 ||
				idxAttr == Product.Attribute.kExternalFilterField2 ||
				idxAttr == Product.Attribute.kExternalFilterField3 ||
				idxAttr == Product.Attribute.kExternalFilterField4 ||
				idxAttr == Product.Attribute.kExternalFilterField5 ||
				idxAttr == Product.Attribute.kUT1 ||
				idxAttr == Product.Attribute.kUT2 ||
				idxAttr == Product.Attribute.kUT3 ||
				idxAttr == Product.Attribute.kUT4 ||
				idxAttr == Product.Attribute.kUT5
				) {
			//Multi value index.
			StringTokenizer st = new StringTokenizer(indexVal, MULTI_VALUE_INDEX_DELIM);
			ArrayList<String> indexVals = st.getTokens();
			for (String val : indexVals) {
				if (val == null) {
					continue;
				}
				//Url decode
				try {
					val = URLDecoder.decode(val, "utf-8");
					val = val.toLowerCase();
				} catch (UnsupportedEncodingException e) {
					log.error("Could not decode the value : " + val);
					continue;
				} catch (IllegalArgumentException e) {
					val = val.toLowerCase();
					//the underlying issue is that LP is not encoding ut1-ut5
					//log.error("Could not decode the value, IllegalArgumentException : " + val);
					//we are not logging this error because most of content has passthru fields populated and joz-logs could bloat.
					//continue;
				}
				TreeMap<Long, ArrayList<Handle>> mindex = new TreeMap<Long, ArrayList<Handle>>();
				long key = IndexUtils.createLongIndexKey(idxAttr, IndexUtils.getIndexIdFromDictionary(idxAttr, val));
				mindex.put(key, pids);
				if (operation == PersistantIndexLine.IndexOperation.kAdd || operation == PersistantIndexLine.IndexOperation.kAddModified
						|| operation == PersistantIndexLine.IndexOperation.kNoChange) {
					ProductDB.getInstance().updateLongIndex(Product.Attribute.kMultiValueTextField, mindex);
				} else
				if (operation == PersistantIndexLine.IndexOperation.kDelModified || operation == PersistantIndexLine.IndexOperation.kDelete) {
					ProductDB.getInstance().deleteLongIndex(Product.Attribute.kMultiValueTextField, mindex);
				}
			}
		} else if (idxAttr == Product.Attribute.kCountry ||
				idxAttr == Product.Attribute.kState ||
				idxAttr == Product.Attribute.kCity ||
				idxAttr == Product.Attribute.kZip ||
				idxAttr == Product.Attribute.kDMA ||
				idxAttr == Product.Attribute.kArea) {
			//Multi value index.
			StringTokenizer st = new StringTokenizer(indexVal, MULTI_VALUE_INDEX_DELIM);
			ArrayList<String> indexVals = st.getTokens();
			for (String val : indexVals) {
				if (val == null) {
					continue;
				}
				val = val.toLowerCase();
				TreeMap<Integer, ArrayList<Handle>> mindex = new TreeMap<Integer, ArrayList<Handle>>();
				mindex.put(IndexUtils.getIndexIdFromDictionary(idxAttr, val), pids);
				if (operation == PersistantIndexLine.IndexOperation.kAdd || operation == PersistantIndexLine.IndexOperation.kAddModified
						|| operation == PersistantIndexLine.IndexOperation.kNoChange) {
					ProductDB.getInstance().updateIntegerIndex(idxAttr, mindex);
				} else
				if (operation == PersistantIndexLine.IndexOperation.kDelModified || operation == PersistantIndexLine.IndexOperation.kDelete) {
					ProductDB.getInstance().deleteIntegerIndex(idxAttr, mindex);
				}
				if (idxAttr == Product.Attribute.kZip) {
					//Add to lat and long index
					updateLatLongIndex(val, operation, pids);
				}
			}
		}


	}

	/**
	 * Updates the product db index for lat and long
	 *
	 * @param zipCode
	 * @param op
	 * @param pids
	 */
	private static void updateLatLongIndex(String zipCode, PersistantIndexLine.IndexOperation op, ArrayList<Handle> pids) {
		Integer lat, along;
		Pair<Integer, Integer> latlong = ZipCodeDB.getInstance().getNormalizedLatLong(zipCode);
		if (latlong == null) {
			log.debug("Could not get the lat long for the zip code : " + zipCode);
			return;
		}
		lat = latlong.getFirst();
		along = latlong.getSecond();

		if (lat != null && along != null) {
			updateIntegerIndex(Product.Attribute.kLatitude, lat, op, pids);
			updateIntegerIndex(Product.Attribute.kLongitude, along, op, pids);
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
	private static void updateIntegerIndex(Product.Attribute idxAttr, Integer indexVal, PersistantIndexLine.IndexOperation operation, ArrayList<Handle> pids) {
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
