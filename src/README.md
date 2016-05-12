Gaby Reilly

Partial implementation of TFTP per https://www.ietf.org/rfc/rfc1350.txt

This implementation includes the following features:

**TftpReader**

* The purpose of TftpReader is to read a file from the file system and send it over UDP to a specified address and port.  
* The TftpReader sends these in the form of DATA packets, and includes a block number with 512 bytes of the target file per packet.
* The TftpReader waits for the client to repsond with an ACK packet with the same block number that was sent.
* The TftpReader tolerates repeat ACK packets from the client

**TftpWriter**

* The purpose of TftpWriter is to receive file data over UDP and save it to a specified file on the file system.
* The TftpWriter waits for DATA packets from the client, saves them to the file system, and responds with an ACK request.
* The TftpWriter tolerates repeat DATA packets from the client

**TftpMessage interface**
* This interface specifies that each Message must implement toBytes
* The TftpMessage classes correspond to the Tftp packet types: ACK, DATA, and ERROR packets are represented by classes 
of the same name. StartRequestMessage represents RRQ and WRQ, but are not completely implemented.
* The TfpMessageFactory parses the body of the DatagramPacket (which is the class that java uses for UDP) into one of the
TftpMessage classes.
* The TftpMessage classes have two constructors.  One constructor is used when the code needs to construct a packet with 
a set block, message, data, etc.  One constructor is used when the TftpMessageFactory needs to parse a byte array into 
the fields of the class

I chose these classes to implement because the Read and Write functionality is core to Tftp functionality. 
I also thought the Factory and Interface design was going to be interesting.  

I also implemented tests for the TftpWriter, TftpReader, and the TftpMessageFactory parsing in src/greilly/tests.

I did not implement the TftpInit class, which I think would include parsing RRQ and WRQ requests, checking for invalid or 
un-writeable or un-readable file paths, any security that the service would need, and then spinning up threads for TftpReader or TftpWriter as needed.  

If I had this project to do again, I would write the TftpMessageFactoryTest earlier -- the parsing was a little tricky to 
get right, and it was time-consuming to track down those bugs in the context of the Reader and Writer.

If I were doing it again, I might choose to implement the TftpInit and one of Reader or Writer.  It was fun to do both, because 
I could do a round-trip test, but the two classes were pretty similar, and working on TftpInit instead could have been 
a good exercise.
