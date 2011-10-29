package uk.ac.cam.db538.cryptosms;

import uk.ac.cam.db538.cryptosms.activities.MainActivity;
import uk.ac.cam.db538.cryptosms.storage.EncryptedStorage;
import greendroid.app.GDApplication;

public class CryptoSMSApplication extends GDApplication {

    @Override
    public Class<?> getHomeActivityClass() {
        return MainActivity.class;
    }

	@Override
	public void onCreate() {
		super.onCreate();
		
		String storageFile = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + "storage.dat";
		EncryptedStorage storageCrypto = new EncryptedStorage(storageFile, new CryptoKey(new byte[16]));
		storageCrypto.close();
	}
    
}
