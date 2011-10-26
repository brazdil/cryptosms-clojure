package uk.ac.cam.db538.cryptosms.activities;

import uk.ac.cam.db538.cryptosms.R;
import greendroid.app.GDActivity;
import android.os.Bundle;

public class ListsActivity extends GDActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActionBarContentView(R.layout.main);
    }
}