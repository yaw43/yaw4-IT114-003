package Project.Common;

public class PointsPayload extends Payload {
    private int points;

    public PointsPayload()
    {
        setPayloadType(PayloadType.SYNC_POINTS);
    }

    public int getPoints()
    {
        return points;
    }

    public int setPoints(int points) // yaw4 12/10 used to send/set points of users
    {
        this.points = points;
        return points;
    }

    @Override
    public String toString()
    {
        return super.toString() + "{ points: " + points + "}";
    }
}
