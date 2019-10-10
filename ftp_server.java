/*
 * ftp_server.java
 * Data Comm Project
 */
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class ftp_server {

    public static void main(String[] args) {
        try(ServerSocket listener = new ServerSocket(10500)) {
            System.out.println("Server running");
            var threadPool = Executors.newFixedThreadPool(15);
            while(true) {
                threadPool.execute(new ClientHandler(listener.accept())); 
            } 
        } catch(Exception e){e.printStackTrace();}
    }


    private static class ClientHandler implements Runnable {
        private Socket socket;
        private InputStream input;            
        private OutputStream output;
        final int maxFileSize = 1000000;
        ClientHandler(Socket socket) throws Exception {
            
            this.socket = socket;
            this.input = socket.getInputStream();
            this.output = socket.getOutputStream();
        }
        @Override
        public void run() {
            
            try { 
                
                
                //read command into byte array
                byte[] cmd = new byte[1024];
                input.read(cmd);
                String command = new String(cmd);
                command = command.trim();
                String[] components = command.split(" ", 0);
                    
                switch(components[0]) {
                    case "LIST":
                        
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
                        File send = new File(components[1]);
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
                        FileOutputStream fileOut = new FileOutputStream(components[1]);
                            
                        byte[] file = new byte[this.maxFileSize];
                            
                        int numR;
                        while((numR = input.read(file)) > 0) { 
                            fileOut.write(file, 0, numR);
                        }
                        fileOut.close();
                            
                        break;
                    case "QUIT":
                        input.close();
                        output.close();
                        socket.close();
                        System.out.println("Connection closed.");
                        break; 
            
                }
               

            } catch(Throwable e){e.printStackTrace();}
           
        
        }
    
    }
}
