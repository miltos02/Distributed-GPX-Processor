import java.io.*;

public class Waypoint implements Serializable 
{
    
    private Double lat;
    private Double lon;
    private Double ele;
    private Time time;

    public Waypoint (Double lat, Double lon, Double ele, Time timeInstance) 
    {
        this.lat = lat;
        this.lon = lon;
        this.ele = ele;
        this.time = timeInstance;
    }

    public Double getLat()
    {
        return this.lat;
    }

    public Double getLon()
    {
        return this.lon;
    }

    public Double getEle()
    {
        return this.ele;
    }

    public Time getTime()
    {
        return this.time;
    }

    public String toString()
    {
        return 
        " lon: " + this.lon + 
        " lat: " + this.lat + 
        " ele: " + this.ele +
        " time: " + this.time.toString();
    }

}
