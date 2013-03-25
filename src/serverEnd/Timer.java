package serverEnd;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Timer extends Thread
{
	/** Rate at which timer is checked */
	protected int m_rate = 1000;
	
	/** Length of timeout */
	private int m_length;

	/** Time elapsed */
	private int m_elapsed;
	
	private DataOutputStream outStream;
	private Socket clientSock;

	/**
	  * Creates a timer of a specified length
	  * @param	length	Length of time before timeout occurs
	  */
	public Timer ( int length, DataOutputStream out, Socket sock )
	{
		// Assign to member variable
		m_length = length * 1000;

		// Set time elapsed
		m_elapsed = 0;
		
		outStream = out;
		clientSock = sock;
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
	@SuppressWarnings("deprecation")
	public void timeout()
	{
		try {
			byte[] fileData = Server.readFileAsString("./resource/400.html");
			String header = Server.ConstructHttpHeader(400, "html", fileData.length);
			outStream.writeBytes(header);
			outStream.write(fileData);	
			this.stop();
		} catch(IOException e) {
			System.out.println("Timeout Error");
		}
		try {
			outStream.flush();
			/* Interaction with this client complete, close() the socket */
			clientSock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}