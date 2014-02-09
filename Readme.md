# FaultySim
Simulates the effect of network packet loss on streaming audio

## Usage

		java Simulator <inFile> <outFile> <percent> <packet_size>
		
**inFile**: Input file. Should be a Sun `.au` audio file. At this point, does not parse header to ensure it is an `.au` file.
**outFile**: Output file
**percent**: Simulated success rate
**packet_size**: Simulated packet size


## TODO
* Implement packet size