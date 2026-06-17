package com.chamanotrampo.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView lista = findViewById(R.id.txtLista);
        Button btnWhatsapp = findViewById(R.id.btnWhatsapp);
        Button btnPublicar = findViewById(R.id.btnPublicar);

        lista.setText(
                "VAGA - Auxiliar de produção\n" +
                "Guariba - Centro\n" +
                "Salário a combinar\n\n" +
                "SERVIÇO - Pedreiro para reforma\n" +
                "Jardinópolis - Jardim Primavera\n" +
                "Enviar orçamento\n\n" +
                "BICO - Ajudante para descarregar caminhão\n" +
                "Ribeirão Preto - Distrito Industrial\n" +
                "R$ 120,00 no dia\n\n" +
                "URGENTE - Eletricista hoje\n" +
                "Jaboticabal - Nova Jaboticabal\n" +
                "A combinar"
        );

        btnWhatsapp.setOnClickListener(view -> abrirWhatsapp());
        btnPublicar.setOnClickListener(view -> Toast.makeText(
                this,
                "Em breve: tela para publicar vaga, bico ou serviço.",
                Toast.LENGTH_LONG
        ).show());
    }

    private void abrirWhatsapp() {
        String telefone = "5516999999999";
        String mensagem = "Olá! Vi uma oportunidade no Chama no Trampo e tenho interesse.";
        String url = "https://wa.me/" + telefone + "?text=" + Uri.encode(mensagem);

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception erro) {
            Toast.makeText(this, "Não foi possível abrir o WhatsApp.", Toast.LENGTH_LONG).show();
        }
    }
}
