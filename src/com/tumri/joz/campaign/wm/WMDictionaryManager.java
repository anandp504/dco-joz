package com.tumri.joz.campaign.wm;

import com.tumri.utils.dictionary.Dictionary;
import com.tumri.utils.dictionary.IDictionary;

import java.util.Hashtable;

/**
 * Dictionary Manager impl for Weights
 */
public class WMDictionaryManager {
	private static WMDictionaryManager g_Instance = null;
	Hashtable<WMAttribute, IDictionary> m_table = new Hashtable<WMAttribute, IDictionary>();

	static {
		WMDictionaryManager dm = getInstance();
	}

	public static WMDictionaryManager getInstance() {
		if (g_Instance == null) {
			synchronized (WMDictionaryManager.class) {
				if (g_Instance == null) {
					g_Instance = new WMDictionaryManager();
				}
			}
		}
		return g_Instance;
	}

	protected final IDictionary getDictionary(WMAttribute aAttribute) {
		if (m_table.containsKey(aAttribute)) {
			return m_table.get(aAttribute);
		}
		//Create a new dictionary
		Dictionary d = new Dictionary<String>();
		m_table.put(aAttribute, d);
		return d;
	}

	@SuppressWarnings("unchecked")
	public final Integer getId(WMAttribute aAttribute, Object obj) {
		IDictionary dict = getDictionary(aAttribute);
		if (dict != null) {
			// ??? This gets an "unchecked call" warning.
			return dict.getId(obj);
		}
		return -1;
	}

	public final Object getValue(WMAttribute aAttribute, int index) {
		IDictionary dict = m_table.get(aAttribute);
		if (dict != null) {
			return dict.getValue(index);
		}
		return null;
	}

	public int maxId(WMAttribute aAttribute) {
		IDictionary dict = m_table.get(aAttribute);
		if (dict != null) {
			return dict.maxId();
		}
		return 0;
	}

	public int minId(WMAttribute aAttribute) {
		IDictionary dict = m_table.get(aAttribute);
		if (dict != null) {
			return dict.minId();
		}
		return 0;
	}


	public void addType(WMAttribute aAttribute, IDictionary dict) {
		if (!m_table.containsKey(aAttribute))
			m_table.put(aAttribute, dict);
	}

	public void remove(WMAttribute aAttribute, int index) {
		IDictionary dict = getDictionary(aAttribute);
		if (dict != null) {
			dict.remove(index);
		}
	}
}