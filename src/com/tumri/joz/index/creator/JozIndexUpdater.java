package com.tumri.joz.index.creator;

import com.tumri.content.data.CategorySpec;
import com.tumri.content.data.Product;
import com.tumri.content.data.CategoryAttributeDetails;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.JozIndexHelper;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.IndexUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Implementation class that will handle the update of Joz Indexes, or write a debug file if required.
 * @author: nipun
 * Date: Feb 15, 2008
 * Time: 8:46:27 PM
 */
public class JozIndexUpdater {

    boolean bDebug = false;
    boolean bColdStart = false;
    private static JozIndexUpdater _instance = null;
    private static Logger log = Logger.getLogger(JozIndexUpdater.class);
    private static File debugOutFile = null;

    /**
     * Return a instance of the JozIndexUpdater
     * @return
     */
    public static JozIndexUpdater getInstance() {
        if (_instance == null) {
            boolean bColdStart = JozIndexHelper.isColdStart();
            boolean bDebug = false;
            try {
                bDebug = Boolean.parseBoolean(AppProperties.getInstance().getProperty("com.tumri.joz.index.reader.debug"));
            } catch (Exception e) {
                //
            }
            if (bDebug) {
                log.info("Debug mode = true. The index will be written to a file, and not loaded into Joz");
            }
            _instance = new JozIndexUpdater(bDebug, bColdStart);
        } else {
            _instance.bColdStart = JozIndexHelper.isColdStart();
        }
        return _instance;
    }

    private JozIndexUpdater(boolean bDebug, boolean bColdStart) {
        this.bDebug = bDebug;
        this.bColdStart = bColdStart;
    }

    /**
     * To be used by the test framework
     */
    public static void setInstance(boolean coldStart, boolean bDebug, String outDir) {
        _instance = new JozIndexUpdater(bDebug, coldStart);
        File debugDir = new File(outDir);
        if (!debugDir.exists()) {
            throw new RuntimeException("Directory does not exist : " + debugDir);
        }
        debugOutFile = new File(outDir + "/jozIndexDebugFile.txt");
        if (debugOutFile.exists()) {
            debugOutFile.delete();
        }
    }

    /**
     * Public method to handle a set of lines
     * @param indexName Name of the index that is being updated
     * @param pids Current set of products
     */
    public void handleLine(String indexType, String indexName, ArrayList<Handle> pids, PersistantIndexLine.IndexOperation operation) {
        if (bDebug) {
            handleDebug(indexType,  indexName,pids, operation);
        } else {
            handleUpdate(indexType, indexName,pids, operation);
        }
    }

    /**
     * Write the current set of lines to the console. Do not update the Joz Index.
     * The output file will be a tab delimited file of the format:
     * <Index Name> <Index Val> <MODE>  <Comma separated product IDs>
     */
    private void handleDebug(String indexType, String indexVal, ArrayList<Handle> pids, PersistantIndexLine.IndexOperation operation) {
        FileWriter fw = null;
        char _delim = '\t';

        if (debugOutFile == null) {
            String debugFileDir = AppProperties.getInstance().getProperty("com.tumri.joz.index.reader.debug.outdir");
            File debugDir = new File(debugFileDir);
            if (!debugDir.exists()) {
                throw new RuntimeException("Directory does not exist : " + debugDir);
            }
            debugOutFile = new File(debugDir.getAbsolutePath() + "/jozIndex.txt");
            if (debugOutFile.exists()) {
                //Delete the file if it already exists
                debugOutFile.delete();
            }
        }

        try {
            fw = new FileWriter(debugOutFile,true);
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
                    count =0;
                    bNewLine = true;
                }
            }
            line.append("\n");
            fw.write(line.toString());

        } catch (IOException e) {
            log.error("Could not write to debug file", e);
        } finally {
            try {
                fw.close();
            } catch(Exception e) {
                //
            }
        }

    }

    /**
     * Update the index
     */
    private void handleUpdate(String indexType, String indexName, ArrayList<Handle> pids, PersistantIndexLine.IndexOperation operation) {
        if (bColdStart)  {
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
                idxAttr == Product.Attribute.kImageWidth ||
                idxAttr == Product.Attribute.kImageHeight ||
                idxAttr == Product.Attribute.kProductType ||
                idxAttr == Product.Attribute.kCountry ||
                idxAttr == Product.Attribute.kState ||
                idxAttr == Product.Attribute.kCity ||
                idxAttr == Product.Attribute.kZip ||
                idxAttr == Product.Attribute.kDMA ||
                idxAttr == Product.Attribute.kArea ||
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
            CategoryAttributeDetails details = IndexUtils.getDetailsForCategoryField(catId, idxAttr);
            if (details != null) {
                CategoryAttributeDetails.DataType type = details.getFieldtype();
                if (type != null) {
                    TreeMap<Long, ArrayList<Handle>> mindex = new TreeMap<Long, ArrayList<Handle>>();
                    String fieldValStr = indexVal.substring(indexVal.indexOf("|") +1, indexVal.length());
                    int fieldValId = 0;
                    if (type == CategoryAttributeDetails.DataType.kText) {
                        fieldValId = IndexUtils.getIndexIdFromDictionary(idxAttr, fieldValStr);
                    } else  if (type == CategoryAttributeDetails.DataType.kInteger) {
                        //Multiply by 100 to support upto 2 decimal places
                        if (fieldValStr.indexOf('.')>-1) {
                            //Double value
                            Double tmpDbl = Double.parseDouble(fieldValStr)*100;
                            fieldValId = tmpDbl.intValue();
                        } else {
                            fieldValId = Integer.parseInt(fieldValStr)*100;
                        }
                    }
                    long key = IndexUtils.createIndexKeyForCategory(catId, idxAttr, fieldValId);
                    mindex.put(key, pids);

                    if (operation == PersistantIndexLine.IndexOperation.kAdd ||
                            operation == PersistantIndexLine.IndexOperation.kAddModified
                            || operation == PersistantIndexLine.IndexOperation.kNoChange) {
                        if (type == CategoryAttributeDetails.DataType.kText) {
                            //Text index
                            ProductDB.getInstance().updateLongIndex(Product.Attribute.kCategoryTextField, mindex);
                        } else  if (type == CategoryAttributeDetails.DataType.kInteger) {
                            //Range Index
                            ProductDB.getInstance().updateLongIndex(Product.Attribute.kCategoryNumericField, mindex);
                        }
                    } else if (operation == PersistantIndexLine.IndexOperation.kDelModified ||
                            operation == PersistantIndexLine.IndexOperation.kDelete){
                        if (type == CategoryAttributeDetails.DataType.kText) {
                            //Text index
                            ProductDB.getInstance().deleteLongIndex(Product.Attribute.kCategoryTextField, mindex);
                        } else  if (type == CategoryAttributeDetails.DataType.kInteger) {
                            //Range Index
                            ProductDB.getInstance().deleteLongIndex(Product.Attribute.kCategoryNumericField, mindex);
                        }
                    }


                }
            }

        }


    }
}
