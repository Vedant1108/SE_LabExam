// Entity: RoomDatabase
// Sequence diagram step 3,6,7: checkAvailability, allotRoom, return confirmation
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomDatabase {

    private List<Room> rooms;
    private Map<String, Student> students;
    private Map<String, String> allotments;        // studentId -> roomNo (pending)
    private Map<String, String> confirmedAllotments; // studentId -> roomNo (confirmed)
    private List<String> waitlist;
    private boolean biometricShouldFail = false;   // for edge case testing

    public RoomDatabase() {
        rooms             = new ArrayList<>();
        students          = new HashMap<>();
        allotments        = new HashMap<>();
        confirmedAllotments = new HashMap<>();
        waitlist          = new ArrayList<>();
    }

    // --- setup ---
    public void addRoom(Room r)       { rooms.add(r); }
    public void addStudent(Student s) { students.put(s.getStudentId(), s); }
    public void setBiometricFail(boolean fail) { this.biometricShouldFail = fail; }

    // --- sequence diagram step 3: checkAvailability ---
    public List<Room> checkAvailability() {
        List<Room> vacant = new ArrayList<>();
        for (Room r : rooms)
            if ("VACANT".equals(r.getOccupancyStatus()))
                vacant.add(r);
        return vacant;
    }

    public boolean studentExists(String studentId) {
        return students.containsKey(studentId);
    }

    public Student getStudent(String studentId) {
        return students.get(studentId);
    }

    // --- sequence diagram step 6: allotRoom (tentative, awaiting payment) ---
    public void allotRoom(String studentId, String roomNo) {
        allotments.put(studentId, roomNo);
        for (Room r : rooms)
            if (r.getRoomNo().equals(roomNo))
                r.setOccupancyStatus("PENDING");
    }

    // --- activity diagram: payment YES -> confirmAllotment ---
    public void confirmAllotment(String studentId) {
        String roomNo = allotments.get(studentId);
        if (roomNo != null) {
            confirmedAllotments.put(studentId, roomNo);
            for (Room r : rooms)
                if (r.getRoomNo().equals(roomNo))
                    r.setOccupancyStatus("OCCUPIED");
            Student s = students.get(studentId);
            if (s != null) s.setRoomNo(roomNo);
        }
    }

    // --- activity diagram: payment NO -> cancelAllotment ---
    public void cancelAllotment(String studentId, String roomNo) {
        allotments.remove(studentId);
        for (Room r : rooms)
            if (r.getRoomNo().equals(roomNo))
                r.setOccupancyStatus("VACANT");
    }

    // --- activity diagram: biometric verification step ---
    public boolean verifyBiometric(String studentId) {
        if (biometricShouldFail) return false;
        return students.containsKey(studentId);
    }

    // --- waitlist ---
    public void addToWaitlist(String studentId) {
        if (!waitlist.contains(studentId))
            waitlist.add(studentId);
    }

    // --- query helpers ---
    public String getAllotment(String studentId)          { return allotments.get(studentId); }
    public String getConfirmedAllotment(String studentId){ return confirmedAllotments.get(studentId); }
    public List<String> getWaitlist()                    { return waitlist; }
    public List<Room> getAllRooms()                       { return rooms; }
}
