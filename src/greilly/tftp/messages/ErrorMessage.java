package greilly.tftp.messages;

import java.nio.ByteBuffer;

/**
 * @date May 2016
 *
 * Describes an ERROR request,
 */
public class ErrorMessage implements TftpMessage {
	public enum ErrorCode {
		GENERAL(0),
		FILE_NOT_FOUND(1),
		ACCESS_VIOLATION(2),
		DISK_FULL(3),
		ILLEGAL_OPERATION(4),
		UNKNOWN_ID(5),
		FILE_EXISTS(6),
		NO_SUCH_USER(7);

		private short code;

		ErrorCode(int code) {
			this.code = (short) code;
		}

		public short getCode(){
			return this.code;
		}
	}

	private final ErrorCode errorCode;

	private final String errorMessage;

	/**
	 * Use this when the code is creating a new ERROR
	 * with a specific code and string
	 *
	 * @param errorCode
	 * @param errorMessage
	 */
	public ErrorMessage(ErrorCode errorCode, String errorMessage){
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Used when parsing an ERROR message from the bytes in the Datagram
	 *
	 * @param byteBuffer
	 */
	public ErrorMessage(ByteBuffer byteBuffer){
		//We are already at position 2, Opcode has already been removed by the Factory
		//Next is the short for the error code
		//In this enumeration, the code is the same as the index in the enum - Handy!
		this.errorCode = ErrorCode.values()[byteBuffer.getShort()];

		String errorMessage = "";
		while (byteBuffer.position() < byteBuffer.capacity()) {
			char c = (char)byteBuffer.get(); //getChar() is two bytes but we are only using one-byte chars
			if (c != 0){
				errorMessage += c;
			} else {
				//We found the 0
				break;
			}
		}
		this.errorMessage = errorMessage;
	}

	@Override
	public short getOpcode() {
		return (short)5;
	}

	@Override
	public byte[] toBytes() {
		byte[] errorMessageBytes = errorMessage.getBytes();

		ByteBuffer byteBuffer = ByteBuffer.allocate(4 + errorMessageBytes.length + 1);
		byteBuffer.putShort(getOpcode());
		byteBuffer.putShort(errorCode.code);
		byteBuffer.put(errorMessageBytes);
		byteBuffer.put((byte)0);

		byteBuffer.clear(); //Back to position 0
		byte[] ret = new byte[byteBuffer.capacity()];
		byteBuffer.get(ret, 0, ret.length);
		return ret;
	}

	public ErrorCode getErrorCode(){
		return this.errorCode;
	}

	public String gerErrorMessage(){
		return this.errorMessage;
	}
}
