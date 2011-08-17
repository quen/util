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

import java.util.*;
import java.util.Set;

/** Class providing utilities that make reflection tasks easier */
public abstract class ReflectionUtils
{
	/**
	 * Returns all interfaces implemented by the given class and its superclasses.
	 * @param c Class (not interface) 
	 * @return Set containing Class object for each interface
	 */
	public static Set<Class<?>> getAllInterfaces(Class<?> c)
	{
		Set<Class<?>> s=new HashSet<Class<?>>();
		addAllInterfaces(s,c);
		return s;		
	}
	
	private static void addAllInterfaces(Set<Class<?>> s,Class<?> c)
	{
		Class<?>[] ac=c.getInterfaces();
		for(int i=0;i<ac.length;i++)
		{
			if(s.add(ac[i])) addAllInterfaces(s,ac[i]);
		}
		
		Class<?> cParent = c.getSuperclass();
		if(cParent!=null)
		{
			addAllInterfaces(s,cParent);		
		}
	}
	
	private static void addSelfAndAllParents(Set<Class<?>> s, Class<?> c)
	{
		if(!s.add(c))
		{
			return;
		}
		
		Class<?>[] ac = c.getInterfaces();
		for(int i=0; i<ac.length; i++)
		{
			addSelfAndAllParents(s, ac[i]);
		}
		
		Class<?> cParent = c.getSuperclass();
		if(cParent!=null)
		{
			addSelfAndAllParents(s, cParent);		
		}
	}
	
	/**
	 * @param c Class 
	 * @return All interfaces, all superclasses, and the class itself.
	 */
	public static Set<Class<?>> getSelfAndAllParents(Class<?> c)
	{
		Set<Class<?>> s = new HashSet<Class<?>>();
		addSelfAndAllParents(s,c);
		return s;		
	}
	
	/**
	 * Class.getSuperclass() only works for classes, not interfaces; this 
	 * method works for interfaces too, defining the parent interface as the
	 * first one on the definition line. 
	 * @param c Class or interface
	 * @return Parent, or null if none
	 */
	public static Class<?> getSuperclassOrInterface(Class<?> c)
	{
		if(c.isInterface())
		{
			Class<?>[] ac = c.getInterfaces();
			if(ac.length==0)
			{
				return null;
			}
			else
			{
				return ac[0];
			}
		}
		else
		{
			return c.getSuperclass();
		}		
	}
}
