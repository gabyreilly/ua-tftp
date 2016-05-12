package greilly.tftp.messages;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @date May 2016
 *
 * Describes an ACK request, which acknowledges
 * receipt of the previous packet.
 *
 */
public class AckMessage implements TftpMessage{
	private final short block;

	/**
	 * Used when creating an ACK message explicitly in code
	 *
	 * @param block
	 */
	public AckMessage(short block){
		this.block = block;
	}

	/**
	 * Used when parsing an ACK message from the bytes in the Datagram
	 *
	 * @param byteBuffer
	 */
	public AckMessage(ByteBuffer byteBuffer){
		//We are already at position 2, Opcode has already been removed by the Factory
		//Next is the short for the block id
		this.block = byteBuffer.getShort();
	}

	public short getBlock(){
		return this.block;
	}

	@Override
	public short getOpcode() {
		return (short)4;
	}

	@Override
	public byte[] toBytes() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(4);
		byteBuffer.putShort(getOpcode());
		byteBuffer.putShort(block);

		byteBuffer.clear(); //Back to position 0
		byte[] ret = new byte[byteBuffer.capacity()];
		byteBuffer.get(ret, 0, ret.length);
		return ret;
	}




}
