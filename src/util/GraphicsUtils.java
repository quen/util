package util;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;

import javax.swing.JPanel;

/**
 * Graphical utils.
 */
public abstract class GraphicsUtils
{
	/** Global font render context */
	private static FontRenderContext frc=null;
		
	/** @return Font render context used for text measurement etc. */
	public static FontRenderContext getFontRenderContext()
	{
		if(frc==null)
		{
			BufferedImage bi=new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
			frc=bi.createGraphics().getFontRenderContext();
		}
		return frc;
	}
	
	/**
	 * Returns a colour with new opacity. If the original colour is solid, the
	 * result will have alpha=opacity. Otherwise it'll be the combination.
	 * @param c Original colour
	 * @param opacity Opacity 0-255
	 * @return New colour
	 */
	public static Color combineOpacity(Color c,int opacity)
	{
		if(opacity==255)
			return c;
		else
			return new Color(c.getRed(),c.getGreen(),c.getBlue(),(opacity*c.getAlpha())/255);
	}
	
	/**
	 * @param s Colour string #rrggbb or #rgb
	 * @return Colour object
	 * @throws NumberFormatException If the format doesn't match
	 */
	public static Color parseColour(String s)
	{
		if(s.matches("#[0-9a-fA-F]{6}"))
		{
			try
			{
				return new Color(
					Integer.parseInt(s.substring(1,3),16),
					Integer.parseInt(s.substring(3,5),16),
					Integer.parseInt(s.substring(5,7),16));
			}
			catch(NumberFormatException nfe)
			{
				throw new NumberFormatException(
					"Invalid colour syntax '"+s+"' expecting #rrggbb");
			}
		}
		else if(s.matches("#[0-9a-fA-F]{3}"))
		{
			try
			{
				int 
					r=Integer.parseInt(s.substring(1,2),16),
					g=Integer.parseInt(s.substring(2,3),16),
					b=Integer.parseInt(s.substring(3,4),16);
				r=r+(16*r);
				g=g+(16*g);
				b=b+(16*b);
				return new Color(r,g,b);
			}
			catch(NumberFormatException nfe)
			{
				throw new NumberFormatException(
					"Invalid colour syntax '"+s+"' expecting #rgb");
			}
		}
		else
		{
			throw new NumberFormatException(
				"Invalid colour syntax '"+s+"' expecting #rgb or #rrggbb");
		}		
	}
	
	/** Used only if we need a component for MediaTracker */
	public static JPanel pStupid=null;

 	/**
 	 * Draw a focus rectangle on a graphics context.
 	 * @param g2 Graphics context
 	 * @param iX X position (e.g. 0)
 	 * @param iY Y position
 	 * @param iWidth Actual desired width (e.g. getWidth())
 	 * @param iHeight Actual desired height
 	 */
	public static void drawFocus(Graphics2D g2,int iX,int iY,int iWidth,int iHeight)
	{
		int FOCUSTRANSPARENCY=96;
		Color beforeColor=g2.getColor();
		Stroke beforeStroke=g2.getStroke();
		g2.setColor(new Color(255,255,255,FOCUSTRANSPARENCY));
		g2.setStroke(new BasicStroke(1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,
			1.0f,new float[] {1.0f,1.0f},0.0f));
		g2.drawRect(iX,iY,iWidth-1,iHeight-1);
		g2.setColor(new Color(0,0,0,FOCUSTRANSPARENCY));
		g2.setStroke(new BasicStroke(1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,
			1.0f,new float[] {1.0f,1.0f},1.0f));
		g2.drawRect(iX,iY,iWidth-1,iHeight-1);
		g2.setColor(beforeColor);
		g2.setStroke(beforeStroke);
	}
	
	/**
	 * Loads an image that isn't JPEG (with default Java loader).
	 * @param data
	 * @return Image (ready for display)
	 */
	public static Image loadOther(byte[] data)
	{
		Image iReturn;
		if(pStupid==null) pStupid=new JPanel();
		iReturn=Toolkit.getDefaultToolkit().createImage(data);
		waitImage(iReturn);
		return iReturn;
	}
	
	/**
	 * Loads any image from a URL.
	 * @param u URL
	 * @return Image
	 * @throws IOException Any error
	 */
	public static Image loadImage(URL u) throws IOException
	{
		return loadImage(u.openStream(),u.getPath());
	}
	
	/**
	 * Loads an image (and waits for it to load completely).
	 * @param is Input stream to load from
	 * @param fileName File name (only used to check for a .jpg extension, rest can be anything)
	 * @return Image
	 * @throws IOException If image can't be loaded
	 */
	public static Image loadImage(InputStream is,String fileName) throws IOException
	{
		if(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
		{
		  return loadJPEG(is);
		}
		else
		{
		  return loadOther(IOUtils.loadBytes(is));
		}
	}
	
	/**
	 * Loads an image as a 'stamp'; the image's greyscale value (actually
	 * blue channel if it's colour) is used as alpha value for a new image
	 * with the specified colour.
	 * @param u Image URL
	 * @param c Colour for stamp
	 * @return New image in the defined colour
	 * @throws IOException Any loading error
	 */
	public static BufferedImage loadStamp(URL u, Color c) throws IOException
	{
		// Load image and copy into buffered image
		Image i = loadImage(u);		
		BufferedImage bi = new BufferedImage(i.getWidth(null),i.getHeight(null),
		  BufferedImage.TYPE_INT_ARGB);
		bi.getGraphics().drawImage(i,0,0,null);
		
		// Lame loop to do 'stamp' thing
		int colour = c.getRGB() & 0xffffff;
		for(int iY=0;iY<bi.getHeight();iY++)
		{
			for(int iX=0;iX<bi.getWidth();iX++)
			{
				int rgb=bi.getRGB(iX,iY);
				rgb = colour | ((rgb & 0xff)<<24);
				bi.setRGB(iX,iY,rgb);
			}
		}
		
		return bi;		
	}
	
	/**
	 * Loads a JPEG image using Sun's loader in prefrence to  the standard one,
	 * if it's available. (Probably there is no point in doing this any more...)
	 * @param is Input stream
	 * @return Image
	 * @throws IOException
	 */
	public static Image loadJPEG(InputStream is) throws IOException
	{
		// If Sun's codec is present, use it (for some ungodly reason, it's twice
		// as fast as the normal loader). Do reflection to avoid causing link errors
		// if it's unavailable
		try
		{			
			Class<?> cJPEGCodec=Class.forName("com.sun.image.codec.jpeg.JPEGCodec");
			Method m = cJPEGCodec.getDeclaredMethod(
				"createJPEGDecoder", InputStream.class);
			Object decoder = m.invoke(null, is);
			m = decoder.getClass().getDeclaredMethod("decodeAsBufferedImage");
			return (Image)m.invoke(decoder);
		}
		catch(Exception e)
		{
			return loadOther(IOUtils.loadBytes(is));
		}
	}
	
	private static void waitImage(Image i)
	{
		MediaTracker mt=new MediaTracker(pStupid);
		mt.addImage(i,1);
		try
		{
			mt.waitForAll();
		}
		catch (InterruptedException e2)
		{
		}
	}
}
