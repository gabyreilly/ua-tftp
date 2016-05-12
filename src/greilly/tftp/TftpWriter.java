package greilly.tftp;

import greilly.tftp.messages.*;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @date May 2016
 *
 * Listens on a port given in the constructor.
 * Will write to a file path given in the constructor.
 * When the other communicator sends Datagrams over the port,
 * we expect those Datagrams to be DATA type.
 * If so, we write the data to file.
 *
 * Terminates when the DATA has a length < 512 (including 0)
 * or when an unexpected packet is received.
 */
public class TftpWriter extends Thread {
	private int port;

	private String filePath;

	public TftpWriter(int port, String filePath) {
		this.port = port;
		this.filePath = filePath;
	}

	@Override
	public void run() {
		DatagramSocket socket = null;
		BufferedOutputStream outputStream = null;
		short latestBlock = 0;
		try {
			socket = new DatagramSocket(port);
			outputStream = new BufferedOutputStream(new FileOutputStream(filePath));

			while (true) {
				byte[] buffer = new byte[516]; //We only have to receive DATA here, which is max length 516

				//Receive request from socket
				DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
				socket.receive(receivedPacket);

				//System.out.println("Received in buffer!: " + new String(receivedPacket.getData()));
				//Convert the packet to one of our known TftpMessage types
				TftpMessage message = TftpMessageFactory.getMessage(receivedPacket.getData());

				if (message instanceof DataMessage){
					//We expect a DataMessage here, first check its block
					DataMessage dataMessage = (DataMessage) message;
					if (dataMessage.getBlock() == latestBlock + 1) {
						//This is the right block
						outputStream.write(dataMessage.getData(), 0, dataMessage.getData().length);

						//Send an ACK back over the socket echoing the block number
						AckMessage ackMessage = new AckMessage(dataMessage.getBlock());
						DatagramPacket ackPacket = new DatagramPacket(ackMessage.toBytes(),
																	  ackMessage.toBytes().length,
																	  receivedPacket.getAddress(),
																	  receivedPacket.getPort());
						socket.send(ackPacket);

						latestBlock++;
						//If this was the terminal packet, return (which closes streams and socket in finally)
						if (dataMessage.shouldTerminate()){
							return;
						}
					} else if (dataMessage.getBlock() == latestBlock){
						//This is a repeat send from client, ignore
					} else {
						//Some other number, send an error message, but this is not fatal
						ErrorMessage errorMessage = new ErrorMessage(ErrorMessage.ErrorCode.UNKNOWN_ID,
																	 "Unexpected block ID: " + dataMessage.getBlock());
						DatagramPacket errorPacket = new DatagramPacket(errorMessage.toBytes(), errorMessage.toBytes().length);
						socket.send(errorPacket);
					}
				} else {
					//All other message types are unsupported
					ErrorMessage errorMessage = new ErrorMessage(ErrorMessage.ErrorCode.ILLEGAL_OPERATION,
																 "Only DATA packets are supported, received " + message.getClass() + "instead.");

					DatagramPacket errorPacket = new DatagramPacket(errorMessage.toBytes(), errorMessage.toBytes().length);
					socket.send(errorPacket);
				}

				//System.out.println("Message has class " + message.getClass());
				//outputStream.write(receivedPacket.getData(), 0, receivedPacket.getLength());
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
			System.out.println("Closing stream and socket from TftpWriter");
			if (null != outputStream){
				try {
					outputStream.close();
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
