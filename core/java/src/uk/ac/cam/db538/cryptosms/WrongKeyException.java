package uk.ac.cam.db538.cryptosms;

/**
 * Exception saying that decryption couldn't have been
 * finished, because the key doesn't fit.
 */
public class WrongKeyException extends Exception {
	private static final long serialVersionUID = 5669112745854309856L;

	public WrongKeyException() {
		super();
	}
	
	public WrongKeyException(String message) {
		super(message);
	}
}
