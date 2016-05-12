package greilly.tftp;

import greilly.tftp.messages.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @date May 2016
 *
 * Listens on a port given in the constructor.
 * Reads data from the file given in the constructor.
 * When the other communicator sends Datagrams over the port, we expect
 * them to be ACK type.
 *
 * In response to each ACK, this Reader will send the next
 * DATA block from the file.
 *
 * Each DATA block will be opcode + block # + 512 bytes with the exception of the last,
 * which must be <512 (possible to send a DATA with 0 bytes)
 */
public class TftpReader extends Thread {
	private int recipientPort;

	private InetAddress recipientAddress;

	private String filePath;

	public TftpReader(int recipientPort, InetAddress recipientAddress, String filePath) {
		this.recipientPort = recipientPort;
		this.recipientAddress = recipientAddress;
		this.filePath = filePath;
	}

	@Override
	public void run() {
		//Note that Writer starts with "latestBlock" at 0 because currentBlock must be 1 higher
		short currentBlock = 1;
		DatagramSocket socket = null;
		BufferedInputStream inputStream = null;
		try {
			socket =  new DatagramSocket(); //No port, writing first
			inputStream = new BufferedInputStream(new FileInputStream(filePath));

			while (true) {
				//Create DATA packet from file
				byte[] fileBuffer = new byte[512];

				//return from inputStream.read is number of bytes read
				int readCode = inputStream.read(fileBuffer, 0, fileBuffer.length);
				DataMessage dataMessage = new DataMessage(currentBlock, fileBuffer);
				//Send DATA packet to socket
				DatagramPacket dataPacket = new DatagramPacket(dataMessage.toBytes(),
															   dataMessage.toBytes().length,
															   recipientAddress,
															   recipientPort);
				socket.send(dataPacket);

				//Receive request from socket
				DatagramPacket receivedPacket = new DatagramPacket(fileBuffer, fileBuffer.length);
				socket.receive(receivedPacket);

				//Convert the packet to one of our known TftpMessage types
				TftpMessage message = TftpMessageFactory.getMessage(receivedPacket.getData());

				if (message instanceof AckMessage){
					//We expect an AckMessage here, check to see if it has the right block
					AckMessage ackMessage = (AckMessage)message;
					//Any block lower (resent old message) or equal to the block is fine
					//Any block higher is an error
					if (ackMessage.getBlock() > currentBlock){
						ErrorMessage errorMessage = new ErrorMessage(ErrorMessage.ErrorCode.UNKNOWN_ID,
																	 "Unexpected block ID: " + dataMessage.getBlock());
						DatagramPacket errorPacket = new DatagramPacket(errorMessage.toBytes(), errorMessage.toBytes().length);
						socket.send(errorPacket);
					}
				} else {
					//All other message types are unsupported
					ErrorMessage errorMessage = new ErrorMessage(ErrorMessage.ErrorCode.ILLEGAL_OPERATION,
																 "Only ACK packets are supported, received " + message.getClass() + "instead.");

					DatagramPacket errorPacket = new DatagramPacket(errorMessage.toBytes(), errorMessage.toBytes().length);
					socket.send(errorPacket);
				}
				//If the last packet we sent was the end of the file, end the loop
				if (readCode < 512){
					return;
				}

				currentBlock ++;

			}
		} catch (Exception ex) {
			//Some Exceptions are from the socket and these will fail to send here as well
			// However, some Exceptions will may be from the write, and these can be sent over the socket.
			System.out.println("Exception: " + ex.getMessage());
			ErrorMessage errorMessage = new ErrorMessage(ErrorMessage.ErrorCode.GENERAL,
														 "Exception: " + ex.getMessage());

			DatagramPacket errorPacket = new DatagramPacket(errorMessage.toBytes(), errorMessage.toBytes().length);
			try {
				if (null != socket) {
					socket.send(errorPacket);
				}
			} catch (IOException e) {
				e.printStackTrace();  // Give up!
			}
		} finally {
			System.out.println("Closing stream and socket from TftpReader");
			if (null != inputStream){
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();  // Nothing to recover here
				}
			}
			if (null != socket) {
				socket.close();
			}
		}
	}

}
