package util.clone;

import java.lang.reflect.*;
import java.util.*;

/** Random things that are useful with collections */
public abstract class CollectionUtils
{
	/**
	 * Clones a map including all items in it.
	 * @param source Map
	 * @return Cloned map
	 */
  public static <A, B> Map<A, B> deepClone(Map<A, B> source)
  {
    try
    {
    	@SuppressWarnings("unchecked")
      Map<A, B>  target = (Map<A, B>)source.getClass().newInstance();
      for(Map.Entry<A, B> me : source.entrySet())
      {
        target.put(me.getKey(),cloneItem(me.getValue()));
      }
      return target;
    }
    catch(Exception e)
    {
      throw new Error(e.toString());
    }
  }

  /**
   * Clones a collection including all items in it.
   * @param source Collection
   * @return Cloned collection
   */
  public static <A> Collection<A> deepClone(Collection<A> source)
  {
    try
    {
    	@SuppressWarnings("unchecked")
      Collection<A> cTarget = (Collection<A>)source.getClass().newInstance();
      for(A item : source)
      {
        cTarget.add(cloneItem(item));
      }
      return cTarget;
    }
    catch(Exception e)
    {
      throw new Error(e.toString());
    }
  }

  @SuppressWarnings("unchecked")
  private static <C> C cloneItem(C o)
  {
    if(o instanceof String || o instanceof Integer)
      return o;
    else if(o instanceof Map)
      return (C)deepClone((Map<?,?>)o);
    else if(o instanceof Collection)
      return (C)deepClone((Collection<?>)o);
    else if(o instanceof DeepCloneable)
      return (C)((DeepCloneable)o).cloneObject();
    else if(o.getClass().isArray())
    {
      Object[] ao=(Object[])
        Array.newInstance(o.getClass().getComponentType(),Array.getLength(o));
      for(int i=0;i<ao.length;i++)
      {
        ao[i]=cloneItem(Array.get(o,i));
      }
      return (C)ao;
    }
    else
    {
      throw new Error("Item does not support cloning: "+
        o.getClass().toString());
    }
  }
	  
  /**
   * Convert the given map to a string.
   * @param m Any map to display
   * @return A string (multi-line) containing a view of the desired map
   */
  public static <A, B> String toString(Map<A, B> m)
  {
  	StringBuffer sb=new StringBuffer();
  	sb.append("[\n");
  	for(A key : m.keySet())
  	{
  		sb.append("  "+key+" => "+m.get(key)+"\n");
  	}
  	sb.append("]");
  	return sb.toString();  	
  }
}
