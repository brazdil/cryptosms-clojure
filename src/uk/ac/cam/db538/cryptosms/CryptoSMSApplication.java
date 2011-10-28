package uk.ac.cam.db538.cryptosms;

import uk.ac.cam.db538.cryptosms.activities.MainActivity;
import greendroid.app.GDApplication;

public class CryptoSMSApplication extends GDApplication {

    @Override
    public Class<?> getHomeActivityClass() {
        return MainActivity.class;
    }
}
