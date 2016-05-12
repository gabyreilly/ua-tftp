package greilly.tftp.messages;

/**
 * @date May 2016
 *
 * Very minimal interface to require toBytes() to be implemented,
 * because each of these messages will need to be saved into the data section of the DatagramPacket.
 *
 */
public interface TftpMessage {
	public short getOpcode();

	public byte[] toBytes();

}
