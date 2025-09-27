import java.util.*;
import java.io.*;
import java.net.*;
import javax.xml.parsers.*; 
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class ActionsForApp extends Thread 
{
    ObjectInputStream in;
    ObjectOutputStream out;
    Socket providerSocketMap;
    Socket providerSocketReduce;
    ServerSocket sRoundRobin;
    ServerSocket sRequestReduce;
    Socket connection;
    private MasterServer masterServer;
    private thisGpx oneGpx = new thisGpx();
    
    public ActionsForApp(Socket connection, MasterServer server) 
    {
        // Initialize the instance variables in and out with the input stream and output stream of the connection socket
        this.connection = connection;
        this.masterServer = server;
        try {
            this.in = new ObjectInputStream(connection.getInputStream());
            this.out = new ObjectOutputStream(connection.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() 
    {
        Client toSendClient = null;
        System.out.println("Actions for app thread started");
        try {
            int fileSize= this.in.readInt(); //read size of file
            byte[] buffer = new byte[fileSize];
            byte[] fileBytes = new  byte[0];
            int bytesRead;
            Double wptForEachChunk; //waypoints for each to-be-sent chunk
            Waypoint newWaypoint; 
            boolean lessWaypointsThanWorkers = false; //variable that is true in the extreme case where waypoints<workers

            // Read the file from the input stream and concatenate the bytes into a byte array
            while (fileBytes.length < fileSize)
            {
                bytesRead = this.in.read(buffer);
                fileBytes = concatenateByteArrays(fileBytes, buffer, bytesRead);
            }

            try{

                System.out.println("app connection made");
                // An instance of the DocumentBuilderFactory class used to create DocumentBuilder object.
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance(); 

                // Instantiate a new object of the DocumentBuilder class and assign it to the variable dBuilder. 
                //This dBuilder object can be utilized for parsing XML documents.
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder(); 

                // Transform the byte array into a fresh Document object with the aid of the DocumentBuilder object.
                Document doc = dBuilder.parse(new ByteArrayInputStream(fileBytes)); 

                //Reformat the document by merging contiguous text nodes and removing any text nodes that are empty. 
                //Then, designate the top-level element as the active element.
                doc.getDocumentElement().normalize(); 

                // Get value of the creator attribute. That's the user.
                String user = doc.getDocumentElement().getAttribute("creator");
                masterServer.addClient(user);

                // Get the list of waypoints
                NodeList waypointList = doc.getElementsByTagName("wpt");

                ArrayList<Waypoint> waypointsToSend = new ArrayList<Waypoint>();

                // Loop through each waypoint and get useful data
                for (int i = 0; i < waypointList.getLength(); i++) 
                {
                    Element wptElement = (Element) waypointList.item(i);
                    Double lat = Double.parseDouble(wptElement.getAttribute("lat"));
                    Double lon = Double.parseDouble(wptElement.getAttribute("lon"));
                    Double ele = Double.parseDouble(wptElement.getElementsByTagName("ele").item(0).getTextContent());
                    String str = wptElement.getElementsByTagName("time").item(0).getTextContent();
                    Time time = new Time(str);
                    newWaypoint = new Waypoint(lat, lon, ele, time);

                    waypointsToSend.add(newWaypoint);  
                }

                //Create list of chunks
                ArrayList<Chunk> listOfChunks = new ArrayList<Chunk>();
                int numOfChunks = masterServer.getNumOfWorkers() + 3; //always chunks>workers so that round robin is utilized

                //If waypoints<workers, which is an extreme case, we don't execute multi threading process.
                //We simply create the intermediate results here and send them to the master for reducing.
                if(waypointsToSend.size() <= masterServer.getNumOfWorkers())
                {
                    Chunk newChunk = new Chunk(user);
                    newChunk.intermediateResults();
                    lessWaypointsThanWorkers = true;
                    newChunk = new Chunk(newChunk);
                    this.masterServer.reduce(newChunk);
                    toSendClient = masterServer.getClient(user);
                    this.out.reset();    
                    this.out.writeObject(toSendClient);
                    this.out.flush();
                }
                else
                {
                    //If waypoints to send can be equally divided to all chunks, then
                    //each chunk receives a number of (number of waypoints/number of chunks) waypoints to edit.
                    if(waypointsToSend.size() % numOfChunks == 0)
                    {
                        //Calculate number of waypoints for each chunk (number of waypoints/number of chunks)
                        wptForEachChunk = Double.valueOf(waypointsToSend.size()) / Double.valueOf(numOfChunks);
                        //turn to integer to use in the for loop 
                        int intNum = wptForEachChunk.intValue();
                        for(int i=1; i<=numOfChunks; i++)
                        {
                            Chunk newChunk = new Chunk(user);
                            for(int j=1; j <= intNum; j++)
                            {
                                newWaypoint = waypointsToSend.get(waypointsToSend.size() - 1);
                                waypointsToSend.remove(waypointsToSend.size() - 1);
                                newChunk.add(newWaypoint);
                            }
                            listOfChunks.add(newChunk);
                        }
                    }
                    else //if waypoints to send can't be equally divided to all chunks
                    {
                        //in this case all chunks take the integer part of the (number of waypoints/number of chunks) division
                        //and the last chunk takes a chunk of few more waypoints

                        //Calculate number of waypoints for each chunk (number of waypoints/number of chunks)
                        wptForEachChunk = Double.valueOf(waypointsToSend.size()) / Double.valueOf(numOfChunks);
                        //keep the integer part to send to all chunks except the last
                        int intWptForEachChunk  = wptForEachChunk.intValue();

                        for(int i=1; i<=(numOfChunks-1); i++)
                        {
                            Chunk newChunk = new Chunk(user);
                            for(int j=1; j <= intWptForEachChunk; j++)
                            {
                                newWaypoint = waypointsToSend.get(waypointsToSend.size() - 1);
                                waypointsToSend.remove(waypointsToSend.size() - 1);
                                newChunk.add(newWaypoint);
                            }
                            listOfChunks.add(newChunk);
                        }
                        //Chunk with the remaining waypoints, to be sent to the last chunk
                        Chunk newChunk = new Chunk(user);
                        while(waypointsToSend.size()!=0)
                        {
                            newWaypoint = waypointsToSend.get(waypointsToSend.size() - 1);
                            waypointsToSend.remove(waypointsToSend.size() - 1);
                            newChunk.add(newWaypoint);
                        }
                        listOfChunks.add(newChunk);
                    }
                } 

                //We create a list of outputs for the master to save so the chunks can be sent
                //to the workers with round-robin
                //ArrayList<ObjectOutputStream> outputs = new ArrayList<ObjectOutputStream>();

                if(!lessWaypointsThanWorkers) //all this of course happens if were not in the extreme waypoints<workers situation
                {
                    try {

                        //----------SENDING CHUNKS FOR PROCESSING----------

                        int currentOutputIndex = 0; //round-robin index
                        int repetitions = listOfChunks.size(); //to make sure round-robin process stops when all chunks are sent

                        for (int j = 1; j <= repetitions; j++) 
                        {
                            synchronized(this.masterServer.getListOfOutputs().get(currentOutputIndex)) //lock output stream
                            {
                                Chunk c = new Chunk(listOfChunks.get(listOfChunks.size()-1));
                                this.masterServer.getListOfOutputs().get(currentOutputIndex).writeObject(c);
                                this.masterServer.getListOfOutputs().get(currentOutputIndex).flush();
                                listOfChunks.remove(listOfChunks.size()-1);
                            }

                            //if we sent to last worker the index becomes 0 again:
                            currentOutputIndex = (currentOutputIndex + 1) % this.masterServer.getListOfOutputs().size(); 
                        }

                        //----------RECEIVING CHUNKS OF INTERMEDIATE RESULTS----------

                        /* Create Server Socket */
                        sRequestReduce = new ServerSocket(8080,masterServer.getNumOfWorkers());

                        //counter to make sure results are sent back o application after all intermediate results are reduced
                        int counter = 0; 
                        while (true) 
                        {
                            /* Accept the connection */
                            providerSocketReduce = sRequestReduce.accept();
                            in = new ObjectInputStream(providerSocketReduce.getInputStream());

                            Chunk chunk = (Chunk) in.readObject();
                            this.masterServer.reduce(chunk);
                            oneGpx.addChunk(chunk);
                            counter++;
                            if(counter == numOfChunks)
                            {

                                this.masterServer.calculateClientPercentages(user);
                                //send this client's total statistics
                                toSendClient = masterServer.getClient(user);

                                String clientStats = toSendClient.toString();
                                System.out.println("Client Stats to be sent for the app's statistics:\n" + clientStats);
                                this.out.reset();    
                                this.out.writeObject(clientStats);
                                this.out.flush();
                                
                                //send the calculations for this gpx
                                String gpxStats = oneGpx.toString();
                                System.out.println("One Route's stats to be sent for the app's messages:\n"+gpxStats);
                                this.out.reset();    
                                this.out.writeObject(gpxStats);
                                this.out.flush();

                                //send the difference of this client's statistics from the majority of clients
                                Double[] percents = new Double[9];
                                percents = this.masterServer.getClient(user).getPercentages();
                                this.out.reset();    
                                this.out.writeObject(percents);
                                this.out.flush();

                                break;
                            }
                        }
                    
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                    } finally {
                        try {
                            providerSocketReduce.close();
                            sRequestReduce.close();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                }

            } catch (ParserConfigurationException | SAXException e) {
                e.printStackTrace();
            } 


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
                this.in.close();
                this.out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private byte[] concatenateByteArrays(byte[] array1, byte[] array2, int length) {
        byte[] result = new byte[array1.length + length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length,length);
        return result;
    }

}
            