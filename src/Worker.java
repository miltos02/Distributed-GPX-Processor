import java.io.*;
import java.net.*;

public class Worker extends Thread
{
    private static ObjectInputStream in; // input stream to receive objects from server
    private ObjectOutputStream out; // output stream to send objects to server

    // Port for receiving chunks
    private static int receivingPort = 3900;
    // Port for sending chunks with intermediate results:
    private static int sendingPort = 8080;
    private Chunk chunk; // chunk object to process
    Socket s; // connection socket to server

    public Worker(Socket connectionSocket, Chunk c)
    {
        this.s = connectionSocket; // initialize connection socket
        this.chunk =  c; // initialize chunk object
        try {
            this.out = new ObjectOutputStream(connectionSocket.getOutputStream()); // initialize output stream
        } catch (IOException e) 
        {
            e.printStackTrace(); // handle exceptions
        }
    }
    
    @Override
    public void run() {
        System.out.println("got chunk");
        System.out.println("Worker Thread Initiated.");
        this.chunk.intermediateResults(); // process chunk and get intermediate results
        try {
            // Send chunk with intermediate results to server
            this.out.writeObject(chunk);
            this.out.flush(); // flush output stream to ensure everything is sent
        } catch (IOException e) {
            e.printStackTrace(); // handle exceptions
        }
    }

    public static void main(String[] args) {

        System.out.println("Worker Object Initialized\n\n");
        /* Create connection with server */
        String host = "localhost"; // server host
        Socket connection = null; // initialize connection socket to null
        try{
            try {
                connection = new Socket(host, receivingPort); // create new connection socket to server
                Worker.in = new ObjectInputStream(connection.getInputStream()); // initialize input stream to receive objects from server
            } catch (UnknownHostException e) {
                e.printStackTrace(); // handle exceptions
            } catch (IOException e) {
                e.printStackTrace(); // handle exceptions
            }
            /* Receive chunk and start processing thread */
            while (true) {
                try {
                    Chunk chunk = (Chunk) in.readObject(); // receive chunk object from server
                    Socket sendingSocket = new Socket(host, sendingPort); // create new socket for sending objects to server
                    new Worker(sendingSocket, chunk).start(); // create new worker thread to process chunk and send intermediate results back to server
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace(); // handle exceptions
                }
            }
        } finally {
            try {
                connection.close();;
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
