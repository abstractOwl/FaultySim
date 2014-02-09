import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Simulates the effect of network loss on streaming audio.
 * @author AbstractOwl
 */
public class Simulator {
  private static final int HEADER_LENGTH = 40;
  
  private final Mode   mode;
  private final int    percent;
  private final Random random;
  
  private byte lastByte;
  
  public enum Mode { SILENT, REPEAT };
  
  /**
   * Creates an instance of the Simulator object.
   * @param mode Strategy used to replace lost packets 
   * @param percent Percentage of packets sent successfully
   */
  public Simulator(Mode mode, int percent) {
    this.mode     = mode;
    this.percent  = percent;

    lastByte = 0;
    random   = new Random();
  }
  
  /**
   * Returns the input byte or a computed fake byte based on the specified
   * probability.
   * @param nextByte Byte to be computed on
   * @return Resulting byte
   */
  private byte applyLoss(byte nextByte) {
    int randomNumber = random.nextInt(100) + 1; // Always loss if % = 0
    
    if (randomNumber <= percent) {
      if (mode == Mode.REPEAT) lastByte = nextByte;
      return nextByte;
    }
    
    // Simulate packet loss/compute fake byte
    byte falseByte;
    switch (mode) {
    case REPEAT: falseByte = lastByte;
    default:     falseByte = 0;
    }
    if (mode == Mode.REPEAT) lastByte = falseByte;
    return falseByte;
  }
  
  /**
   * Performs simulation on a specified Sun `.au` audio file and writes the
   * resulting data to an output stream.
   * @param in FileInputStream to read from
   * @param out FileOutputStream to write to
   */
  public void processFile(FileInputStream in, FileOutputStream out) {
    int next;
    int bytesRead = 0;
    try {
      while ((next = in.read()) != -1) {
        byte nextByte = (byte) next;
        byte faultyByte = (bytesRead < HEADER_LENGTH) ? nextByte : applyLoss(nextByte);
        out.write(faultyByte);
        bytesRead++;
      }
    } catch (IOException e) {
      throw new RuntimeException("Error while reading file.", e);
    }
  }
  
  /**
   * Prints the usage information to System.err.
   */
  public static void usage() {
    System.err.println("USAGE: java Simulator <infile> <outfile> <0-100>");
    System.exit(-1);
  }
  
  public static void main(String[] args) {
    if (args.length != 3) {
      usage();
    }
    
    String inFile  = args[0];
    String outFile = args[1];
    int    percent = Integer.parseInt(args[2], 10);
    
    // Check arguments
    if (percent < 0 || percent > 100) {
      usage();
    }

    // Initialize file objects
    FileInputStream  input;
    FileOutputStream output;
    try {
      input = new FileInputStream(inFile);
    } catch (IOException e) {
      throw new RuntimeException("Error while opening inFile.", e);
    }
    try {
      output = new FileOutputStream(outFile);
    } catch (IOException e) {
      throw new RuntimeException("Error while opening outFile.", e);
    }
    
    // Write files
    Simulator simulator = new Simulator(Mode.SILENT, percent);
    simulator.processFile(input, output);

    // Finalize objects
    try {
      input.close();
      output.close();
    } catch (IOException e) {
      throw new RuntimeException("Error while closing resources.", e);
    }
  }
}
