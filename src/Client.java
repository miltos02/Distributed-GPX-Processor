import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class Client implements Serializable
{
    private String name;
    private Double totalDistance = 0.0;
    private Double totalElevation = 0.0;
    private Double totalDistancePercentageDifference=0.0;
    private Double totalElevationPercentageDifference=0.0;
    private int totalTimeInHours = 0;
    private int totalTimeInMinutes = 0;
    private int totalTimeInSeconds = 0;
    private int numOfInstances = 0;
    private int id;
    private ArrayList<Double> avgSpeeds = new ArrayList<Double>();
    private Double totalSpeeds = 0.0;
    private Double avgSpeed=0.0;
    private Double avgDistance;
    private Double avgElevation;
    private int avgTime[] = {0,0,0}; //[[avgTimeInHours],[avgTimeInMinutes],[avgTimeInSeconds]]
    private Double timePercentageDifference = 0.0;
    private Double percentageDifferences[];
    //[totalDistancePercentageDifference,totalElevationPercentageDifference, [percentageDifferenceTime]]

    Random r = new Random();


    public Client(String n)
    {
        this.name = n;
        this.id = r.nextInt(100000000);
    }

    public Client(Client other)
    {
        this.name = other.name;
        this.totalDistance = other.totalDistance;
        this.totalElevation = other.totalElevation;
        this.totalTimeInHours = other.totalTimeInHours;
        this.totalTimeInMinutes = other.totalTimeInMinutes;
        this.totalTimeInSeconds = other.totalTimeInSeconds;
        this.avgSpeed = other.avgSpeed;
        this.id = other.id;
        this.avgTime = other.avgTime;
        this.avgDistance=other.avgDistance;
        this.avgElevation = other.avgElevation;
        this.avgSpeeds = other.avgSpeeds;

    }

    public void addTotalDistance (Double x)
    {
        this.totalDistance += x;
        this.avgDistance = totalDistance/numOfInstances;
    }

    public void updateAverageSpeed (Double x)
    {
        totalSpeeds+=x;
        numOfInstances+=1;
        this.avgSpeed = this.totalDistance/((double)this.totalTimeInHours + ((double)this.totalTimeInMinutes/60.0)+((double)this.totalTimeInSeconds/3600.0));
    }

    public void addTotalElevation (Double x)
    {
        this.totalElevation += x;
        this.avgElevation = totalElevation/numOfInstances;
    }

    public void addTotalTimeInHours (int x)
    {
        this.totalTimeInHours += x;

        //Last time instance to be updated in the reduce method, 
        //so average time in minutes,seconds and hours is to be updated here:

        //average hours(some minutes may be lost from this calculation)
        avgTime[0] = (int) this.totalTimeInHours/numOfInstances;

        //average minutes (some seconds may be lost from this calculation)
        this.avgTime[1] = (int) this.totalTimeInMinutes/numOfInstances;
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
        this.avgTime[2] = (int) this.totalTimeInSeconds/numOfInstances;
        if(this.avgTime[2]>=60)
        {
            int i = Integer.valueOf(avgTime[2]/60);
            for(int j=1; j<=i;j++)
            {
                this.avgTime[1] += 1;
                this.avgTime[2] -= 60;
            }
        }
    }

    public void addTotalTimeInMinutes (int x)
    {
        this.totalTimeInMinutes += x;
        if(this.totalTimeInMinutes>=60)
        {
            int i = Integer.valueOf(totalTimeInMinutes/60);
            for(int j=1; j<=i;j++)
            {
                this.totalTimeInHours += 1;
                this.totalTimeInMinutes -= 60;
            }
        }
    }

    public void addTotalTimeInSeconds (int x)
    {
        this.totalTimeInSeconds += x;
        if(this.totalTimeInSeconds>=60)
        {
            int i = Integer.valueOf(totalTimeInSeconds/60);
            for(int j=1; j<=i;j++)
            {
                this.totalTimeInMinutes += 1;
                this.totalTimeInSeconds -= 60;
            }
        }
    }

    public Double getTotalDistance ()
    {
        return this.totalDistance;
    }

    public Double getAverageSpeed ()
    {
        return this.avgSpeed;
    }

    public Double getAverageElevation ()
    {
        return this.avgElevation;
    }

    public Double getAverageDistance()
    {
        return this.avgDistance;
    }

    public Double getTotalElevation ()
    {
        return this.totalElevation; 
    }

    public int getID(){
        return this.id;
    }

    public int getTotalTimeInHours ()
    {
        return this.totalTimeInHours;
    }

    public int getTotalTimeInMinutes ()
    {
        return this.totalTimeInMinutes;
    }

    public int getTotalTimeInSeconds ()
    {
        return this.totalTimeInSeconds;
    }

    public int getAvgHours()
    {
        return this.avgTime[0];
    }

    public int getAvgMinutes()
    {
        return this.avgTime[1];
    }

    public int getAvgSeconds()
    {
        return this.avgTime[2];
    }

    public String getName()
    {
        return this.name;
    }

    public String toString(){
        return String.format("Client: %s\n-----------\nYour total activity is: \nAverage speed :\n%.2f km per hour \nTotal distance: \n%.2f km \nTotal Elevation: \n%.2f meters \nTotal time: \n%02d:%02d:%02d",
        this.name, this.getAverageSpeed(),this.getTotalDistance(),this.getTotalElevation(), this.getTotalTimeInHours() ,this.getTotalTimeInMinutes() , this.getTotalTimeInSeconds());
    }

    public int speeds(){
        return this.avgSpeeds.size();
    }

    public void calculatePercentages(double dist, double ele, int[] time) {
        // Calculate percentage difference for total distance
        if((this.totalDistance + dist)!=0){
            this.totalDistancePercentageDifference = (this.totalDistance/dist)*100.0;
        }
        // Calculate percentage difference for total elevation
        if((this.totalElevation + ele)!=0){
            totalElevationPercentageDifference = (this.totalElevation/ele)*100.0;
        }
        //calculate seconds difference
        int totalTimeGeneral = time[0] * 3600 + time[1] * 60 + time[0];
        int totalTimeOfThis = this.totalTimeInHours*3600 + this.totalTimeInMinutes*60 + this.totalTimeInSeconds;
        if ((totalTimeGeneral+totalTimeOfThis) != 0) {
            this.timePercentageDifference = (totalTimeOfThis/totalTimeGeneral)*100.0;
        }

        //make the array
        Double timeGeneral = totalTimeGeneral * 1.0;
        Double timeOfThis = totalTimeOfThis * 1.0;
        this.percentageDifferences = new Double[]{this.totalDistancePercentageDifference, totalElevationPercentageDifference, this.timePercentageDifference, this.totalDistance, dist, this.totalElevation, ele, timeOfThis, timeGeneral};
    }

    public Double[] getPercentages(){
        return this.percentageDifferences;
    }

    public String getPercentageOfDistance(){
        String str = "distance percentage: " + this.percentageDifferences[0];
        return str;
    }


}