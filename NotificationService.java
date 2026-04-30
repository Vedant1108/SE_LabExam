// Boundary: NotificationService
// Sequence diagram step 8-9: sendNotification -> notify student
// Activity diagram: sends allotment alert, waitlist notice, cancellation notice
public class NotificationService {

    private boolean shouldFail;

    public NotificationService()                   { this.shouldFail = false; }
    public NotificationService(boolean shouldFail) { this.shouldFail = shouldFail; }

    // Seq step 8: sendNotification(studentId, roomDetails)
    public void sendAllotmentAlert(String studentId, Room room) {
        if (shouldFail) throw new RuntimeException("Notification service unavailable");
        System.out.println("  [NOTIFY] Allotment: Student " + studentId
            + " -> Room " + room.getRoomNo() + " Block " + room.getBlock());
    }

    // Activity diagram: NO branch of rooms available
    public void sendWaitlistNotice(String studentId) {
        System.out.println("  [NOTIFY] Waitlist: Student " + studentId
            + " added to waitlist.");
    }

    // Activity diagram: payment NO branch
    public void sendCancellationNotice(String studentId) {
        System.out.println("  [NOTIFY] Cancelled: Allotment for student " + studentId
            + " cancelled due to non-payment.");
    }
}
