/*
 * ftp_client.java
 * Data Comm Project
 */

import java.net.Socket;
import java.util.Scanner;
import java.io.*;

public class ftp_client { 
    public static void main(String[] args) throws Exception {
        boolean connected = false;
        Socket socket = null;
        OutputStream clientOut = null;
        InputStream clientIn = null;
        String ip = "";
        int port = 0;
        final int maxFileSize = 1000000;
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter connect command to begin.\n" +
                "CONNECT <address> <port>\n");


        while(scanner.hasNextLine()) {
            String command = scanner.nextLine();
            String[] components = command.split(" ", 0);
            if(connected) {
                //if connected, open new socket
                socket = new Socket(ip, port); 
                clientOut = socket.getOutputStream();
                clientIn = socket.getInputStream();
            }
            components[0] = components[0].toUpperCase();
            switch(components[0]) {
                case "CONNECT":
                    if(connected){
                        System.out.println("We are already connected.");
                    } else {
                        try {
                            ip = components[1];
                            port = Integer.parseInt(components[2]);
                            socket = new Socket(ip, port); 
                            clientOut = socket.getOutputStream();
                            clientIn = socket.getInputStream();
                            connected = true;
                            System.out.println("Connected to server at " + components[1] + " on port " + components[2]);
                      } catch(Throwable e) {
                         System.out.println("Connection failed, check IP and port number.");
                      }
                    }
                    break;
                case "QUIT":
                    try {
                        clientOut.write(components[0].getBytes());
                        socket.close();
                    } catch(Exception e){e.printStackTrace();}
                    clientOut = null;
                    clientIn = null;
                    connected = false;
                    System.out.println("Connection closed.");
                    System.exit(0);
                    break;
                case "LIST": 
                    //write list command to OutputStream
                    clientOut.write(components[0].getBytes(), 0, components[0].getBytes().length);
                    byte[] fname = new byte[4096];
                    //read from InputStream to fname, and then print out contents
                    int numB;
                    while((numB = clientIn.read(fname)) > 0) {
                        System.out.write(fname, 0, numB);
                    }

                    System.out.println(""); 
                 
                    break;

                case "RETRIEVE":
                    if(components.length > 1) {
                        //send command to server
                        clientOut.write(command.getBytes());
                        FileOutputStream fileOut = new FileOutputStream(components[1]);
                        byte[] file = new byte[maxFileSize];

                        //read from InputStream to the FileOutputStream
                        //write only as many bits as we recieve, so
                        //the file does not take up more space than necessary
                        int numR;
                        while ((numR = clientIn.read(file)) > 0) {
                            fileOut.write(file, 0, numR);
                        }
                        fileOut.close();
                    }
                    else{
                        System.out.print("No file given.\n" +
                                "Enter 'help' to see commands\n");
                    }
                    break;
                case "STORE":
                    if(components.length > 1) {
                        try {
                            //send command
                            clientOut.write(command.getBytes());
                            File send = new File(components[1]);
                            InputStream in = new FileInputStream(send);
                            byte[] bArr = new byte[(int) send.length()];
                            //read from FileInputStream into bArr as many
                            //bytes as the file contains.
                            in.read(bArr, 0, (int) send.length());
                            clientOut.write(bArr, 0, (int) send.length());
                            clientIn.close();
                            clientOut.close();
                            socket.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        System.out.print("No file given.\n" +
                                "Enter 'help' to see commands\n");
                    }
                    break;
                case "HELP":
                case "?":
                case "H":
                    System.out.print("---------HELP---------\n" +
                            "LIST - list server files\n" +
                            "STORE <filename> - send a file\n" +
                            "RETRIEVE <filename> - retrieve a file\n" +
                            "QUIT - end program\n" +
                            "----------------------\n");
                    break;
                default:
                    System.out.printf("%s - not a valid input.\n" +
                            "Enter 'help' to see list of valid input\n",
                            components[0]);
            }
            if(!components[0].equalsIgnoreCase("QUIT")){
                System.out.print("Enter command to continue or 'help' to get started\n");
            }
        }
    }
}
