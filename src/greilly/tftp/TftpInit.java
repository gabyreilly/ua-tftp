package greilly.tftp;

/**
 * This class handles initiation of TFTP request
 * This is what receives a WRQ or RRQ request,
 * chooses a port to move to, and spins up a TftpWriter
 * or TftpReader to handle the communication
 */
public class TftpInit {

    public static void main(String[] args) {
	    //TODO:
        //Open socket to dedicated port, 69
        //Accept a WRQ or RRQ
        //Both packets:
        // Check "mode" (ascii, octet, mail)
        //RRQ:
        //Check existence of & permissions for file
        //WRQ:
        //Check permissions of folder
        //Send ACK
        //Spin up TfpWriter
    }
}
