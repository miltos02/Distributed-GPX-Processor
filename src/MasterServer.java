import java.util.*;
import java.io.*;
import java.net.*;

public class MasterServer 
{
    //ObjectOutputStream out = null;
    private static int APP_PORT = 4321; // Port for communicating with the application
    // Store number of workers and clients
    private int numOfWorkers;
    private ArrayList<Client> listOfClients = new ArrayList<Client>();
    private ArrayList<ObjectOutputStream> outputs = new ArrayList<ObjectOutputStream>();
    private ArrayList<Double[]> statsOfEachUser = new ArrayList<>();
    private ArrayList<int[]> timesOfEachUser = new ArrayList<>();
    private Double totalDistance = 0.0;
    private Double totalElevation = 0.0;
    private Double avgDistance = 0.0;
    private Double avgElevation = 0.0;
    private int totalTimeInHours = 0;
    private int totalTimeInMinutes = 0;
    private int totalTimeInSeconds = 0;
    int avgTime[] = {0,0,0}; //[[avgTimeInHours],[avgTimeInMinutes],[avgTimeInSeconds]]


    // Create server socket objects
    ServerSocket appSocket;
    Socket providerSocket;

    // Main method to start the server
    public static void main(String[] args) 
    {
        // Parse command line argument for number of workers
        String readData = args[0];
        new MasterServer().openServer(readData);
    }

    // Method to open server
    void openServer(String workers) 
    {
        System.out.println("server opened");
        try {
            // Parse number of workers
            this.numOfWorkers = Integer.parseInt(workers);
            System.out.println("Number of workers: "+workers);

            Thread workerThread = new connectWithWorkers(this);
            System.out.println("Connect With Workers thread initialized");
            workerThread.start();
            

            // Create server socket for application communication
            appSocket = new ServerSocket(APP_PORT);
            System.out.println("Application socket created");

            while (true) 
            {
                // Accept incoming connection from application
                providerSocket = appSocket.accept();
                System.out.println("Accepted application connection");

                // Create new thread to handle the request
                Thread t = new ActionsForApp(providerSocket, this);
                System.out.println("Application thread initialized");
                t.start();
            }
            
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }

    // Method to get number of workers
    public int getNumOfWorkers()
    {
        return this.numOfWorkers;
    }

    // Method to add new client to the list
    public void addClient(String cl)
    {
        boolean found = false;
        for(Client c: listOfClients)
        {
            if(c.getName().equals(cl))
            {
                found= true;
            }
        }
        if(!found)
        {
            Client newClient = new Client(cl);
            this.listOfClients.add(newClient);
        }
    }

    // Method to perform reduce operation
    public void reduce(Chunk c)
    {
        for (Client cl : this.listOfClients) 
        {
            // Check if the client matches the chunk key and update its activity data
            if (cl.getName().equals(c.getKey())) 
            {
                cl.updateAverageSpeed(c.getAvgSpeed());
                cl.addTotalDistance(c.getTotalDistance());
                this.totalDistance+=c.getTotalDistance();
                cl.addTotalElevation(c.getTotalElevation());
                this.totalElevation+=c.getTotalElevation();
                cl.addTotalTimeInSeconds(c.getTotalSeconds());
                this.totalTimeInSeconds += c.getTotalSeconds();
                if(this.totalTimeInSeconds>=60)
                {
                    int i = Integer.valueOf(totalTimeInSeconds/60);
                    for(int j=1; j<=i;j++)
                    {
                        this.totalTimeInMinutes += 1;
                        this.totalTimeInSeconds -= 60;
                    }
                }
                cl.addTotalTimeInMinutes(c.getTotalMinutes());
                this.totalTimeInMinutes += c.getTotalMinutes();
                if(this.totalTimeInMinutes>=60)
                {
                    int i = Integer.valueOf(totalTimeInMinutes/60);
                    for(int j=1; j<=i;j++)
                    {
                        this.totalTimeInHours += 1;
                        this.totalTimeInMinutes -= 60;
                    }
                }
                cl.addTotalTimeInHours(c.getTotalHours());

                //Update total distance and total elevation for this client
                double id = (double)cl.getID();
                Double avgStatsOfOneUser[] = {id, cl.getAverageDistance(), cl.getAverageElevation()};
                int timesOfOneUser[] = {cl.getID(), cl.getAvgHours(), cl.getAvgMinutes(), cl.getAvgSeconds()};
                
                // Flag to track if the ID is found
                boolean found = false;
                double idToSearch = cl.getID();

                // Iterate through the ArrayList to search for the ID
                for (int i = 0; i < statsOfEachUser.size(); i++) {
                    Double[] array = statsOfEachUser.get(i);

                    if (array[0] == idToSearch) {
                        // Remove the existing element at index i
                        statsOfEachUser.remove(i);
                        timesOfEachUser.remove(i);
                        // Add the replacement array at index i
                        statsOfEachUser.add(avgStatsOfOneUser);
                        timesOfEachUser.add(timesOfOneUser);
                        found = true;
                        break;
                    }
                }

                // If the ID is not found, add the replacement array
                if (!found) {
                    statsOfEachUser.add(avgStatsOfOneUser);
                    timesOfEachUser.add(timesOfOneUser);
                }

                //Update total distance and total elevation for all clients
                this.avgDistance = this.totalDistance/this.listOfClients.size();
                this.avgElevation = this.totalElevation/this.listOfClients.size();

                //Update total time for all clients
                //average hours(some minutes may be lost from this calculation)
                avgTime[0] = (int) this.totalTimeInHours/this.getListOfClients().size();

                //average minutes (some seconds may be lost from this calculation)
                this.avgTime[1] = (int) this.totalTimeInMinutes/this.getListOfClients().size();
                if(this.avgTime[1]>=60)
                {
                    int i = Integer.valueOf(avgTime[1]/60);
                    for(int j=1; j<=i;j++)
                    {
                        this.avgTime[0] += 1;
                        this.avgTime[1] -= 60;
                    }
                }

                //average seconds
                this.avgTime[2] = (int) this.totalTimeInSeconds/this.getListOfClients().size();
                if(this.avgTime[2]>=60)
                {
                    int i = Integer.valueOf(avgTime[2]/60);
                    for(int j=1; j<=i;j++)
                    {
                        this.avgTime[1] += 1;
                        this.avgTime[2] -= 60;
                    }
                }
                break;
            }
        }
    }

    // Method to get a client by its name
    public Client getClient(String name)
    {
        Client found = null;
        for (Client cl : this.listOfClients) 
        {
            if (cl.getName().equals(name)) 
            {
                found = cl;
            }
        }

        return found;
    }

    public ArrayList<Client> getListOfClients()
    {
        return this.listOfClients;
    };

    // Method to add new client to the list
    public void addOutput(ObjectOutputStream out)
    {
       this.outputs.add(out);
    }

    public ArrayList<ObjectOutputStream> getListOfOutputs()
    {
        return this.outputs;
    };


    public Double getAverageElevationOfAllClients ()
    {
        return this.avgElevation;
    }

    public Double getAverageDistanceOfAllClients()
    {
        return this.avgDistance;
    }

    public int getAvgHoursOfAllClients()
    {
        return this.avgTime[0];
    }

    public int getAvgMinutesOfAllClients()
    {
        return this.avgTime[1];
    }

    public int getAvgSecondsOfAllClients()
    {
        return this.avgTime[2];
    }

    public Double getAverageElevationOfOneClient (String name)
    {
        double avgEle = 0.0;
        Client thisClient = this.getClient(name);
        double thisClientId = thisClient.getID();
        for (Double[] i: statsOfEachUser)
        {
            if(i[0]==thisClientId)
            {
                avgEle = i[2];
            }
        }
        return avgEle;
    }

    public Double getAverageDistanceOfOneClient(String name)
    {
        double avgDist = 0.0;
        Client thisClient = this.getClient(name);
        double thisClientId = thisClient.getID();
        for (Double[] i: statsOfEachUser)
        {
            if(i[0]==thisClientId)
            {
                avgDist = i[1];
            }
        }
        return avgDist;
    }

    public int getAvgHoursOfOneClient(String name)
    {
        int hours = 0;
        Client thisClient = this.getClient(name);
        int thisClientId = thisClient.getID();
        for (int[] i: timesOfEachUser)
        {
            if(i[0]==thisClientId)
            {
                hours = i[1];
            }
        }
        return hours;
    }

    public int getAvgMinutesOfOneClient(String name)
    {
        int minutes = 0;
        Client thisClient = this.getClient(name);
        int thisClientId = thisClient.getID();
        for (int[] i: timesOfEachUser)
        {
            if(i[0]==thisClientId)
            {
                minutes = i[2];
            }
        }
        return minutes;
    }

    public int getAvgSecondsOfOneClient(String name)
    {
        int seconds = 0;
        Client thisClient = this.getClient(name);
        int thisClientId = thisClient.getID();
        for (int[] i: timesOfEachUser)
        {
            if(i[0]==thisClientId)
            {
                seconds = i[3];
            }
        }
        return seconds;
    }

    public void calculateClientPercentages(String aUser)
    {
        int times[] = {this.totalTimeInHours,this.totalTimeInMinutes,this.totalTimeInSeconds};
        this.getClient(aUser).calculatePercentages(this.totalDistance, this.totalElevation, times);
    }
}