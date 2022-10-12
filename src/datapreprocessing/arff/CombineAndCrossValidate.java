package datapreprocessing.arff;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 
 */

/**
 * This class can be used to take a series of ARFF files that have the same headers,
 * and combine them all to create separate train/test files for leave one out cross-validation.
 * It also creates one "All" file that contains all combined records.
 * 
 * @author Chad Williams
 *
 */
public class CombineAndCrossValidate {

	/**
	 * This method takes the specified input files, assuming all have the given header length and
	 * combines them into a single arff data file.
	 * @param inputDirectory Directory ARFF input files reside
	 * @param filenames Array of input file names
	 * @param headerLength Number of lines of ARFF header before data begins
	 * @param outputDirectory Directory combined file should be output
	 * @throws IOException Exception thrown if there is a problem reading or writing the files
	 */
  public static void writeCombinedFile(String inputDirectory, String[] filenames, int headerLength,
      String outputDirectory) throws IOException {
    // Output file name
    String outputFilename = outputDirectory + "/All.arff";
    // Output training file writer
    BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFilename, false));

    boolean firstTrainingHeader = true;
    // Loop through each file
    for (int fileIndex = 0; fileIndex < filenames.length; fileIndex++) {
      // Open the current file for reading
      BufferedReader reader =
          new BufferedReader(new FileReader(inputDirectory + "/" + filenames[fileIndex]));

      int lineNum = 1;
      // Read first line
      String line = reader.readLine();
      // Copy all contents of the current file to the appropriate location
      System.out.println("Adding " + filenames[fileIndex] + "to all file " + outputFilename);

      while (line != null) {
        if (firstTrainingHeader && (lineNum > headerLength)) {
          firstTrainingHeader = false;
        }
        if ((lineNum <= headerLength) && (!firstTrainingHeader)) {
          // Skip header on all but first
        } else {
          outputWriter.append(line + "\n");
        }
        // read next line
        line = reader.readLine();
        lineNum++;
      }
      reader.close();
    }
    outputWriter.close();
  }

  /**
   * This method will process an array of ARFF files to combine and will use
   * leave one out methodology to have the training file be based on of the combination
   * of all files except one which will be the test.  It does this for all files so
   * each file is one test file and all other files combined are a training file for that 
   * test file.  The output files are labeled Train0.arff/Test0.arff with the number
   * generated corresponding to the number of files given.
   * 
   * Note currently the files processed and header length are hard coded in this method, 
   * extension would be to parameterize these if/when needed in the future.
   * 
   * @param args
   */
  public static void main(String[] args) {
    String inputDataFileDirectory = "GeneralizedDataFiles";
    String outputDataFileDirectory = "GeneralizedOutputDataFiles";

    // Assumes 79 attributes
    int headerLength = 83;
    // Set array of data files
    String[] filenames = {"Monday-WorkingHours-Gen.pcap_ISCX.arff",
        "Tuesday-WorkingHours-Gen.pcap_ISCX.arff", "Wednesday-workingHours-Gen.pcap_ISCX.arff",
        "Thursday-WorkingHours-Morning-WebAttacks-Gen.pcap_ISCX.arff",
        "Thursday-WorkingHours-Afternoon-Infilteration-Gen.pcap_ISCX.arff",
        "Friday-WorkingHours-Morning-Gen.pcap_ISCX.arff",
        "Friday-WorkingHours-Afternoon-Ddos-Gen.pcap_ISCX.arff",
        "Friday-WorkingHours-Afternoon-PortScan-Gen.pcap_ISCX.arff"};

    BufferedReader bufReader = null;

    // This block just checks that the program can find all files
    try {
      for (int fileIndex = 0; fileIndex < filenames.length; fileIndex++) {
        System.out
            .println("Trying to read:  " + inputDataFileDirectory + "/" + filenames[fileIndex]);
        bufReader =
            new BufferedReader(new FileReader(inputDataFileDirectory + "/" + filenames[fileIndex]));
        System.out.println(bufReader.readLine());
        bufReader.close();
      }
    } catch (Exception e) {
      System.err.println("Problem reading that file");
      e.printStackTrace();
    }

    try {
      // Combine all files into an "All" file
      writeCombinedFile(inputDataFileDirectory, filenames, headerLength, outputDataFileDirectory);

      // ***** Now combine the training files *******/
      int curTestFileIndex = 7; // The index of the file that is the current "leave one out"

      // Loop through number of training/test sets to create
      for (int setIndex = 0; setIndex < filenames.length; setIndex++) {
        System.out.println(
            "@@@@@@@@@@@@@@@@@ Starting set number " + setIndex + " @@@@@@@@@@@@@@@@@@@@@");
        // Current training file name
        String trainingFilename = outputDataFileDirectory + "/Train" + setIndex + ".arff";
        // Output training file writer
        BufferedWriter trainingWriter = new BufferedWriter(new FileWriter(trainingFilename, false));
        // Current test file name
        String testFilename = outputDataFileDirectory + "/Test" + setIndex + ".arff";
        // Output test file writer
        BufferedWriter testWriter = new BufferedWriter(new FileWriter(testFilename, false));

        boolean firstTrainingHeader = true;
        // Loop through each file
        for (int fileIndex = 0; fileIndex < filenames.length; fileIndex++) {
          System.out
              .println("############### Starting file number " + fileIndex + "##################");
          // Open the current file for reading
          BufferedReader reader = new BufferedReader(
              new FileReader(inputDataFileDirectory + "/" + filenames[fileIndex]));

          int lineNum = 1;
          // Read first line
          String line = reader.readLine();
          // Copy all contents of the current file to the appropriate location
          if (fileIndex == curTestFileIndex) {
            System.out.println("Adding " + filenames[fileIndex] + "to test file " + testFilename);
          } else {
            System.out
                .println("Adding " + filenames[fileIndex] + "to training file " + trainingFilename);
          }
          while (line != null) {
            // If currently on the test file copy to the test file
            if (fileIndex == curTestFileIndex) {
              testWriter.append(line + "\n");
            } else {
              if (firstTrainingHeader && (lineNum > headerLength)) {
                firstTrainingHeader = false;
              }
              if ((lineNum <= headerLength) && (!firstTrainingHeader)) {
                // Skip header on all but first
              } else {
                trainingWriter.append(line + "\n");
              }
            }
            // read next line
            line = reader.readLine();
            lineNum++;
          }
          reader.close();
        }
        trainingWriter.close();
        testWriter.close();
        // Move current test file down one index
        curTestFileIndex--;
      }
      System.out.println("Test file creation complete");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
