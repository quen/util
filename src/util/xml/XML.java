package util.xml;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Static utility class for XML DOM processing.
 */
public abstract class XML
{
	private static DocumentBuilderFactory dbf;
	private static DocumentBuilder db;

	/**
	 * Parses a document from an input stream.
	 * @param is Input stream
	 * @return Parsed document
	 * @throws XMLException If there's a parsing error
	 */
	public static Document parse(InputStream is) throws XMLException
	{
		return parse(is,null);
	}
	/**
	 * Parses a document from a file.
	 * @param f File
	 * @return Parsed document
	 * @throws XMLException If there's a parsing error
	 */
	public static Document parse(File f) throws XMLException
	{
		return parse(null,f);
	}	
	/**
	 * Parses a document from a string.
	 * @param s String of well-formed XML
	 * @return Parsed document
	 * @throws XMLException If there's a parsing error
	 */
	public static Document parse(String s) throws XMLException
	{
		try
		{
			return parse(new ByteArrayInputStream(s.getBytes("UTF-8")));
		}
		catch(UnsupportedEncodingException uee)
		{
			throw new XMLException("UTF-8 is always supported, this can't happen",uee);
		}
	}	
	/**
	 * Parses a document from a class resource. This is a shortcut for 
	 * parse(c.getResourceAsStream(resource)) with some extra error checking.
	 * @param c Class
	 * @param resource Filename of resource relative to class
	 * @return Parsed document
	 * @throws XMLException If there's a parsing error
	 */
	public static Document parse(Class<?> c,String resource) throws XMLException
	{
		try
		{
			InputStream is=c.getResourceAsStream(resource);
			if(is==null)
				throw new XMLException("Class XML content not found: "+resource);
			Document d=parse(is,null);
			is.close();
			return d;
		}
		catch(IOException ioe)
		{
			throw new XMLException("Error closing class data stream, this can't happen",ioe);			
		}
	}
	/**
	 * Internal method used for all parsing.
	 * @param stream Input stream
	 * @param f File used as reference for DTDs etc.
	 * @return Parsed document
	 * @throws XMLException If there's a parsing error
	 */
	private synchronized static Document parse(InputStream stream, File f) 
		throws XMLException
	{
		checkStatics();
		
		try
		{
			if(stream != null) 
			{
				return db.parse(stream);
			}
			if(f != null)
			{
				// Ensure that the file gets closed; an automatic bug report suggested
				// this might occasionally not happen. (Speculation only, can't
				// reproduce.)
				stream = new FileInputStream(f);
				InputSource source = new InputSource(stream);
				source.setSystemId(f.toURI().toString());
			  return db.parse(source);
			}
			throw new XMLException("No input source specified");
		}
		catch(SAXParseException spe)
		{
			throw new XMLException(
        "XML error on line "+spe.getLineNumber()+", column "+spe.getColumnNumber(),
        spe);
		}
		catch (SAXException se)
		{
			throw new XMLException("XML error at unknown location",se);
		}
		catch (IOException e)
		{
			throw new XMLException("Error reading XML data",e);
		}
		finally
		{
			if(stream != null)
			{
				// It should get closed anyway, but just in case
				try
				{
					stream.close();
				}
				catch(IOException e)
				{
					// Ignore
				}
			}
		}
	}
	
	/**
	 * Creates a new XML document.
	 * @param documentElement Name for document element (root tag)
	 * @return Document object
	 * @throws XMLException If create fails
	 */
	public synchronized static Document newDocument(String documentElement) throws XMLException
	{
		Document d=newDocument();
		d.appendChild(d.createElement(documentElement));		
		return d;
	}

	/**
	 * Creates a new empty XML document.
	 * @return Document object
	 * @throws XMLException If create fails
	 */
	public synchronized static Document newDocument() throws XMLException
	{
		checkStatics();

		return db.newDocument();
	}

	/**
	 * Sets up the static items used.
	 * @throws XMLException If there is a problem with the Java VM's XML setup
	 */
	private synchronized static void checkStatics() throws XMLException
	{
		if(db!=null) return;
		try
		{
			dbf=DocumentBuilderFactory.newInstance();
			db=dbf.newDocumentBuilder();
		}
		catch(ParserConfigurationException pce)
		{
			throw new XMLException(pce);			
		} 
	}
	
	/**
	 * Obtains the value of a required attribute.
	 * @param e Element
	 * @param name Attribute name
	 * @return Attribute value
	 * @throws XMLException If attribute doesn't exist
	 */
	public static String getRequiredAttribute(Element e,String name) throws XMLException
	{
		if(!e.hasAttribute(name)) throw new XMLException("<"+e.getTagName()+">: Missing attribute "+name+"=");
		return e.getAttribute(name);
	}
	
	/**
	 * Obtains the value of a required attribute which must be an integer
	 * @param e Element
	 * @param name Attribute name
	 * @return Attribute value
	 * @throws XMLException If attribute doesn't exist or isn't an integer
	 */
	public static int getIntAttribute(Element e,String name) throws XMLException
	{
		try
		{
			return Integer.parseInt(getRequiredAttribute(e,name));
		}
		catch(NumberFormatException nfe)
		{
			throw new XMLException("<"+e.getTagName()+">: Invalid attribute "+name+"=, expecting integer");
		}
	}
	
	/**
	 * Returns child element of given tag name.
	 * @param parent Parent
	 * @param name Desired tag name
	 * @return Element
	 * @throws XMLException If one doesn't exist
	 */
	public static Element getChild(Node parent,String name) 
	  throws XMLException
	{
		for(Node n=parent.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element)
			{
				Element e=(Element)n;
				if(e.getTagName().equals(name)) return e;
			}
		}
		throw new XMLException("Element "+name+" not found");
	}
	
	/**
	 * @param parent Parent
	 * @param name Desired tag name
	 * @return True if child of that name exists
	 */
	public static boolean hasChild(Node parent,String name) 
	{
		for(Node n=parent.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element)
			{
				Element e=(Element)n;
				if(e.getTagName().equals(name)) return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns child elements of given tag name.
	 * @param parent Parent
	 * @param name Desired tag name
	 * @return Elements
	 */
	public static Element[] getChildren(Node parent,String name) 
	{
		List<Element> l = new LinkedList<Element>();
		for(Node n=parent.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element)
			{
				Element e=(Element)n;
				if(e.getTagName().equals(name)) l.add(e);
			}
		}
		return l.toArray(new Element[l.size()]);
	}

	/**
	 * Returns all child elements.
	 * @param parent Parent
	 * @return Elements
	 */
	public static Element[] getChildren(Node parent) 
	{
		List<Element> l = new LinkedList<Element>();
		for(Node n=parent.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element) l.add((Element)n);
		}
		return l.toArray(new Element[l.size()]);
	}
	
	/**
	 * Gets text from within a named child element.
	 * @param parent Parent node
	 * @param element Element to look for
	 * @return Text within that element, trimmed and whitespace-converted; 
	 *   "" if there is no text
	 * @throws XMLException If the element does not exist
	 */
	public static String getChildText(Node parent, String element) throws XMLException
	{
		return getChildText(parent,element,true);
	}
	
	/**
	 * Gets text from within a named child element.
	 * @param parent Parent node
	 * @param element Element to look for
	 * @param fixText If true, trims and normalises whitespace
	 * @return Text within that node; "" if there is no text
	 * @throws XMLException If the element does not exist
	 */
	public static String getChildText(Node parent, String element,boolean fixText) throws XMLException
	{
		Element e=getChild(parent,element);
		return getText(e,fixText);
	}
	
	/**
	 * Gets text from within named child elements.
	 * @param parent Parent node
	 * @param element Element name to look for
	 * @return Array of text within each element
	 */
	public static String[] getChildTexts(Node parent, String element)
	{
		Element[] ae=getChildren(parent,element);
		String[] as=new String[ae.length];
		for (int i= 0; i < as.length; i++)
		{	
			as[i]=getText(ae[i]);
		}
		return as;
	}
	
	/**
	 * Gets text from within a node.
	 * @param parent Node containing text
	 * @return Text within that node, trimmed and whitespace-converted; 
	 *   "" if there is no text
	 */
	public static String getText(Node parent)
	{
		return getText(parent, true, false);
	}
	
	/**
	 * Gets text from within a node.
	 * @param parent Node containing text
	 * @param fixText If true, trims and normalises whitespace
	 * @return Text within that node; "" if there is no text
	 */
	public static String getText(Node parent,boolean fixText)
	{
		return getText(parent, fixText, false);
	}

	/**
	 * Gets text from within a node and any child nodes.
	 * @param parent Node containing text
	 * @param fixText If true, trims and normalises whitespace
	 * @param recursive If true, also obtains text from child elements
	 * @return Text within that node; "" if there is no text
	 */
	public static String getText(Node parent, boolean fixText, boolean recursive)
	{
		StringBuffer sb=new StringBuffer();
		for(Node n=parent.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Text)
			{
				sb.append(n.getNodeValue());
			}
			else if(recursive)
			{
				sb.append(getText(n, false, true));
			}
		}
		if(fixText)
			return fixText(sb.toString());
		else
			return sb.toString();
	}
	
	/**
	 * Removes whitespace at beginning and end of string; runs of whitespace in the 
	 * middle are converted to a single space. See also {@link #normaliseText(String)}.
	 * @param s Text
	 * @return Text trimmed and whitespace-converted
	 */
	private static String fixText(String s)
	{
		s=s.trim();
		boolean bLastWhitespace=false;
		
		StringBuffer sb=new StringBuffer();		
		for(int i=0;i<s.length();i++)
		{
			char c=s.charAt(i);
			if(Character.isWhitespace(c))
			{
				if(!bLastWhitespace)
				{
					sb.append(' ');
					bLastWhitespace=true;
				}				
			}
			else
			{
			  sb.append(c);
			  bLastWhitespace=false;
			}
		}
		return sb.toString();
	}
	
	/** Save document in XML mode */
	public final static int MODE_XML=0;
	/** Save document in XHTML mode */
	public final static int MODE_XHTML=1;
	
	/**
	 * Saves a document quickly (without using XSLT API).
	 * @param mode MODE_xx constant
	 * @param d Document to save
	 * @param w Writer to save it to
	 * @throws IOException Any save error
	 */
	public static void fastSave(int mode,Document d,Writer w) throws IOException
	{
		if(mode==MODE_XHTML)
		{
			w.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n"+
				"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
		}
		fastSave(mode,d.getDocumentElement(),w);
	}
	
	private final static Set<String> XHTMLMINIMISETAGS = new HashSet<String>(
		Arrays.asList(new String[] 
	{
		"br","img","meta","link","input"
	}));
	
	/**
	 * Saves an element quickly (without using XSLT API).
	 * @param mode MODE_xx constant
	 * @param e Element to save
	 * @param w Writer to save it to
	 * @throws IOException Any save error
	 */
	public static void fastSave(int mode,Element e,Writer w) throws IOException
	{
		StringBuffer sb=new StringBuffer();
		String tag=e.getTagName();
		sb.append('<');
		sb.append(tag);
		NamedNodeMap nnm=e.getAttributes();
		for(int i=0;i<nnm.getLength();i++)
		{
			Attr a=(Attr)nnm.item(i);
			sb.append(' ');
			sb.append(a.getName());
			sb.append("=\"");
			sb.append(esc(a.getValue()));
			sb.append("\"");
		}
		if(mode==MODE_XHTML && tag.equals("html"))
		{
			sb.append(" xmlns=\"http://www.w3.org/1999/xhtml\"");
		}
		else if(e.getNamespaceURI() != null)
		{
			sb.append(" xmlns=\"" + e.getNamespaceURI() + "\"");
		}
		NodeList children=e.getChildNodes();
		if(children.getLength()==0 && 
			(mode==MODE_XML || XHTMLMINIMISETAGS.contains(tag)))
		{
			sb.append(" />");
			w.write(sb.toString());
			return;
		}
		sb.append(">");
		w.write(sb.toString());
		sb=null;
		fastSaveInner(mode,e,w);
		w.write("</"+tag+">");
	}
	
	/**
	 * Writes the contents of an element (not including the element tag itself)
	 * to the given writer.
	 * @param mode MODE_xx constant
	 * @param e Element to write contents of
	 * @param w Writer 
	 * @throws IOException Any error writing
	 */
	public static void fastSaveInner(int mode,Element e,Writer w) throws IOException
	{
		NodeList children=e.getChildNodes();
		for(int i=0;i<children.getLength();i++)
		{
			Node n=(Node)children.item(i);
			if(n instanceof Element)
			{
				fastSave(mode,(Element)n,w);
			}
			else if(n instanceof Text)
			{				
				String data=((Text)n).getData();
				if(data!=null) w.write(esc(data,false));
			}
		}
	}
	
	
	/**
	 * Saves an XML document to a file.
	 * @param f File target
	 * @param d Document to save
	 * @throws XMLException If there's any problem saving
	 */
	public synchronized static void save(File f, Document d) throws XMLException
	{
		try
		{			
			OutputStreamWriter w = new OutputStreamWriter(
				new FileOutputStream(f), "UTF-8");
			fastSave(MODE_XML, d, w);
			w.close();
		}
		catch(IOException e)
		{
			throw new XMLException(e);
		}
	}
	
	/**
	 * Saves an XML document into a string.
	 * @param d Document to save
	 * @return XML string
	 * @throws XMLException If there's any problem saving
	 */
	public synchronized static String saveString(Document d) throws XMLException
	{
		StringWriter w = new StringWriter();
		try
		{
			fastSave(MODE_XML, d, w);
			return w.toString();
		}
		catch (IOException te)
		{
			throw new XMLException(te);
		} 
	}

	/**
	 * Sets text contained within the given element. This clears all other
	 * contained children first.
	 * @param e Element to set
	 * @param text Text to place inside
	 */
	public static void setText(Element e, String text)
	{
		NodeList nl=e.getChildNodes();
		for(int i=nl.getLength()-1;i>=0;i--)
		{
			e.removeChild(nl.item(i));
		}		
		e.appendChild(e.getOwnerDocument().createTextNode(text));		
	}
	
	/**
	 * @param e Element for which attributes will be examined
	 * @return Names of all attributes set on the element
	 */
	public static String[] getAttributeNames(Element e)
	{
		List<String> l = new LinkedList<String>();
		NamedNodeMap nnm=e.getAttributes();
		for(int i=0;i<nnm.getLength();i++)
		{
			String sName=nnm.item(i).getNodeName();
			l.add(sName);
		}
		return l.toArray(new String[l.size()]);
	}
	
	/** 
	 * Trims text as per standard HTML processing, for example removes double-spaces.
	 * Leaves spaces at the beginning and end.
	 * @param s Text to trim
	 * @return Text without multiple spaces in a row, but with spaces at beginning
	 *   and end
	 */
	public static String normaliseText(String s)
	{
		StringBuffer sb=new StringBuffer();

		boolean bLastSpace=false; // Leave initial space
		for(int i=0;i<s.length();i++)
		{
			char c=s.charAt(i);
			switch(c)
			{
				case ' ' :
				case '\t':
				case '\n':
					if(!bLastSpace) sb.append(' ');
					bLastSpace=true;
					break;

				default :
					bLastSpace=false;
					if(c>32) sb.append(c);
					break;
			}
		}

		return sb.toString();
	}
	
	/**
	 * Escapes special characters in a string (angle brackets, ampersands, both
	 * types of quote) so that it can be included in the text of an XML element
	 * or in an attribute.
	 * <p>
	 * Note that any control characters in the string will turn into 
	 * &lt;controlchar num='3'&gt; as these cannot be represented in XML.
	 * @param s String to escape
	 * @return XML-escaped version of string
	 */
	public static String esc(String s)	
	{
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<s.length();i++)
		{
			char c=s.charAt(i);
			if(c<32 && c!=9 && c!=10 && c!=13)
			{
				sb.append("<controlchar num='" + (int)c + "'/>");
				continue;
			}
			switch(c)
			{
			case '<' : sb.append("&lt;"); break;
			case '&' : sb.append("&amp;"); break;
			case '\'': sb.append("&apos;"); break;
			case '"' : sb.append("&quot;"); break;
			default: sb.append(c); break;
			}
		}
		return sb.toString();
	}
	
	/**
	 * Replaces the four entities that 'esc' generates with the original
	 * characters. (Note: this is not precisely the reverse of the esc function,
	 * as control characters are not handled, and it also replaces the 'gt'
	 * entity.)
	 * @param s String to un-escape
	 * @return Resulting string 
	 */
	public static String unesc(String s)
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<s.length(); i++)
		{
			char c = s.charAt(i);
			if(c == '&')
			{
				int start = i + 1;
				do
				{
					i++;
				}
				while(i < s.length() && s.charAt(i) != ';');
				String entity = s.substring(start, i);
				if(entity.equals("lt"))
				{
					sb.append('<');
				}
				else if(entity.equals("gt"))
				{
					sb.append('>');
				}
				else if(entity.equals("amp"))
				{
					sb.append('&');
				}
				else if(entity.equals("apos"))
				{
					sb.append('\'');
				}
				else if(entity.equals("quot"))
				{
					sb.append('"');
				}
				else
				{
					// Output as-is
					sb.append('&');
					sb.append(entity);
					sb.append(';');
				}
			}
			else
			{
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Escapes special characters in a string (angle brackets, ampersands, both
	 * types of quote) so that it can be included in the text of an XML element
	 * or in an attribute.
	 * @param s String to escape
	 * @param quotes If true, escapes quotes, otherwise leaves them alone
	 * @return XML-escaped version of string
	 */
	public static String esc(String s,boolean quotes)	
	{
		if(quotes) return esc(s);
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<s.length();i++)
		{
			char c=s.charAt(i);
			if(c<32 && c!=9 && c!=10 && c!=13) continue;
			switch(c)
			{
			case '<' : sb.append("&lt;"); break;
			case '&' : sb.append("&amp;"); break;
			default: sb.append(c); break;
			}
		}
		return sb.toString();
	}
	
	/**
	 * Converts multiple spaces to non-breaking spaces so that they will be 
	 * retained in some forms of XML display.
	 * @param s String to convert
	 * @return Each space after first is converted to Unicode 160
	 */
	public static String convertMultipleSpaces(String s)
	{
		return s.replaceAll("((^)|(?<= )) ","\u00a0");
	}

	/**
	 * Create new element.
	 * @param parent Parent
	 * @param tagName Tagname for new element
	 * @return New element
	 */
	public static Element createChild(Node parent,String tagName)
	{
		Document d=parent instanceof Document ? (Document)parent : parent.getOwnerDocument();
		Element eNew=d.createElement(tagName);
		parent.appendChild(eNew);
		return eNew;		
	}
	
	/**
	 * Create new element with text inside.
	 * @param parent Parent
	 * @param tagName Tagname for new element
	 * @param text Text to add in child
	 * @return New element
	 */
	public static Element createChild(Node parent,String tagName,String text)
	{
		Document d=parent instanceof Document ? (Document)parent : parent.getOwnerDocument();
		Element eNew=d.createElement(tagName);
		eNew.appendChild(d.createTextNode(text));
		parent.appendChild(eNew);
		return eNew;		
	}
	
	/**
	 * Removes a node from the document.
	 * @param old Node to get rid of
	 */
	public static void remove(Node old)
	{
		old.getParentNode().removeChild(old);
	}
}
