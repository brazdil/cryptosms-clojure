package uk.ac.cam.db538.cryptosms;

public class CryptoKey {
	public static final int KEY_LENGTH = 16;
	
	private final byte[] mKey;
	
	public CryptoKey(byte[] key) {
		if (key.length != KEY_LENGTH)
			throw new IllegalArgumentException("CryptoKey has to be 16 bytes long.");
		mKey = key;
	}
	
	public byte[] getKey() {
		return mKey;
	}
}
