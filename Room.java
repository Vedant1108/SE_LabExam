// Entity class: Room
public class Room {
    private String roomNo;
    private String block;
    private int floor;
    private int preferenceType; // 1-5
    private String occupancyStatus; // "VACANT" or "OCCUPIED"

    public Room(String roomNo, String block, int floor, int preferenceType) {
        this.roomNo = roomNo;
        this.block = block;
        this.floor = floor;
        this.preferenceType = preferenceType;
        this.occupancyStatus = "VACANT";
    }

    public String getRoomNo()        { return roomNo; }
    public String getBlock()         { return block; }
    public int getFloor()            { return floor; }
    public int getPreferenceType()   { return preferenceType; }
    public String getOccupancyStatus() { return occupancyStatus; }
    public void setOccupancyStatus(String status) { this.occupancyStatus = status; }

    @Override
    public String toString() {
        return "Room[" + roomNo + ", Block:" + block + ", Floor:" + floor
                + ", Type:" + preferenceType + ", " + occupancyStatus + "]";
    }
}
