package greilly.tftp;

import greilly.tftp.messages.AckMessage;
import greilly.tftp.messages.DataMessage;
import greilly.tftp.messages.TftpMessage;
import greilly.tftp.messages.TftpMessageFactory;
import org.junit.Test;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static org.junit.Assert.*;

/**
 * @date May 2016
 *
 * The TftpWriter class receives DATA over the socket, and writes it out to file.
 */
public class TftpWriterTest {

	@Test
	public void testRun() throws Exception {

		String workingDirectory = System.getProperty("user.dir");
		String fileName = workingDirectory + File.separator + "test.txt";
		int port = 4445;
		InetAddress address = InetAddress.getByName("127.0.0.1");

		new TftpWriter(port, fileName).start();


		String hello = "Hello World";

		// get a datagram socket
		try (DatagramSocket socket = new DatagramSocket()) {

			// send request with DATA message
			DataMessage dataMessage = new DataMessage((short) 1, hello.getBytes());
			byte[] buf = dataMessage.toBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
			socket.send(packet);


			// get response, should be an ACK with block 1
			packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);

			TftpMessage message = TftpMessageFactory.getMessage(packet.getData());
			assertTrue(message instanceof AckMessage);
			assertEquals(1, ((AckMessage) message).getBlock());
		}

		//Assert the output file matches the "Hello World" string
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
			String line = bufferedReader.readLine();
			assertNotNull(line);
			assertEquals(hello, line);
		}

	}



}