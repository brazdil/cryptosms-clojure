package uk.ac.cam.db538.cryptosms.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

//import org.spongycastle.crypto.BlockCipher;
//import org.spongycastle.crypto.engines.AESFastEngine;
//import org.spongycastle.crypto.modes.CBCBlockCipher;
//import org.spongycastle.crypto.params.KeyParameter;
//import org.spongycastle.crypto.params.ParametersWithIV;

import uk.ac.cam.db538.cryptosms.low_level.export.ExportableType;
import uk.ac.cam.db538.cryptosms.utils.Numeric;

public class BouncyCastle  {
	
	private static SecureRandom sRandom = null;
	
	private void foo() {
		uk.ac.cam.db538.cryptosms.low_level.export.ExportableType type = null;
	}
	
//	private SecureRandom getRandom() {
//		if (sRandom == null)
//			try {
//				sRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
//			} catch (NoSuchAlgorithmException e) {
//				throw new RuntimeException(e);
//			} catch (NoSuchProviderException e) {
//				throw new RuntimeException(e);
//			}
//		return sRandom;
//	}
//	
//	private byte[] getRandomBytes(int length) {
//		byte[] random = new byte[length];
//		getRandom().nextBytes(random);
//		return random;
//	}
//	
//	private byte[] getOutcome(BlockCipher cipher, byte[] data) {
//		int lengthExpected = Numeric.leastGreaterMultiple(data.length, cipher.getBlockSize());
//		byte[] result = new byte[lengthExpected];
//		int lengthProcessed = cipher.processBlock(data, 0, result, 0);
//		
//		if (lengthExpected != lengthProcessed)
//			throw new RuntimeException("Block cipher produced unexpected data");
//		
//		return result;
//	}

//	@Override
//	public byte[] encryptAES_CBC(byte[] data, SymmetricKey key) {
//		AESFastEngine aes = new AESFastEngine();
//		CBCBlockCipher cbc = new CBCBlockCipher(aes);
//		
//		cbc.init(true, new ParametersWithIV(new KeyParameter(key.getBytes()), getRandomBytes(cbc.getBlockSize())));
//		return getOutcome(cbc, data);
//		return null;
//	}
}
