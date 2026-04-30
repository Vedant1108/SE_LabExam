/**
 * WhiteBoxTests.java — 15 White Box Test Cases
 *
 * Mirrors the structure of BlackBoxTests.java.
 * Tests are organised by CFG path (basis paths P1–P8) and
 * additional branch/statement coverage cases (WB09–WB15).
 *
 * Control Flow Graph — submitApplication() decisions:
 *   D1 (N2) : studentId == null || trim().isEmpty()
 *   D2 (N4) : !db.studentExists(studentId)
 *   D3 (N6) : roomPreference < 1 || roomPreference > 5
 *   D4 (N9) : roomList.isEmpty()
 *   D5 (N15): !"PAID".equals(feeStatus)
 *   D6 (N18): !biometricOk
 *
 * Cyclomatic Complexity = E - N + 2P = 26 - 20 + 2 = 8
 * => 8 independent basis paths required for path coverage.
 *
 * Coverage achieved:
 *   Statement Coverage : 23/24 statements (~95.8%) — L83 structurally unreachable
 *   Branch Coverage    : 6/6 decisions, both TRUE & FALSE branches = 100%
 *   Path Coverage      : 8/8 basis paths = 100%
 *
 * Compile & run:
 *   javac *.java
 *   java WhiteBoxTests
 */
import java.util.List;

public class WhiteBoxTests {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=============================================================");
        System.out.println("  WHITE BOX TESTS — Room Allotment Subsystem");
        System.out.println("  CFG Path Coverage + Branch Coverage + Statement Coverage");
        System.out.println("=============================================================\n");

        runBasisPathTests();
        runAdditionalCoverageTests();

        System.out.println("=============================================================");
        System.out.println("  RESULTS:  PASSED=" + passed + "  FAILED=" + failed
            + "  TOTAL=" + (passed + failed));
        double pct = (passed * 100.0) / (passed + failed);
        System.out.printf("  PASS RATE: %.0f%%%n", pct);
        System.out.println("=============================================================");
    }

    // ============================================================
    // HELPERS  (same as BlackBoxTests)
    // ============================================================

    static RoomDatabase freshDB() {
        RoomDatabase db = new RoomDatabase();
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
    // BASIS PATH TESTS  (WB01 – WB08)
    // One test per independent path through the CFG.
    // Together these achieve full path coverage (CC = 8).
    // ============================================================

    static void runBasisPathTests() {
        System.out.println("--- Basis Path Tests (P1–P8) ---");
        System.out.println("  Path: N1=Entry  N2=D1  N4=D2  N6=D3  N8=checkAvail");
        System.out.println("        N9=D4  N11=rankMerit  N12=nullCheck  N14=allot");
        System.out.println("        N15=D5  N17=confirm  N18=D6  N20=return ALLOTTED\n");

        // ------------------------------------------------------------------
        // WB01 — Path P1: N1→N2→N3
        // D1 = TRUE (studentId is null)  → InvalidStudentException
        // Statements covered: L56-57
        // Branch: D1 TRUE branch (null side)
        // ------------------------------------------------------------------
        System.out.println("  WB01: [P1] D1=TRUE (null studentId) → N1→N2→N3");
        try {
            ctrl(dbWithRooms()).submitApplication(null, 3, "PAID");
            check("WB01", "null studentId -> InvalidStudentException", false);
        } catch (InvalidStudentException e) {
            check("WB01", "null studentId -> InvalidStudentException", true);
        }

        // ------------------------------------------------------------------
        // WB02 — Path P1 (alternate branch): N1→N2→N3
        // D1 = TRUE (studentId is whitespace)  → InvalidStudentException
        // Statements covered: L56-57 (trim().isEmpty() sub-condition)
        // Branch: D1 TRUE branch (empty/whitespace side)
        // ------------------------------------------------------------------
        System.out.println("  WB02: [P1-alt] D1=TRUE (whitespace studentId) → N1→N2→N3");
        try {
            ctrl(dbWithRooms()).submitApplication("   ", 3, "PAID");
            check("WB02", "whitespace studentId -> InvalidStudentException", false);
        } catch (InvalidStudentException e) {
            check("WB02", "whitespace studentId -> InvalidStudentException", true);
        }

        // ------------------------------------------------------------------
        // WB03 — Path P2: N1→N2→N4→N5
        // D1=FALSE, D2=TRUE (student not in DB) → InvalidStudentException
        // Statements covered: L56,59-60
        // Branch: D1 FALSE, D2 TRUE
        // ------------------------------------------------------------------
        System.out.println("  WB03: [P2] D1=FALSE, D2=TRUE (unregistered student) → N1→N2→N4→N5");
        try {
            ctrl(dbWithRooms()).submitApplication("S999", 3, "PAID");
            check("WB03", "unregistered student -> InvalidStudentException", false);
        } catch (InvalidStudentException e) {
            check("WB03", "unregistered student -> InvalidStudentException", true);
        }

        // ------------------------------------------------------------------
        // WB04 — Path P3 (low side): N1→N2→N4→N6→N7
        // D1=FALSE, D2=FALSE, D3=TRUE (pref < 1) → InvalidPreferenceException
        // Statements covered: L56,59,64-66
        // Branch: D3 TRUE (pref < 1)
        // ------------------------------------------------------------------
        System.out.println("  WB04: [P3] D3=TRUE (pref=0, below min) → N1→N2→N4→N6→N7");
        try {
            ctrl(dbWithRooms()).submitApplication("S001", 0, "PAID");
            check("WB04", "pref=0 -> InvalidPreferenceException", false);
        } catch (InvalidPreferenceException e) {
            check("WB04", "pref=0 -> InvalidPreferenceException", true);
        }

        // ------------------------------------------------------------------
        // WB05 — Path P3 (high side): N1→N2→N4→N6→N7
        // D3=TRUE (pref > 5) → InvalidPreferenceException
        // Statements covered: L64-66 (pref > 5 sub-condition)
        // Branch: D3 TRUE (pref > 5)
        // ------------------------------------------------------------------
        System.out.println("  WB05: [P3-alt] D3=TRUE (pref=6, above max) → N1→N2→N4→N6→N7");
        try {
            ctrl(dbWithRooms()).submitApplication("S001", 6, "PAID");
            check("WB05", "pref=6 -> InvalidPreferenceException", false);
        } catch (InvalidPreferenceException e) {
            check("WB05", "pref=6 -> InvalidPreferenceException", true);
        }

        // ------------------------------------------------------------------
        // WB06 — Path P4: N1→N2→N4→N6→N8→N9→N10
        // D4=TRUE (roomList empty) → WAITLISTED
        // Statements covered: L56,59,64,70,73-76
        // Branch: D4 TRUE
        // ------------------------------------------------------------------
        System.out.println("  WB06: [P4] D4=TRUE (no rooms in DB) → N1→…→N9→N10");
        try {
            RoomDatabase db = freshDB(); // no rooms added
            String result = ctrl(db).submitApplication("S001", 3, "PAID");
            boolean waitlisted  = "WAITLISTED".equals(result);
            boolean inWaitlist  = db.getWaitlist().contains("S001");
            check("WB06", "No rooms -> WAITLISTED + added to waitlist", waitlisted && inWaitlist);
        } catch (Exception e) {
            check("WB06", "No rooms -> WAITLISTED", false);
        }

        // ------------------------------------------------------------------
        // WB07 — Path P5: N1→…→N14→N15→N16
        // D5=TRUE (feeStatus = "PENDING") → ALLOTMENT_CANCELLED, room freed
        // Statements covered: L56,59,64,70,73,80,82,87,90,93-96
        // Branch: D5 TRUE
        // ------------------------------------------------------------------
        System.out.println("  WB07: [P5] D5=TRUE (fee PENDING) → N1→…→N15→N16");
        try {
            RoomDatabase db = dbWithRooms();
            String result = ctrl(db).submitApplication("S001", 3, "PENDING");
            boolean cancelled = "ALLOTMENT_CANCELLED".equals(result);
            // Room must be freed (VACANT) after cancel — statement L94 coverage
            boolean roomFreed = db.getAllRooms().stream()
                .anyMatch(r -> r.getRoomNo().equals("301")
                            && "VACANT".equals(r.getOccupancyStatus()));
            check("WB07", "fee PENDING -> ALLOTMENT_CANCELLED + room freed", cancelled && roomFreed);
        } catch (Exception e) {
            check("WB07", "fee PENDING -> ALLOTMENT_CANCELLED", false);
        }

        // ------------------------------------------------------------------
        // WB08 — Path P7 (full happy path): N1→N2→N4→N6→N8→N9→N11→N12→N14→N15→N17→N18→N20
        // All decisions FALSE + biometric passes → ALLOTTED
        // Statements covered: ALL 20 nodes / every statement in method
        // Branch: D1 F, D2 F, D3 F, D4 F, D5 F, D6 F
        // ------------------------------------------------------------------
        System.out.println("  WB08: [P7] Full happy path — all decisions FALSE → N1→…→N20");
        try {
            RoomDatabase db = dbWithRooms();
            String result = ctrl(db).submitApplication("S001", 3, "PAID");
            boolean allotted  = result.startsWith("ALLOTTED:");
            boolean confirmed = "301".equals(db.getConfirmedAllotment("S001"));
            boolean occupied  = db.getAllRooms().stream()
                .anyMatch(r -> r.getRoomNo().equals("301")
                            && "OCCUPIED".equals(r.getOccupancyStatus()));
            check("WB08", "Happy path -> ALLOTTED: 301 + confirmed + room OCCUPIED",
                allotted && confirmed && occupied);
        } catch (Exception e) {
            check("WB08", "Happy path -> ALLOTTED", false);
        }

        System.out.println();
    }

    // ============================================================
    // ADDITIONAL COVERAGE TESTS  (WB09 – WB15)
    // Cover remaining branches, statements and loop paths.
    // ============================================================

    static void runAdditionalCoverageTests() {
        System.out.println("--- Additional Branch / Statement / Loop Coverage Tests ---\n");

        // ------------------------------------------------------------------
        // WB09 — Path P6: N1→…→N17→N18→N19
        // D6=TRUE (biometric fails) → InvalidStudentException
        // Statements covered: L100-103
        // Branch: D6 TRUE (only branch not hit by WB08)
        // ------------------------------------------------------------------
        System.out.println("  WB09: [P6] D6=TRUE (biometric fail) → N1→…→N18→N19");
        try {
            RoomDatabase db = dbWithRooms();
            db.setBiometricFail(true);        // force verifyBiometric() → false
            ctrl(db).submitApplication("S001", 3, "PAID");
            check("WB09", "biometric fail -> InvalidStudentException", false);
        } catch (InvalidStudentException e) {
            boolean correctMsg = e.getMessage().contains("Biometric verification failed");
            check("WB09", "biometric fail -> InvalidStudentException (correct msg)", correctMsg);
        }

        // ------------------------------------------------------------------
        // WB10 — rankByMerit(): exact-match loop fires (L112-113)
        // First for-loop finds a room with getPreferenceType() == preference
        // and returns immediately — second (minDiff) loop never runs.
        // Statements covered: L112-113
        // ------------------------------------------------------------------
        System.out.println("  WB10: rankByMerit exact-match loop (L112-113) — pref=1 hits Room 101");
        try {
            String result = ctrl(dbWithRooms()).submitApplication("S001", 1, "PAID");
            check("WB10", "pref=1 -> exact match -> ALLOTTED: 101", "ALLOTTED: 101".equals(result));
        } catch (Exception e) {
            check("WB10", "pref=1 exact match", false);
        }

        // ------------------------------------------------------------------
        // WB11 — rankByMerit(): NO exact match → minDiff fallback loop (L114-120)
        // DB has only type-1 and type-5 rooms; pref=3 → no exact match →
        // second loop picks nearest by |diff| (both differ by 2; type-1 found first).
        // Statements covered: L114-120 (all lines of fallback loop)
        // ------------------------------------------------------------------
        System.out.println("  WB11: rankByMerit minDiff fallback loop (L114-120) — no exact match for pref=3");
        try {
            RoomDatabase db = freshDB();
            db.addRoom(new Room("101", "A", 1, 1));   // |1-3| = 2
            db.addRoom(new Room("501", "C", 5, 5));   // |5-3| = 2
            String result = ctrl(db).submitApplication("S001", 3, "PAID");
            // Either 101 or 501 is acceptable (same diff; implementation picks first)
            boolean allotted = result.startsWith("ALLOTTED:");
            check("WB11", "no exact match -> minDiff fallback -> ALLOTTED (nearest room)", allotted);
        } catch (Exception e) {
            check("WB11", "minDiff fallback loop", false);
        }

        // ------------------------------------------------------------------
        // WB12 — D5=TRUE via null feeStatus (statement L93 null-safe check)
        // "PAID".equals(null) → false  →  cancel branch executed
        // Statements covered: L93-96 via null input
        // Branch: D5 TRUE (feeStatus=null)
        // ------------------------------------------------------------------
        System.out.println("  WB12: D5=TRUE (feeStatus=null) — null-safe equals check → ALLOTMENT_CANCELLED");
        try {
            RoomDatabase db = dbWithRooms();
            String result = ctrl(db).submitApplication("S001", 3, null);
            boolean cancelled = "ALLOTMENT_CANCELLED".equals(result);
            boolean roomFreed = db.getAllRooms().stream()
                .anyMatch(r -> r.getRoomNo().equals("301")
                            && "VACANT".equals(r.getOccupancyStatus()));
            check("WB12", "feeStatus=null -> ALLOTMENT_CANCELLED + room freed", cancelled && roomFreed);
        } catch (Exception e) {
            check("WB12", "feeStatus=null -> ALLOTMENT_CANCELLED", false);
        }

        // ------------------------------------------------------------------
        // WB13 — D3=FALSE, pref=1 (minimum valid boundary)
        // Verifies the LOW side of D3 is correctly accepted.
        // Statements covered: L64 (condition evaluates false for pref=1)
        // Branch: D3 FALSE (pref not < 1 and not > 5)
        // ------------------------------------------------------------------
        System.out.println("  WB13: D3=FALSE boundary (pref=1, min valid) — full allotment path");
        try {
            RoomDatabase db = dbWithRooms();
            String result = ctrl(db).submitApplication("S001", 1, "PAID");
            check("WB13", "pref=1 (min valid) -> D3 FALSE -> ALLOTTED: 101",
                "ALLOTTED: 101".equals(result));
        } catch (Exception e) {
            check("WB13", "pref=1 boundary -> D3 FALSE", false);
        }

        // ------------------------------------------------------------------
        // WB14 — D3=FALSE, pref=5 (maximum valid boundary)
        // Verifies the HIGH side of D3 is correctly accepted.
        // Statements covered: L64 (condition evaluates false for pref=5)
        // Branch: D3 FALSE (pref not > 5)
        // ------------------------------------------------------------------
        System.out.println("  WB14: D3=FALSE boundary (pref=5, max valid) — full allotment path");
        try {
            RoomDatabase db = dbWithRooms();
            String result = ctrl(db).submitApplication("S001", 5, "PAID");
            check("WB14", "pref=5 (max valid) -> D3 FALSE -> ALLOTTED: 501",
                "ALLOTTED: 501".equals(result));
        } catch (Exception e) {
            check("WB14", "pref=5 boundary -> D3 FALSE", false);
        }

        // ------------------------------------------------------------------
        // WB15 — D4 both branches exercised in one test (Path P8)
        // Call 1: rooms available → D4=FALSE → room allotted to S001
        // Call 2: no rooms left  → D4=TRUE  → S002 waitlisted
        // This is the only test that exercises BOTH TRUE and FALSE of D4.
        // Statements covered: L73-76 (D4 TRUE) and L80-... (D4 FALSE) in sequence
        // Branch: D4 FALSE then D4 TRUE
        // ------------------------------------------------------------------
        System.out.println("  WB15: [P8] D4 both branches — S001 allotted, S002 waitlisted (1 room only)");
        try {
            RoomDatabase db = freshDB();
            db.addRoom(new Room("301", "B", 3, 3));   // exactly one room
            AllotmentController c = ctrl(db);
            String r1 = c.submitApplication("S001", 3, "PAID");  // D4=FALSE → ALLOTTED
            String r2 = c.submitApplication("S002", 3, "PAID");  // D4=TRUE  → WAITLISTED
            boolean s1ok = r1.startsWith("ALLOTTED:");
            boolean s2ok = "WAITLISTED".equals(r2);
            boolean s2inList = db.getWaitlist().contains("S002");
            check("WB15", "S001 -> ALLOTTED, S002 -> WAITLISTED (D4 both branches)",
                s1ok && s2ok && s2inList);
        } catch (Exception e) {
            check("WB15", "D4 both branches", false);
        }

        System.out.println();
    }
}
