# Introduction

The Grep App is a Java-based command-line application
designed to search for lines matching a specific regular expression
in a text file and output the results to another file. This app is build using Core Java,
SLF4J for logging and Maven for dependency management and build automation

# Quick Start

1. Run Locally

Clone the repository and navigate to the project directory:

git clone <repository_url>
cd core_java/grep

Build the application using Maven:

mvn clean package

Run the application:

java -jar target/grep-1.0-SNAPSHOT-shaded.jar "regex" "input_file_path" "output_file_path"

2. Run with Docker:

Build the docker image:

docker build -t grep-app .

Run the application with volume mappings:

docker run --rm \
-v /path/to/data:/usr/local/app/grep/data \
-v /path/to/output:/usr/local/app/grep/out \
grep-app "regex" "/usr/local/app/grep/data/input.txt" "/usr/local/app/grep/out/output.txt"


# Implementation

Pseudocode: process Method

process():
for each file in the directory:
read file line by line
for each line:
if line matches regex:
store line in a list
write all matching lines to the output file

# Performance issues

The current implementation loads all matching lines 
into memory before writing them to the output file, 
which can cause memory issues with large files. 
To fix this, the app should write matching lines to the file incrementally,
processing one line at a time.

# Test 

The application was tested manually using the following steps:

Prepared sample text files, including shakespeare.txt.
Ran the app locally and with Docker using various regex patterns.
Verified the output file contents matched the expected results.
Logged all warnings and errors to validate edge cases, such as invalid file paths.


# Deployment

The app is dockerized to simplify distribution and execution.
A Dockerfile is used to package the application along with its dependencies into a lightweight image.
Volumes are mapped during container runtime to allow users to specify input and output files dynamically.

# Improvement

- Optimize memory usage by processing lines one by one instead of loading them all at once.
- Add automated test cases for better regression testing.
- Improve logging by including more detailed error messages and execution details.


