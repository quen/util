package util;

import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.util.regex.*;

import javax.swing.*;

/** 
 * Utilities to support running on different platforms.
 */
public abstract class PlatformUtils
{
	private final static Pattern MACOSVERSION =
		Pattern.compile("^([0-9]+)\\.([0-9]+)\\.([0-9]+).*$");
	private final static Pattern JAVAVERSION =
		Pattern.compile("^([0-9]+)\\.([0-9]+).*$");

	private final static long MACHIDDEN_CACHE_EXPIRY = 2000;
	private static boolean macHiddenCache;
	private static long macHiddenCacheTime;

	private static int macIndentEdit = -1, macIndentButton = -1;

	/** @return True if user is on Mac */
	public static boolean isMac()
	{
		// Code from http://developer.apple.com/technotes/tn2002/tn2110.html
		String lcOSName=System.getProperty("os.name").toLowerCase();
		return lcOSName.startsWith("mac os x");
	}

	/**
	 * @param major Major version (eg 10)
	 * @param minor Minor version (eg 5)
	 * @param sub Sub-version (eg 0)
	 * @return True if user is on Mac and the OS X version is at least major.minor.sub
	 */
	public static boolean isMacOSVersion(int major, int minor, int sub)
	{
		if(!isMac()) return false;
		Matcher m=MACOSVERSION.matcher(System.getProperty("os.version"));
		if(!m.matches()) return false;
		int
			actualMajor=Integer.parseInt(m.group(1)),
			actualMinor=Integer.parseInt(m.group(2)),
			actualSub=Integer.parseInt(m.group(3));
		return
			actualMajor>major ||
			  (actualMajor==major &&
			  	(actualMinor>minor ||
			  		(actualMinor==minor && actualSub>=sub)));
	}

	/**
	 * Exception thrown when there is a failure running AppleScript
	 */
	public static class AppleScriptException extends Exception
	{
		private AppleScriptException(String message,Throwable cause)
		{
			super(message,cause);
		}
		private AppleScriptException(String message)
		{
			super(message);
		}
	}

	/**
	 * Runs AppleScript (Mac only, see {@link PlatformUtils#isMac()}).
	 * @param script Script
	 * @return Result of AppleScript as string
	 * @throws AppleScriptException If there is an error launching the AppleScript
	 *   or if the AppleScript itself causes an error (e.g. syntax error)
	 */
	public static String runAppleScript(String script) throws AppleScriptException
	{
		if(!isMac())
			throw new AppleScriptException("Not a Mac");

		if(isJavaVersionAtLeast(1,6))
		{
			Method eval;
			Object engine;
			try
			{
				Object mgr=Class.forName("javax.script.ScriptEngineManager").newInstance();
				engine=mgr.getClass().getMethod("getEngineByName",new Class[] {String.class}).
				  invoke(mgr,new Object[] {"AppleScript"});
			  eval=engine.getClass().getMethod("eval",new Class[] {String.class});
			}
			catch(Exception e)
			{
				throw new AppleScriptException("[javax.script] Error launching AppleScript",e);
			}

			Object result;
		  try
			{
				result=eval.invoke(engine,new Object[] {script});
			}
			catch(Exception e)
			{
				throw new AppleScriptException("[javax.script] Error running AppleScript",e);
			}
			return result.toString();
		}
		else
		{
			String stdOut,stdErr;
			try
			{
				Process p=Runtime.getRuntime().exec(new String[] { "osascript", "-e",
			    script });
			  p.waitFor();
		    stdOut=IOUtils.loadString(p.getInputStream()).trim();
		    stdErr=IOUtils.loadString(p.getErrorStream()).trim();
			}
			catch(Exception e)
			{
				throw new AppleScriptException("[osascript] Error launching AppleScript",e);
			}
		  if(!stdErr.equals(""))
		  	throw new AppleScriptException("[osascript] Error running AppleScript: "+stdErr);
		  return stdOut;
		}
	}

	/** @return True if user is on a Mac and this application is hidden */
	public static boolean isMacAppHidden()
	{
		// Not a Mac? Can't be hidden
		if(!isMac()) return false;
		// Window is active? Can't be hidden
		if(KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow()!=null)
			return false;
		// Got cached details?
		long now=System.currentTimeMillis();
		if(now-macHiddenCacheTime < MACHIDDEN_CACHE_EXPIRY)
			return macHiddenCache;
		// Oh dear, better call AppleScript to find out
	  try
		{
	  	String stdOut=runAppleScript(
	  		"tell app \"System Events\" to get visible of process \"leafChat\"");
			if(stdOut.equals("false") || stdOut.equals("0"))
				macHiddenCache=true;
			else if(stdOut.equals("true") || stdOut.equals("1"))
				macHiddenCache=false;
			macHiddenCacheTime=now;
		}
		catch(AppleScriptException e)
		{
			// Well this isn't working so let's not try it again
			if(e.getMessage().indexOf("Can’t get process")!=-1)
				System.err.println("Unable to find leafChat process to check hidden state");
			else
			  e.printStackTrace();
			macHiddenCache=false;
			macHiddenCacheTime=now+1000L*60L*60L*24L;
		}
		return macHiddenCache;
	}

	/**
	 * @param major Major version (eg 10)
	 * @param minor Minor version (eg 5)
	 * @param sub Sub-version (eg 0)
	 * @return True if user is on Mac and the OS X version is not at least major.minor.sub
	 */
	public static boolean isMacOSVersionLessThan(int major,int minor,int sub)
	{
		if(!isMac()) return false;
		return !isMacOSVersion(major,minor,sub);
	}

	/**
	 * Obtains indent used on some controls on Mac so that they line up
	 * horizontally with other controls like edit boxes that have an added
	 * margin for the focus ring. If not running on a Mac, returns zero.
	 * @deprecated Use a specific method such as {@link #getMacIndentEdit()}.
	 * @return Indent in pixels
	 */
	public static int getMacIndent()
	{
		return getMacIndentEdit();
	}
	
	/**
	 * Obtains indent used on some controls on Mac so that they line up
	 * horizontally with edit boxes that have an added margin for the focus
	 * ring. If not running on a Mac, returns zero.
	 * @return Indent in pixels
	 */
	public static int getMacIndentEdit()
	{
		if(macIndentEdit == -1)
		{
			if(isMac())
			{
				JTextField test = new JTextField();
				macIndentEdit = test.getInsets().left - 4;
			}
			else
			{
				macIndentEdit = 0;
			}
		}
		return macIndentEdit;
	}

	/**
	 * Obtains indent used on some controls on Mac so that they line up
	 * horizontally with buttons that have an added margin for the focus
	 * ring. If not running on a Mac, returns zero.
	 * @return Indent in pixels
	 */
	public static int getMacIndentButton()
	{
		if(macIndentButton == -1)
		{
			if(isMac())
			{
				JButton test = new JButton("Test");
				macIndentButton = test.getInsets().left - 16;
			}
			else
			{
				macIndentButton = 0;
			}
		}
		return macIndentButton;
	}

	/**
	 * @param major Java major version, i.e. 1
	 * @param minor Java minor version e.g. 6
	 * @return True if user has Java major.minor or greater
	 */
	public static boolean isJavaVersionAtLeast(int major,int minor)
	{
		Matcher m=JAVAVERSION.matcher(System.getProperty("java.version"));
		if(!m.matches())
		{
			System.err.println("Unexpected error matching Java version");
			return false; // What?
		}
		int
		  actualMajor=Integer.parseInt(m.group(1)),
		  actualMinor=Integer.parseInt(m.group(2));
		return actualMajor>major || (actualMajor==major && actualMinor>=minor);
	}

		/** @return True if user is on Windows */
		public static boolean isWindows()
		{
			return System.getProperty("os.name").indexOf("Windows")!=-1;
		}

	/**
	 * @return The InputEvent modifier used for action commands (Ctrl on most
	 *   platforms, Command on Mac)
	 */
	public static int getActionKeyMask()
	{
		return isMac() ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK;
	}

	/**
	 * @param programName Name of program (to use as last folder)
	 * @return A File object representing the user folder for the program
	 */
	public static File getUserFolder(String programName)
	{
		File f;
		if(isMac())
		{
			f=new File(new File(System.getProperty("user.home")),"Library/"+programName);
		}
		else if(isWindows())
		{
			String appData=null;
			try
			{
				appData=System.getenv("APPDATA");
			}
			catch(Error java14isbroken) // getenv throws Error on 1.4, apparently
			{
			}
			if(appData==null)
				appData=System.getProperty("user.home")+"/Application Data";
			f=new File(appData+"/"+programName);
		}
		else
		{
			f=new File(new File(System.getProperty("user.home")),"."+programName);
		}
		if(!f.exists())
		{
			f.mkdir();
			if(!isWindows())
			{
				String path;
				try
				{
					path=f.getCanonicalPath();
					Runtime.getRuntime().exec(new String[]
					{
						"chmod","700",path
					});
				}
				catch(IOException e)
				{
					// Ignore failure, maybe the OS doesn't have chmod
				}
			}
		}
		return f;
	}

	/** User folder */
	private static File fUser=null;

	/**
	 * Obtains the user folder for current program (that set with setUserFolder)
	 * @return Folder
	 * @throws Error if folder wasn't set
	 */
	public static File getUserFolder()
	{
		if(fUser==null)
			throw new Error("Cannot call getUserFolder() unless setUserFolder() was previously called");
		return fUser;
	}

	/**
	 * Should be called during program init to set the program's name used
	 * for preferences (allows getUserFolder to work without needing a
	 * parameter).
	 * @param sProgramName Name of program (prefs folder)
	 */
	public static void setUserFolder(String sProgramName)
	{
		fUser=getUserFolder(sProgramName);
	}

	/**
	 * @return The user's desktop folder, or suitable equivalent.
	 */
	public static String getDesktopFolder()
	{
		if(isMac() || isWindows())
      return System.getProperty("user.home")+"/Desktop";
		else
      return System.getProperty("user.home");
	}

	/**
	 * @return The default download folder, or desktop folder if download folder
	 *   is not defined by the platform.
	 */
	public static String getDownloadFolder()
	{
		if(isMac() &&
			Float.parseFloat(
				System.getProperty("os.version").replaceAll("^([0-9]+\\.[0-9]+).*$","$1"))
				>10.4999f) // Mac 10.5
			return System.getProperty("user.home")+"/Downloads";
		else
			return getDesktopFolder();
	}

	/**
	 * @return The user's documents folder, or suitable equivalent.
	 */
	public static String getDocumentsFolder()
	{
		if(isMac() || isWindows())
      return System.getProperty("user.home")+"/Documents";
		else
      return System.getProperty("user.home");
	}

	/**
	 * Opens the user's Web browser with a given URL.
	 * @param u URL to open
	 * @throws IOException If there's a problem
	 */
	public static void showBrowser(URL u) throws IOException
	{
		try
		{
			try
			{
				// Use Java 1.6 Desktop if available
				Object desktop=Class.forName("java.awt.Desktop").
					getMethod("getDesktop",new Class[] {}).invoke(null,new Object[] {});
				desktop.getClass().getMethod("browse",new Class[] {URI.class}).
					invoke(desktop,new Object[] { u.toURI() });
			}
			catch(Exception x)
			{
				// Fallback for pre-Java 1.6
				if(isMac())
				{
					Class.forName("com.apple.mrj.MRJFileUtils")
						.getMethod("openURL",new Class[]{String.class})
						.invoke(null,new Object[]{u.toString()});
				}
				else if(isWindows())
				{
					String url=u.toString();
					if(url.startsWith("file:/"))
					{
						url=url.substring("file:/".length());
						url=url.replace('/','\\');
					}
					Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler \"" + url+"\"");
				}
				else
				{
					// Well we could try running Firefox
					Runtime.getRuntime().exec(new String[] {"firefox",u.toString()});
				}
			}
		}
		catch(Exception x)
		{
			IOException ioe=new IOException("Failed to open Web browser");
			ioe.initCause(x);
			throw ioe;
		}
	}

	/**
	 * Opens the given file or folder using Java 6 Desktop API.
	 * @param f File or folder to open
	 * @return True if the file or folder was opened; false if it could not be
	 *   opened for any reason
	 */
	public static boolean systemOpen(File f)
	{
		try
		{
			// Use Java 1.6 Desktop if available
			Object desktop=Class.forName("java.awt.Desktop").
				getMethod("getDesktop",new Class[] {}).invoke(null,new Object[] {});
			desktop.getClass().getMethod("open",new Class[] {File.class}).
				invoke(desktop,new Object[] { f });
			return true;
		}
		catch(Exception x)
		{
			return false;
		}
	}
}
