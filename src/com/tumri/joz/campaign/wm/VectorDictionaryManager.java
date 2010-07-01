package com.tumri.joz.campaign.wm;

import com.tumri.utils.dictionary.Dictionary;
import com.tumri.utils.dictionary.IDictionary;

import java.util.Hashtable;

/**
 * Dictionary Manager impl for Weights
 */
public class VectorDictionaryManager {
	private static VectorDictionaryManager g_Instance = null;
	Hashtable<VectorAttribute, IDictionary> m_table = new Hashtable<VectorAttribute, IDictionary>();

	static {
		VectorDictionaryManager dm = getInstance();
	}

	public static VectorDictionaryManager getInstance() {
		if (g_Instance == null) {
			synchronized (VectorDictionaryManager.class) {
				if (g_Instance == null) {
					g_Instance = new VectorDictionaryManager();
				}
			}
		}
		return g_Instance;
	}

	protected final IDictionary getDictionary(VectorAttribute aAttribute) {
		if (m_table.containsKey(aAttribute)) {
			return m_table.get(aAttribute);
		}
		//Create a new dictionary
		Dictionary d = new Dictionary<String>();
		m_table.put(aAttribute, d);
		return d;
	}

	@SuppressWarnings("unchecked")
	public final Integer getId(VectorAttribute aAttribute, Object obj) {
		IDictionary dict = getDictionary(aAttribute);
		if (dict != null) {
			// ??? This gets an "unchecked call" warning.
			return dict.getId(obj);
		}
		return -1;
	}

	public final Object getValue(VectorAttribute aAttribute, int index) {
		IDictionary dict = m_table.get(aAttribute);
		if (dict != null) {
			return dict.getValue(index);
		}
		return null;
	}

	public int maxId(VectorAttribute aAttribute) {
		IDictionary dict = m_table.get(aAttribute);
		if (dict != null) {
			return dict.maxId();
		}
		return 0;
	}

	public int minId(VectorAttribute aAttribute) {
		IDictionary dict = m_table.get(aAttribute);
		if (dict != null) {
			return dict.minId();
		}
		return 0;
	}


	public void addType(VectorAttribute aAttribute, IDictionary dict) {
		if (!m_table.containsKey(aAttribute))
			m_table.put(aAttribute, dict);
	}

	public void remove(VectorAttribute aAttribute, int index) {
		IDictionary dict = getDictionary(aAttribute);
		if (dict != null) {
			dict.remove(index);
		}
	}
}