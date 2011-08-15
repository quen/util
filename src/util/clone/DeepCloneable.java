package util.clone;

/** 
 * This interface basically does the same as Cloneable. I can't remember why
 * I did it, but I think there was a good reason...
 */
public interface DeepCloneable
{
  /**
   * @return Cloned object
   */
  public Object cloneObject();
}