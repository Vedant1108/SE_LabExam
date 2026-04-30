/**
 * BlackBoxTests — ECP (7 cases) + BVA (8 cases)
 *
 * All inputs/outputs follow the Activity Diagram flow:
 *   Student fills form -> checkAvailability -> [rooms?] -> rankByMerit -> allotRoom
 *   -> sendNotification -> [payment?] -> confirm/cancel -> biometric -> key issued
 *
 * And the Sequence Diagram lifelines:
 *   Student -> RoomApplicationForm -> AllotmentController -> RoomDatabase -> NotificationService
 *
 * Input variables (from RoomApplicationForm, per CRC card):
 *   - studentId       : String  (CRC: AllotmentController validates against Student entity)
 *   - roomPreference  : int 1–5 (CRC: RoomApplicationForm collects preference)
 *   - feeStatus       : String  (activity: payment decision node)
 *   - roomAvailability: DB state (activity: rooms available decision node)
 */
public class BlackBoxTests {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=============================================================");
        System.out.println("  BLACK BOX TESTS — Room Allotment Subsystem");
        System.out.println("  (Activity Diagram + Sequence Diagram + CRC Cards)");
        System.out.println("=============================================================\n");

        runECPTests();
        runBVATests();

        System.out.println("=============================================================");
        System.out.println("  RESULTS:  PASSED=" + passed + "  FAILED=" + failed
            + "  TOTAL=" + (passed + failed));
        double pct = (passed * 100.0) / (passed + failed);
        System.out.printf("  PASS RATE: %.0f%%%n", pct);
        System.out.println("=============================================================");
    }

    // ============================================================
    // HELPERS
    // ============================================================

    static RoomDatabase freshDB() {
        RoomDatabase db = new RoomDatabase();
        // Registered students (CRC: Student entity stores studentId, name, room)
        db.addStudent(new Student("S001", "Vedant Patil",   85, "9900001111"));
        db.addStudent(new Student("S002", "Ananya Joshi",   90, "9900002222"));
        db.addStudent(new Student("S003", "Rahul Mehta",    78, "9900003333"));
        db.addStudent(new Student("S004", "Priya Shah",     92, "9900004444"));
        db.addStudent(new Student("S005", "Karan Nair",     88, "9900005555"));
        db.addStudent(new Student("S006", "Sneha Iyer",     75, "9900006666"));
        db.addStudent(new Student("S007", "Arjun Desai",    82, "9900007777"));
        db.addStudent(new Student("S008", "Meera Kulkarni", 95, "9900008888"));
        return db;
    }

    static RoomDatabase dbWithRooms() {
        RoomDatabase db = freshDB();
        // Rooms with preferenceType 1-5 (activity: checkAvailability returns these)
        db.addRoom(new Room("101", "A", 1, 1));
        db.addRoom(new Room("201", "A", 2, 2));
        db.addRoom(new Room("301", "B", 3, 3));
        db.addRoom(new Room("401", "B", 4, 4));
        db.addRoom(new Room("501", "C", 5, 5));
        return db;
    }

    static AllotmentController ctrl(RoomDatabase db) {
        return new AllotmentController(db, new NotificationService());
    }

    static void check(String tcId, String desc, boolean condition) {
        if (condition) {
            System.out.printf("  [PASS] %-7s %s%n", tcId, desc);
            passed++;
        } else {
            System.out.printf("  [FAIL] %-7s %s%n", tcId, desc);
            failed++;
        }
    }

    // ============================================================
    // ECP TESTS (7 cases)
    // Partitions based on Activity Diagram inputs + CRC validation
    // ============================================================

    static void runECPTests() {
        System.out.println("--- ECP: Equivalence Class Partitioning ---");
        System.out.println("  Input partitions follow RoomApplicationForm fields (CRC)\n");

        // ECP01 — Valid class: all inputs valid, rooms available, fee PAID
        // Activity path: fill form -> checkAvail(YES) -> rankMerit -> allot -> notify -> payment(YES) -> biometric -> ALLOTTED
        // Expected: "ALLOTTED: 301" (exact preference match on type 3)
        System.out.println("  ECP01: [Valid] All valid inputs, rooms available, fee PAID");
        try {
            String result = ctrl(dbWithRooms()).submitApplication("S001", 3, "PAID");
            check("ECP01", "All valid -> ALLOTTED", result.startsWith("ALLOTTED:"));
        } catch (Exception e) {
            check("ECP01", "All valid -> ALLOTTED", false);
        }

        // ECP02 — Invalid studentId (null)
        // Activity: form validation fails before checkAvailability even runs
        // CRC: AllotmentController "Handle exceptions (invalid input, missing student)"
        System.out.println("  ECP02: [Invalid] studentId = null");
        try {
            ctrl(dbWithRooms()).submitApplication(null, 3, "PAID");
            check("ECP02", "null studentId -> InvalidStudentException", false);
        } catch (InvalidStudentException e) {
            check("ECP02", "null studentId -> InvalidStudentException", true);
        }

        // ECP03 — Invalid studentId (not in DB / unregistered student)
        // CRC: AllotmentController "Validate complaint against student record"
        System.out.println("  ECP03: [Invalid] studentId not registered in system");
        try {
            ctrl(dbWithRooms()).submitApplication("S999", 3, "PAID");
            check("ECP03", "Unregistered student -> InvalidStudentException", false);
        } catch (InvalidStudentException e) {
            check("ECP03", "Unregistered student -> InvalidStudentException", true);
        }

        // ECP04 — Invalid roomPreference (outside range 1-5)
        // CRC: RoomApplicationForm "Validate fields are not empty" + preference range
        System.out.println("  ECP04: [Invalid] roomPreference = 7 (out of range)");
        try {
            ctrl(dbWithRooms()).submitApplication("S001", 7, "PAID");
            check("ECP04", "pref=7 -> InvalidPreferenceException", false);
        } catch (InvalidPreferenceException e) {
            check("ECP04", "pref=7 -> InvalidPreferenceException", true);
        }

        // ECP05 — Rooms NOT available (empty DB)
        // Activity Diagram: Decision "Rooms Available? NO" -> addToWaitlist -> sendWaitlistNotice
        System.out.println("  ECP05: [Valid-edge] No rooms available -> WAITLISTED");
        try {
            RoomDatabase db = freshDB(); // no rooms added
            String result = ctrl(db).submitApplication("S001", 3, "PAID");
            check("ECP05", "No rooms -> WAITLISTED", "WAITLISTED".equals(result));
        } catch (Exception e) {
            check("ECP05", "No rooms -> WAITLISTED", false);
        }

        // ECP06 — Fee NOT PAID
        // Activity Diagram: Payment decision = NO -> cancelAllotment -> sendCancellationNotice
        // Room was allotted then cancelled — room should go back to VACANT
        System.out.println("  ECP06: [Valid-edge] Rooms available but fee = PENDING");
        try {
            RoomDatabase db = dbWithRooms();
            String result = ctrl(db).submitApplication("S001", 3, "PENDING");
            boolean cancelled = "ALLOTMENT_CANCELLED".equals(result);
            // Room should be back to VACANT (activity: cancel branch frees the room)
            boolean roomFreed = db.getAllRooms().stream()
                .anyMatch(r -> r.getRoomNo().equals("301") && "VACANT".equals(r.getOccupancyStatus()));
            check("ECP06", "Fee PENDING -> ALLOTMENT_CANCELLED + room freed", cancelled && roomFreed);
        } catch (Exception e) {
            check("ECP06", "Fee PENDING -> ALLOTMENT_CANCELLED", false);
        }

        // ECP07 — Multiple students, first-come-first-served allotment
        // Activity: each submitApplication is independent; second student gets next best room
        System.out.println("  ECP07: [Valid] Two students, both get different rooms");
        try {
            RoomDatabase db = dbWithRooms();
            AllotmentController c = ctrl(db);
            String r1 = c.submitApplication("S001", 3, "PAID");
            String r2 = c.submitApplication("S002", 3, "PAID"); // pref 3 taken, gets nearest
            boolean bothAllotted = r1.startsWith("ALLOTTED:") && r2.startsWith("ALLOTTED:");
            boolean differentRooms = !r1.equals(r2);
            check("ECP07", "Two students -> allotted different rooms", bothAllotted && differentRooms);
        } catch (Exception e) {
            check("ECP07", "Two students -> different rooms", false);
        }

        System.out.println();
    }

    // ============================================================
    // BVA TESTS (8 cases)
    // Boundary analysis on roomPreference (1–5) and room capacity
    // ============================================================

    static void runBVATests() {
        System.out.println("--- BVA: Boundary Value Analysis ---");
        System.out.println("  Boundaries: roomPreference [1–5], roomCapacity [0,1,2,N]\n");

        // BVA01 — roomPreference = 0 (min - 1) -> INVALID
        System.out.println("  BVA01: roomPreference = 0 (below minimum)");
        try {
            ctrl(dbWithRooms()).submitApplication("S001", 0, "PAID");
            check("BVA01", "pref=0 -> InvalidPreferenceException", false);
        } catch (InvalidPreferenceException e) {
            check("BVA01", "pref=0 -> InvalidPreferenceException", true);
        }

        // BVA02 — roomPreference = 1 (min boundary) -> VALID
        // Activity: rooms available, exact match on type 1
        System.out.println("  BVA02: roomPreference = 1 (minimum valid)");
        try {
            String r = ctrl(dbWithRooms()).submitApplication("S001", 1, "PAID");
            check("BVA02", "pref=1 -> ALLOTTED: 101", "ALLOTTED: 101".equals(r));
        } catch (Exception e) {
            check("BVA02", "pref=1 -> ALLOTTED", false);
        }

        // BVA03 — roomPreference = 5 (max boundary) -> VALID
        // Activity: rooms available, exact match on type 5
        System.out.println("  BVA03: roomPreference = 5 (maximum valid)");
        try {
            String r = ctrl(dbWithRooms()).submitApplication("S001", 5, "PAID");
            check("BVA03", "pref=5 -> ALLOTTED: 501", "ALLOTTED: 501".equals(r));
        } catch (Exception e) {
            check("BVA03", "pref=5 -> ALLOTTED", false);
        }

        // BVA04 — roomPreference = 6 (max + 1) -> INVALID
        System.out.println("  BVA04: roomPreference = 6 (above maximum)");
        try {
            ctrl(dbWithRooms()).submitApplication("S001", 6, "PAID");
            check("BVA04", "pref=6 -> InvalidPreferenceException", false);
        } catch (InvalidPreferenceException e) {
            check("BVA04", "pref=6 -> InvalidPreferenceException", true);
        }

        // BVA05 — roomCapacity = 0 (boundary: empty DB)
        // Activity Diagram: "Rooms Available? NO" branch -> WAITLISTED
        System.out.println("  BVA05: roomCapacity = 0 (no rooms in DB)");
        try {
            RoomDatabase db = freshDB();
            String r = ctrl(db).submitApplication("S001", 3, "PAID");
            boolean waitlisted = "WAITLISTED".equals(r);
            boolean inWaitlist = db.getWaitlist().contains("S001");
            check("BVA05", "0 rooms -> WAITLISTED + added to waitlist", waitlisted && inWaitlist);
        } catch (Exception e) {
            check("BVA05", "0 rooms -> WAITLISTED", false);
        }

        // BVA06 — roomCapacity = 1 (minimum rooms available)
        // Activity: exactly 1 room, allotted to student
        System.out.println("  BVA06: roomCapacity = 1 (exactly one room)");
        try {
            RoomDatabase db = freshDB();
            db.addRoom(new Room("301", "B", 3, 3));
            String r = ctrl(db).submitApplication("S001", 3, "PAID");
            check("BVA06", "1 room -> ALLOTTED: 301", "ALLOTTED: 301".equals(r));
        } catch (Exception e) {
            check("BVA06", "1 room -> ALLOTTED", false);
        }

        // BVA07 — roomCapacity = 1, second student has no room left
        // Activity: first student takes the only room; second hits "Rooms Available? NO"
        System.out.println("  BVA07: roomCapacity = 1, second student should be WAITLISTED");
        try {
            RoomDatabase db = freshDB();
            db.addRoom(new Room("301", "B", 3, 3));
            AllotmentController c = ctrl(db);
            String r1 = c.submitApplication("S001", 3, "PAID"); // gets the room
            String r2 = c.submitApplication("S002", 3, "PAID"); // no rooms left
            check("BVA07", "S001 ALLOTTED, S002 WAITLISTED",
                r1.startsWith("ALLOTTED:") && "WAITLISTED".equals(r2));
        } catch (Exception e) {
            check("BVA07", "1 room: first allotted, second waitlisted", false);
        }

        // BVA08 — feeStatus = "PAID" (exact boundary) vs null (missing)
        // Activity: payment decision YES vs null treated as NO
        System.out.println("  BVA08: feeStatus = null (missing payment)");
        try {
            RoomDatabase db = dbWithRooms();
            String r = ctrl(db).submitApplication("S001", 3, null);
            check("BVA08", "feeStatus=null -> ALLOTMENT_CANCELLED", "ALLOTMENT_CANCELLED".equals(r));
        } catch (Exception e) {
            check("BVA08", "feeStatus=null -> ALLOTMENT_CANCELLED", false);
        }

        System.out.println();
    }
}
