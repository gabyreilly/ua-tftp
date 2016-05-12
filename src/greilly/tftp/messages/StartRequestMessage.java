package greilly.tftp.messages;

import java.nio.ByteBuffer;

/**
 * @date May 2016
 *
 * Captures the Read Request and Write Request messages
 *
 */
public class StartRequestMessage implements TftpMessage {
	public enum RequestType {
		READ(1), WRITE(2);

		private short code;

		RequestType(int code) {
			this.code = (short) code;
		}

		public short getCode(){
			return this.code;
		}
	}

	public RequestType requestType;

	public String fileName;

	public String mode;

	public StartRequestMessage(RequestType requestType, String fileName, String mode){
		this.requestType = requestType;
		this.fileName = fileName;
		this.mode = mode;
	}

	/**
	 * Used when parsing a DATA message from the bytes in the Datagram
	 *
	 * @param byteBuffer
	 */
	public StartRequestMessage(ByteBuffer byteBuffer){
		throw new RuntimeException("Parsing for StartRequestMessage is not yet implemented");
	}

	@Override
	public short getOpcode() {
		return requestType.getCode();
	}

	@Override
	public byte[] toBytes() {
		byte[] fileNameBytes = fileName.getBytes();
		byte[] modeBytes = mode.getBytes();

		ByteBuffer byteBuffer = ByteBuffer.allocate(2 + fileNameBytes.length + 1 + modeBytes.length + 1);
		byteBuffer.putShort(getOpcode());
		byteBuffer.put(fileNameBytes);
		byteBuffer.put((byte)0);
		byteBuffer.put(modeBytes);
		byteBuffer.put((byte)0);

		byteBuffer.clear(); //Back to position 0
		byte[] ret = new byte[byteBuffer.capacity()];
		byteBuffer.get(ret, 0, ret.length);
		return ret;
	}
}
