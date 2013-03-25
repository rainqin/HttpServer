package serverEnd;

import java.io.DataOutputStream;
import java.io.IOException;

public class Timer extends Thread
{
	/** Rate at which timer is checked */
	protected int m_rate = 1000;
	
	/** Length of timeout */
	private int m_length;

	/** Time elapsed */
	private int m_elapsed;
	
	private DataOutputStream outStream;

	/**
	  * Creates a timer of a specified length
	  * @param	length	Length of time before timeout occurs
	  */
	public Timer ( int length, DataOutputStream out )
	{
		// Assign to member variable
		m_length = length;

		// Set time elapsed
		m_elapsed = 0;
		
		outStream = out;
	}

	
	/** Resets the timer back to zero */
	public synchronized void reset()
	{
		m_elapsed = 0;
	}

	/** Performs timer specific code */
	public void run()
	{
		// Keep looping
		for (;;)
		{
			// Put the timer to sleep
			try
			{ 
				Thread.sleep(m_rate);
			}
			catch (InterruptedException ioe) 
			{
				continue;
			}

			// Use 'synchronized' to prevent conflicts
			synchronized ( this )
			{
				// Increment time remaining
				m_elapsed += m_rate;

				// Check to see if the time has been exceeded
				if (m_elapsed > m_length)
				{
					// Trigger a timeout
					timeout();
				}
			}

		}
	}

	// Override this to provide custom functionality
	public void timeout()
	{
		try {
			byte[] fileData = Server.readFileAsString("./resource/400.html");
			String header = Server.ConstructHttpHeader(400, "html", fileData.length);
			outStream.writeBytes(header);
			outStream.write(fileData);	
		} catch(IOException e) {
			System.out.println("Timeout Error");
		}
	}
}