// User-configurable parameters (implemented as properties).

package com.tumri.joz.jozMain;

public class Props
{
    private static final int DEFAULT_SERVER_PORT = 2501 /*42000*/;
    private static final int DEFAULT_MONITOR_PORT = 42080;

    private static int server_port = DEFAULT_SERVER_PORT;
    private static int monitor_port = DEFAULT_MONITOR_PORT;

    public static int
    get_server_port ()
    {
	return server_port;
    }

    public static int
    get_monitor_port ()
    {
	return monitor_port;
    }
}
