// Exception used to report receiving a bad command.

package com.tumri.joz.jozMain;

class BadCommandException extends Exception
{
    public BadCommandException (String msg)
    {
	super (msg);
    }
}
