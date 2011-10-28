package uk.ac.cam.db538.cryptosms.activities;

import uk.ac.cam.db538.cryptosms.R;
import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends GDActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addActionBarItem(Type.Talk, R.id.action_bar_threads);
        addActionBarItem(Type.AllFriends, R.id.action_bar_contacts);
        addActionBarItem(Type.Star, R.id.action_bar_notifications);
        
        setActionBarContentView(R.layout.main);
    }

    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
        switch (item.getItemId()) {
            case R.id.action_bar_threads:
                Toast.makeText(this, "Threads", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_bar_contacts:
                Toast.makeText(this, "Contacts", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_bar_notifications:
                Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show();
                break;
            default:
                return super.onHandleActionBarItemClick(item, position);
        }
        return true;
    }
}