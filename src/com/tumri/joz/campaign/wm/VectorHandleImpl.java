package com.tumri.joz.campaign.wm;

import com.tumri.joz.products.Handle;

import java.util.*;

/**
 * Handle object that refers to a request vector
 */
public class VectorHandleImpl implements VectorHandle, Cloneable {

	/**
	 * Id is made up of adpod id or exp id + vector id
	 */
	private long id;

	/**
	 * Flag to indicate whether this is a none attribute handle or not.
	 */
	private boolean noneHandle = false;

	/**
	 * Type is 0 for default rule, 1 for optimization rule, and  2 for personalization rule
	 */
	private byte type;

	/**
	 * This array encapsulates all the context map contents
	 * 0th position is the address book info of what types are populated, and rest of the items are the values for that type
	 * there could be multiple values for a type, we have a -1 to indicate that we move to the next type
	 */
	private int[] contextDetails;

	/**
	 * The score assigned to a handle during the set intersection logic
	 */
	private double score = 0.0;

	private int m_size = 0;


	private VectorHandleImpl() {

	}

	public VectorHandleImpl(int expId, int vId, int type, Map<VectorAttribute, List<Integer>> contextMap, boolean isMultiple) {
		this.id = createId(expId, vId);
		this.type = (byte) type;
		this.contextDetails = getContextDetailsArr(contextMap, isMultiple);
		computeSize();
	}


	public VectorHandleImpl(long id, int type, Map<VectorAttribute, List<Integer>> contextMap, boolean isMultiple) {
		this.id = id;
		this.type = (byte) type;
		this.contextDetails = getContextDetailsArr(contextMap, isMultiple);
		computeSize();
	}

	public VectorHandleImpl(long id) {
		this.id = id;
		this.contextDetails = null;
		computeSize();
	}

	public VectorHandleImpl(long id, boolean isNonHandle) {
		this.id = id;
		this.noneHandle = isNonHandle;
		this.contextDetails = null;
		computeSize();
	}

	public VectorHandleImpl(int eid, int vecid) {
		this.id = createId(eid, vecid);
		this.contextDetails = null;
		computeSize();
	}


	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VectorHandleImpl that = (VectorHandleImpl) o;
		if (getOid() != that.getOid()) return false;
		return true;
	}

	public int hashCode() {
		return (int) getOid();
	}


	/**
	 * Returns the size of the dimensions that are turned on in this handle
	 * Should be atleast one since the default context is there
	 *
	 * @return
	 */
	public int getSize() {
		return m_size;
	}

	private void computeSize() {
		if (contextDetails != null) {
			int addr = contextDetails[0];
			int size = 0;
			while (addr > 0) {
				if ((addr & 0x1) == 1) {
					size++;
				}
				addr >>>= 1;
			}
			m_size = size;
		}
	}

	/**
	 * This comparator is used during search operations
	 *
	 * @param handle
	 * @return
	 */
	public int compareTo(Object handle) {
		VectorHandle ph = (VectorHandle) handle;
		long oid1 = getOid();
		long oid2 = ph.getOid();
		if (oid1 < oid2) return -1;
		if (oid1 > oid2) return 1;
		return 0;
	}


	/**
	 * This comparator is used during sort operations
	 *
	 * @param h1
	 * @param h2
	 * @return
	 */
	public int compare(Object h1, Object h2) {
		VectorHandle ph1 = (VectorHandle) h1;
		VectorHandle ph2 = (VectorHandle) h2;
		int diffSize = ph2.getSize() - ph1.getSize();
		if (diffSize != 0) {
			return diffSize;
		}

//        double score = ph1.getScore();
//        double phScore  = ph2.getScore();
//
//
//        if (score > phScore) return -1;
//        if (score < phScore) return 1;

		int[] addr1 = ph1.getContextDetails();
		int[] addr2 = ph2.getContextDetails();
		if (addr1 != null && addr2 != null) {
			int diff = addr2[0] - addr1[0];   //this instead of addr2[0] - addr1[0] because of pos of attributes in VectorUtils.
			if (diff != 0) {
				return diff;
			}
		}

		long oid = ph1.getOid();
		long phOid = ph2.getOid();
		return (oid < phOid ? -1 :
				oid == phOid ? 0 : 1);

	}

	public int getExpId() {
		long hiVal = id >> 32;
		return (int) hiVal;
	}

	public int getVectorId() {
		return (int) id;
	}

	/**
	 * For a given attribute gets the dictionary ids stored in the context details array ( there could be more than one )
	 * This is needed just for the console purposes.
	 *
	 * @param kAttr
	 * @return
	 */
	private ArrayList<Integer> getContextVals(VectorAttribute kAttr) {
		//0th position is the dictionary
		int addr = getContextDetails()[0];
		int attrPos = VectorUtils.getAttributePos(kAttr);
		int numFieldsToSkip = 0;
		for (int i = 0; i < attrPos; i++) {
			if ((addr & (1 << i)) > 0) {
				numFieldsToSkip++;
			}
		}
		boolean isMultiple = ((addr & (1 << 30)) > 0);
		ArrayList<Integer> list = new ArrayList<Integer>();

		if (!isMultiple) {
			list.add(getContextDetails()[numFieldsToSkip + 1]);
		} else {
			int count = 0;
			for (int j = 1; j < getContextDetails().length; j++) {
				if (getContextDetails()[j] == -1) {
					count++;
					continue;
				}
				if (count == numFieldsToSkip) {
					list.add(getContextDetails()[j]);
				} else if (count > numFieldsToSkip) {
					break;
				}
			}
		}

		return list;
	}

	/**
	 * Gets the context keys for the given handle
	 * Needed to determine the attribute weight
	 *
	 * @return
	 */
	private ArrayList<VectorAttribute> getContextKeys() {
		//0th position is the dictionary
		ArrayList<VectorAttribute> keys = new ArrayList<VectorAttribute>();
		int addr = getContextDetails()[0];
		for (int i = 0; (i < 30 && addr > 0); i++) {
			if ((addr & 0x1) == 1) {
				keys.add(VectorUtils.getAttribute(i));
			}
			addr >>>= 1;
		}
		return keys;
	}

	/**
	 * Given the context map get the details arr
	 *
	 * @param contextMap
	 * @return
	 */
	private int[] getContextDetailsArr(Map<VectorAttribute, List<Integer>> contextMap, boolean isMultiple) {
		if (contextMap == null) {
			return null;
		}
		ArrayList<Integer> list = new ArrayList<Integer>();
		int addr = 0;
		Map<Integer, VectorAttribute> posAttrMap = new HashMap<Integer, VectorAttribute>();
		int[] posArr = new int[contextMap.size()];
		int i = 0;
		for (VectorAttribute key : contextMap.keySet()) {
			int pos = VectorUtils.getAttributePos(key);
			addr |= 1 << pos;
			posAttrMap.put(pos, key);
			posArr[i] = pos;
			i++;
		}
		Arrays.sort(posArr);
		for (int pos : posArr) {
			list.addAll(contextMap.get(posAttrMap.get(pos)));
			if (isMultiple) {
				list.add(-1);
			}

		}
		if (isMultiple) {
			//Set the highest order bit of addr here
			addr |= 1 << 30;
		}

		int[] retList = new int[list.size() + 1];

		retList[0] = addr;
		for (int j = 0; j < list.size(); j++) {
			retList[j + 1] = list.get(j);
		}
		return retList;
	}

	public Map<VectorAttribute, List<Integer>> getContextMap() {
		Map<VectorAttribute, List<Integer>> map = new HashMap<VectorAttribute, List<Integer>>();
		ArrayList<VectorAttribute> keys = getContextKeys();
		for (VectorAttribute k : keys) {
			map.put(k, getContextVals(k));
		}
		return map;
	}

	/**
	 * Checks if the handles are matching or not
	 *
	 * @return
	 */
	public boolean isMatch(VectorHandle that) {
		if (that == null || that.getContextDetails() == null || getContextDetails() == null) {
			return false;
		}
		int addr = getContextDetails()[0];
		int taddr = that.getContextDetails()[0];
		return (addr ^ taddr) == 0;
	}

	/**
	 * Checks if the given handle is fully contained in the current handle
	 *
	 * @param that
	 * @return
	 */
	public boolean isDimensionSubset(VectorHandle that) {

		if (that.getContextDetails() == null || getContextDetails() == null) {
			return false;
		}
		int addr = getContextDetails()[0];
		int taddr = that.getContextDetails()[0];
		int tmp = addr & taddr;
		return (taddr ^ tmp) == 0;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("This is not supported");
	}

	public Handle createHandle(double score) {
		ImmutableVectorHandle newHandle = new ImmutableVectorHandle(score);
		return newHandle;
	}

	public long getOid() {
		return id;
	}

	public double getScore() {
		return score;
	}

	public int getType() {
		return (int) type;
	}

	public boolean isNoneHandle() {
		return noneHandle;
	}

	public int[] getContextDetails() {
		return contextDetails;
	}

	/**
	 * Constructs an id from experience id and a vector id
	 *
	 * @param expId
	 * @param vectorId
	 * @return
	 */
	public static long createId(int expId, int vectorId) {
		long leid = expId;
		long id = (leid << 32) & 0xFFFFFFFF00000000L;
		id = id | (vectorId & 0x00000000FFFFFFFFL);
		return id;
	}

	/**
	 * Breaks up the id back into the experience id and the vector id
	 *
	 * @param id
	 * @return - array of length 2. The 0th position is the vector id and the 1st position is the exp id.
	 */
	public static int[] getIdDetails(long id) {
		int[] dets = new int[2];
		dets[0] = (int) id;
		long hiVal = id >> 32;
		dets[1] = (int) hiVal;
		return dets;
	}


	public class ImmutableVectorHandle implements VectorHandle {

		private double newScore = 0.0;

		private ImmutableVectorHandle(double score) {
			newScore = score;
		}

		public double getScore() {
			return this.newScore;
		}

		public long getOid() {
			return VectorHandleImpl.this.getOid();
		}

		public int getType() {
			return VectorHandleImpl.this.getType();
		}

		public Handle createHandle(double score) {
			return VectorHandleImpl.this.createHandle(score);
		}

		public int compare(Object o, Object o1) {
			return VectorHandleImpl.this.compare(o, o1);
		}

		public int compareTo(Object o) {
			return VectorHandleImpl.this.compareTo(o);
		}

		public boolean isMatch(VectorHandle h) {
			return VectorHandleImpl.this.isMatch(h);
		}

		public boolean isDimensionSubset(VectorHandle h) {
			return VectorHandleImpl.this.isDimensionSubset(h);
		}

		public int[] getContextDetails() {
			return VectorHandleImpl.this.getContextDetails();
		}

		public int getSize() {
			return VectorHandleImpl.this.getSize();
		}

		public Map<VectorAttribute, List<Integer>> getContextMap() {
			return VectorHandleImpl.this.getContextMap();
		}

		public boolean isNoneHandle() {
			return VectorHandleImpl.this.isNoneHandle();
		}
	}

}
