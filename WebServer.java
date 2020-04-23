import java.io.* ;
import java.net.* ;
import java.util.* ;

public final class WebServer
{
	public static void main(String argv[]) throws Exception
	{
	     // This is the port number the ServerSocket will listen for incoming requests on
         	int port = 6789;

            // This creates the actual listening socket. There is a warning because the socket
         	// is never closed, but it can't be closed or else it will only be able to accept
         	// one client once
	       final ServerSocket server = new ServerSocket(port);

	   // Process HTTP service requests in an infinite loop. The loop needs to be infinite 
	       // in order to listen for requests indefinitely 
		while (true) {
   	  // Listen for a TCP connection request.
			 final Socket client = server.accept();
            // Construct an object to process the HTTP request message from created class.
	       HttpRequest request = new HttpRequest( client );

	   // Create a new thread to process the request, we can have as many threads as is possible, if
	       // we didnt have this part, the server can only have one client.
	       Thread thread = new Thread(request);

	  // Start the thread.
	       thread.start();
           }
	}
}

final class HttpRequest implements Runnable
{
	final static String CRLF = "\r\n";
	Socket socket;

	// Constructor
	public HttpRequest(Socket socket) throws Exception 
	{
		this.socket = socket;
	}

	// Implement the run() method of the Runnable interface.
	public void run()
	{
		// This will attempt to do what our server intends to do through the process request method
		// within the HttpRequest class, throwing an exception when something goes wrong
	    try {
		processRequest();
	    } catch (Exception e) {
		System.out.println(e);
	    }
	}

	
	private void processRequest() throws Exception
	{
		// Get a reference to the socket's input and output streams.
		   InputStream is = socket.getInputStream();
		   DataOutputStream os = new DataOutputStream(socket.getOutputStream());
	
		// Set up input stream filters. isr will be the parameter of br because we need 
		   // a BufferedReader to actually read the data incoming from the stream
		   InputStreamReader isr =  new InputStreamReader(socket.getInputStream()); 
	
		   
  	           BufferedReader br = new BufferedReader(isr);
		
		 // Get the request line of the HTTP request message.
		   String requestLine = br.readLine();

               // Display the request line.
		   System.out.println();
                   System.out.println(requestLine);
               // Get and display the header lines.
		   String headerLine = null;
		   while ((headerLine = br.readLine()).length() != 0) {
   			System.out.println(headerLine);
                   }
		   
               // Extract the filename from the request line.
		   // A stringTokenizer breaks a string into smaller pieces
                   StringTokenizer tokens = new StringTokenizer(requestLine);
                   tokens.nextToken();  // skip over the method, which should be "GET"
                   String fileName = tokens.nextToken();

              // Prepend a "." so that file request is within the current directory.
                   fileName = "." + fileName;
                   
              // Open the requested file.
                   FileInputStream fis = null;
                   boolean fileExists = true;
                   try {
                  	fis = new FileInputStream(fileName);
                   } catch (FileNotFoundException e) {
                	fileExists = false;
                   }
                   
             // Construct the response message.
                   // Simply concatenate the message from the tokens
		   String statusLine = null;
		   String contentTypeLine = null;
		   String entityBody = null;
		   if (fileExists) {
 			statusLine = tokens.nextToken();
			contentTypeLine = "Content-type: " + 
			contentType( fileName ) + CRLF;
		   } else {
			statusLine = tokens.nextToken();
			contentTypeLine = tokens.nextToken();
	 		entityBody = "<HTML>" + 
			"<HEAD><TITLE>Not Found</TITLE></HEAD>" +
			"<BODY>Not Found</BODY></HTML>";
		   }
		   
             // Send the status line.
		  os.writeBytes(statusLine);

            // Send the conten  t type line.
		  os.writeBytes(contentTypeLine);

	   // Send a blank line to indicate the end of the header lines.
		  os.writeBytes(CRLF);
          // Send the entity body.
		  if (fileExists)	{
			sendBytes(fis, os);
			fis.close();
		  } else {
			os.writeBytes(entityBody);
		  }

	     // Close streams and socket.
                   os.close();
                   br.close();
                   socket.close();

	}
	
        private static void sendBytes(FileInputStream fis, OutputStream os) 
	throws Exception
	{
        // Construct a 1K buffer to hold bytes on their way to the socket.
   		byte[] buffer = new byte[1024];
		int bytes = 0;

       // Copy requsted file into the socket's output stream.
                while((bytes = fis.read(buffer)) != -1 ) {
                   os.write(buffer, 0, bytes);
                 }
         }
         
        private static String contentType(String fileName)
        {
	      if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
		     return "text/html";
    	      }
              if(fileName.endsWith(".gif")) {
		    return "image/gif";
	      }
	      if(fileName.endsWith(".jpeg")) {
		return "image/jpeg";
	      }
              return "application/octet-stream";
         }

}
