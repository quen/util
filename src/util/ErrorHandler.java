package util;

/** 
 * Interface that must be implemented by some other part of system to handle
 * errors caused within util.
 */
public interface ErrorHandler
{
	/**
	 * Called when an error occurs.
	 * @param t Throwable
	 */
	public void reportError(Throwable t);
}