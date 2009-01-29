package com.tumri.joz.index.creator;

import com.tumri.content.data.Product;
import com.tumri.content.data.ProductAttributeDetails;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.JozIndexHelper;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.IndexUtils;
import com.tumri.joz.utils.ZipCodeDB;
import com.tumri.utils.strings.StringTokenizer;
import com.tumri.utils.Pair;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Implementation class that will handle the update of Joz Indexes, or write to a buffer for inspection purposes.
 * @author: nipun
 * Date: Feb 15, 2008
 * Time: 8:46:27 PM
 */
public class JozIndexUpdater {
	private StringBuffer debugBuffer = new StringBuffer();
	private static Logger log = Logger.getLogger(JozIndexUpdater.class);
	protected ArrayList<Long> productIds = new ArrayList<Long>();
	private static final char MULTI_VALUE_INDEX_DELIM = AppProperties.getInstance().getMultiValueDelimiter();


	public JozIndexUpdater() {
		debugBuffer = new StringBuffer(); //need to clear buffer each time this class is constructed.
	}

	public void setProdIds(ArrayList<Long> prods) {
		productIds = prods;
	}

	public void reset() {
		productIds.clear();
		debugBuffer = new StringBuffer();
	}

	/**
	 * Handle the event when a set of index details are read in
	 * @param indexName Name of the index that is being updated
	 * @param pids Current set of products
	 */
	public void handleLine(String indexType, String indexName, ArrayList<Handle> pids, PersistantIndexLine.IndexOperation operation) {
		if (JozIndexHelper.getInstance().isDebugMode()) {
			if(productIds.size()>0){
				handleDebug(indexType,  indexName,pids, operation, productIds);
			}   else {
				handleDebug(indexType,  indexName,pids, operation);
			}
		} else {
			//Update the ProductDB
			handleUpdate(indexType, indexName,pids, operation);
		}
	}

	/**
	 * Return the buffer that contains the details read from the index files
	 * @return
	 */
	public StringBuffer getBuffer(){
		return debugBuffer;
	}

	/**
	 * Write the index details into a StringBuffer - in the case of debug mode
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
		switch(operation) {
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
		for (Handle p:pids) {
			long tempId = p.getOid();               //new line
			if(ids.contains(new Long(tempId))){     //new line
				flag = true;
				if (bNewLine) {
					line.append(preFix);
					bNewLine = false;
				}
				line.append(tempId + ",");          //changed line
				count++;
				if (count>24){
					line.append("\n");
					count =0;
					bNewLine = true;
				}
			}
		}
		if(flag){
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
		switch(operation) {
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
		for (Handle p:pids) {
			if (bNewLine) {
				line.append(preFix);
				bNewLine = false;
			}
			line.append(p.getOid() + ",");
			count++;
			if (count>24){
				line.append("\n");
				count =0;
				bNewLine = true;
			}
		}
		line.append("\n");
		debugBuffer.append(line);

	}

	/**
	 * Update the index
	 */
	private void handleUpdate(String indexType, String indexName, ArrayList<Handle> pids, PersistantIndexLine.IndexOperation operation) {
		if (JozIndexHelper.getInstance().isColdStart())  {
			if (operation == PersistantIndexLine.IndexOperation.kDelete
					|| operation == PersistantIndexLine.IndexOperation.kDelModified) {
				//Skip Deletes for Cold Start
				return;
			}
		} else {
			//Skip no change for Hot load
			if (operation ==  PersistantIndexLine.IndexOperation.kNoChange) {
				return;
			}
		}
		updateIndex(operation, IndexUtils.getAttribute(indexType), indexName, pids);

	}


	/**
	 * Update the given set of pids into the index.
	 * @param idxAttr
	 * @param pids
	 */
	private static void updateIndex(PersistantIndexLine.IndexOperation operation, Product.Attribute idxAttr,
	                                String indexVal, ArrayList<Handle> pids) {
        if (indexVal == null || "".equals(indexVal)) {
			return;
		}
		//Handle the Provider Category using the same Category index - since Joz does not differentiate between these
		if (idxAttr == Product.Attribute.kProviderCategory ) {
			idxAttr = Product.Attribute.kCategory;
		}

		if (idxAttr == Product.Attribute.kCPC || idxAttr == Product.Attribute.kCPO || idxAttr == Product.Attribute.kPrice) {
			//Double
			TreeMap<Double, ArrayList<Handle>> mindex = new TreeMap<Double, ArrayList<Handle>>();
			mindex.put(new Double(indexVal), pids);
			if (operation == PersistantIndexLine.IndexOperation.kAdd || operation == PersistantIndexLine.IndexOperation.kAddModified
					|| operation == PersistantIndexLine.IndexOperation.kNoChange) {
				ProductDB.getInstance().updateDoubleIndex(idxAttr, mindex);
			} else if (operation == PersistantIndexLine.IndexOperation.kDelModified || operation == PersistantIndexLine.IndexOperation.kDelete){
				ProductDB.getInstance().deleteDoubleIndex(idxAttr, mindex);
			}
		}  else if (idxAttr == Product.Attribute.kCategory ||
				idxAttr == Product.Attribute.kBrand ||
				idxAttr == Product.Attribute.kSupplier ||
				idxAttr == Product.Attribute.kProvider ||
//				idxAttr == Product.Attribute.kImageWidth ||
//				idxAttr == Product.Attribute.kImageHeight ||
//				idxAttr == Product.Attribute.kProductType ||
				idxAttr == Product.Attribute.kGeoEnabledFlag ||
				idxAttr == Product.Attribute.kProviderCategory ||
				idxAttr == Product.Attribute.kGlobalId) {
			//Integer
            TreeMap<Integer, ArrayList<Handle>> mindex = new TreeMap<Integer, ArrayList<Handle>>();
			mindex.put(IndexUtils.getIndexIdFromDictionary(idxAttr, indexVal), pids);
			if (operation == PersistantIndexLine.IndexOperation.kAdd || operation == PersistantIndexLine.IndexOperation.kAddModified
					|| operation == PersistantIndexLine.IndexOperation.kNoChange) {
				ProductDB.getInstance().updateIntegerIndex(idxAttr, mindex);
			} else if (operation == PersistantIndexLine.IndexOperation.kDelModified || operation == PersistantIndexLine.IndexOperation.kDelete){
				ProductDB.getInstance().deleteIntegerIndex(idxAttr, mindex);
			}

		} else if (idxAttr == Product.Attribute.kCategoryField1 ||
				idxAttr == Product.Attribute.kCategoryField2 ||
				idxAttr == Product.Attribute.kCategoryField3 ||
				idxAttr == Product.Attribute.kCategoryField4 ||
				idxAttr == Product.Attribute.kCategoryField5) {
			String catIdStr = indexVal.substring(0,indexVal.indexOf("|"));
			int catId = IndexUtils.getIndexIdFromDictionary(Product.Attribute.kCategory, catIdStr);
			ProductAttributeDetails details = IndexUtils.getDetailsForCategoryField(catId, idxAttr);
			if (details != null) {
				ProductAttributeDetails.DataType type = details.getFieldtype();
				if (type != null) {
					TreeMap<Long, ArrayList<Handle>> mindex = new TreeMap<Long, ArrayList<Handle>>();
					String fieldValStr = indexVal.substring(indexVal.indexOf("|") +1, indexVal.length());
					int fieldValId = 0;
					if (type == ProductAttributeDetails.DataType.kText) {
						fieldValId = IndexUtils.getIndexIdFromDictionary(idxAttr, fieldValStr);
					} else  if (type == ProductAttributeDetails.DataType.kInteger) {
						//Multiply by 100 to support upto 2 decimal places
						if (fieldValStr.indexOf('.')>-1) {
							//Double value
							Double tmpDbl = Double.parseDouble(fieldValStr)*100;
							fieldValId = tmpDbl.intValue();
						} else {
							fieldValId = Integer.parseInt(fieldValStr)*100;
						}
					}
					long key = IndexUtils.createIndexKeyForCategoryAttribute(catId, idxAttr, fieldValId);
					mindex.put(key, pids);

					if (operation == PersistantIndexLine.IndexOperation.kAdd ||
							operation == PersistantIndexLine.IndexOperation.kAddModified
							|| operation == PersistantIndexLine.IndexOperation.kNoChange) {
						if (type == ProductAttributeDetails.DataType.kText) {
							//Text index
							ProductDB.getInstance().updateLongIndex(Product.Attribute.kCategoryTextField, mindex);
						} else  if (type == ProductAttributeDetails.DataType.kInteger) {
							//Range Index
							ProductDB.getInstance().updateLongIndex(Product.Attribute.kCategoryNumericField, mindex);
						}
					} else if (operation == PersistantIndexLine.IndexOperation.kDelModified ||
							operation == PersistantIndexLine.IndexOperation.kDelete){
						if (type == ProductAttributeDetails.DataType.kText) {
							//Text index
							ProductDB.getInstance().deleteLongIndex(Product.Attribute.kCategoryTextField, mindex);
						} else  if (type == ProductAttributeDetails.DataType.kInteger) {
							//Range Index
							ProductDB.getInstance().deleteLongIndex(Product.Attribute.kCategoryNumericField, mindex);
						}
					}


				}
			}

		}  else if (idxAttr == Product.Attribute.kExternalFilterField1 ||
				idxAttr == Product.Attribute.kExternalFilterField2 ||
				idxAttr == Product.Attribute.kExternalFilterField3 ||
				idxAttr == Product.Attribute.kExternalFilterField4 ||
				idxAttr == Product.Attribute.kExternalFilterField5) {
			//Multi value index.
			StringTokenizer st = new StringTokenizer(indexVal, MULTI_VALUE_INDEX_DELIM);
			ArrayList<String> indexVals = st.getTokens();
			for (String val: indexVals) {
				if (val == null) {
					continue;
				}
				//Url decode
				try {
					val = URLDecoder.decode(val,"utf-8");
					val = val.toLowerCase();
				} catch(UnsupportedEncodingException e){
					log.error("Could not decode the value : " + val);
					continue;
				}
				TreeMap<Long, ArrayList<Handle>> mindex = new TreeMap<Long, ArrayList<Handle>>();
				long key = IndexUtils.createLongIndexKey(idxAttr, IndexUtils.getIndexIdFromDictionary(idxAttr, val));
				mindex.put(key, pids);
				if (operation == PersistantIndexLine.IndexOperation.kAdd || operation == PersistantIndexLine.IndexOperation.kAddModified
						|| operation == PersistantIndexLine.IndexOperation.kNoChange) {
					ProductDB.getInstance().updateLongIndex(Product.Attribute.kMultiValueTextField, mindex);
				} else if (operation == PersistantIndexLine.IndexOperation.kDelModified || operation == PersistantIndexLine.IndexOperation.kDelete){
					ProductDB.getInstance().deleteLongIndex(Product.Attribute.kMultiValueTextField, mindex);
				}
			}
		}  else if (idxAttr == Product.Attribute.kCountry ||
				idxAttr == Product.Attribute.kState ||
				idxAttr == Product.Attribute.kCity ||
				idxAttr == Product.Attribute.kZip ||
				idxAttr == Product.Attribute.kDMA ||
				idxAttr == Product.Attribute.kArea) {
			//Multi value index.
			StringTokenizer st = new StringTokenizer(indexVal, MULTI_VALUE_INDEX_DELIM);
			ArrayList<String> indexVals = st.getTokens();
			for (String val: indexVals) {
				if (val == null) {
					continue;
				}
                val = val.toLowerCase();
                TreeMap<Integer, ArrayList<Handle>> mindex = new TreeMap<Integer, ArrayList<Handle>>();
				mindex.put(IndexUtils.getIndexIdFromDictionary(idxAttr, val), pids);
				if (operation == PersistantIndexLine.IndexOperation.kAdd || operation == PersistantIndexLine.IndexOperation.kAddModified
						|| operation == PersistantIndexLine.IndexOperation.kNoChange) {
					ProductDB.getInstance().updateIntegerIndex(idxAttr, mindex);
				} else if (operation == PersistantIndexLine.IndexOperation.kDelModified || operation == PersistantIndexLine.IndexOperation.kDelete){
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
     * @param zipCode
     * @param op
     * @param pids
     */
    private static void updateLatLongIndex(String zipCode, PersistantIndexLine.IndexOperation op,ArrayList<Handle> pids) {
        Integer lat, along;
        Pair<Integer, Integer> latlong = ZipCodeDB.getInstance().getNormalizedLatLong(zipCode);
        if (latlong==null){
            log.warn("Could not get the lat long for the zip code : " + zipCode);
            return;
        }
        lat = latlong.getFirst();
        along = latlong.getSecond();

        if (lat!=null && along !=null ) {
            updateIntegerIndex(Product.Attribute.kLatitude, lat, op, pids);
            updateIntegerIndex(Product.Attribute.kLongitude, along, op, pids);
        }

    }

    /**
     * Updates the double index
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
        } else if (operation == PersistantIndexLine.IndexOperation.kDelModified || operation == PersistantIndexLine.IndexOperation.kDelete){
            ProductDB.getInstance().deleteIntegerIndex(idxAttr, mindex);
        }
    }
}
