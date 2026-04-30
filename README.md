# SE_LabExam
# Room Allotment Subsystem

## How to Run

### 1. Put all `.java and .class` files in one folder

### 2. Compile
```bash
javac *.java
```

### 3. Run Black Box Tests (ECP + BVA)
```bash
java BlackBoxTests
```

### 4. Run White Box Tests (Path + Branch + Statement Coverage)
```bash
java WhiteBoxTests
```

### Expected Output
Both test runners print `[PASS]` / `[FAIL]` per test and a final summary:
```
RESULTS:  PASSED=15  FAILED=0  TOTAL=15
PASS RATE: 100%
```

---

## Requirements
- Java 8 or higher (`java -version` to check)
- No external libraries needed
