package com.tumri.joz.index.creator;

import java.io.Serializable;

/**
 * Class to hold the index details
 * @author: nipun
 * Date: Feb 15, 2008
 * Time: 1:01:45 PM
 */
public class PersistantIndexLine implements Serializable, Comparable {
    private IndexOperation operation;
    private String indexValue;
    private Long[] pids;

    public PersistantIndexLine(String value, IndexOperation op, Long[] ids) {
        this.indexValue = value;
        this.operation = op;
        this.pids = ids;
    }

    public Long[] getPids() {
        return pids;
    }

    public void setPids(Long[] pids) {
        this.pids = pids;
    }

    public String getIndexValue() {
        return indexValue;
    }

    public void setIndexValue(String indexValue) {
        this.indexValue = indexValue;
    }

    public IndexOperation getOperation() {
        return operation;
    }

    public void setOperation(IndexOperation operation) {
        this.operation = operation;
    }

    public int compareTo(Object o) {
        if (!(o instanceof PersistantIndexLine))
            throw new ClassCastException("A PersistantIndexLine object expected.");
        int opCompare = operation.compareTo(((PersistantIndexLine)o).getOperation());
        if (opCompare == 0) {
            opCompare = indexValue.compareTo(((PersistantIndexLine)o).getIndexValue());
        }
        return opCompare;
    }

    public enum IndexOperation {
        kAdd,
        kDelModified,
        kAddModified,
        kDelete,
        kNoChange,
    };

}
