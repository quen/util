/*
This file is part of leafdigital util.

util is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

util is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with util.  If not, see <http://www.gnu.org/licenses/>.

Copyright 2011 Samuel Marshall.
*/
package util;

import java.text.*;
import java.util.*;

/**
 * String-handling utility methods.
 */
public abstract class StringUtils
{
	/**
	 * Joins an array of strings using the specified separator.
	 * @param separator Separator added between strings
	 * @param strings Strings to join
	 * @return Combined string
	 */
	public static String join(String separator, String[] strings)
	{
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<strings.length;i++)
		{
			if(i>0) sb.append(separator);
			sb.append(strings[i]);
		}
		return sb.toString();
	}

	/**
	 * Basic non-regular-expression replace.
	 * @param original Original string
	 * @param search Search string
	 * @param replace Replace string
	 * @return String with all instances of 'search' replaced with 'replace'
	 */
	public static String replace(String original, String search, String replace)
	{
		String s="";
		while(true)
		{
			int found=original.indexOf(search);
			if(found==-1)
			{
				s+=original;
				return s;
			}
			s+=original.substring(0,found)+replace;
			original=original.substring(found+search.length());
		}
	}

	/**
	 * Removes the last entry from a string array.
	 * @param strings Original array (which won't be modified)
	 * @return New array without the last item
	 */
	public static String[] removeLast(String[] strings)
	{
		String[] shorter=new String[strings.length-1];
		System.arraycopy(strings,0,shorter,0,shorter.length);
		return shorter;
	}

	/**
	 * Adds last entry to a string array.
	 * @param strings Original array (which won't be modified)
	 * @param extra Extra string
	 * @return New array with extra string at end
	 */
	public static String[] addLast(String[] strings, String extra)
	{
		String[] newStrings=new String[strings.length+1];
		System.arraycopy(strings,0,newStrings,0,strings.length);
		newStrings[strings.length]=extra;
		return newStrings;
	}

	/**
	 * Capitalises the first letter of a string. Works (does nothing)
	 * with empty or null strings.
	 * @param s String to capitalise
	 * @return String with first letter capitalises.
	 */
	public static String capitalise(String s)
	{
		if(s==null || s.length()==0) return s;
		return Character.toUpperCase(s.charAt(0))+s.substring(1);
	}

	/**
	 * Matches simple wildcards with * only.
	 * @param pattern Pattern
	 * @param s String
	 * @return True if it matches, false otherwise
	 */
	public static boolean matchWildcard(String pattern, String s)
	{
		StringBuffer regex=new StringBuffer();
		for(int i=0;i<pattern.length();i++)
		{
			char c=pattern.charAt(i);
			if(c=='*')
				regex.append(".*");
			else
			{
				// A-Z, a-z, 0-9 and non-ASCII characters don't need escaping
				if(!((c>='a' && c<='z') || (c>='A' && c<='Z') || (c>='0' && c<='9') || c>127))
					regex.append('\\');
				regex.append(c);
			}
		}

		return s.matches(regex.toString());
	}

	/**
	 * Given an integer 10 times the number you want, displays that number with
	 * one decimal place.
	 * @param number10 E.g. 666
	 * @return E.g. "66.6"
	 */
	private static String display10(long number10)
	{
		String result=number10+"";
		if(number10<10) result="0"+result;
		return result.substring(0,result.length()-1)+"."+result.charAt(result.length()-1);
	}

	/**
	 * @param bytes Number of bytes
	 * @return String describing number e.g "4.2 MB"
	 */
	public static String displayBytes(long bytes)
	{
		long kb10=(bytes*5)/512;
		if(kb10==0) // Show bytes where less than 0.1 KB
			return bytes+" B";
		if(kb10<100) // Up to 10KB, display with one decimal place
			return display10(kb10)+" KB";
		long mb10=(kb10/1024);
		if(mb10==0) // Show KB where less than 0.1 MB
			return (kb10+5)/10 + " KB";
		if(mb10<100) // Up to 10MB, display with one decimal place
			return display10(mb10)+" MB";
		long gb10=(mb10/1024);
		if(gb10==0) // Show MB where less than 0.1 GB
			return (mb10+5)/10 + " MB";
		if(gb10<100) // Up to 10GB, display with one decimal place
			return display10(gb10)+" GB";
		return (gb10+5)/10 + " GB";
	}

	/**
	 * @param milliseconds Number of milliseconds
	 * @return String describing time e.g. "5 hours"
	 */
	public static String displayMilliseconds(long milliseconds)
	{
		if(milliseconds==0)
			return "0 seconds";
		if(milliseconds<100)
			return "0.1 seconds";
		if(milliseconds<950) // Up to 1 second
			return display10((milliseconds+50)/100)+" seconds";
		int seconds=(int)((milliseconds+500)/1000);
		if(seconds==1)
			return "1 second";
		if(seconds<120) // Up to 120 seconds - in seconds
			return seconds+" seconds";
		int minutes=(seconds+30)/60;
		if(minutes<90) // Up to 90 minutes - in minutes
			return minutes+" minutes";
		int hours=(minutes+30)/60;
		if(hours<36) // Up to 36 hours - in hours
			return hours+" hours";
		int days=(hours+12)/24;
			return days+" days";
	}

	/**
	 * Displays a date and time in friendly format that's reliably short. Formats are:
	 * "Today, 11:14" "Yesterday, 16:15" "Aug 10, 11:15" and finally "March 8 2006"
	 * @param d Date
	 * @return Date in friendly format
	 */
	public static String displayShortDate(Date d)
	{
		Calendar c=Calendar.getInstance();
		c.setTime(d);

		SimpleDateFormat timeFormat=new SimpleDateFormat("HH:mm");

		Calendar ref=Calendar.getInstance();
		ref.set(Calendar.HOUR_OF_DAY,0);
		ref.set(Calendar.MINUTE,0);
		ref.set(Calendar.SECOND,0);
		ref.set(Calendar.MILLISECOND,0);
		if(c.getTimeInMillis() >= ref.getTimeInMillis())
		{
			return "Today, "+timeFormat.format(d);
		}
		ref.add(Calendar.DATE,-1);
		if(c.getTimeInMillis() >= ref.getTimeInMillis())
		{
			return "Yesterday, "+timeFormat.format(d);
		}
		ref.set(Calendar.DAY_OF_YEAR,0);
		if(c.getTimeInMillis() >= ref.getTimeInMillis())
		{
			return new SimpleDateFormat("MMM dd, HH:mm").format(d);
		}
		return new SimpleDateFormat("MMM dd yyyy").format(d);
	}

	/**
	 * Formats a list of strings to become "x, y, and z"
	 * @param items List of one or more items
	 * @return Combined string
	 */
	public static String formatList(String[] items)
	{
		if(items.length==0) return "";
		if(items.length==1) return items[0];
		if(items.length==2) return items[0]+" and "+items[1];
		StringBuffer result=new StringBuffer(items[0]);
		for(int i=1;i<items.length-1;i++)
		{
			result.append(", "+items[i]);
		}
		result.append(" and "+items[items.length-1]);
		return result.toString();
	}

	/**
	 * Formats a list of strings to become "x, y, and z"
	 * @param items List of one or more items
	 * @param max Maximum number of items to display before adding ...
	 * @return Combined string
	 */
	public static String formatList(String[] items, int max)
	{
		if(items.length <= max)
		{
			return formatList(items);
		}
		StringBuffer result=new StringBuffer(items[0]);
		for(int i=1; i<max; i++)
		{
			result.append(", "+items[i]);
		}
		result.append(", ...");
		return result.toString();
	}

	/**
	 * Pads a nunber with spaces so that it matches the required number of
	 * characters.
	 * @param l Number
	 * @param characters Required characters
	 * @return New string
	 */
	public static String padNumber(long l,int characters)
	{
		StringBuffer sb=new StringBuffer();
		String number=l+"";
		for(int start=characters-number.length();start>0;start--)
		{
			sb.append(' ');
		}
		sb.append(number);
		return sb.toString();
	}

	/**
	 * Pads a nunber with spaces so that it matches the required number of
	 * characters.
	 * @param i Number
	 * @param characters Required characters
	 * @return New string
	 */
	public static String padNumber(int i,int characters)
	{
		StringBuffer sb=new StringBuffer();
		String number=i+"";
		for(int start=characters-number.length();start>0;start--)
		{
			sb.append(' ');
		}
		sb.append(number);
		return sb.toString();
	}

	/**
	 * @param data Data bytes
	 * @return Hex string version of data
	 */
	public static String getHexString(byte[] data)
	{
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<data.length;i++)
		{
			int value=data[i];
			for(int nibble=0;nibble<2;nibble++)
			{
				int part=(nibble==0 ? (value>>>4 & 0xf) : (value & 0xf));
				if(part<10)
					sb.append((char)('0'+part));
				else
					sb.append((char)('A'+(part-10)));
			}
		}
		return sb.toString();
	}

	/**
	 * Replaces 'placeholders' in a string. For example, if border is "%%" then
	 * a placeholder might be %%SOMETHING%%; this would be replaced by the
	 * value for "SOMETHING" in the map.
	 * @param border Border either side of placeholders e.g. "%%"
	 * @param values Values to replace
	 * @param source Input string
	 * @return Resulting string
	 */
	public static String replacePlaceholders(String border,
		Map<String, String> values, String source)
	{
		StringBuffer sb=new StringBuffer();
		int pos=0;
		while(true)
		{
			int found=source.indexOf(border,pos);
			int nextFound=found==-1 ? -1 : source.indexOf(border,found+border.length());
			if(found==-1 || nextFound==-1)
			{
				sb.append(source.substring(pos));
				return sb.toString();
			}
			sb.append(source.substring(pos,found));
			String keyword = source.substring(found+border.length(),nextFound);
			String replace = values.get(keyword);
			if(replace==null)
			{
				replace = border + keyword + border;
			}
			sb.append(replace);
			pos = nextFound + border.length();
		}
	}

	/**
	 * Escapes a string so that it can be used as a literal in regular
	 * expressions.
	 * @param input Input string
	 * @return Escaped string (pattern characters are escaped)
	 */
	public static String regexpEscape(String input)
	{
		StringBuffer output = new StringBuffer();
		for(int i=0; i<input.length(); i++)
		{
			char c = input.charAt(i);
			if("\\[]{}.^$?*+|():!<=>".indexOf(c)!=-1)
			{
				output.append("\\"+c);
			}
			else
			{
				output.append(c);
			}
		}
		return output.toString();
	}
}
