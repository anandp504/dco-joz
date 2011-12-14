package com.tumri.joz.index;

import java.util.Hashtable;

import com.tumri.content.ContentProviderFactory;
import com.tumri.content.InvalidConfigException;
import com.tumri.joz.products.IProduct;
import com.tumri.utils.dictionary.Dictionary;
import com.tumri.utils.dictionary.IDictionary;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class DictionaryManager {
	private static DictionaryManager g_Instance = null;
	Hashtable<IProduct.Attribute, IDictionary> m_table = new Hashtable<IProduct.Attribute, IDictionary>();

	static {
		// Hopefully by now, we have initialized the content provider factory.
		DictionaryManager dm = getInstance();
		dm.addType(IProduct.Attribute.kId, new ProductDictionary());
	}

	public static final DictionaryManager getInstance() {
		if (g_Instance == null) {
			synchronized (DictionaryManager.class) {
				if (g_Instance == null) {
					g_Instance = new DictionaryManager();
				}
			}
		}
		return g_Instance;
	}

	protected final IDictionary getDictionary(IProduct.Attribute aAttribute) {
		if (m_table.containsKey(aAttribute)) {
			return m_table.get(aAttribute);
		}
		// For now start caching the dictionary. Do we need to cache it ?
		IDictionary d = ContentProviderFactory.getDictionary(aAttribute);
		if (d != null) {
			m_table.put(aAttribute, d);
		}
		return d;
	}

	@SuppressWarnings("unchecked")
	public final Integer getId(IProduct.Attribute aAttribute, Object obj) {
		IDictionary dict = getDictionary(aAttribute);
		if (dict != null && obj != null) {
			// ??? This gets an "unchecked call" warning.
			return dict.getId(obj);
		}
		return -1;
	}

	@SuppressWarnings("unchecked")
	public final Integer lookupId(IProduct.Attribute aAttribute, Object obj) {
		IDictionary dict = getDictionary(aAttribute);
		if (dict != null && obj != null) {
			// ??? This gets an "unchecked call" warning.
			return dict.lookupId(obj);
		}
		return -1;
	}

	public final Object getValue(IProduct.Attribute aAttribute, int index) {
		IDictionary dict = m_table.get(aAttribute);
		if (dict != null) {
			return dict.getValue(index);
		}
		return null;
	}

	public int maxId(IProduct.Attribute aAttribute) {
		IDictionary dict = m_table.get(aAttribute);
		if (dict != null) {
			return dict.maxId();
		}
		return 0;
	}

	public int minId(IProduct.Attribute aAttribute) {
		IDictionary dict = m_table.get(aAttribute);
		if (dict != null) {
			return dict.minId();
		}
		return 0;
	}


	public void addType(IProduct.Attribute aAttribute, IDictionary dict) {
		if (!m_table.containsKey(aAttribute))
			m_table.put(aAttribute, dict);
	}

	public void remove(IProduct.Attribute aAttribute, int index) {
		IDictionary dict = getDictionary(aAttribute);
		if (dict != null) {
			dict.remove(index);
		}
	}
}