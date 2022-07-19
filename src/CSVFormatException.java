/**
 * The class {@code CSVFormatException} is a {@code Exception} to indicate a
 * syntax error occurred while processing a CSV file.
 *
 * This exception contains informational fields to help track down the invalid
 * section of the input and position in the processed output around the fault.
 *
 * @author  Joshua Hanson
 * @author  Rafi Rubin
 * @see     java.lang.Exception
 */
public class CSVFormatException extends Exception {
    private static final long serialVersionUID = 3160622090049L;
    private final int line;
    private final int column;
    private final int row;
    private final int field;



    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  line the line number in the input file with the syntax error.
     * @param  column the character within the current line in the input file
     *         with the syntax error.
     * @param  row the index current row being processed.
     * @param  field the index of the current field within the current row.
     */
    public CSVFormatException(String message, int line, int column, int row, int field) {
        super((message == null ? "" : message + ": ") + "error at " + line + ", " + column + ", " + row + ", " + field);
        this.line = line;
        this.column = column;
        this.row = row;
        this.field = field;
    }

    /**
     * Constructs a new exception with a default message.
     *
     * @param  line the line number in the input file with the syntax error.
     * @param  column the character within the current line in the input file
     *         with the syntax error.
     * @param  row the index current row being processed.
     * @param  field the index of the current field within the current row.
     */
    public CSVFormatException(int line, int column, int row, int field) {
        this(null, line, column, row, field);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CSVFormatException)) return false;
        CSVFormatException that = (CSVFormatException) o;
        return line == that.line && column == that.column && row == that.row && field == that.field;
    }

    public int hashCode() {
        return java.util.Objects.hash(line, column, row, field);
    }
}
