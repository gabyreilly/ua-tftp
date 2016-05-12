package greilly.tftp.messages;

import greilly.tftp.messages.*;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @date May 2016
 *
 * Given the byte array from the Datagram,
 * create a TftpMessage based on the opcode in the first two bytes
 */
public class TftpMessageFactory {

	public static TftpMessage getMessage(byte[] data) {

		//NOTE: this trims the trailing 0 that is specified in RRQ, WRQ, and ERROR blocks
		// so, don't depend on it for parsing
		data = trimBytes(data);
		ByteBuffer byteBuffer = ByteBuffer.wrap(data);

		short opcode = byteBuffer.getShort();

		switch (opcode){
			case 1:
			case 2:
				return new StartRequestMessage(byteBuffer);
			case 3:
				return new DataMessage(byteBuffer);
			case 4:
				return new AckMessage(byteBuffer);
			case 5:
				return new ErrorMessage(byteBuffer);
			default:
				throw new RuntimeException("Could not understand opcode " + opcode);
		}
	}

	/**
	 * Remove trailing zeros from the byte array
	 *
	 * @param bytes
	 * @return
	 */
	private static byte[] trimBytes(byte[] bytes){
		int i = bytes.length - 1;
		while (i >= 0 ){
			byte atIndex = bytes[i];
			if (atIndex != 0){
				//We found the last non-zero, stop!
				break;
			}
			i--;
		}

		return Arrays.copyOfRange(bytes, 0, i + 1); //Include the last non-zero. Second param is exclusive.
		//Corner case: If the entire array is zero, the range here will be 0 to 1 exclusive.
	}

	/*
	Notes:
	Should each message be an implementor of some interface or should i use one class & only set relevant fields?

	The interface may be very light because there aren't that many attributes or behaviors that are shared between the messages
	Using an interface means I need a factory method that takes a byte array and returns the right "shape" of object
	However the calling code may need to use instanceOf and then (cast) to get to the desired methods
	Excessive use of instanceOf and cast are code smells to me, but what is the alternative?

	Creating one class with all the possible fields (opcode, filename, block number, data, model, error code, error msg)
	 is tempting so I can avoid instanceOf and cast.  But, the calling code would still have to make decisions based on the
	 opcode, and then it would use some of the other fields to carry out the read/write actions.  But, if we
	 arent using classes, those fields arent guaranteed to be there and we don't have the compiler to help us manage that
	 type of error that might arise in the future.
	 */

}
