package com.chamanotrampo.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
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
        montarTela();
    }

    private void montarTela() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.rgb(245, 247, 250));

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(16), dp(16), dp(16), dp(24));

        raiz.addView(criarHero());
        raiz.addView(criarEspaco(14));
        raiz.addView(criarBarraPesquisa());
        raiz.addView(criarEspaco(14));
        raiz.addView(criarTituloSecao("Categorias"));
        raiz.addView(criarCategorias());
        raiz.addView(criarEspaco(14));
        raiz.addView(criarLinhaTituloComAcao());

        listaContainer = new LinearLayout(this);
        listaContainer.setOrientation(LinearLayout.VERTICAL);
        raiz.addView(listaContainer);

        atualizarLista("TODOS");

        scrollView.addView(raiz);
        setContentView(scrollView);
    }

    private View criarHero() {
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(18), dp(18), dp(18), dp(18));
        hero.setBackground(criarFundo(0xFF111827, 22));

        TextView selo = criarTexto("APP LOCAL", 12, 0xFFE5E7EB, true);
        selo.setLetterSpacing(0.12f);
        hero.addView(selo);

        TextView titulo = criarTexto("Chama no Trampo", 30, 0xFFFFFFFF, true);
        titulo.setPadding(0, dp(8), 0, 0);
        hero.addView(titulo);

        TextView subtitulo = criarTexto("Empregos, bicos e servicos perto de voce.", 16, 0xFFD1D5DB, false);
        subtitulo.setPadding(0, dp(6), 0, dp(14));
        hero.addView(subtitulo);

        LinearLayout botoes = new LinearLayout(this);
        botoes.setOrientation(LinearLayout.HORIZONTAL);

        Button procurar = criarBotao("Procurar trampo", 0xFF22C55E, 0xFFFFFFFF);
        procurar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                atualizarLista("VAGA");
            }
        });
        botoes.addView(procurar, new LinearLayout.LayoutParams(0, dp(46), 1));

        View espaco = new View(this);
        botoes.addView(espaco, new LinearLayout.LayoutParams(dp(10), 1));

        Button publicar = criarBotao("Publicar", 0xFFFFFFFF, 0xFF111827);
        publicar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Em breve: cadastro de oportunidade.", Toast.LENGTH_LONG).show();
            }
        });
        botoes.addView(publicar, new LinearLayout.LayoutParams(0, dp(46), 1));

        hero.addView(botoes);
        return hero;
    }

    private View criarBarraPesquisa() {
        TextView barra = criarTexto("Buscar: pedreiro, ajudante, eletricista...", 15, 0xFF6B7280, false);
        barra.setPadding(dp(16), dp(14), dp(16), dp(14));
        barra.setBackground(criarFundo(0xFFFFFFFF, 18));
        barra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Busca real entra na proxima versao.", Toast.LENGTH_SHORT).show();
            }
        });
        return barra;
    }

    private View criarCategorias() {
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linha1 = new LinearLayout(this);
        linha1.setOrientation(LinearLayout.HORIZONTAL);
        linha1.addView(criarCategoria("Todos", "TODOS"), new LinearLayout.LayoutParams(0, dp(46), 1));
        linha1.addView(criarSeparadorHorizontal());
        linha1.addView(criarCategoria("Vagas", "VAGA"), new LinearLayout.LayoutParams(0, dp(46), 1));
        grid.addView(linha1);

        grid.addView(criarEspaco(8));

        LinearLayout linha2 = new LinearLayout(this);
        linha2.setOrientation(LinearLayout.HORIZONTAL);
        linha2.addView(criarCategoria("Servicos", "SERVICO"), new LinearLayout.LayoutParams(0, dp(46), 1));
        linha2.addView(criarSeparadorHorizontal());
        linha2.addView(criarCategoria("Urgentes", "URGENTE"), new LinearLayout.LayoutParams(0, dp(46), 1));
        grid.addView(linha2);

        return grid;
    }

    private Button criarCategoria(final String texto, final String filtro) {
        Button botao = criarBotao(texto, 0xFFFFFFFF, 0xFF111827);
        botao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                atualizarLista(filtro);
            }
        });
        return botao;
    }

    private View criarLinhaTituloComAcao() {
        LinearLayout linha = new LinearLayout(this);
        linha.setOrientation(LinearLayout.HORIZONTAL);
        linha.setGravity(Gravity.CENTER_VERTICAL);

        TextView titulo = criarTexto("Oportunidades perto de voce", 19, 0xFF111827, true);
        linha.addView(titulo, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView verTudo = criarTexto("Ver tudo", 14, 0xFF16A34A, true);
        verTudo.setPadding(dp(8), dp(8), dp(8), dp(8));
        verTudo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                atualizarLista("TODOS");
            }
        });
        linha.addView(verTudo);

        return linha;
    }

    private void atualizarLista(String filtro) {
        filtroAtual = filtro;
        if (listaContainer == null) {
            return;
        }

        listaContainer.removeAllViews();

        adicionarSeCombinar("VAGA", "Auxiliar de producao", "Guariba - Centro", "Salario a combinar", "Empresa local buscando inicio imediato.");
        adicionarSeCombinar("SERVICO", "Pedreiro para reforma", "Jardinopolis - Jardim Primavera", "Enviar orcamento", "Cliente precisa reformar uma area pequena.");
        adicionarSeCombinar("BICO", "Ajudante para descarregar caminhao", "Ribeirao Preto - Distrito Industrial", "R$ 120,00 no dia", "Servico rapido, pagamento no mesmo dia.");
        adicionarSeCombinar("URGENTE", "Eletricista hoje", "Jaboticabal - Nova Jaboticabal", "Valor a combinar", "Atendimento ainda hoje, preferencia com ferramentas.");
        adicionarSeCombinar("VAGA", "Tecnico de seguranca do trabalho", "Ribeirao Preto e regiao", "Enviar pretensao", "Vaga para acompanhamento de obra e documentacao.");
    }

    private void adicionarSeCombinar(String tipo, String titulo, String local, String valor, String descricao) {
        if (!"TODOS".equals(filtroAtual) && !tipo.equals(filtroAtual)) {
            return;
        }
        listaContainer.addView(criarCard(tipo, titulo, local, valor, descricao));
        listaContainer.addView(criarEspaco(10));
    }

    private View criarCard(final String tipo, final String titulo, String local, String valor, String descricao) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackground(criarFundo(0xFFFFFFFF, 18));

        TextView tag = criarTexto(tipo, 12, corDoTipo(tipo), true);
        card.addView(tag);

        TextView tituloView = criarTexto(titulo, 20, 0xFF111827, true);
        tituloView.setPadding(0, dp(6), 0, 0);
        card.addView(tituloView);

        TextView localView = criarTexto(local, 14, 0xFF6B7280, false);
        localView.setPadding(0, dp(4), 0, 0);
        card.addView(localView);

        TextView valorView = criarTexto(valor, 16, 0xFF16A34A, true);
        valorView.setPadding(0, dp(8), 0, 0);
        card.addView(valorView);

        TextView descView = criarTexto(descricao, 14, 0xFF374151, false);
        descView.setPadding(0, dp(8), 0, dp(10));
        card.addView(descView);

        Button acao = criarBotao("Tenho interesse", 0xFF111827, 0xFFFFFFFF);
        acao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Interesse enviado: " + titulo, Toast.LENGTH_LONG).show();
            }
        });
        card.addView(acao, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(44)));

        return card;
    }

    private TextView criarTituloSecao(String texto) {
        TextView titulo = criarTexto(texto, 18, 0xFF111827, true);
        titulo.setPadding(0, 0, 0, dp(8));
        return titulo;
    }

    private TextView criarTexto(String texto, int tamanho, int cor, boolean negrito) {
        TextView textView = new TextView(this);
        textView.setText(texto);
        textView.setTextSize(tamanho);
        textView.setTextColor(cor);
        if (negrito) {
            textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return textView;
    }

    private Button criarBotao(String texto, int fundo, int corTexto) {
        Button botao = new Button(this);
        botao.setText(texto);
        botao.setTextColor(corTexto);
        botao.setTextSize(14);
        botao.setAllCaps(false);
        botao.setBackground(criarFundo(fundo, 16));
        return botao;
    }

    private GradientDrawable criarFundo(int cor, int raio) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(cor);
        drawable.setCornerRadius(dp(raio));
        return drawable;
    }

    private int corDoTipo(String tipo) {
        if ("URGENTE".equals(tipo)) {
            return 0xFFDC2626;
        }
        if ("SERVICO".equals(tipo)) {
            return 0xFF2563EB;
        }
        if ("BICO".equals(tipo)) {
            return 0xFFD97706;
        }
        return 0xFF16A34A;
    }

    private View criarEspaco(int alturaDp) {
        View view = new View(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(1, dp(alturaDp)));
        return view;
    }

    private View criarSeparadorHorizontal() {
        View view = new View(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(dp(8), 1));
        return view;
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density + 0.5f);
    }
}
