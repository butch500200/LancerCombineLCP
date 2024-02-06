# CombineLCP Application

The CombineLCP application is a CLI tool that generates an output .lcp file which combines all the .lcp files in the given input folder. This is particularly useful in situations where you want to combine multiple .lcp files into one.

## Executing the application

The application can be executed in two ways:

### 1. Command Line Execution

To execute the CombineLCP application from the command line, navigate to the directory containing the `combinelcp.exe` file and execute the following command:
./combinelcp.exe <input folder>  <output .lcp filename>
Replace `<input folder>` with the path to the directory holding your .lcp files and `<output .lcp filename>` with the desired name of the output file.

### 2. Double Click Execution

If you double-click the `combinelcp.exe` file to execute it without passing any arguments, the tool will use the folder that the .exe is located in as the input directory and will create an output .lcp file with the default name of `modpack.lcp`.

**Note**: In both methods, the output filename will overwrite the name displayed in CompCon, so if you rely on CompCon to identify which .lcp a specific item is from, this tool won't help with that.

## Built with

- Quarkus
- GraalVM