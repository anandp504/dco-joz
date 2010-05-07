package com.tumri.joz.utils;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility to run shell commands, handling the error and input stream correctly.
 */
public class ShellCommand
{
    static Logger log =Logger.getLogger(ShellCommand.class);

    private String m_command;
    private String m_error;
    private File m_workingDirectory;

    public static int executeCommand(String cmd, File cwd) throws Throwable {
        try {
            ShellCommand scmd = new ShellCommand(cmd, cwd.getAbsolutePath());
            return scmd.exec();
		} catch (Throwable e) {
			String msg = "Error occurred during process execution "+cmd+"\n";
			msg=msg+e.getMessage();
			log.error(msg);
			throw e;
		}
	}
    
    /**
     * We need to always supply a working directory, so that there are no mistakes
     * @param aCommand - The command to run
     * @param workingDir - working dir
     */
    public ShellCommand(String aCommand, String workingDir) {
        m_command = aCommand;
        m_workingDirectory = new File(workingDir);
        if (!m_workingDirectory.exists() || !m_workingDirectory.isDirectory())
            throw new RuntimeException("Script Directory not found " + workingDir);
        else
            log.debug("script working directory is :" + workingDir);
    }

    public int exec() {
        InputStream is = null;
        InputStream es = null;
        int ret = -1;
        try {
            Process p = Runtime.getRuntime().exec(m_command, null, m_workingDirectory);
            is = p.getInputStream();
            es = p.getErrorStream();

            InputReader inr = new InputReader(is, "info");
            Thread inReader = new Thread(inr);

            InputReader err = new InputReader(es, "error");
            Thread erReader = new Thread(err);

            inReader.start();
            erReader.start();
            try {
                erReader.join(getTimeout());
                inReader.join(getTimeout());
            } catch (InterruptedException e) {
                log.error("Command Timed out!!");
                throw new RuntimeException(e);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            ret = p.waitFor();
        } catch (InterruptedException e) {
        	log.error("ShellCommand Error : "+e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
        	log.error("ShellCommand Error : "+e.getMessage());
            throw new RuntimeException(e);
        } finally {
            close(is);
            close(es);
        }
        if (m_error!=null) {
            throw new RuntimeException(m_error);
        }

        return ret;
    }

    protected long getTimeout() {
        return 600000; // time in milli seconds 10 minutes
    }

    public String toString() {
        return m_command;
    }

    private void close(InputStream is) {
        try {
            if (is != null)
                is.close();
        } catch (IOException e) {
        }
    }

    class InputReader implements Runnable {
        InputStream m_is;
        byte[] m_bytes;
        String m_logType;

        public InputReader(InputStream aIs, String logType) {
            m_is = aIs;
            m_logType=logType;
        }

        public byte[] getBytes() {
            return m_bytes;
        }

        public void run() {
            m_bytes = readStream(m_is);
        }

        private byte[] readStream(InputStream is) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int len;
                while ((len = is.read(buf)) > 0) {
                	log.info(new String(buf, 0, len));
                }
                out.close();
                return out.toByteArray();
            } catch (Exception e) {
                log.error("ShellCommand error : ",e);
                m_error = e.getMessage();
            }
            return null;
        }
    }
}