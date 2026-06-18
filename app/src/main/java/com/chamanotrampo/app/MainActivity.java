package com.chamanotrampo.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView = new TextView(this);
        textView.setText("Chama no Trampo\n\nApp iniciado com sucesso.\n\nEmpregos, bicos e servicos perto de voce.");
        textView.setTextSize(22);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(32, 32, 32, 32);

        setContentView(textView);
    }
}
