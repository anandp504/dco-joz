/*
 * ProductDataLoader.java
 *
 * COPYRIGHT (C) 2006 TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY,
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR
 * WRITTEN PERMISSION OF TUMRI INC.
 *
 * @author Bhavin Doshi (bdoshi@tumri.com)
 * @version 1.0     Jun 21, 2006
 *
 */
package com.tumri.joz.products;

import com.tumri.joz.Query.*;
import com.tumri.joz.index.CategoryIndex;
import com.tumri.joz.index.DictionaryManager;
import com.tumri.joz.utils.DOMUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.StringTokenizer;

public class MUPLoader {
  File m_file;

  private MUPLoader() {
    this(new File("MUP-USpub0012_MUP_US0050-US-DEFAULT_.utf8"));
  }

  private MUPLoader(File f) {
    m_file = f;
  }

  /*
  public void loadProducts() throws IOException {
    ProductDB pdb = ProductDB.getInstance();

    ArrayList<IProduct> products = getAll();
    pdb.addProduct(products);
  }

  public ArrayList<IProduct> getAll() throws IOException {
    if (!m_file.exists()) {
      throw new RuntimeException("File " + m_file + " doesn't exist");
    }
    if (!m_file.canRead()) {
      throw new RuntimeException("File " + m_file + " cannot be read");
    }

    FileInputStream fir = new FileInputStream(m_file);
    try {
      InputStreamReader isr = new InputStreamReader(fir, "utf8");
      BufferedReader br = new BufferedReader(isr);

      boolean eof = false;
      String line = null;
      ArrayList<IProduct> products = new ArrayList<IProduct>();
      while (!eof) {
        line = br.readLine();
        if (line == null) {
          eof = true;
          continue;
        }
        IProduct p = convertLine(line);
        products.add(p);
      }
      return products;
    } finally {
      fir.close();
    }
  }

  private IProduct convertLine(String line) {
    Product p = new Product();
    if (line != null) {
      StringTokenizer str = new StringTokenizer(line, "\t", true);
      ArrayList<String> strings = new ArrayList<String>();
      // The reason to code this way is because if there are 2 consecutive delimiters, Java treats them as one
      // But in products file, 2 consecutive tabs mean an empty string value.
      String s1 = null;

      while (str.hasMoreTokens()) {
        s1 = str.nextToken();
        if (s1.equals("\t")) {
          strings.add("");
          continue;
        }
        strings.add(s1);
        if (str.hasMoreTokens()) str.nextToken(); // skip the \t
      }

      if (strings.size() != 24) {
        throw new RuntimeException("Invalid Entry found(Size=" + strings.size() + "): " + line);
      }
      String productId = strings.get(2);
      String gId = productId.substring(productId.indexOf('.'));

      while (gId.charAt(0) < '0' || gId.charAt(0) > '9') {
        gId = gId.substring(1);
      }
      if (strings.get(11) == null || "".equals(strings.get(11))) {
        strings.set(11, "0");
      }

      p.setGId(gId);
      p.setCatalog(strings.get(1));
      p.setCategory(strings.get(3));
      p.setPrice(strings.get(4));
      p.setDiscountPrice(strings.get(5));
      p.setBrand(strings.get(6));
      p.setSupplier(strings.get(7));
      p.setProvider(strings.get(8));
      p.setProductName(strings.get(9));
      p.setDescription(strings.get(10));
      p.setRank(strings.get(11));
      p.setThumbnail(strings.get(12));
      p.setPurchaseUrl(strings.get(13));
      p.setImageUrl(strings.get(14));
      p.setImageWidth(strings.get(15));
      p.setImageHeight(strings.get(16));
      p.setCPC(strings.get(17));
      p.setCurrency(strings.get(18));
      p.setDiscountPriceCurrency(strings.get(19));
      p.setBlackWhiteListStatus(strings.get(20));
      p.setProductType(strings.get(21));
      p.setCPO(strings.get(22));
      p.setBaseProductNumber(strings.get(23));

    }
    return p;
  }
   */

  /* This class is not going to be used. Commenting out test
  @Test public void test() {
    // try {
      // loadProducts();
      QueryProcessor qp = new ProductQueryProcessor();
      // TaxonomyLoader tl = new TaxonomyLoader();
      DictionaryManager dm = DictionaryManager.getInstance();
      ProductDB.getInstance();
      new TSpecLoader(true);
      long start = System.currentTimeMillis();
      for (int i = 0; i < 1; i++) {
        CNFQuery q = new CNFQuery();
        ConjunctQuery cq = new ConjunctQuery(qp);
        AttributeQuery aq = new AttributeQuery(IProduct.Attribute.kProvider,
            dm.getId(IProduct.Attribute.kProvider, "AUDIBLE.COM"));
        //aq.setNegation(true);
        cq.addQuery(aq);
        cq.addQuery(new AttributeQuery(IProduct.Attribute.kSupplier,
            dm.getId(IProduct.Attribute.kSupplier, "AUDIBLE.COM")));
        q.addQuery(cq);
        q.exec();
      }
      System.out.println("Time is " + (System.currentTimeMillis() - start));
    // } catch (IOException e) {
    //  e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    // }
  }
*/
}

/*
class TaxonomyLoader {
  Document m_document = null;

  public TaxonomyLoader() {
    Taxonomy tax = Taxonomy.getInstance();
    JOZTaxonomy jtax = JOZTaxonomy.getInstance();
    jtax.build(tax);
    CategoryIndex catIndex = (CategoryIndex)ProductDB.getInstance().getIndex(IProduct.Attribute.kCategory);
    catIndex.update(jtax);
  }
}
 */
/*
//Temp class for testing purposes
class TSpecLoader {
  Document m_document = null;
  ArrayList<TSpec> m_tspecs = new ArrayList<TSpec>();
  private boolean m_validate = false;

  TSpecLoader(boolean validate) {
    m_validate = validate;
    File ftspecs = new File("t-specs.xml");
    m_document = DOMUtils.parse(ftspecs);
    process();
    materialize();
  }

  public ArrayList<TSpec> getTspecs() {
    return m_tspecs;
  }

  private void process() {
    NodeList nlist = m_document.getDocumentElement().getChildNodes();
    for (int i = 0; i < nlist.getLength(); i++) {
      Node child = nlist.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("T-SPEC-ADD")) {
        TSpec tspec = new TSpec((Element) child);
        m_tspecs.add(tspec);
      }
    }
  }

  public void materialize() {
    long start = System.currentTimeMillis();
    ProductDB pdb = ProductDB.getInstance();
    for (int i = 0; i < m_tspecs.size(); i++) {
      Handle ref = pdb.genReference();
      TSpec lTSpec = m_tspecs.get(i);
      ConjunctQuery cjq = lTSpec.getQuery().getQueries().get(0);
      cjq.setStrict(true);
      cjq.setReference(ref);
      SortedSet<Handle> results = cjq.exec();
      if (m_validate) {
        cjq.clear();
        cjq.setScan(true);
        cjq.setReference(ref);
        SortedSet<Handle> results1 = cjq.exec();
        Iterator<Handle> iter = results.iterator();
        Iterator<Handle> iter1 = results1.iterator();
        boolean hasNext = iter.hasNext();
        boolean hasNext1 = iter1.hasNext();
        Assert.assertEquals(hasNext,hasNext1);
        while(hasNext && hasNext1) {
          Assert.assertEquals(iter.next().getOid(),iter1.next().getOid());
          hasNext = iter.hasNext();
          hasNext1 = iter1.hasNext();
          Assert.assertEquals(hasNext,hasNext1);
        }
      }
    }
    System.out.println("TSpec Time is "+(System.currentTimeMillis()-start));
  }
}

class TSpec {
  private static final String NAME = "NAME";
  private static final String VERSION = "VERSION";
  private static final String INCLUDECATEGORIES = "INCLUDE-CATEGORIES";
  private static final String EXCLUDECATEGORIES = "EXCLUDE-CATEGORIES";
  private static final String INCLUDEPROVIDER = "ATTR_INCLUSIONS-PROVIDER";
  private static final String EXCLUDEPROVIDER = "ATTR_EXCLUSIONS-PROVIDER";
  private static final String INCLUDESUPPLIER = "ATTR_INCLUSIONS-SUPPLIER";
  private static final String EXCLUDESUPPLIER = "ATTR_EXCLUSIONS-SUPPLIER";
  private static final String INCLUDEBRAND = "ATTR_INCLUSIONS-BRAND";
  private static final String EXCLUDEBRAND = "ATTR_EXCLUSIONS-BRAND";
  private static final String WEIGHTMAPCOMBOSCHEME = "WEIGHT-MAP-COMBO-SCHEME";
  private static final String LOADTIMEKEYWORDEXPR = "LOAD-TIME-KEYWORD-EXPR";
  private static final String INCOMEPERCENTILE = "INCOME-PERCENTILE";
  private static final String CPCRANGE = "CPC-RANGE";
  private static final String REFPRICECONSTRAINTS = "REF-PRICE-CONSTRAINTS";

  private String m_name;
  private CNFQuery m_query = new CNFQuery();
  private ConjunctQuery m_cjquery = new ConjunctQuery(new ProductQueryProcessor());

  TSpec(Element e) {
    m_query.addQuery(m_cjquery);
    buildQuery(e);
  }

  public CNFQuery getQuery() {
    return m_query;
  }

  public String getName() {
    return m_name;
  }

  private void buildQuery(Element e) {
    NamedNodeMap attrs = e.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      Node at = attrs.item(i);
      if (at.getNodeType() == Node.ATTRIBUTE_NODE) {
        Attr attr = (Attr) at;
        String name = attr.getName();
        String value = attr.getValue();
        SimpleQuery sq = buildSimpleQuery(name, value);
        if (sq != null)
          m_cjquery.addQuery(sq);
      }
    }
  }

  private SimpleQuery buildSimpleQuery(String name, String value) {
    if (name.equals("NAME")) {
      m_name = value;
      return null;
    } else if (VERSION.equals(name)) {
      return null;
    } else if (INCLUDECATEGORIES.equals(name)) {
      ArrayList<Integer> values = getValues(IProduct.Attribute.kCategory, value);
      SimpleQuery sq =  new AttributeQuery(IProduct.Attribute.kCategory,values);
      return sq;
    } else if (EXCLUDECATEGORIES.equals(name)) {
      ArrayList<Integer> values = getValues(IProduct.Attribute.kCategory, value);
      SimpleQuery sq =  new AttributeQuery(IProduct.Attribute.kCategory,values);
      sq.setNegation(true);
      return sq;
    } else if (INCLUDEPROVIDER.equals(name)) {
      ArrayList<Integer> values = getValues(IProduct.Attribute.kProvider, value);
      SimpleQuery sq =  new AttributeQuery(IProduct.Attribute.kProvider,values);
      return sq;
    } else if (EXCLUDEPROVIDER.equals(name)) {
      ArrayList<Integer> values = getValues(IProduct.Attribute.kProvider, value);
      SimpleQuery sq =  new AttributeQuery(IProduct.Attribute.kProvider,values);
      sq.setNegation(true);
      return sq;
    } else if (INCLUDESUPPLIER.equals(name)) {
      ArrayList<Integer> values = getValues(IProduct.Attribute.kSupplier, value);
      SimpleQuery sq =  new AttributeQuery(IProduct.Attribute.kSupplier,values);
      return sq;
    } else if (EXCLUDESUPPLIER.equals(name)) {
      ArrayList<Integer> values = getValues(IProduct.Attribute.kSupplier, value);
      SimpleQuery sq =  new AttributeQuery(IProduct.Attribute.kSupplier,values);
      sq.setNegation(true);
      return sq;
    } else if (INCLUDEBRAND.equals(name)) {
      ArrayList<Integer> values = getValues(IProduct.Attribute.kBrand, value);
      SimpleQuery sq =  new AttributeQuery(IProduct.Attribute.kBrand,values);
      return sq;
    } else if (EXCLUDEBRAND.equals(name)) {
      ArrayList<Integer> values = getValues(IProduct.Attribute.kBrand, value);
      SimpleQuery sq =  new AttributeQuery(IProduct.Attribute.kBrand,values);
      sq.setNegation(true);
      return sq;
    } else if (WEIGHTMAPCOMBOSCHEME.equals(name)) {
      return null;
    } else if (LOADTIMEKEYWORDEXPR.equals(name)) {
      SimpleQuery sq = new KeywordQuery(value);
      return sq;
    } else if (INCOMEPERCENTILE.equals(name)) {
      return null;
    } else if (CPCRANGE.equals(name)) {
      ArrayList<Double> values = getRangeValues(value);
      SimpleQuery sq =  new RangeQuery(IProduct.Attribute.kCPC, values.get(0), values.get(1));
      return sq;
    } else if (REFPRICECONSTRAINTS.equals(name)) {
      ArrayList<Double> values = getRangeValues(value);
      SimpleQuery sq =  new RangeQuery(IProduct.Attribute.kPrice, values.get(0), values.get(1));
      return sq;
    } else {
      System.err.println("Unknown attribute "+name+"="+value);
    }
    return null;
  }
  private ArrayList<Integer> getValues(IProduct.Attribute attr, String value) {
    StringTokenizer tokens = new StringTokenizer(value," ",false);
    DictionaryManager dm = DictionaryManager.getInstance();
    ArrayList<Integer> vals = new ArrayList<Integer>();
    String last = null;
    while(tokens.hasMoreTokens()) {
      String token = tokens.nextToken().trim();
      if (last == null) {
        if (token.startsWith("|")) {
          last = token;
          if (token.endsWith("|")) {
            last = last.substring(1,last.length()-1);
            vals.add(dm.getId(attr,last));
            last = null;
          }
        } else {
          vals.add(dm.getId(attr,token));
        }
      } else {
        last = last + " " + token;
        if (token.endsWith("|")) {
          last = last.substring(1,last.length()-1);
          vals.add(dm.getId(attr,last));
          last = null;
        }
      }
    }
    return vals;
  }

  private ArrayList<Double> getRangeValues(String value) {
    StringTokenizer tokens = new StringTokenizer(value, " ", false);
    ArrayList<Double> vals = new ArrayList<Double>();
    while (tokens.hasMoreTokens()) {
      String token = tokens.nextToken().trim();
      try {
        vals.add(Double.parseDouble(token));
      } catch (NumberFormatException e) {
        vals.add(new Double(vals.size() == 0 ? 0 : 100000));
      }
    }
    while (vals.size() < 2) {
      vals.add(new Double(vals.size() == 0 ? 0 : 100000));
    }
    return vals;
  }


}
*/