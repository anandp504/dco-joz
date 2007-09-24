package com.tumri.joz.keywordServer;

/**
 * Class to tokenize the input stream at alpha-numeric boundaries.
 * Normally we don't want numbers, but this cuts out things like model
 * numbers, e.g. ps3.  What we do is pass through all alpha-numeric
 * characters, lowercase the letters, and leave it to the stopword
 * filter to remove single digit "words" leaving alone all double digit and
 * greater numbers as well as "words" consisting of numbers and letters
 * to be indexed.
 *
 * For reference see src/java/org/apache/lucene/analysis/*.java
 * in the Lucene sources, e.g. lucene-2.1.0-src.tar.gz.
 */

import org.apache.lucene.analysis.CharTokenizer;

import java.io.Reader;

public class LowerAlnumTokenizer extends CharTokenizer {
  public LowerAlnumTokenizer(Reader in) {
    super(in);
  }

  protected boolean
  isTokenChar(char c) {
    return Character.isLetterOrDigit(c);
  }

  protected char
  normalize(char c) {
    return Character.toLowerCase(c);
  }
}
