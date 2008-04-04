package com.tumri.joz.index.creator;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Class that will hold the add, delete and no-change map corresponding to a specific index
 *
 */
public class ProviderIndexBuilder {

    private String indexName = null;
    private HashMap<String, ArrayList<Long>> addIndexMap = null;
    private HashMap<String, ArrayList<Long>> addModifiedIndexMap = null;
    private HashMap<String, ArrayList<Long>> delModifiedIndexMap = null;
    private HashMap<String, ArrayList<Long>> ncIndexMap = null;
    private HashMap<String, ArrayList<Long>> delIndexMap = null;

    /**
     * Default constructor for the Index builder.
     */
    public ProviderIndexBuilder(String indexName) {
        this.indexName = indexName;
        addIndexMap = new HashMap<String, ArrayList<Long>>();
        ncIndexMap = new HashMap<String, ArrayList<Long>>();
        delIndexMap = new HashMap<String, ArrayList<Long>>();
        addModifiedIndexMap = new HashMap<String, ArrayList<Long>>();
        delModifiedIndexMap = new HashMap<String, ArrayList<Long>>();
    }

    public void handleAdd(String key, Long productId) {
       ArrayList<Long> pids = addIndexMap.get(key);
        if (pids == null) {
           pids = new ArrayList<Long>();
           pids.add(productId);
        } else {
           pids.add(productId);
        }
        addIndexMap.put(key, pids);
    }

    public void handleAddModified(String key, Long productId) {
       ArrayList<Long> pids = addModifiedIndexMap.get(key);
        if (pids == null) {
           pids = new ArrayList<Long>();
           pids.add(productId);
        } else {
           pids.add(productId);
        }
        addModifiedIndexMap.put(key, pids);
    }

    public void handleNoChange(String key, Long productId) {
        ArrayList<Long> pids = ncIndexMap.get(key);
         if (pids == null) {
            pids = new ArrayList<Long>();
            pids.add(productId);
         } else {
            pids.add(productId);
         }
         ncIndexMap.put(key, pids);
    }

    public void handleDelete(String key, Long productId) {
        ArrayList<Long> pids = delIndexMap.get(key);
         if (pids == null) {
            pids = new ArrayList<Long>();
            pids.add(productId);
         } else {
            pids.add(productId);
         }
         delIndexMap.put(key, pids);
    }

    public void handleDeleteModified(String key, Long productId) {
        ArrayList<Long> pids = delModifiedIndexMap.get(key);
         if (pids == null) {
            pids = new ArrayList<Long>();
            pids.add(productId);
         } else {
            pids.add(productId);
         }
         delModifiedIndexMap.put(key, pids);
    }


    /**
     * Serialize the index and reset. All modes of the index is written out
     * @return
     */
    public PersistantIndex serializeIndex() {
        PersistantIndex pIndex = new PersistantIndex();
        pIndex.setIndexName(indexName);
        //Write the add
        if (!addIndexMap.isEmpty()) {
            Set<String> indexDetailsSet = addIndexMap.keySet();
            ArrayList<String> sortedIndexVals = new ArrayList<String>();
            sortedIndexVals.addAll(indexDetailsSet);
            Collections.sort(sortedIndexVals);
            TreeSet<PersistantIndexLine> lines = new TreeSet<PersistantIndexLine>();
            for (String s: sortedIndexVals) {
                ArrayList<Long> pids = addIndexMap.get(s);
                PersistantIndexLine pline = new PersistantIndexLine(s,PersistantIndexLine.IndexOperation.kAdd,
                        pids.toArray(new Long[0]));
                lines.add(pline);
            }
            pIndex.addDetails(lines);
            clear("add");
        }
        //Write the del mod
        if (!delModifiedIndexMap.isEmpty()) {
            Set<String> indexDetailsSet = delModifiedIndexMap.keySet();
            ArrayList<String> sortedIndexVals = new ArrayList<String>();
            sortedIndexVals.addAll(indexDetailsSet);
            Collections.sort(sortedIndexVals);
            TreeSet<PersistantIndexLine> lines = new TreeSet<PersistantIndexLine>();
            for (String s: sortedIndexVals) {
                ArrayList<Long> pids = delModifiedIndexMap.get(s);
                PersistantIndexLine pline = new PersistantIndexLine(s,PersistantIndexLine.IndexOperation.kDelModified,
                        pids.toArray(new Long[0]));
                lines.add(pline);
            }
            pIndex.addDetails(lines);
            clear("delmod");
        }
        //Write the add mod
        if (!addModifiedIndexMap.isEmpty()) {
            Set<String> indexDetailsSet = addModifiedIndexMap.keySet();
            ArrayList<String> sortedIndexVals = new ArrayList<String>();
            sortedIndexVals.addAll(indexDetailsSet);
            Collections.sort(sortedIndexVals);
            TreeSet<PersistantIndexLine> lines = new TreeSet<PersistantIndexLine>();
            for (String s: sortedIndexVals) {
                ArrayList<Long> pids = addModifiedIndexMap.get(s);
                PersistantIndexLine pline = new PersistantIndexLine(s,PersistantIndexLine.IndexOperation.kAddModified,
                        pids.toArray(new Long[0]));
                lines.add(pline);
            }
            pIndex.addDetails(lines);
            clear("addmod");

        }
        //Write the del
        if (!delIndexMap.isEmpty()) {
            Set<String> indexDetailsSet = delIndexMap.keySet();
            ArrayList<String> sortedIndexVals = new ArrayList<String>();
            sortedIndexVals.addAll(indexDetailsSet);
            Collections.sort(sortedIndexVals);
            TreeSet<PersistantIndexLine> lines = new TreeSet<PersistantIndexLine>();
            for (String s: sortedIndexVals) {
                ArrayList<Long> pids = delIndexMap.get(s);
                PersistantIndexLine pline = new PersistantIndexLine(s,PersistantIndexLine.IndexOperation.kDelete,
                        pids.toArray(new Long[0]));
                lines.add(pline);
            }
            pIndex.addDetails(lines);
            clear("del");

        }
        //Write the nc
        if (!ncIndexMap.isEmpty()) {
            Set<String> indexDetailsSet = ncIndexMap.keySet();
            ArrayList<String> sortedIndexVals = new ArrayList<String>();
            sortedIndexVals.addAll(indexDetailsSet);
            Collections.sort(sortedIndexVals);
            TreeSet<PersistantIndexLine> lines = new TreeSet<PersistantIndexLine>();
            for (String s: sortedIndexVals) {
                ArrayList<Long> pids = ncIndexMap.get(s);
                PersistantIndexLine pline = new PersistantIndexLine(s,PersistantIndexLine.IndexOperation.kNoChange,
                        pids.toArray(new Long[0]));
                lines.add(pline);
            }
            pIndex.addDetails(lines);
            clear("nc");
        }

        return pIndex;
    }

    /**
     * Clear the internal maps each time a chunk is written
     * @param mode
     */
    private void clear(String mode) {
        if (mode.equals("add")) {
            addIndexMap.clear();
        } else if (mode.equals("del")) {
            delIndexMap.clear();
        } else if (mode.equals("nc")) {
            ncIndexMap.clear();
        } else if (mode.equals("addmod")) {
            addModifiedIndexMap.clear();
        } else if (mode.equals("delmod")) {
            delModifiedIndexMap.clear();
        }
    }
    
    /**
     * Check if the given mode has any entries or not.
     * @param mode
     * @return
     */
    public boolean hasEntries(String mode) {
        boolean rResult = false;
        if (mode.equals("add")) {
            rResult = !addIndexMap.isEmpty();
        } else if (mode.equals("del")) {
            rResult = !delIndexMap.isEmpty();
        } else if (mode.equals("nc")) {
            rResult = !ncIndexMap.isEmpty();
        } else if (mode.equals("addmod")) {
            rResult = !addModifiedIndexMap.isEmpty();
        } else if (mode.equals("delmod")) {
            rResult = !delModifiedIndexMap.isEmpty();
        }
        return rResult;
    }


}

