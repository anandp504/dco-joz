package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public abstract class QueryProcessor {

  protected QueryProcessor() {
  }

  public abstract SetIntersector<Handle> buildTableScanner(ArrayList<SimpleQuery> aQueries, Handle reference, boolean isStrict);
  public abstract SetIntersector<Handle> buildIntersector(ArrayList<SimpleQuery> aQueries, Handle reference, boolean isStrict);
}
