// Exception used to report bad mup data.

package com.tumri.joz.jozMain;

public class BadMUPDataException extends Exception
{
    public BadMUPDataException (String msg)
    {
	super (msg);
    }
}
