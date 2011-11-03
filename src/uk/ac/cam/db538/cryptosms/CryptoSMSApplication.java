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
		
		String storageDir = getApplicationContext().getFilesDir().getAbsolutePath() + "/";
		String storageFile = storageDir + "storage.dat";
		String storageJournal = storageDir + "journal.dat";
		EncryptedStorage storageCrypto = new EncryptedStorage(storageFile, storageJournal, new CryptoKey(new byte[16]));
		storageCrypto.close();
	}
    
}
