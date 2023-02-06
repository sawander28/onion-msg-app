// SPDX-FileCopyrightText: 2022 Michael Pöhn <michael@poehn.at>
// SPDX-License-Identifier: MIT

package info.guardianproject.arti.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.text);
        textView.setText("");

        fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(view -> checkConnection());
    }

    private void checkConnection() {
        fab.setActivated(false);
        textView.setText("performing request ...");

        new AsyncTask<Void, Void, TorConnectionStatus>() {
            @Override
            protected TorConnectionStatus doInBackground(Void... voids) {
                return Helpers.checkTorProxyConnectivity("localhost", 9150);
            }

            @Override
            protected void onPostExecute(TorConnectionStatus s) {
                super.onPostExecute(s);
                switch (s) {
                    case DIRECT:
                        textView.setText("⚠ connected to check.torproject.org without arti");
                        break;
                    case TOR:
                        textView.setText("✅ connected to check.torproject.org using arti");
                        break;
                    case ERROR:
                        textView.setText("⛔ connecting failed");
                        break;
                }
                fab.setActivated(true);
            }
        }.execute(null, null, null);
    }
}