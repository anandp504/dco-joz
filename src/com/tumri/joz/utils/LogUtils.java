/* 
 * LogUtils.java
 * 
 * COPYRIGHT (C) 2007 TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE 
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
 * @version 1.0     Oct 10, 2007
 * 
 */
package com.tumri.joz.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;

import java.util.Enumeration;


/**
 *
 * Created by Bhavin Doshi (bdoshi@tumri.com) on Oct 10, 2007
 * Company: Tumri Inc.
 */
public class LogUtils {
	protected static Logger fatallog = Logger.getLogger("fatal");
	protected static Logger timinglog = Logger.getLogger("timing");
	protected static Logger logger = Logger.getLogger("com.tumri");

	public static Logger getFatalLog() {
		return fatallog;
	}

	public static Logger getTimingLog() {
		return timinglog;
	}

	public static Logger getLogger(){
		return logger;
	}

	public static void setLogLevel(String logName, String lvl){
		Logger rootLog = Logger.getRootLogger();
		LoggerRepository repo = rootLog.getLoggerRepository();

		Logger log;
		if(logName == null || lvl == null || "".equals(logName.trim()) || "".equals(lvl.trim())){
			return;
		}
		if("root".equalsIgnoreCase(logName)){
			log = Logger.getRootLogger();
		} else {
			log = repo.exists(logName);
		}
	    if(log == null){
		    return;
	    }
		
		if("debug".equalsIgnoreCase(lvl)){
			log.setLevel(Level.DEBUG);
		} else if("error".equalsIgnoreCase(lvl)){
			log.setLevel(Level.ERROR);
		} else if("info".equalsIgnoreCase(lvl)){
			log.setLevel(Level.INFO);
		} else if("fatal".equalsIgnoreCase(lvl)){
			log.setLevel(Level.FATAL);
		} else if("warn".equalsIgnoreCase(lvl)){
			log.setLevel(Level.WARN);
		} else if("off".equalsIgnoreCase(lvl)){
			log.setLevel(Level.OFF);
		}
	}

	public static void setLogLevel(String s){
		Logger rootLogger = Logger.getRootLogger();

		LoggerRepository repo = rootLogger.getLoggerRepository();
		Enumeration logEnum = repo.getCurrentLoggers();
		if("debug".equalsIgnoreCase(s)){
				rootLogger.setLevel(Level.DEBUG);
			} else if("error".equalsIgnoreCase(s)){
				rootLogger.setLevel(Level.ERROR);
			} else if("info".equalsIgnoreCase(s)){
				rootLogger.setLevel(Level.INFO);
			} else if("fatal".equalsIgnoreCase(s)){
				rootLogger.setLevel(Level.FATAL);
			} else if("warn".equalsIgnoreCase(s)){
				rootLogger.setLevel(Level.WARN);
			} else if("off".equalsIgnoreCase(s)){
				rootLogger.setLevel(Level.OFF);
			}
		int i = 0;
		while(logEnum.hasMoreElements()){
			Logger log = (Logger)logEnum.nextElement();

			if("debug".equalsIgnoreCase(s)){
				log.setLevel(Level.DEBUG);
			} else if("error".equalsIgnoreCase(s)){
				log.setLevel(Level.ERROR);
			} else if("info".equalsIgnoreCase(s)){
				log.setLevel(Level.INFO);
			} else if("fatal".equalsIgnoreCase(s)){
				log.setLevel(Level.FATAL);
			} else if("warn".equalsIgnoreCase(s)){
				log.setLevel(Level.WARN);
			} else if("off".equalsIgnoreCase(s)){
				log.setLevel(Level.OFF);
			}
		}
	}

}
