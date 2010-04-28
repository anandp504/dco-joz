package com.tumri.joz.index;

import com.tumri.content.data.Product;
import com.tumri.utils.FSUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author: nipun
 * Date: Feb 16, 2008
 * Time: 8:15:17 PM
 */
public class TestSortLines {
    static final int flagbit3 = 4;    // 2^^2    000...00000100
    static final int flagbit6 = 32;   // 2^^5    000...00100000
    static final int flagbit29 = (int) Math.pow(2, 28);
    static final String TAXONOMY_CATSPEC_FILE_FORMAT = ".*-CategorySpec_.*.utf8";

    public static void main(String[] args) {
        long key = createLongIndexKey(Product.Attribute.kExternalFilterField1, 932456);
        int[] result = getValuesFromLongAttrKey(key);
        System.out.println(result[0]);
        System.out.println(result[1]);
    }

    /**
     * Pack the values into a single long value.
     * The field pos is expected to be a integer between 1 and 7 since only 3 bits are used.
     * <field pos: 3 bits><val : 32>
     * @param kAttr
     * @param valId
     * @return
     */
    public static long createLongIndexKey(Product.Attribute kAttr, long valId) {
        long fieldPos = 1;
        long l2 = (fieldPos << (64-(3+1))) & 0xF000000000000000L;
        long l3 = l2 | (valId & 0x00000000FFFFFFFFL);
        return l3;
    }

    /**
     * Returns a int array of unpacked values.
     * 0 --> ValId
     * 1 --> Field Pos.
     * @param key
     * @return
     */
    public static int[] getValuesFromLongAttrKey(long key) {
        int[] results = new int[2];
        long rVal = key & 0x00000000FFFFFFFFL;
        long fVal = key & 0xF000000000000000L;
        fVal = fVal >> (64-(3+1));
        results[0] = (int)rVal;
        results[1] = (int)fVal;
        return results;
    }

    /**
     * Helper method to invoke the unix sort on the file
     */
    private static boolean sortFile(String filePath, String fileName) {
        File currentFile = new File(filePath + fileName);
        if (!currentFile.exists()) {
           return false;
        }

        File tmpFile = new File(filePath + "tmp" + fileName);

        String[] args = new String[]{"sh", "-c", "sort " + currentFile.getAbsolutePath() + " > " + tmpFile.getAbsolutePath()};
        boolean bSuccess = false;
        Runtime rt = Runtime.getRuntime();
        try {
            Process proc = rt.exec(args);
            int rc = proc.waitFor();
            if (rc == 0) {
                bSuccess = true;
                //Copy the tmp file back to the orig file
                FSUtils.copyFile(tmpFile, currentFile);
                tmpFile.delete();
            }
        } catch (IOException e) {
           bSuccess = false;
        } catch (InterruptedException e) {
           bSuccess = false;
        }
        if (!bSuccess) {
            System.out.println("Sort failed for the file : " + fileName);
        }

        return bSuccess;
    }


}
