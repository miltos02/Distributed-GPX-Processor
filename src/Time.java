import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Time implements Serializable
{
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;

    public Time(String timeString)
    {
        year = 0;
        year += (Integer.parseInt(String.valueOf(timeString.charAt(0))))*1000;
        year += (Integer.parseInt(String.valueOf(timeString.charAt(2))))*10;
        year += Integer.parseInt(String.valueOf((timeString.charAt(3))));

        month = 0;
        month += (Integer.parseInt(String.valueOf(timeString.charAt(5))))*10;
        month+= (Integer.parseInt(String.valueOf(timeString.charAt(6))));

        day = 0;
        day += (Integer.parseInt(String.valueOf(timeString.charAt(8))))*10;
        day += (Integer.parseInt(String.valueOf(timeString.charAt(9))));

        hour = 0; 
        hour = (Integer.parseInt(String.valueOf(timeString.charAt(11))))*10;
        hour += (Integer.parseInt(String.valueOf(timeString.charAt(12))));

        minute = 0;
        minute+= (Integer.parseInt(String.valueOf(timeString.charAt(14))))*10;
        minute+= (Integer.parseInt(String.valueOf(timeString.charAt(15))));

        second = 0;
        second+= (Integer.parseInt(String.valueOf(timeString.charAt(17))))*10;
        second+= (Integer.parseInt(String.valueOf(timeString.charAt(18))));

    }

    public int getYear()
    {
        return this.year;
    }

    public int getMonth()
    {
        return this.month;
    }

    public int getDay()
    {
        return this.day;
    }

    public int getHour()
    {
        return this.hour;
    }

    public int getMinute()
    {
        return this.minute;
    }

    public int getSecond()
    {
        return this.second;
    }

    public String toString()
    {
        return "\nYear: "+ this.year+" Month: "+ this.month+" Day : " + this.day + " \nHour: "+this.hour+" Minute: "+this.minute+ " Second: "+this.second+"";
    }

    public ArrayList<Integer> getTimeDifference(Time otherTime) {

        ArrayList<Integer> array = new ArrayList<Integer>();

        LocalDateTime thisTime = LocalDateTime.of(year, month, day, hour, minute, second);
        LocalDateTime thatTime = LocalDateTime.of(otherTime.getYear(), otherTime.getMonth(), otherTime.getDay(), otherTime.getHour(), otherTime.getMinute(), otherTime.getSecond());
    
        long secondsDiff = ChronoUnit.SECONDS.between(thisTime, thatTime);
    
        int hours = (int) (secondsDiff / 3600);
        array.add(hours);
        int minutes = (int) ((secondsDiff % 3600) / 60);
        array.add(minutes);
        int seconds = (int) (secondsDiff % 60);
        array.add(seconds);

        return array;
    }
}