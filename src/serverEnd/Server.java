package serverEnd;

/**
 * @file: Server.java
 * 
 * @author: Chinmay Kamat <chinmaykamat@cmu.edu>
 * 
 * @date: Feb 15, 2013 1:13:37 AM EST
 * 
 */
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.PatternSyntaxException;

public class Server {
	private static ServerSocket srvSock;
	private static byte[] fileData;
	private static String header;
	private static String targetFile;
	private static String method;
	private static String format;
	private static Timer timer;
	
	public static byte[] readFileAsString(String filePath)
				   throws IOException {
		File file = new File(filePath);
		int length = (int)file.length();
		FileInputStream inStream = null;
		byte[] data = new byte[length];
		inStream = new FileInputStream(file);
		inStream.read(data);
		inStream.close();
		return data;
	}
	
	public static String ConstructHttpHeader(int return_code, String file_type, int dataLen) {
		String s = "HTTP/1.0 ";
		//you probably have seen these if you have been surfing the web a while
		switch (return_code) {
		case 200:
			s = s + "200 OK";
		    break;
		case 400:
			s = s + "400 Bad Request";
			break;
		case 403:
			s = s + "403 Forbidden";
			break;
		case 404:
			s = s + "404 Not Found";
			break;
		case 500:
			s = s + "500 Internal Server Error";
			break;
		case 501:
			s = s + "501 Not Implemented";
			break;
		}

		s = s + "\r\n"; //other header fields,
		s = s + "Connection: Keep-Alive\r\n"; //we can't handle persistent connections
		s = s + "Server: SimpleHTTP\r\n"; //server name
		s = s + "Content-Length: " + dataLen + "\r\n";
		
		if (file_type.equals("html")) {
			s = s + "Content-Type: text/html\r\n";
		} else if (file_type.equals("css")) {
			s = s + "Content-Type: text/css\r\n";
		} else if (file_type.equals("jpeg")){
			s = s + "Content-Type: image/jpeg\r\n";
		} else if (file_type.equals("png")) {
			s = s + "Content-Type: image/png\r\n";
		} else if (file_type.equals("jpg")) {
			s = s + "Content-Type: image/jpg\r\n";
		} else if (file_type.equals("gif")) {
			s = s + "Content-Type: image/gif\r\n";
		} else {
			s = s + "Content-Type: text/html\r\n";
		}
		    ////so on and so on......
		s = s + "\r\n"; //this marks the end of the httpheader
		    //and the start of the body
		    //ok return our newly created header!
		return s;
	}
	

	@SuppressWarnings("deprecation")
	public static void main(String args[]) {
		String buffer = null;
		int port = 8080;
		BufferedReader inStream = null;
		DataOutputStream outStream = null;

		/* Parse parameter and do args checking */
		if (args.length < 1) {
			System.err.println("Usage: java Server <port_number>");
			System.exit(1);
		}

		try {
			port = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.err.println("Usage: java Server <port_number>");
			System.exit(1);
		}

		if (port > 65535 || port < 1024) {
			System.err.println("Port number must be in between 1024 and 65535");
			System.exit(1);
		}

		try {
			/*
			 * Create a socket to accept() client connections. This combines
			 * socket(), bind() and listen() into one call. Any connection
			 * attempts before this are terminated with RST.
			 */
			srvSock = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Unable to listen on port " + port);
			System.exit(1);
		}

		while (true) {
			Socket clientSock;
			try {
				/*
				 * Get a sock for further communication with the client. This
				 * socket is sure for this client. Further connections are still
				 * accepted on srvSock
				 */
				clientSock = srvSock.accept();
				
				System.out.println("Accpeted new connection from "
						+ clientSock.getInetAddress() + ":"
						+ clientSock.getPort());
			} catch (IOException e) {
				continue;
			}
			try {
				inStream = new BufferedReader(new InputStreamReader(
						clientSock.getInputStream()));
				outStream = new DataOutputStream(clientSock.getOutputStream());
				/* Read the data send by the client */
				buffer = inStream.readLine();
				
				timer = new Timer(20, outStream);
				timer.start();
				
				try {
					method = buffer.split("\\ ")[0];
					targetFile = buffer.split(method + "\\ ")[1].split(" HTTP")[0];
					if (targetFile.equals("/")) {
						targetFile = "/index.html";
					}
					format = targetFile.split("\\.")[1];
				
					if (method.equals("Head")) {
						header = ConstructHttpHeader(200, "html", 0);
						outStream.writeBytes(header);
					} else {
						try {
							fileData = readFileAsString("./resource" + targetFile);
							header = ConstructHttpHeader(200, format, fileData.length);
							outStream.writeBytes(header);
							outStream.write(fileData);					
						} catch(IOException e) {
							fileData = readFileAsString("./resource/404.html");
							header = ConstructHttpHeader(404, "html", fileData.length);
							outStream.writeBytes(header);
							outStream.write(fileData);	
						}
						
						System.out.println("Read from client "
								+ clientSock.getInetAddress() + ":"
								+ clientSock.getPort() + " " + header);
					}
				} catch (PatternSyntaxException e){
					fileData = readFileAsString("./resource/400.html");
					header = ConstructHttpHeader(400, "html", fileData.length);
					outStream.writeBytes(header);
					outStream.write(fileData);	
				} catch (ArrayIndexOutOfBoundsException e) {
					fileData = readFileAsString("./resource/400.html");
					header = ConstructHttpHeader(400, "html", fileData.length);
					outStream.writeBytes(header);
					outStream.write(fileData);	
				} finally {
					timer.stop();
				}
				/*
				 * Echo the data back and flush the stream to make sure that the
				 * data is sent immediately
				 */
				
				outStream.flush();
				/* Interaction with this client complete, close() the socket */
				clientSock.close();		
			} catch (IOException e) {
				clientSock = null;
				continue;
			}
			
		}
	}
}

