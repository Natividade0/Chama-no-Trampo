package com.chamanotrampo.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private LinearLayout listaContainer;
    private String filtroAtual = "TODOS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            montarTela();
        } catch (Throwable erro) {
            mostrarTelaErro(erro);
        }
    }

    private void montarTela() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(241, 245, 249));

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(16), dp(16), dp(16), dp(24));

        TextView cabecalho = bloco("Chama no Trampo\n\nEmpregos, bicos e servicos perto de voce.", 24, Color.WHITE, true, Color.rgb(15, 23, 42));
        cabecalho.setGravity(Gravity.CENTER_VERTICAL);
        raiz.addView(cabecalho, larguraAltura(LinearLayout.LayoutParams.MATCH_PARENT, dp(150)));

        raiz.addView(espaco(12));
        raiz.addView(bloco("Buscar: pedreiro, ajudante, eletricista...", 15, Color.rgb(71, 85, 105), false, Color.WHITE));
        raiz.addView(espaco(12));
        raiz.addView(texto("Categorias", 18, Color.rgb(15, 23, 42), true));
        raiz.addView(espaco(8));
        raiz.addView(linhaCategorias());
        raiz.addView(espaco(14));
        raiz.addView(texto("Oportunidades perto de voce", 19, Color.rgb(15, 23, 42), true));
        raiz.addView(espaco(8));

        listaContainer = new LinearLayout(this);
        listaContainer.setOrientation(LinearLayout.VERTICAL);
        raiz.addView(listaContainer);

        scroll.addView(raiz);
        setContentView(scroll);

        atualizarLista("TODOS");
    }

    private LinearLayout linhaCategorias() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linha1 = new LinearLayout(this);
        linha1.setOrientation(LinearLayout.HORIZONTAL);
        linha1.addView(categoria("Todos", "TODOS"), peso());
        linha1.addView(espacoLargura(8));
        linha1.addView(categoria("Vagas", "VAGA"), peso());
        box.addView(linha1);

        box.addView(espaco(8));

        LinearLayout linha2 = new LinearLayout(this);
        linha2.setOrientation(LinearLayout.HORIZONTAL);
        linha2.addView(categoria("Servicos", "SERVICO"), peso());
        linha2.addView(espacoLargura(8));
        linha2.addView(categoria("Urgentes", "URGENTE"), peso());
        box.addView(linha2);

        return box;
    }

    private TextView categoria(String nome, final String filtro) {
        TextView item = bloco(nome, 15, Color.rgb(15, 23, 42), true, Color.WHITE);
        item.setGravity(Gravity.CENTER);
        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                atualizarLista(filtro);
            }
        });
        return item;
    }

    private void atualizarLista(String filtro) {
        filtroAtual = filtro;
        if (listaContainer == null) {
            return;
        }

        listaContainer.removeAllViews();
        oportunidade("VAGA", "Auxiliar de producao", "Guariba - Centro", "Salario a combinar", "Empresa local buscando inicio imediato.");
        oportunidade("SERVICO", "Pedreiro para reforma", "Jardinopolis", "Enviar orcamento", "Cliente precisa reformar uma area pequena.");
        oportunidade("BICO", "Ajudante para descarregar caminhao", "Ribeirao Preto", "R$ 120,00 no dia", "Servico rapido, pagamento no mesmo dia.");
        oportunidade("URGENTE", "Eletricista hoje", "Jaboticabal", "Valor a combinar", "Atendimento ainda hoje.");
        oportunidade("VAGA", "Tecnico de seguranca do trabalho", "Ribeirao Preto e regiao", "Enviar pretensao", "Acompanhamento de obra e documentacao.");
    }

    private void oportunidade(String tipo, final String titulo, String local, String valor, String descricao) {
        if (!"TODOS".equals(filtroAtual) && !tipo.equals(filtroAtual)) {
            return;
        }

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackgroundColor(Color.WHITE);

        TextView tag = texto(tipo, 12, corTipo(tipo), true);
        card.addView(tag);
        card.addView(texto(titulo, 20, Color.rgb(15, 23, 42), true));
        card.addView(texto(local, 14, Color.rgb(100, 116, 139), false));
        card.addView(texto(valor, 16, Color.rgb(22, 163, 74), true));
        card.addView(texto(descricao, 14, Color.rgb(51, 65, 85), false));

        TextView interesse = bloco("Tenho interesse", 15, Color.WHITE, true, Color.rgb(15, 23, 42));
        interesse.setGravity(Gravity.CENTER);
        interesse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Interesse enviado: " + titulo, Toast.LENGTH_LONG).show();
            }
        });
        card.addView(espaco(8));
        card.addView(interesse, larguraAltura(LinearLayout.LayoutParams.MATCH_PARENT, dp(44)));

        listaContainer.addView(card);
        listaContainer.addView(espaco(10));
    }

    private TextView bloco(String texto, int tamanho, int corTexto, boolean negrito, int fundo) {
        TextView view = texto(texto, tamanho, corTexto, negrito);
        view.setPadding(dp(14), dp(12), dp(14), dp(12));
        view.setBackgroundColor(fundo);
        return view;
    }

    private TextView texto(String texto, int tamanho, int cor, boolean negrito) {
        TextView view = new TextView(this);
        view.setText(texto);
        view.setTextSize(tamanho);
        view.setTextColor(cor);
        view.setPadding(0, dp(3), 0, dp(3));
        if (negrito) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return view;
    }

    private void mostrarTelaErro(Throwable erro) {
        TextView view = new TextView(this);
        view.setText("Chama no Trampo\n\nO app abriu, mas houve erro na tela.\n\n" + erro.getClass().getSimpleName() + "\n" + erro.getMessage());
        view.setTextSize(18);
        view.setTextColor(Color.WHITE);
        view.setBackgroundColor(Color.rgb(127, 29, 29));
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(24), dp(24), dp(24), dp(24));
        setContentView(view);
    }

    private int corTipo(String tipo) {
        if ("URGENTE".equals(tipo)) return Color.rgb(220, 38, 38);
        if ("SERVICO".equals(tipo)) return Color.rgb(37, 99, 235);
        if ("BICO".equals(tipo)) return Color.rgb(217, 119, 6);
        return Color.rgb(22, 163, 74);
    }

    private LinearLayout.LayoutParams peso() {
        return new LinearLayout.LayoutParams(0, dp(48), 1);
    }

    private LinearLayout.LayoutParams larguraAltura(int largura, int altura) {
        return new LinearLayout.LayoutParams(largura, altura);
    }

    private View espaco(int altura) {
        View view = new View(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(1, dp(altura)));
        return view;
    }

    private View espacoLargura(int largura) {
        View view = new View(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(dp(largura), 1));
        return view;
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density + 0.5f);
    }
}
