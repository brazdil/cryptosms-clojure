package uk.ac.cam.db538.cryptosms.storage;

/**
 * Exception saying that decryption couldn't have been
 * finished, because the key doesn't fit.
 */
public class StorageFileException extends Exception {
	private static final long serialVersionUID = 5662112745854309856L;

	public StorageFileException() {
		super();
	}
	
	public StorageFileException(String message) {
		super(message);
	}
}
