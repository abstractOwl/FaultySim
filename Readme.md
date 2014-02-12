# FaultySim
Simulates the effect of network packet loss on streaming uncompressed audio

## Usage

	java Simulator <inFile> <outFile> <percent> <packet_size> [<mode>]
		
* **<inFile>**: Input file. Should be a Sun `.au` audio file. At this point, does not parse header to ensure it is an `.au` file.

* **<outFile>**: Output file

* **<packet_size>**: Simulated packet size

* **<percent>**: Simulated success rate

* **<mode>**: Optional, can be "silent" or "repeat". The technique used to fill in lost segments


## Future Work

Use Java AudioFileFormat API for file IO. See http://docs.oracle.com/javase/tutorial/sound/converters.html