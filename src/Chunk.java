import java.io.Serializable;
import java.util.ArrayList;

public class Chunk implements Serializable{
    //Input for mapping
    private ArrayList<Waypoint> listWaypoints;
    private String key;

    // Output of mapping
    private double totalTravelDistance = 0.0;
    private double avgSpeed=0.0;
    private double totalElevationGain=0.0;
    int timeSpent[] = new int[3]; //Array in which: Hours = timeSpent[0], Minutes = timeSpent[1], Seconds = timeSpent[2].

    public Chunk(String key)
    {
        this.key = key;
        listWaypoints = new ArrayList<Waypoint>();
    }

    public Chunk(Chunk other) 
    {
        this.listWaypoints = other.listWaypoints;
        this.key = other.key;
        this.totalTravelDistance = other.totalTravelDistance;
        this.avgSpeed = other.avgSpeed;
        this.totalElevationGain = other.totalElevationGain;
    }

    private double distance(Waypoint wpt1, Waypoint wpt2) 
    {
        double EARTH_RADIUS_KM = 6371.0;
        double dLat = Math.toRadians(wpt2.getLat() - wpt1.getLat());
        double dLon = Math.toRadians(wpt2.getLon() - wpt1.getLon());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(wpt1.getLat())) * Math.cos(Math.toRadians(wpt2.getLat())) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;
        return distance;
    }

    public void intermediateResults() 
    {
        for (int i = 1; i < this.listWaypoints.size(); i++) 
        {
            Waypoint prev = this.listWaypoints.get(i-1);
            Waypoint curr = this.listWaypoints.get(i);
            
            double distance = this.distance(prev, curr);
            double elevationGain = Math.max(0, curr.getEle() - prev.getEle()); 
            ArrayList<Integer> temp = curr.getTime().getTimeDifference(prev.getTime());
            timeSpent[0] += temp.get(0); //Hours
            timeSpent[1] += temp.get(1); //Minutes
            if(timeSpent[1] >= 60)
            {
                int k = Integer.valueOf(timeSpent[1]/60);
                for(int j=1; j<=k;j++)
                {
                    timeSpent[1] -= 60;
                    timeSpent[0] += 1;
                }
            }
            timeSpent[2] += temp.get(2);//Seconds
            if(timeSpent[2] >= 60)
            {
                int k = Integer.valueOf(timeSpent[2]/60);
                for(int j=1; j<=k;j++)
                {
                    timeSpent[2] -= 60;
                    timeSpent[1] += 1;
                }
            }
            this.totalTravelDistance += distance;
            this.totalElevationGain += elevationGain;
        }


        if (this.timeSpent[2] != 0) 
        {
            this.avgSpeed =  ( this.totalTravelDistance/1000.0f ) / ( this.timeSpent[2]/3600.0f );
        }

    }

    public String getKey() 
    {
        return this.key;
    }

    public void setKey(String key) 
    {
        this.key = key;
    }

    public void add(Waypoint wpt)
    {
        this.listWaypoints.add(wpt);
    }

    public int size() {
        return this.listWaypoints.size();
    }

    public Waypoint get(int i) {
        return listWaypoints.get(i);
    }

    public int getTotalHours() {
        return this.timeSpent[0];
    }

    public int getTotalMinutes() {
        return this.timeSpent[1];
    }

    public int getTotalSeconds() {
        return this.timeSpent[2];
    }

    public double getAvgSpeed() {
        return this.avgSpeed;
    }

    public double getTotalDistance() {
        return this.totalTravelDistance;
    }

    public double getTotalElevation() {
        return this.totalElevationGain;
    }

}
