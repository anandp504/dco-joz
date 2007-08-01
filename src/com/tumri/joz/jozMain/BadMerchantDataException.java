// Exception used to report bad mup data.

package com.tumri.joz.jozMain;

public class BadMerchantDataException extends Exception
{
    public BadMerchantDataException (String msg)
    {
	super (msg);
    }
}
