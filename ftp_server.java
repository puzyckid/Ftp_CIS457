/*
 * ftp_server.java
 * Data Comm Project
 */
import java.net.*;
import java.io.*;
import java.util.concurrent.Executors;

public class ftp_server {

    /***
     * Main method that starts the server
     * @param args If the user so chooses, they can enter a port number to further
     *             add functionality and mobility to the server
     */
    public static void main(String[] args) {
        if (args.length > 0){
            try{
                int port = Integer.parseInt(args[0]);
                try (ServerSocket listener = new ServerSocket(port)) {
                    System.out.println("Server running on port: " + port);
                    var threadPool = Executors.newFixedThreadPool(15);
                    while (true) {
                        threadPool.execute(new ClientHandler(listener.accept()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.print("Unexpected error, closing server...\n");
                    System.exit(0);
                }
            }
            catch (Exception e){
                System.out.print("Could not parse port.\n" +
                        "Closing...\n");
                System.exit(-1);
            }
        }
        else {
            try (ServerSocket listener = new ServerSocket(10500)) {
                System.out.println("Server running on default port: " + 10500);
                var threadPool = Executors.newFixedThreadPool(15);
                while (true) {
                    threadPool.execute(new ClientHandler(listener.accept()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.print("Unexpected error, closing server...\n");
                System.exit(0);
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private InputStream input;            
        private OutputStream output;
		private PrintStream printStream;
        final int maxFileSize = 1000000;
        ClientHandler(Socket socket) throws Exception {
            
            this.socket = socket;
            this.input = socket.getInputStream();
            this.output = socket.getOutputStream();
			this.printStream = new PrintStream(this.output);
        }
        @Override
        public void run() {
            
            try { 
                System.out.println("Client connected: " + socket);
                //read command into byte array
                byte[] cmd = new byte[1024];
                input.read(cmd);
                String command = new String(cmd);
                command = command.trim();
                String[] components = command.split(" ", 0);
                    
                switch(components[0].toUpperCase()) {
                    case "LIST":
						System.out.println(socket + ": List");
                        //gram an array of files from directory
                        File dir  = new File(System.getProperty("user.dir"));
                        File[] files = dir.listFiles();

                        //for each file, write it's name to a byte array and write it
                        //to OutputStream (writing only as many bytes as required)
                        for(File f : files) {
                            byte[] tmp = (f.getName() + '\n').getBytes();
                            output.write(tmp, 0, tmp.length);
                        }
                        input.close();
                        output.close();
                        socket.close();
                        break;

                    case "RETRIEVE":
						System.out.println(socket + ": Retrieve");
                        File send = new File(components[1]);
						if(!send.exists())
						{
							printStream.println(send + " does not exist in the server's current directory."); 		
							printStream.close();
							break;
						}
						InputStream in = new FileInputStream(send);
						byte[] bArr = new byte[(int)send.length()];
						//read from FileInputStream into bArr, only as many bytes
						//as the file contains
						in.read(bArr, 0, (int)send.length());
						//write the byte array to OutputStream
						output.write(bArr, 0, (int)send.length());
						input.close();
						output.close();
						socket.close();					
						break;
						
                    case "STORE":
						System.out.println(socket + ": Store");
                        FileOutputStream fileOut = new FileOutputStream(components[1]);

                        byte[] file = new byte[this.maxFileSize];

                        int numR;
                        while((numR = input.read(file)) > 0) {
                            fileOut.write(file, 0, numR);
                        }
                        fileOut.close();

                        break;
                    case "QUIT":
						System.out.println(socket + ": Quit");
                        input.close();
                        output.close();
                        socket.close();
                        System.out.println("Connection closed.");
                        break;
                }
            }
            catch (SocketException e){
                System.out.println("Unexpected disconnect: " + socket);
            }
            catch(Throwable e){
                e.printStackTrace();
            }
        }
    }
}