package Project.Common;

public class CoordPayload extends Payload {
    private int x;
    private int y;

    public CoordPayload(int x, int y)
    {
        this.x = x;
        this.y = y; 
        setPayloadType(PayloadType.COORD);
    }

    public int getX()  //yaw4 12/10 used for sending payloads of ship/action coordinates
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    @Override
    public String toString()
    {
        return super.toString() + "{ x:" + x + ", y: " + y + "}";
    }

    public void placeCoordinate(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public void attackCoordinate(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

}
