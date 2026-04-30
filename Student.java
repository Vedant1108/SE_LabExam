// Entity class: Student
import java.util.ArrayList;
import java.util.List;

public class Student {
    private String studentId;
    private String name;
    private String roomNo;
    private String block;
    private String contactInfo;
    private int meritScore;
    private List<String> complaints;

    public Student(String studentId, String name, int meritScore, String contactInfo) {
        this.studentId = studentId;
        this.name = name;
        this.meritScore = meritScore;
        this.contactInfo = contactInfo;
        this.complaints = new ArrayList<>();
    }

    public String getStudentId()  { return studentId; }
    public String getName()       { return name; }
    public int getMeritScore()    { return meritScore; }
    public String getContactInfo(){ return contactInfo; }
    public String getRoomNo()     { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    public void setBlock(String block)   { this.block = block; }

    @Override
    public String toString() {
        return "Student[" + studentId + ", " + name + ", Merit:" + meritScore + "]";
    }
}
