// SPDX-FileCopyrightText: 2022 Michael Pöhn <michael@poehn.at>
// SPDX-License-Identifier: MIT

package info.guardianproject.sample.arti;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import IPtProxy.IPtProxy;
import info.guardianproject.arti.ArtiProxy;


public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private FloatingActionButton fab;
    private ViewPropertyAnimator fabSpin;
    private Spinner spinner;
    private EditText bridgeLineInput;
    private EditText obfs4Port;
    private EditText stunServerInput;
    private EditText targetInput;
    private EditText frontInput;
    private TextView noSelection;
    private TextView connectionStatus;
    private int selectedOption;
    private TextView logOutput;
    private ScrollView logScrollView;
    private BroadcastReceiver logReceiver;
    private ConstraintLayout constraintLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        constraintLayout = findViewById(R.id.constraint_layout);

        textView = findViewById(R.id.text);
        textView.setText(getString(R.string.intro_text));

        fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(view -> {
            checkConnection();
        });
        fabSpin = fab.animate();

        spinner = findViewById(R.id.spinner);
        bridgeLineInput = findViewById(R.id.bridgeLineInput);
        stunServerInput = findViewById(R.id.stunServerInput);
        targetInput = findViewById(R.id.targetInput);
        frontInput = findViewById(R.id.frontInput);
        noSelection = findViewById(R.id.no_option_selected);
        connectionStatus = findViewById(R.id.status);
        obfs4Port = findViewById(R.id.obfs4Port);

        logOutput = findViewById(R.id.logTextView);
        logScrollView = findViewById(R.id.logScrollView);
        logReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String logMessage = intent.getStringExtra("logMessage");
                appendLog(logMessage);
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(logReceiver,
                new IntentFilter("LOG_MESSAGE"));

        ArrayAdapter<CharSequence> adapter= ArrayAdapter.createFromResource(this, R.array.connection_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedOption = position;
                onSelectionChanged(position); // get options
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(logReceiver);
    }
    private void appendLog(String text) {
        logOutput.append(text + "\n");
        ScrollView scrollView = findViewById(R.id.logScrollView);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void checkConnection() {
        switch (selectedOption){
            case 1:
                ((App)getApplication()).connectTorDirect();
                break;
            case 2:
                List<String> bridgeLines = Arrays.asList(
                        // NOTICE: you'll need to provide bridge lines to make this work!
                        //obfs4 69.235.46.22:30913 F79914011EB368C94E58F6CCF8A55A92EFD5F496 cert=ZKLm+4biqgPIf/g1s3slv8jLSzIzLSXAHFOfBLqtrNvnTM6LVbxe/K8e8jJKiXwOpvkoDw iat-mode=0
                        //obfs4 82.74.251.112:9449 628B95EEAE48758CBAA2812AE99E1AB5B3BE44D4 cert=i7tmgWvq4X2rncJz4FQsQWwkXiEWVE7Nvm1gffYn5ZlVsA0kBF6c/8041dTB4mi0TSShWA iat-mode=0
                        bridgeLineInput.getText().toString()
                );
                ((App) getApplication()).connectWithLyrebird(Integer.parseInt(obfs4Port.getText().toString()), bridgeLines);
                break;
            default:
                break;
        }
        fab.setEnabled(false);
        fabSpin.setDuration(1000*60).rotationBy((float) (1000 * 60) /4).setInterpolator(new LinearInterpolator()).start();
        connectionStatus.setVisibility(View.VISIBLE);
        connectionStatus.setText(R.string.performing_request);

        new CheckTorConnectionTask(this).execute();
    }

    private static class CheckTorConnectionTask extends AsyncTask<Void, Void, TorConnectionStatus> {
        private final WeakReference<MainActivity> mainActivityWeakReference;

        CheckTorConnectionTask(MainActivity mainActivity) {
            mainActivityWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        protected TorConnectionStatus doInBackground(Void... voids) {
            MainActivity mainActivity = mainActivityWeakReference.get();
            if (mainActivity == null) {
                return TorConnectionStatus.ERROR;
            }
            return Helpers.checkTorProxyConnectivity("localhost", 9150);
        }

        @Override
        protected void onPostExecute(TorConnectionStatus s) {
            MainActivity mainActivity = mainActivityWeakReference.get();
            if (mainActivity == null) {
                return;
            }
            mainActivity.onCheckTorConnectionTaskCompleted(s);
        }
    }

    private void onCheckTorConnectionTaskCompleted(TorConnectionStatus s) {
        fabSpin.cancel();
        fab.setEnabled(true);
        switch (s) {
            case DIRECT:
                connectionStatus.setText(R.string.tor_no_arti);
                break;
            case TOR:
                connectionStatus.setText(R.string.tor_with_arti);
                break;
            case ERROR:
                connectionStatus.setText(R.string.connection_failed);
                break;
        }
        fab.setActivated(true);
    }

    private void onSelectionChanged(int selection) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

        // Update the UI or perform some action based on the selection
        switch (selection) {
            case 0:
                stunServerInput.setVisibility(View.GONE);
                targetInput.setVisibility(View.GONE);
                frontInput.setVisibility(View.GONE);
                bridgeLineInput.setVisibility(View.GONE);
                obfs4Port.setVisibility(View.GONE);
                noSelection.setVisibility(View.VISIBLE);
                logScrollView.setVisibility(View.GONE);
                break;
            case 1:
                // do nothing
                stunServerInput.setVisibility(View.GONE);
                obfs4Port.setVisibility(View.GONE);
                targetInput.setVisibility(View.GONE);
                frontInput.setVisibility(View.GONE);
                bridgeLineInput.setVisibility(View.GONE);
                noSelection.setVisibility(View.GONE);
                logScrollView.setVisibility(View.VISIBLE);
                constraintSet.connect(logScrollView.getId(), ConstraintSet.TOP, spinner.getId(), ConstraintSet.BOTTOM);
                break;
            case 2:
                bridgeLineInput.setVisibility(View.VISIBLE);
                obfs4Port.setVisibility(View.VISIBLE);
                stunServerInput.setVisibility(View.GONE);
                targetInput.setVisibility(View.GONE);
                frontInput.setVisibility(View.GONE);
                noSelection.setVisibility(View.GONE);
                logScrollView.setVisibility(View.VISIBLE);
                constraintSet.connect(logScrollView.getId(), ConstraintSet.TOP, bridgeLineInput.getId(), ConstraintSet.BOTTOM);
                break;
            case 3:
                bridgeLineInput.setVisibility(View.VISIBLE);
                stunServerInput.setVisibility(View.VISIBLE);
                targetInput.setVisibility(View.VISIBLE);
                frontInput.setVisibility(View.VISIBLE);
                noSelection.setVisibility(View.GONE);
                obfs4Port.setVisibility(View.GONE);
                logScrollView.setVisibility(View.VISIBLE);
                constraintSet.connect(logScrollView.getId(), ConstraintSet.TOP, frontInput.getId(), ConstraintSet.BOTTOM);
                break;
        }
    }


}