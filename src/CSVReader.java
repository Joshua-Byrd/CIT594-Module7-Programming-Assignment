import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * {@code CSVReader} provides a stateful API for streaming individual CSV rows
 * as arrays of strings that have been read from a given CSV file.
 *
 * @author Joshua Byrd
 */
public class CSVReader {
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 5130490650040L;
    private final CharacterReader reader;

    public CSVReader(CharacterReader reader) {
        this.reader = reader;
    }

    private int lineNumber = 1;

    private int rowNumber = 1;

    private int colNumber = 1;

    private int fieldNumber = 1;

    enum State {INITIAL, QUOTE, INNER_QUOTE, ESCAPE_QUOTE, TEXT_DATA};

    /**
     * This method uses the class's {@code CharacterReader} to read in just enough
     * characters to process a single valid CSV row, represented as an array of
     * strings where each element of the array is a field of the row. If formatting
     * errors are encountered during reading, this method throws a
     * {@code CSVFormatException} that specifies the exact point at which the error
     * occurred.
     *
     * @return a single row of CSV represented as a string array, where each
     *         element of the array is a field of the row; or {@code null} when
     *         there are no more rows left to be read.
     * @throws IOException when the underlying reader encountered an error
     * @throws CSVFormatException when the CSV file is formatted incorrectly
     */
    public String[] readRow() throws IOException, CSVFormatException {
        //reset column and field with each new line
        colNumber = 1;
        fieldNumber = 1;

        //initialize arraylist to store values
        Queue<String> values = new LinkedList<>();

        //initialize stringBuilder to build each value
        StringBuilder stringBuilder = new StringBuilder();

        //set state to initial for beginning of line
        State state = State.INITIAL;

        int currentChar = 0;

        while (true) {
            //get a character from reader
            currentChar = reader.read();
            if (currentChar == -1 && lineNumber == 1 && colNumber == 1) {
                throw new CSVFormatException(1, 1, 1, 1);
            } else if (currentChar == -1) {return null;}

            //check state of the line
            switch (state) {
                case INITIAL:
                    switch(currentChar) {
                        case('\r'):
                            break;
                        case('\n'):
                            values.add(stringBuilder.toString());
                            //increment field every time one is added
                            fieldNumber++;
                            stringBuilder = new StringBuilder();
                            //increment line each time there's a newline character
                            lineNumber++;
                            rowNumber++;
                            return values.toArray(new String[values.size()]);
                        case(','):
                            //add empty string if a comma is found initially
                            values.add("");
                            fieldNumber++;
                            break;
                        case('"'):
                            //change state if a double quote is encountered
                            state = State.QUOTE;
                            break;
                        default:
                            //otherwise, append character to stringBuilder
                            stringBuilder.append((char)currentChar);
                            state = State.TEXT_DATA;
                            break;
                    }
                    break;
                case TEXT_DATA:
                    switch(currentChar) {
                        case('\r'):
                            break;
                        case('\n'):
                            values.add(stringBuilder.toString());
                            fieldNumber++;
                            stringBuilder = new StringBuilder();
                            lineNumber++;
                            rowNumber++;
                            return values.toArray(new String[values.size()]);
                        case(','):
                            //if a comma is reached, add the word to the list and reset stringBuilder and state
                            values.add(stringBuilder.toString());
                            fieldNumber++;
                            stringBuilder = new StringBuilder();
                            state = State.INITIAL;
                            break;
                        case('"'):
                            throw new CSVFormatException(lineNumber, colNumber, rowNumber, fieldNumber);
                        default:
                            stringBuilder.append((char)currentChar);
                            break;
                    }
                    break;
                case QUOTE:
                    switch(currentChar) {
                        case('\r'):
                            break;
                        case('\n'):
                            stringBuilder.append((char)currentChar);
                            lineNumber++;
                            break;
                        //if state is QUOTE and another QUOTE is encountered, change state to INNER_QUOTE,
                        //otherwise add the current character.
                        case('"'):
                            state = State.ESCAPE_QUOTE;
                            break;
                        default:
                            stringBuilder.append((char)currentChar);
                            break;
                    }
                    break;
                case ESCAPE_QUOTE:
                    switch (currentChar) {
                        case ('\r'):
                            break;
                        case ('\n'):
                            values.add(stringBuilder.toString());
                            fieldNumber++;
                            stringBuilder = new StringBuilder();
                            lineNumber++;
                            rowNumber++;
                            return values.toArray(new String[values.size()]);
                        case (','):
                            values.add(stringBuilder.toString());
                            fieldNumber++;
                            stringBuilder = new StringBuilder();
                            state = State.INITIAL;
                            break;
                        case ('"'):
                            state = State.INNER_QUOTE;
                            break;
                        default:
                            throw new CSVFormatException(lineNumber, colNumber, rowNumber, fieldNumber);
                    }

                case INNER_QUOTE:
                    switch (currentChar) {
                        case('\r'):
                            break;
                        case('\n'):
                            values.add(stringBuilder.toString());
                            fieldNumber++;
                            stringBuilder = new StringBuilder();
                            lineNumber++;
                            rowNumber++;
                            return values.toArray(new String[values.size()]);
                        case(','):
                            values.add(stringBuilder.toString());
                            fieldNumber++;
                            stringBuilder = new StringBuilder();
                            state = State.INITIAL;
                            break;
                        case('"'):
                            stringBuilder.append((char)currentChar);
                            state = State.QUOTE;
                            break;
                        default:
                            throw new CSVFormatException(lineNumber, colNumber, rowNumber, fieldNumber);
                    }
                    break;
            }

            //increment column number
            colNumber++;

        }
    }

    /**
     * Feel free to edit this method for your own testing purposes. As given, it
     * simply processes the CSV file supplied on the command line and prints each
     * resulting array of strings to standard out. Any reading or formatting errors
     * are printed to standard error.
     *
     * @param args command line arguments (1 expected)
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("usage: CSVReader <filename.csv>");
            return;
        }

        /*
         * This block of code demonstrates basic usage of CSVReader's row-oriented API:
         * initialize the reader inside try-with-resources, initialize the CSVReader
         * using the reader, and repeatedly call next() until null is encountered. Since
         * CharacterReader implements AutoCloseable, the reader will be automatically
         * closed once the try block is exited.
         */
        var filename = args[0];
        try (var reader = new CharacterReader(filename)) {
            var csvReader = new CSVReader(reader);
            String[] row;
            while ((row = csvReader.readRow()) != null) {
                System.out.println(Arrays.toString(row));
            }
        } catch (IOException | CSVFormatException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }


    }

}
