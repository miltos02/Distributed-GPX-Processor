import java.io.*;
import java.net.*;

public class connectWithWorkers extends Thread 
{
    private MasterServer masterServer;
    ServerSocket sRoundRobin;
    ServerSocket sRequestReduce;
    Socket providerSocketMap;

    public connectWithWorkers(MasterServer server)
    {
        // Initialize the instance variables in and out with the input stream and output stream of the connection socket
        this.masterServer = server;
    
    }

    @Override
    public void run() 
    {
        try 
        {
            /* Create Server Socket */
            sRoundRobin = new ServerSocket(3900, masterServer.getNumOfWorkers());//the same port as before, 10 connections
            while (true) 
            {   
                int i = 1; //counter. We want as many connections as the number of workers
                while(i<= masterServer.getNumOfWorkers())
                {
                    /* Accept the connection */
                    providerSocketMap = sRoundRobin.accept();
                    // /* Handle the request */
                    this.masterServer.addOutput(new ObjectOutputStream(providerSocketMap.getOutputStream())); //updating outputs list
                    i++;
                    System.out.println("connection made");
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }finally {
            try {
                providerSocketMap.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            try {
                sRoundRobin.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }}
        }
}
