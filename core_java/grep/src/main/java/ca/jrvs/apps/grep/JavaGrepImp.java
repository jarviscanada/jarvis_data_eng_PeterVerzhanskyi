package ca.jrvs.apps.grep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaGrepImp implements JavaGrep {

    private static final Logger logger = LoggerFactory.getLogger(JavaGrepImp.class);

    private String regex;
    private String rootPath;
    private String outFile;

    public static void main(String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException("USAGE: JavaGrepImp [regex] [rootPath] [outFile]");
        }

        JavaGrepImp javaGrepImp = new JavaGrepImp();
        javaGrepImp.setRegex(args[0]);
        javaGrepImp.setRootPath(args[1]);
        javaGrepImp.setOutFile(args[2]);

        logger.debug("Regex: {}", javaGrepImp.getRegex());
        logger.debug("Root Path: {}", javaGrepImp.getRootPath());
        logger.debug("Output File: {}", javaGrepImp.getOutFile());

        try {
            javaGrepImp.process();
        } catch (Exception ex) {
            logger.error("Failed to execute grep", ex);
        }
    }

    @Override
    public void process() throws IOException {
        List<String> matchedLines = new ArrayList<>();
        for (File file : listFiles(rootPath)) {
            logger.debug("Processing file: {}", file.getAbsolutePath());
            for (String line : readLines(file)) {
                if (containsPattern(line)) {
                    logger.debug("Matched line: {}", line);
                    matchedLines.add(line);
                }
            }
        }

        if (matchedLines.isEmpty()) {
            logger.warn("No lines matched the given regex.");
        } else {
            logger.debug("Total matched lines: {}", matchedLines.size());
        }

        writeToFile(matchedLines);
    }

    @Override
    public List<File> listFiles(String rootDir) {
        List<File> fileList = new ArrayList<>();
        File root = new File(rootDir);
        File[] files = root.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    fileList.addAll(listFiles(file.getAbsolutePath()));
                } else {
                    fileList.add(file);
                    logger.debug("Found file: {}", file.getAbsolutePath());
                }
            }
        } else {
            logger.warn("No files found in directory: {}", rootDir);
        }

        logger.debug("Total files found: {}", fileList.size());
        return fileList;
    }

    @Override
    public List<String> readLines(File inputFile) throws IOException {
        if (!inputFile.isFile()) {
            throw new IllegalArgumentException("Not a file: " + inputFile);
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        logger.debug("Read {} lines from {}", lines.size(), inputFile.getAbsolutePath());
        return lines;
    }

    @Override
    public boolean containsPattern(String line) {
        Pattern pattern;
        try {
            pattern = Pattern.compile(regex);
        } catch (Exception e) {
            logger.error("Invalid regex pattern: {}", regex, e);
            throw e;
        }

        Matcher matcher = pattern.matcher(line);
        boolean matchFound = matcher.find();
        logger.debug("Line \"{}\" contains pattern: {}", line, matchFound);
        return matchFound;
    }

    @Override
    public void writeToFile(List<String> lines) throws IOException {
        if (lines.isEmpty()) {
            logger.warn("No lines to write to file: {}", outFile);
            return;
        }

        File output = new File(outFile);
        File parentDir = output.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                logger.error("Failed to create directory: {}", parentDir.getAbsolutePath());
                throw new IOException("Could not create directory for output file: " + parentDir.getAbsolutePath());
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }

        logger.debug("Written {} lines to {}", lines.size(), outFile);
    }


    @Override
    public String getRootPath() {
        return rootPath;
    }

    @Override
    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public String getRegex() {
        return regex;
    }

    @Override
    public void setRegex(String regex) {
        this.regex = regex;
    }

    @Override
    public String getOutFile() {
        return outFile;
    }

    @Override
    public void setOutFile(String outFile) {
        this.outFile = outFile;
    }
}
