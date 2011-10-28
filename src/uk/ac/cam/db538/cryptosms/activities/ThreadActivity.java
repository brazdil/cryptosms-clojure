package uk.ac.cam.db538.cryptosms.activities;

import uk.ac.cam.db538.cryptosms.R;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import greendroid.app.GDActivity;
import greendroid.graphics.drawable.ActionBarDrawable;
import greendroid.widget.ActionBarItem;
import greendroid.widget.NormalActionBarItem;
import greendroid.widget.ActionBarItem.Type;

public class ThreadActivity extends GDActivity {
	
	private ActionBarItem mItemEncryption;
	private ActionBarDrawable mDrawableEncryptionActive, mDrawableEncryptionInactive;

	private boolean mActive = true;
	private void updateEncryptionItem(boolean active) {
		mItemEncryption.setDrawable(
			active ? mDrawableEncryptionActive : mDrawableEncryptionInactive);
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("David Brazdil");
        setActionBarContentView(R.layout.thread);
        
        addActionBarItem(Type.Info, R.id.action_bar_info);
        mItemEncryption = addActionBarItem(getActionBar().newActionBarItem(NormalActionBarItem.class), R.id.action_bar_encryption);

        mDrawableEncryptionActive = new ActionBarDrawable(this, R.drawable.encryption_active);
        mDrawableEncryptionInactive = new ActionBarDrawable(this, R.drawable.encryption_inactive, Color.RED, Color.RED);
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		updateEncryptionItem(mActive);
	}

    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
        switch (item.getItemId()) {
            case R.id.action_bar_encryption:
            	mActive = !mActive;
            	updateEncryptionItem(mActive);
                return true;

            default:
                return super.onHandleActionBarItemClick(item, position);
        }
    }
}
