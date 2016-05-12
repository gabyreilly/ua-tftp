package greilly.tftp;

import greilly.tftp.messages.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @date May 2016
 *
 * Test round-trip parsing of Message classes
 */
public class TftpMessageFactoryTest {

	@Test
	public void testAckMessage() throws Exception {
		short block = 100;
		AckMessage ackMessage = new AckMessage(block);

		TftpMessage tftpMessage = TftpMessageFactory.getMessage(ackMessage.toBytes());

		assertTrue(tftpMessage instanceof AckMessage);
		assertEquals(block, ((AckMessage)tftpMessage).getBlock());

	}

	@Test
	public void testDataMessage() throws Exception {
		short block = 100;
		byte[] data = new byte[]{0, 1, 2, 3};

		DataMessage dataMessage = new DataMessage(block, data);

		TftpMessage tftpMessage = TftpMessageFactory.getMessage(dataMessage.toBytes());

		assertTrue(tftpMessage instanceof  DataMessage);
		assertEquals(block, ((DataMessage)tftpMessage).getBlock());
		Assert.assertArrayEquals(data, ((DataMessage) tftpMessage).getData());
	}

	@Test
	public void testErrorMessage() throws Exception {
		ErrorMessage.ErrorCode errorCode = ErrorMessage.ErrorCode.GENERAL;
		String messageString = "General Error";

		ErrorMessage errorMessage = new ErrorMessage(errorCode, messageString);

		TftpMessage tftpMessage = TftpMessageFactory.getMessage(errorMessage.toBytes());

		assertTrue(tftpMessage instanceof  ErrorMessage);
		assertEquals(errorCode, ((ErrorMessage)tftpMessage).getErrorCode());
		assertEquals(messageString, ((ErrorMessage)tftpMessage).gerErrorMessage());
	}

	@Ignore
	@Test
	public void testStartRequestMessage() throws Exception {
		//Not yet implemented
	}


}