import java.util.*;

public class SymbolTable {

    public static class Entry {
        public String banglaName;
        public String dataType;
        public String javaName;
        public int declaredLine;

        public Entry(String banglaName, String dataType, String javaName, int declaredLine) {
            this.banglaName = banglaName;
            this.dataType = dataType;
            this.javaName = javaName;
            this.declaredLine = declaredLine;
        }
    }

    private final Map<String, Entry> table = new LinkedHashMap<>();
    private int counter = 0;
    public boolean hasError = false;

    public boolean declare(String banglaName, String dataType, int line) {
        if (table.containsKey(banglaName)) {
            System.err.println("[Semantic Error] line " + line
                    + ": variable already declared: " + banglaName);
            hasError = true;
            return false;
        }

        String javaName = "v" + counter;
        counter++;

        table.put(banglaName, new Entry(banglaName, dataType, javaName, line));
        return true;
    }

    public boolean isDeclared(String banglaName) {
        return table.containsKey(banglaName);
    }

    public String typeOf(String banglaName) {
        Entry entry = table.get(banglaName);
        return entry == null ? null : entry.dataType;
    }

    public String javaName(String banglaName) {
        Entry entry = table.get(banglaName);
        return entry == null ? "UNKNOWN" : entry.javaName;
    }

    public void printTable() {
        System.out.println("\n===== SYMBOL TABLE =====");
        System.out.printf("%-25s | %-10s | %-10s | %s%n",
                "Bangla Name", "Type", "Java Name", "Line");

        if (table.isEmpty()) {
            System.out.println("(No variables declared)");
            return;
        }

        for (Entry entry : table.values()) {
            System.out.printf("%-25s | %-10s | %-10s | %d%n",
                    entry.banglaName,
                    entry.dataType,
                    entry.javaName,
                    entry.declaredLine);
        }
    }
}