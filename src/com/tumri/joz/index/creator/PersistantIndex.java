package com.tumri.joz.index.creator;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.products.ProductHandleFactory;
import com.tumri.content.data.dictionary.DictionaryManager;
import com.tumri.content.data.Product;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Container class to hold the index lines
 * @author: nipun
 * Date: Feb 15, 2008
 * Time: 1:03:46 PM
 */
public class PersistantIndex implements Serializable{

    static final long serialVersionUID = 1L;
    private String indexName;
    private int numLines;

    private TreeSet<PersistantIndexLine> details;

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public TreeSet<PersistantIndexLine> getDetails() {
        return details;
    }

    public void setDetails(TreeSet<PersistantIndexLine> details) {
        this.details = details;
    }

    public void addDetails(TreeSet<PersistantIndexLine> moreDetails) {
        if (moreDetails!=null) {
            if (details==null) {
                details = moreDetails;
            } else {
                details.addAll(moreDetails);
            }
        }
    }

    /**
     * Override the default writeObject method
     * @param out
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        if (indexName!=null && details!=null && details.size() > 0) {
            out.writeInt(indexName.length());
            out.writeChars(indexName);
            numLines = details.size();
            out.writeInt(numLines);
            for (PersistantIndexLine line: details) {
                //out.writeObject(line);
                out.writeObject(line.getOperation());
                out.writeInt(line.getIndexValue().length());
                out.writeChars(line.getIndexValue());
                Long[] pidsArr = line.getPids();
                out.writeInt(pidsArr.length);
                for (int i=0;i< pidsArr.length;i++){
                    out.writeLong(pidsArr[i]);
                }
            }
        }
    }

    /**
     * Override the default readObject method
     * This method needs to be able to update joz index, or to write to a debug outfile depending upon somesort of flag.
     * @param in
     * @throws IOException
     */
    private void readObject(ObjectInputStream in) throws IOException {
        int indexNameLen = in.readInt();
        StringBuffer sbuff = new StringBuffer();
        for (int i=0;i<indexNameLen;i++){
            sbuff.append(in.readChar());
        }
        String indexName = sbuff.toString();
        int numLines =  in.readInt();
        for (int i=0;i<numLines;i++) {
            try {
                PersistantIndexLine.IndexOperation op = (PersistantIndexLine.IndexOperation)in.readObject();
                int numIndexVal = in.readInt();
                StringBuffer sivbuff = new StringBuffer();
                for (int j=0;j<numIndexVal;j++){
                    sivbuff.append(in.readChar());
                }
                String indexVal = sivbuff.toString();
                int numPids = in.readInt();
                ArrayList<Handle> currPids = new ArrayList<Handle>(numPids);
                for (int k=0;k<numPids;k++) {
                    long pid = in.readLong();
                    //Get the handle
                    Handle p = ProductDB.getInstance().getHandle(pid);
                    if (p==null) {
                        //New product
                        p = ProductHandleFactory.getInstance().getHandle(pid);
                    }
                    currPids.add(p);
                }
                //Now add these to the index
                JozIndexUpdater.getInstance().handleLine(indexName, indexVal, currPids, op);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new NotSerializableException("ClassNotFoundException caught during read");
            }
        }
    }


}
