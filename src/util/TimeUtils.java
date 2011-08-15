package util;

import java.util.*;

import javax.swing.SwingUtilities;

/**
 * Time-related utilities.
 */
public abstract class TimeUtils
{
	/** Synchronization */
	private static Object synch=new Object();
	
	/** Events in queue */
	private static SortedSet<TimedEvent> eventQueue=new TreeSet<TimedEvent>();
	
	/** Error handler */
	private static ErrorHandler errorHandler;
	
	// A static thread handles all timing
	static
	{
		(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				thread();
			}
		},"TimeUtils thread")).start();
	}
	
	/**
	 * Sets the error handler used if an error occurs during a timed event.
	 * @param eh Handler
	 */
	public static void setErrorHandler(ErrorHandler eh)
	{
		TimeUtils.errorHandler=eh;
	}
	
	/**
	 * Add a new timed event, which occurs after a certain delay.
	 * @param r Event code
	 * @param lDelay Delay
	 * @param inEventThread If true, runs event in the user-interface event thread
	 * @return Unique ID for the event
	 */	 
	public static int addTimedEvent(Runnable r, long lDelay, boolean inEventThread)
	{
		synchronized(synch)
		{
			TimedEvent te = new TimedEvent();
			te.r = r;
			te.due = System.currentTimeMillis() + lDelay;
			te.inEventThread = inEventThread;
			
			eventQueue.add(te);
			
			synch.notify();
			
			return te.id;
		}		 
	}
	
	/**
	 * Cancel an existing timed event.
	 * @param id ID of event
	 */
	public static void cancelTimedEvent(int id)
	{
		synchronized(synch)
		{
			for(Iterator<TimedEvent> i=eventQueue.iterator(); i.hasNext();)
			{
				TimedEvent te = i.next();
				if(te.id == id) 
				{
					i.remove();
					synch.notify();
					break;
				}					 
			}
		}
	}
	
	/** Timer thread handles timed events */
	private static void thread()
	{
		List<Runnable> runList = new LinkedList<Runnable>(),
			swingRunList = new LinkedList<Runnable>();
		while(true)
		{
			runList.clear();
			swingRunList.clear();
			
			// Wait for next event timeout
			synchronized(synch)
			{
				// Wait for next event, or notify
				try
				{
					if(eventQueue.isEmpty()) 
					{
						synch.wait();
					}
					else
					{
						TimedEvent first = (TimedEvent)eventQueue.first();
						long wait = first.due-System.currentTimeMillis();
						if(wait>0)
						{
							synch.wait(wait);
						}
					}
				}
				catch(InterruptedException ie)
				{
				}
				
				// See if it's time to run events
				long now=System.currentTimeMillis();
				while(!eventQueue.isEmpty())
				{
					TimedEvent first = eventQueue.first();
					if(first.due <= now)
					{						
						(first.inEventThread ? swingRunList : runList).add(first.r);
						eventQueue.remove(first);
					}
					else
					{
						// The list is sorted, so no need to check others
						break;
					}
				}
			}
			
			// Move outside synchronization in order to run events safely
			for(Runnable r : runList)
			{
				try
				{
					r.run();	
				}
				catch(Throwable t)
				{
					if(errorHandler!=null)
					{
						errorHandler.reportError(t);
					}
					else
					{
						t.printStackTrace();
					}
				}				
			}
			for(final Runnable r : swingRunList)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							r.run();	
						}
						catch(Throwable t)
						{
							if(errorHandler!=null)
							{
								errorHandler.reportError(t);
							}
							else
							{
								t.printStackTrace();
							}
						}
					}
				});
			}
		}	
	}
	
	/** 
	 * Represents an event that's supposed to occur at a particular time.
	 * Must be created within oSynch synchronization
	 */ 
	private static class TimedEvent implements Comparable<TimedEvent>
	{
		/** Runnable that actually runs at the given time */
		Runnable r;
		
		/** Time it's due */
		long due;
		
		/** True if it should run in the (UI) event thread */
		boolean inEventThread;
		
		/** Unique event ID */
		int id = (staticID++);
				
		/** Static ID assignment */
		private static int staticID = 0;

		@Override
		public int compareTo(TimedEvent te)
		{
			if(te==this) return 0;
			
			if(due < te.due) 
				return -1;
			else if(due > te.due) 
				return 1;
			else // Same, sort by ID
			  return id - te.id;			  
		}
	}
}
