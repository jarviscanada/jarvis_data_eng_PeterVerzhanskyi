package ca.jrvs.apps.grep;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface JavaGrep {

    /**
     * Top-level search workflow.
     *
     * @throws IOException if an I/O error occurs
     */
    void process() throws IOException;

    /**
     * Traverse a given directory and return all files.
     *
     * @param rootDir input directory
     * @return Stream of files under the rootDir
     */
    List<File> listFiles(String rootDir) throws IOException;

    /**
     * Read a file and return all lines.
     *
     * @param inputFile file to be read
     * @return List of lines in the file
     * @throws IOException if an error occurs while reading the file
     */
    List<String> readLines(File inputFile) throws IOException;

    /**
     * Check if a line contains the regex pattern (passed by user).
     *
     * @param line input string
     * @return true if there is a match
     */
    boolean containsPattern(String line);

    /**
     * Write lines to a file.
     *
     * @param lines matched lines
     * @throws IOException if write fails
     */
    void writeToFile(List<String> lines) throws IOException;

    // Getters and setters for rootPath, regex, and output file
    String getRootPath();
    void setRootPath(String rootPath);

    String getRegex();
    void setRegex(String regex);

    String getOutFile();
    void setOutFile(String outFile);
}
