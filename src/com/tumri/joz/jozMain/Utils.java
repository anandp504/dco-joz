// random generic utilities

package com.tumri.joz.jozMain;

import org.apache.log4j.Logger;

public class Utils {
    
    private static Logger log = Logger.getLogger(Utils.class);
    
    static int skipWhitespace(String line, int idx) {
        while (idx < line.length() && Character.isWhitespace(line.charAt(idx)))
            ++idx;
        return idx;
    }
    
    static int skipNonWhitespace(String line, int idx) {
        while (idx < line.length() && !Character.isWhitespace(line.charAt(idx)))
            ++idx;
        return idx;
    }
    
    /**
     * The delimiter may not be \, it is used to prefix delimiters embedded in
     * the string.
     * 
     * @param line string to search
     * @param idx index of first delimiter
     * @return idx of next char after closing delimiter
     */
    
    static int extractDelimitedString(String line, int idx) throws Exception {
        int startIdx = idx;
        char delimiter = line.charAt(idx);
        
        if (delimiter == '\\') {
            String msg = "extractDelimitedString: received '\\' as first delimiter.";
            log.error(msg);
            throw new Exception(msg);
        }
        
        while (true) {
            ++idx;
            if (idx >= line.length()) {
                String msg = "extractDelimitedString: delimited string prematurely terminated, line ["
                        + line
                        + "], started at "
                        + startIdx
                        + ", delimiter "
                        + delimiter;
                log.error(msg);
                throw new Exception(msg);
            }
            char ch = line.charAt(idx);
            if (ch == delimiter)
                return idx + 1;
            if (ch == '\\') {
                if (idx >= line.length()) {
                    String msg = "extractDelimitedString: got backslash as last char on line.";
                    log.error(msg);
                    throw new Exception(msg);
                }
                ch = line.charAt(++idx);
            }
        }
    }
}
