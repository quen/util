package util;

import java.io.*;
import java.lang.reflect.*;

/**
 * File and IO-related utility methods.
 */
public abstract class IOUtils
{
	/** Used when free space is unknown */
	public final static long UNKNOWN = -1;
	
	/**
	 * Safely list the files in a folder.
	 * @param folder Folder to list
	 * @return All files in that folder; zero-length array if none
	 */
	public static File[] listFiles(File folder)
	{
		File[] result=folder.listFiles();
		if(result==null)
			return new File[0];
		else
			return result;
	}
	
	/**
	 * Recursively delete all files in a folder and subfolders, then the folder
	 * itself. 
	 * @param folder Folder to delete
	 * @throws IOException If anything can't be deleted
	 */
	public static void recursiveDelete(File folder) throws IOException
	{
		File[] result=folder.listFiles();
		if(result==null) result=new File[0];
		for(int i=0;i<result.length;i++)
		{
			if(result[i].isDirectory())
				recursiveDelete(result[i]);
			else
			{
				if(!result[i].delete())
				{
					throw new IOException("Unable to delete file: "+result[i]);
				}
			}
		}
		if(!folder.delete())
		{
			throw new IOException("Unable to delete folder: "+folder);
		}
	}
	
	private final static int BUFFERSIZE=65536;
	
	/**
	 * Loads all the bytes from an inputstream, then closes it.
	 * @param is Stream
	 * @return Bytes
	 * @throws IOException
	 */
	public static byte[] loadBytes(InputStream is) throws IOException
	{
		byte[] buffer=new byte[BUFFERSIZE];
		int pos=0;
		while(true)
		{
			// Read data
			int read=is.read(buffer,pos,buffer.length-pos);
			
			// Check EOF
			if(read==-1)
			{
				byte[] trimmed=new byte[pos];
				System.arraycopy(buffer,0,trimmed,0,pos);
				is.close();
				return trimmed;
			}
			
			// Advance position in buffer
			pos+=read;
			
			// Enlarge buffer if needed
			if(pos==buffer.length)
			{
				byte[] newBuffer=new byte[buffer.length*2];
				System.arraycopy(buffer,0,newBuffer,0,buffer.length);
				buffer=newBuffer;
			}
		}
	}
	
	/**
	 * Loads a UTF-8 string from an input stream, then closes it.
	 * @param is Stream to read
	 * @return String
	 * @throws IOException Any I/O error
	 */
	public static String loadString(InputStream is) throws IOException
	{
		return new String(loadBytes(is),"UTF-8");
	}
	
	/**
	 * Loads a string from an input stream in platform encoding, then closes it.
	 * @param is Stream to read
	 * @return String
	 * @throws IOException Any I/O error
	 */
	public static String loadStringPlatformEncoding(InputStream is) throws IOException
	{
		return new String(loadBytes(is));
	}
	
	/**
	 * Saves a UTF-8 string to an output stream, then closes it.
	 * @param s String to write
	 * @param os Stream
	 * @throws IOException Any I/O error
	 */
	public static void saveString(String s,OutputStream os) throws IOException
	{
		byte[] bytes=s.getBytes("UTF-8");
		os.write(bytes);
		os.close();
	}
	
	/**
	 * Copies from one input stream to another.
	 * @param is Source input stream. Will be closed when complete.
	 * @param os Target output stream
	 * @param closeOutput If true, target stream will also be closed when complete.
	 * @throws IOException 
	 */
	public static void copy(InputStream is,OutputStream os,boolean closeOutput)
		throws IOException
	{
		byte[] buffer=new byte[BUFFERSIZE];
		while(true)
		{
			int read=is.read(buffer);
			if(read==-1) break;
			os.write(buffer,0,read);
		}
		is.close();
		if(closeOutput) os.close();
	}
	
	/**
	 * Copies from one file to another.
	 * @param input Input file
	 * @param target Target file
	 * @param overwrite If true, overwrites existing target
	 * @throws IOException If file already exists, or another IO error
	 */
	public static void copy(File input,File target,boolean overwrite)
	  throws IOException
	{
		if(!overwrite && target.exists())
			throw new IOException("Cannot overwrite existing file: "+target);
		copy(new FileInputStream(input),new FileOutputStream(target),true);
	}

	/**
	 * Obtains the amount of free disk space.
	 * @param target Target file we're trying to write (this will be used to
	 *   determine the disk partition).
	 * @return Free space in bytes
	 */
	public static long getFreeSpace(File target)
	{
		if(PlatformUtils.isJavaVersionAtLeast(1,6))
		{
			try
			{
				Method m = target.getClass().getMethod("getUsableSpace", new Class[]{});
				return ((Long)m.invoke(target, new Object[]{})).longValue();			
			}
			catch(Exception e)
			{
				return UNKNOWN;
			}
		}
		else
		{
			return UNKNOWN;
		}
	}
}
