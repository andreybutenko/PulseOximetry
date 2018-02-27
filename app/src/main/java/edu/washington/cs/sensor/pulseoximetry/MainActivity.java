package edu.washington.cs.sensor.pulseoximetry;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    NfcAdapter mNfcAdapter;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_measure:
                    switchToMeasureFragment();
                    return true;
                case R.id.navigation_history:
                    switchToHistoryFragment();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchToMeasureFragment();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mNfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(this, intent, null, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        mNfcAdapter.getDefaultAdapter(this).disableForegroundDispatch(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("MAIN", "Got a new intent!");
        Log.d("MAIN", String.valueOf(intent));
        setIntent(intent);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content);

        Log.d("MAIN", "fragment check? " + (fragment instanceof MeasureFragment));
        Log.d("MAIN", "action check? " + (NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())));

        if(fragment instanceof MeasureFragment && NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())) {
            ((MeasureFragment) fragment).setStatus("Tag detected");

            Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            MeasurementAsyncTask measurementAsyncTask = new MeasurementAsyncTask((MeasureFragment) fragment, tag);
            measurementAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void switchToMeasureFragment() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, new MeasureFragment()).commit();
    }

    public void switchToHistoryFragment() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, new HistoryFragment()).commit();
    }
}
