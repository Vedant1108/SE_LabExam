/**
 * Control class: AllotmentController
 *
 * Activity Diagram flow:
 *   1. Student fills Online Room Application Form
 *   2. System checks Room Availability
 *   DECISION: Rooms Available?
 *     NO  -> Add to Waitlist -> Notify -> END
 *     YES -> Rank by Merit -> Allot Room -> Send Notification
 *         -> Student pays Fee
 *   DECISION: Payment Successful?
 *     NO  -> Cancel Allotment -> Notify -> END
 *     YES -> Confirm Allotment -> Check-in -> Biometric Verification -> Key Issued -> END
 *
 * Sequence Diagram lifelines:
 *   Student -> RoomApplicationForm -> AllotmentController -> RoomDatabase -> NotificationService
 *
 * CRC responsibilities of AllotmentController:
 *   - Validate student against Student entity
 *   - checkAvailability() from RoomDatabase
 *   - rankStudentsByMerit() internal
 *   - allotRoom() to RoomDatabase
 *   - sendNotification() to NotificationService
 *   - confirmAllotment() / cancelAllotment() in RoomDatabase
 *   - verifyBiometric() for check-in
 */
import java.util.List;

public class AllotmentController {

    private RoomDatabase db;
    private NotificationService notificationService;

    public AllotmentController(RoomDatabase db, NotificationService notificationService) {
        this.db = db;
        this.notificationService = notificationService;
    }

    /**
     * submitApplication() - entry point called by RoomApplicationForm
     * Maps to sequence diagram: submitApplication(preferences) message
     *
     * @param studentId      from form
     * @param roomPreference 1–5
     * @param feeStatus      "PAID" or anything else
     * @return "ALLOTTED: <roomNo>" | "WAITLISTED" | "ALLOTMENT_CANCELLED"
     */
    public String submitApplication(String studentId, int roomPreference, String feeStatus) {

        // Validate studentId (CRC: validate against Student entity)
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new InvalidStudentException("Student ID cannot be null or empty.");
        }
        if (!db.studentExists(studentId)) {
            throw new InvalidStudentException("Student not registered: " + studentId);
        }

        // Validate roomPreference (from RoomApplicationForm)
        if (roomPreference < 1 || roomPreference > 5) {
            throw new InvalidPreferenceException(
                "Room preference must be 1–5. Got: " + roomPreference);
        }

        // Seq step 3: checkAvailability() -> RoomDatabase
        List<Room> roomList = db.checkAvailability();

        // Activity Diagram Decision: Rooms Available?
        if (roomList.isEmpty()) {
            db.addToWaitlist(studentId);
            notificationService.sendWaitlistNotice(studentId);
            return "WAITLISTED";
        }

        // Seq step 5: rankStudentsByMerit() - internal
        Room rankedRoom = rankByMerit(studentId, roomList, roomPreference);

        if (rankedRoom == null) {
            throw new NoSuitableRoomException("No suitable room for: " + studentId);
        }

        // Seq step 6: allotRoom(studentId, roomNo) -> RoomDatabase
        db.allotRoom(studentId, rankedRoom.getRoomNo());

        // Seq step 8: sendNotification() -> NotificationService
        notificationService.sendAllotmentAlert(studentId, rankedRoom);

        // Activity Diagram Decision: Payment Successful?
        if (!"PAID".equals(feeStatus)) {
            db.cancelAllotment(studentId, rankedRoom.getRoomNo());
            notificationService.sendCancellationNotice(studentId);
            return "ALLOTMENT_CANCELLED";
        }

        // Confirm allotment + Biometric verification (activity: check-in step)
        db.confirmAllotment(studentId);
        boolean biometricOk = db.verifyBiometric(studentId);
        if (!biometricOk) {
            throw new InvalidStudentException("Biometric verification failed: " + studentId);
        }

        // Seq step 10: showConfirmation -> Student
        return "ALLOTTED: " + rankedRoom.getRoomNo();
    }

    // Internal self-message (seq step 5)
    private Room rankByMerit(String studentId, List<Room> roomList, int preference) {
        for (Room r : roomList)
            if (r.getPreferenceType() == preference) return r;
        Room best = null;
        int minDiff = Integer.MAX_VALUE;
        for (Room r : roomList) {
            int diff = Math.abs(r.getPreferenceType() - preference);
            if (diff < minDiff) { minDiff = diff; best = r; }
        }
        return best;
    }
}
