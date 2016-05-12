package greilly.tftp;

import greilly.tftp.messages.AckMessage;
import greilly.tftp.messages.DataMessage;
import greilly.tftp.messages.TftpMessage;
import org.junit.Test;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @date May 2016
 *
 * Tests TftpReader and TftpWriter on a file that requires multiple packets
 */
public class TftpReadWriteTest {
	@Test
	public void testLargerFile() throws Exception {

		int port = 4447;

		String workingDirectory = System.getProperty("user.dir");

		String inputFileName = workingDirectory + File.separator + "largeWriterInput.txt";
		String outputFileName = workingDirectory + File.separator + "largeWriterOutput.txt";

		File testFile = new File(inputFileName);
		testFile.createNewFile();

		//try with resource and write to the file
		try (FileWriter fileWriter = new FileWriter(inputFileName)) {
			for (int i = 0; i < 100; i++) {
				fileWriter.write("Line " + i + "\n");
			}
		}
		//Spin up a thread for the writer

		new TftpWriter(port, outputFileName).start();


		InetAddress address = InetAddress.getByName("127.0.0.1");


		//Run TftpReader in this thread
		new TftpReader(port, address, inputFileName).run();

		/*
		short currentBlock = 1;
		try (DatagramSocket socket = new DatagramSocket(); //No port, writing first
			 BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(inputFileName))){

			while (true) {
				byte[] fileBuffer = new byte[512];

				//Create DATA packet from file
				//return from inputStream.read is number of bytes read
				int readCode = inputStream.read(fileBuffer, 0, fileBuffer.length);
				DataMessage dataMessage = new DataMessage(currentBlock, fileBuffer);
				//Send DATA packet to socket
				DatagramPacket dataPacket = new DatagramPacket(dataMessage.toBytes(),
															   dataMessage.toBytes().length,
															   address,
															   port);
				socket.send(dataPacket);

				//Receive request from socket
				DatagramPacket receivedPacket = new DatagramPacket(fileBuffer, fileBuffer.length);
				socket.receive(receivedPacket);

				TftpMessage receivedMessage = TftpMessageFactory.getMessage(receivedPacket.getData());

				//Add some assertions to the code from TftpReader
				assertTrue(receivedMessage instanceof AckMessage);
				assertEquals(currentBlock, ((AckMessage)receivedMessage).getBlock());

				currentBlock++;

				//If the last buffer was not full, we are done
				if (readCode < 512) {
					break;
				}

			}
		}
		*/

		//Assert the output file matches the input file
		try (BufferedReader bufferedInputFile = new BufferedReader(new FileReader(inputFileName));
			 BufferedReader bufferedOutputFile = new BufferedReader(new FileReader(outputFileName))) {

			String inputFileLine;
			while (null != (inputFileLine = bufferedInputFile.readLine())) {
				String outputFileLine = bufferedOutputFile.readLine();
				assertNotNull(outputFileLine);
				assertEquals(inputFileLine, outputFileLine);
			}
		}

	}

	@Test
	public void testEvenPacketFile() throws Exception {
		//To implement: Try an input file with a multiple of 512 bytes to ensure a zero-length packet is sent at the end
	}
}
