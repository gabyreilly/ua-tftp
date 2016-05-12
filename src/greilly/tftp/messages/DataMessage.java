package greilly.tftp.messages;

import java.nio.ByteBuffer;

/**
 * @date May 2016
 *
 * Describes a DATA request, where the data field is the
 * actual data that is being sent across TFTP.
 */
public class DataMessage implements TftpMessage {
	private final short block;

	private final byte[] data;

	/**
	 * Used when creating a DATA message explicitly in code
	 *
	 * @param block
	 * @param data
	 */
	public DataMessage(short block, byte[] data) {
		this.block = block;
		this.data = data;
	}

	/**
	 * Used when parsing a DATA message from the bytes in the Datagram
	 *
	 * @param byteBuffer
	 */
	public DataMessage(ByteBuffer byteBuffer){
		//We are already at position 2, Opcode has already been removed by the Factory
		//Next is the short for the block id
		this.block = byteBuffer.getShort();

		//Next is data
		byte[] ret = new byte[byteBuffer.remaining()];
		byteBuffer.get(ret, 0, ret.length);
		this.data = ret;
	}

	@Override
	public short getOpcode() {
		return (short) 3;
	}

	@Override
	public byte[] toBytes() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(4 + data.length);
		byteBuffer.putShort(getOpcode());
		byteBuffer.putShort(block);
		byteBuffer.put(data);

		byteBuffer.clear(); //Back to position 0
		byte[] ret = new byte[byteBuffer.capacity()];
		byteBuffer.get(ret, 0, ret.length);
		return ret;
	}

	public short getBlock(){
		return this.block;
	}

	public byte[] getData() {
		return this.data;
	}

	public boolean shouldTerminate() {
		return data.length < 512;
	}
}
