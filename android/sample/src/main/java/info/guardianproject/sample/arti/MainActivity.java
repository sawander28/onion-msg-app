// SPDX-FileCopyrightText: 2022 Michael Pöhn <michael@poehn.at>
// SPDX-License-Identifier: MIT

package info.guardianproject.sample.arti;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference;

import info.guardianproject.sample.arti.R;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private FloatingActionButton fab;
    private ViewPropertyAnimator fabSpin;
    private Spinner spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.text);
        textView.setText(getString(R.string.intro_text));

        fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(view -> checkConnection());
        fabSpin = fab.animate();

        spinner = findViewById(R.id.spinner);
    }

    private void checkConnection() {
        fab.setEnabled(false);
        fabSpin.setDuration(1000*60).rotationBy((float) (1000 * 60) /4).setInterpolator(new LinearInterpolator()).start();
        textView.setText(R.string.performing_request);

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
                textView.setText(R.string.tor_no_arti);
                break;
            case TOR:
                textView.setText(R.string.tor_with_arti);
                break;
            case ERROR:
                textView.setText(R.string.connection_failed);
                break;
        }
        fab.setActivated(true);
    }
}