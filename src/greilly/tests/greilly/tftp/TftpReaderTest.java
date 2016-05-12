package greilly.tftp;

import greilly.tftp.messages.DataMessage;
import greilly.tftp.messages.TftpMessage;
import greilly.tftp.messages.TftpMessageFactory;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static org.junit.Assert.*;

/**
 * @date May 2016
 *
 * The TftpReader reads from a file and sends DATA over the socket
 *
 */
public class TftpReaderTest {

	@Test
	public void testRun() throws Exception {
		String testString = "Hello world";

		int port = 4446;
		InetAddress address = InetAddress.getByName("127.0.0.1");


		String workingDirectory = System.getProperty("user.dir");

		String inputFilePath = workingDirectory + File.separator + "readerInput.txt";

		//The Reader reads from a file on the file system, go ahead and create it
		File testFile = new File(inputFilePath);
		testFile.createNewFile();

		FileWriter fileWriter = new FileWriter(inputFilePath);
		fileWriter.write(testString);
		fileWriter.close();

		//Funny flow here -- start the listening socket, then start the TftpReader
		// Must start the listener in a thread because it waits on socket.receive()
		ListenOnSocketThread listenOnSocketThread = new ListenOnSocketThread(port);
		Thread socketThread = new Thread(listenOnSocketThread);
		socketThread.start();

		//Start the Reader thread who is going to write to that same port
		new TftpReader(port, address, inputFilePath).start();

		//Join the thread
		socketThread.join();
		//The reader sends a DATA packet
		TftpMessage message = TftpMessageFactory.getMessage(listenOnSocketThread.datagramPacket.getData());

		assertTrue(message instanceof DataMessage);

		String receivedString =  new String(((DataMessage)message).getData());

		assertEquals(testString, receivedString);

	}

	public class ListenOnSocketThread implements Runnable {
		private int port;

		public DatagramPacket datagramPacket;

		public ListenOnSocketThread(int port){
			this.port = port;
		}

		@Override
		public void run() {
			//The reader sends a DATA packet
			byte[] buffer = new byte[516];
			datagramPacket = new DatagramPacket(buffer, buffer.length);

			try (DatagramSocket socket = new DatagramSocket(port)) {
				socket.receive(datagramPacket);
			} catch (IOException e) {
				e.printStackTrace();  // TODO
			}

		}
	}
}