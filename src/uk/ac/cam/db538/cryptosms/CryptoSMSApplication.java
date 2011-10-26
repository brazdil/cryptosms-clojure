package uk.ac.cam.db538.cryptosms;

import uk.ac.cam.db538.cryptosms.activities.ListsActivity;
import greendroid.app.GDApplication;

public class CryptoSMSApplication extends GDApplication {

    @Override
    public Class<?> getHomeActivityClass() {
        return ListsActivity.class;
    }
}
