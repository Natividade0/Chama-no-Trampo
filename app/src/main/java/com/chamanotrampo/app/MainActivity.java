package com.chamanotrampo.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        montarTelaInicial();
    }

    private void montarTelaInicial() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(0xFFF7F7F7);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        TextView titulo = new TextView(this);
        titulo.setText("Chama no Trampo");
        titulo.setTextSize(30);
        titulo.setTextColor(0xFF1B1B1B);
        titulo.setTypeface(null, 1);
        layout.addView(titulo);

        TextView subtitulo = new TextView(this);
        subtitulo.setText("Empregos, bicos e servicos perto de voce.");
        subtitulo.setTextSize(16);
        subtitulo.setTextColor(0xFF626262);
        subtitulo.setPadding(0, 12, 0, 24);
        layout.addView(subtitulo);

        TextView lista = new TextView(this);
        lista.setText(
                "VAGA - Auxiliar de producao\n" +
                "Guariba - Centro\n" +
                "Salario a combinar\n\n" +
                "SERVICO - Pedreiro para reforma\n" +
                "Jardinopolis - Jardim Primavera\n" +
                "Enviar orcamento\n\n" +
                "BICO - Ajudante para descarregar caminhao\n" +
                "Ribeirao Preto - Distrito Industrial\n" +
                "R$ 120,00 no dia\n\n" +
                "URGENTE - Eletricista hoje\n" +
                "Jaboticabal - Nova Jaboticabal\n" +
                "A combinar"
        );
        lista.setTextSize(16);
        lista.setTextColor(0xFF1B1B1B);
        lista.setBackgroundColor(0xFFFFFFFF);
        lista.setPadding(24, 24, 24, 24);
        layout.addView(lista);

        Button btnWhatsapp = new Button(this);
        btnWhatsapp.setText("Chamar no WhatsApp");
        btnWhatsapp.setAllCaps(false);
        btnWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirWhatsapp();
            }
        });
        layout.addView(btnWhatsapp);

        Button btnPublicar = new Button(this);
        btnPublicar.setText("Publicar oportunidade");
        btnPublicar.setAllCaps(false);
        btnPublicar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Em breve: publicar vaga, bico ou servico.", Toast.LENGTH_LONG).show();
            }
        });
        layout.addView(btnPublicar);

        scrollView.addView(layout);
        setContentView(scrollView);
    }

    private void abrirWhatsapp() {
        String telefone = "5516999999999";
        String mensagem = "Ola! Vi uma oportunidade no Chama no Trampo e tenho interesse.";
        String url = "https://wa.me/" + telefone + "?text=" + Uri.encode(mensagem);

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception erro) {
            Toast.makeText(this, "Nao foi possivel abrir o WhatsApp.", Toast.LENGTH_LONG).show();
        }
    }
}
