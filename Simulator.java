import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * Simulates the effect of network loss on streaming audio.
 * @author AbstractOwl
 */
public class Simulator {
  private static final int HEADER_LENGTH = 40;
  
  private final Mode   mode;
  private final int    packetSize;
  private final int    percent;
  private final Random random;
  
  private byte[] last;
  
  public enum Mode { SILENT, REPEAT };
  
  /**
   * Creates an instance of the Simulator object.
   * @param mode Strategy used to replace lost packets 
   * @param percent Percentage of packets sent successfully
   */
  public Simulator(Mode mode, int packetSize, int percent) {
    this.mode       = mode;
    this.packetSize = packetSize; 
    this.percent    = percent;

    last     = new byte[packetSize];
    random   = new Random();
  }
  
  /**
   * Returns the input byte array or a computed fake byte array based on the
   * specified probability.
   * @param buffer Byte array to be computed on
   * @return Resulting byte array
   */
  private byte[] applyLoss(byte[] buffer) {
    int randomNumber = random.nextInt(100) + 1; // Always loss if % = 0
    
    if (randomNumber <= percent) {
      if (mode == Mode.REPEAT) System.arraycopy(buffer, 0, last, 0, buffer.length);
      return buffer;
    }
    
    // Simulate packet loss/compute fake byte
    byte[] falseByte = new byte[buffer.length];
    switch (mode) {
    case REPEAT:
      System.arraycopy(last, 0, falseByte, 0, last.length);
      break;
    default:
      Arrays.fill(falseByte, (byte) 0);
    }
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
    
    try {
      // Read header
      int bytesRead = 0;
      while ((next = in.read()) != -1 && bytesRead < HEADER_LENGTH) {
        out.write((byte) next);
        bytesRead++;
      }

      byte[] buffer = new byte[packetSize];
      while (in.read(buffer) != -1) {
        byte[] faultyByte = applyLoss(buffer);
        out.write(faultyByte);
      }
    } catch (IOException e) {
      throw new RuntimeException("Error while reading file.", e);
    }
  }
  
  /**
   * Prints the usage information to System.err.
   */
  public static void usage() {
    System.err.println("USAGE: java Simulator <infile> <outfile> <packet_size> <percent> [<mode>]");
    System.err.println("Options:");
    System.err.println("  infile:      Input *.au file");
    System.err.println("  outfile:     Output *.au file");
    System.err.println("  packet_size: Simulated packet size");
    System.err.println("  percent:     Simulated success rate [0-100]");
    System.err.println("  mode:        Optional (\"silent\" or \"repeat\"). Technique used to fill in lost segments.");
    System.exit(-1);
  }
  
  public static void main(String[] args) {
    if (args.length != 4 && args.length != 5) {
      usage();
    }
    
    String inFile     = args[0];
    String outFile    = args[1];
    int    packetSize = Integer.parseInt(args[2], 10);
    int    percent    = Integer.parseInt(args[3], 10);
    
    Mode mode = (args.length == 5 && args[4].equalsIgnoreCase("repeat"))
        ? Mode.REPEAT : Mode.SILENT;
    
    // Check arguments
    if (percent < 0 || percent > 100) {
      usage();
    }
    
    long start = System.currentTimeMillis();
    System.out.print("Processing file... ");

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
    Simulator simulator = new Simulator(mode, packetSize, percent);
    simulator.processFile(input, output);
    
    long elapsed = System.currentTimeMillis() - start;
    System.out.println("done.");
    System.out.println("Task completed in " + elapsed + "ms.");

    // Finalize objects
    try {
      input.close();
      output.close();
    } catch (IOException e) {
      throw new RuntimeException("Error while closing resources.", e);
    }
  }
}
