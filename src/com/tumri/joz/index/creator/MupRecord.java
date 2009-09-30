package com.tumri.joz.index.creator;

import java.util.Comparator;
import java.util.HashMap;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: scbraun
 * Date: Sep 29, 2009
 * Time: 11:26:04 AM
 */
public class MupRecord implements Comparator, Comparable, Externalizable, Serializable {
	long pId;
	HashMap<String, String> prodProperties;

	public MupRecord(){
	}

	public MupRecord(long pId, HashMap<String, String> map){
		this.pId = pId;
		prodProperties = map;
	}

	public long getPId() {
		return pId;
	}

	public void setPId(long pId) {
		this.pId = pId;
	}

	public HashMap<String, String> getProdProperties() {
		return prodProperties;
	}

	public void setProdProperties(HashMap<String, String> prodProperties) {
		this.prodProperties = prodProperties;
	}

	public void addProdPropertie(String propName, String propValue){
		if(prodProperties == null){
			prodProperties = new HashMap<String, String>();
		}
		prodProperties.put(propName, propValue);
	}

	public int compare(Object o, Object o1) {
		if(o==o1){
			return 0;
		}
		if(!(o instanceof MupRecord)){
			return -1;
		} else if(!(o1 instanceof MupRecord)){
			return 1;
		}
		MupRecord r1 = (MupRecord)o;
		MupRecord r2 = (MupRecord)o;
		if(r1.getPId() < r2.getPId()){
			return -1;
		} else if(r1.getPId() > r2.getPId()){
			return 1;
		}
		return 0;
	}

	public int compareTo(Object o) {
		if(!(o instanceof MupRecord)){
			return 1;
		}
		MupRecord r1 = (MupRecord)o;
		if(pId < r1.getPId()){
			return -1;
		} else if(pId > r1.getPId()){
			return 1;
		}
		return 0;
	}

	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeLong(pId);
		int numKeys = 0;
		if(prodProperties == null){
			oo.writeInt(numKeys);
		} else {
			numKeys = prodProperties.keySet().size();
			oo.writeInt(numKeys);
			for(String s : prodProperties.keySet()){
				oo.writeUTF(s);
				oo.writeUTF(prodProperties.get(s));
			}
		}

	}

	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		pId = oi.readLong();
		int numKeys = oi.readInt();
		prodProperties = new HashMap<String, String>();
		for(int i = 0; i < numKeys; i++){
			prodProperties.put(oi.readUTF(), oi.readUTF());
		}
	}
}

