import java.util.ArrayList;

public class thisGpx {
    private ArrayList<Chunk> chunks;
    private Double totalDistance = 0.0;
    private int totalTimeInSeconds = 0;
    private int totalHours = 0;
    private int totalMinutes = 0;
    private int totalSeconds = 0;
    private Double avgSpeed = 0.0;

    public thisGpx() {
        chunks = new ArrayList<Chunk>();
    }

    public void addChunk(Chunk chunk) {
        chunks.add(chunk);
    }

    public double getTotalDistance() {
        double totalDistance = 0.0;
        for (Chunk chunk : chunks) {
            totalDistance += chunk.getTotalDistance();
        }
        this.totalDistance = totalDistance;
        return this.totalDistance;
    }

    public double getAverageSpeed() {
        if (this.chunks.size() > 0) {
            this.avgSpeed = this.totalDistance/((double)this.totalHours + ((double)this.totalMinutes/60.0)+((double)this.totalSeconds/3600.0));
        }
        return this.avgSpeed;
    }

    public double getTotalClimb() {
        double totalClimb = 0.0;
        for (Chunk chunk : chunks) {
            totalClimb += chunk.getTotalElevation();
        }
        return totalClimb;
    }

    public int getTotalTimeInSeconds() {
        int totalHours = 0;
        int totalMinutes = 0;
        int totalSeconds = 0;
        for (Chunk chunk : chunks) {
            totalHours += chunk.getTotalHours();
            totalMinutes += chunk.getTotalMinutes();
            totalSeconds += chunk.getTotalSeconds();
        }
        totalMinutes += totalSeconds / 60;
        totalSeconds %= 60;
        totalHours += totalMinutes / 60;
        totalMinutes %= 60;

        this.totalTimeInSeconds = totalHours * 3600 + totalMinutes * 60 + totalSeconds;
        this.totalHours = this.totalTimeInSeconds / 3600;
        this.totalMinutes = (this.totalTimeInSeconds % 3600) / 60;
        this.totalSeconds = totalSeconds;

        return this.totalTimeInSeconds;
    }

    @Override
    public String toString() {
        double totalDistance = getTotalDistance();
        double totalClimb = getTotalClimb();
        getTotalTimeInSeconds();
        double averageSpeed = getAverageSpeed();

        int totalHours = this.totalHours;
        int totalMinutes = this.totalMinutes;
        int totalSeconds = this.totalSeconds;

        return String.format("One Route's Results:\n------------------------\nTotal Distance:\n%.2f\nAverage Speed: \n%.2f\nTotal Climb: \n%.2f\nTotal Time: \n%02d:%02d:%02d",
                totalDistance, averageSpeed, totalClimb, totalHours, totalMinutes, totalSeconds);
    }
}
