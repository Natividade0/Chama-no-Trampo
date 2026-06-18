package com.chamanotrampo.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private LinearLayout conteudoContainer;
    private String filtroAtual = "TODOS";
    private ArrayList<Oportunidade> oportunidades = new ArrayList<Oportunidade>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            carregarDadosIniciais();
            montarTelaPrincipal();
        } catch (Throwable erro) {
            mostrarTelaErro(erro);
        }
    }

    private void carregarDadosIniciais() {
        if (!oportunidades.isEmpty()) {
            return;
        }

        oportunidades.add(new Oportunidade("VAGA", "Auxiliar de producao", "Guariba - Centro", "Salario a combinar", "Empresa local buscando inicio imediato.", "16999999999"));
        oportunidades.add(new Oportunidade("SERVICO", "Pedreiro para reforma", "Jardinopolis", "Enviar orcamento", "Cliente precisa reformar uma area pequena.", "16999999999"));
        oportunidades.add(new Oportunidade("BICO", "Ajudante para descarregar caminhao", "Ribeirao Preto", "R$ 120,00 no dia", "Servico rapido, pagamento no mesmo dia.", "16999999999"));
        oportunidades.add(new Oportunidade("URGENTE", "Eletricista hoje", "Jaboticabal", "Valor a combinar", "Atendimento ainda hoje.", "16999999999"));
        oportunidades.add(new Oportunidade("VAGA", "Tecnico de seguranca do trabalho", "Ribeirao Preto e regiao", "Enviar pretensao", "Acompanhamento de obra e documentacao.", "16999999999"));
    }

    private void montarTelaPrincipal() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(241, 245, 249));

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(16), dp(16), dp(16), dp(24));

        TextView cabecalho = bloco("Chama no Trampo\n\nEmpregos, bicos e servicos perto de voce.", 24, Color.WHITE, true, Color.rgb(15, 23, 42));
        cabecalho.setGravity(Gravity.CENTER_VERTICAL);
        raiz.addView(cabecalho, larguraAltura(LinearLayout.LayoutParams.MATCH_PARENT, dp(150)));

        raiz.addView(espaco(12));

        LinearLayout acoes = new LinearLayout(this);
        acoes.setOrientation(LinearLayout.HORIZONTAL);
        acoes.addView(botaoTexto("Ver oportunidades", Color.rgb(22, 163, 74), Color.WHITE, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                montarTelaPrincipal();
            }
        }), peso());
        acoes.addView(espacoLargura(8));
        acoes.addView(botaoTexto("Publicar", Color.rgb(15, 23, 42), Color.WHITE, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                montarTelaPublicar();
            }
        }), peso());
        raiz.addView(acoes);

        raiz.addView(espaco(12));
        raiz.addView(bloco("Buscar: pedreiro, ajudante, eletricista...", 15, Color.rgb(71, 85, 105), false, Color.WHITE));
        raiz.addView(espaco(12));
        raiz.addView(texto("Categorias", 18, Color.rgb(15, 23, 42), true));
        raiz.addView(espaco(8));
        raiz.addView(linhaCategorias());
        raiz.addView(espaco(14));
        raiz.addView(texto("Oportunidades perto de voce", 19, Color.rgb(15, 23, 42), true));
        raiz.addView(espaco(8));

        conteudoContainer = new LinearLayout(this);
        conteudoContainer.setOrientation(LinearLayout.VERTICAL);
        raiz.addView(conteudoContainer);

        scroll.addView(raiz);
        setContentView(scroll);

        atualizarLista("TODOS");
    }

    private void montarTelaPublicar() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(241, 245, 249));

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(16), dp(16), dp(16), dp(24));

        raiz.addView(bloco("Publicar oportunidade\n\nCadastre uma vaga, bico ou servico.", 24, Color.WHITE, true, Color.rgb(15, 23, 42)), larguraAltura(LinearLayout.LayoutParams.MATCH_PARENT, dp(140)));
        raiz.addView(espaco(12));

        final EditText tipo = campo("Tipo: VAGA, BICO, SERVICO ou URGENTE");
        final EditText titulo = campo("Titulo da oportunidade");
        final EditText cidade = campo("Cidade / bairro");
        final EditText valor = campo("Valor ou salario");
        final EditText descricao = campo("Descricao");
        final EditText contato = campo("WhatsApp para contato");

        raiz.addView(tipo);
        raiz.addView(espaco(8));
        raiz.addView(titulo);
        raiz.addView(espaco(8));
        raiz.addView(cidade);
        raiz.addView(espaco(8));
        raiz.addView(valor);
        raiz.addView(espaco(8));
        raiz.addView(descricao);
        raiz.addView(espaco(8));
        raiz.addView(contato);
        raiz.addView(espaco(14));

        raiz.addView(botaoTexto("Salvar oportunidade", Color.rgb(22, 163, 74), Color.WHITE, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tipoTexto = tipo.getText().toString().trim().toUpperCase();
                String tituloTexto = titulo.getText().toString().trim();
                String cidadeTexto = cidade.getText().toString().trim();
                String valorTexto = valor.getText().toString().trim();
                String descricaoTexto = descricao.getText().toString().trim();
                String contatoTexto = contato.getText().toString().trim();

                if (tipoTexto.length() == 0) tipoTexto = "BICO";

                if (tituloTexto.length() == 0 || cidadeTexto.length() == 0 || contatoTexto.length() == 0) {
                    Toast.makeText(MainActivity.this, "Preencha titulo, cidade e contato.", Toast.LENGTH_LONG).show();
                    return;
                }

                oportunidades.add(0, new Oportunidade(tipoTexto, tituloTexto, cidadeTexto, valorTexto, descricaoTexto, contatoTexto));
                Toast.makeText(MainActivity.this, "Oportunidade publicada nesta amostra.", Toast.LENGTH_LONG).show();
                montarTelaPrincipal();
            }
        }), larguraAltura(LinearLayout.LayoutParams.MATCH_PARENT, dp(50)));

        raiz.addView(espaco(10));
        raiz.addView(botaoTexto("Voltar", Color.WHITE, Color.rgb(15, 23, 42), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                montarTelaPrincipal();
            }
        }), larguraAltura(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));

        scroll.addView(raiz);
        setContentView(scroll);
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
        linha2.addView(categoria("Bicos", "BICO"), peso());
        linha2.addView(espacoLargura(8));
        linha2.addView(categoria("Servicos", "SERVICO"), peso());
        box.addView(linha2);

        box.addView(espaco(8));

        LinearLayout linha3 = new LinearLayout(this);
        linha3.setOrientation(LinearLayout.HORIZONTAL);
        linha3.addView(categoria("Urgentes", "URGENTE"), peso());
        linha3.addView(espacoLargura(8));
        linha3.addView(categoria("Publicar", "PUBLICAR"), peso());
        box.addView(linha3);

        return box;
    }

    private TextView categoria(String nome, final String filtro) {
        TextView item = bloco(nome, 15, Color.rgb(15, 23, 42), true, Color.WHITE);
        item.setGravity(Gravity.CENTER);
        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("PUBLICAR".equals(filtro)) {
                    montarTelaPublicar();
                } else {
                    atualizarLista(filtro);
                }
            }
        });
        return item;
    }

    private void atualizarLista(String filtro) {
        filtroAtual = filtro;
        if (conteudoContainer == null) {
            return;
        }

        conteudoContainer.removeAllViews();

        int total = 0;
        for (int i = 0; i < oportunidades.size(); i++) {
            Oportunidade item = oportunidades.get(i);
            if ("TODOS".equals(filtroAtual) || item.tipo.equals(filtroAtual)) {
                oportunidade(item);
                total++;
            }
        }

        if (total == 0) {
            conteudoContainer.addView(bloco("Nenhuma oportunidade encontrada nesta categoria.", 15, Color.rgb(71, 85, 105), false, Color.WHITE));
        }
    }

    private void oportunidade(final Oportunidade item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackgroundColor(Color.WHITE);

        card.addView(texto(item.tipo, 12, corTipo(item.tipo), true));
        card.addView(texto(item.titulo, 20, Color.rgb(15, 23, 42), true));
        card.addView(texto(item.local, 14, Color.rgb(100, 116, 139), false));
        card.addView(texto(item.valor.length() == 0 ? "Valor a combinar" : item.valor, 16, Color.rgb(22, 163, 74), true));
        card.addView(texto(item.descricao.length() == 0 ? "Sem descricao informada." : item.descricao, 14, Color.rgb(51, 65, 85), false));

        TextView interesse = bloco("Tenho interesse", 15, Color.WHITE, true, Color.rgb(15, 23, 42));
        interesse.setGravity(Gravity.CENTER);
        interesse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Contato: " + item.contato, Toast.LENGTH_LONG).show();
            }
        });
        card.addView(espaco(8));
        card.addView(interesse, larguraAltura(LinearLayout.LayoutParams.MATCH_PARENT, dp(44)));

        conteudoContainer.addView(card);
        conteudoContainer.addView(espaco(10));
    }

    private EditText campo(String dica) {
        EditText editText = new EditText(this);
        editText.setHint(dica);
        editText.setTextSize(15);
        editText.setSingleLine(false);
        editText.setPadding(dp(12), dp(10), dp(12), dp(10));
        editText.setBackgroundColor(Color.WHITE);
        editText.setTextColor(Color.rgb(15, 23, 42));
        editText.setHintTextColor(Color.rgb(100, 116, 139));
        return editText;
    }

    private TextView botaoTexto(String texto, int fundo, int corTexto, View.OnClickListener listener) {
        TextView view = bloco(texto, 15, corTexto, true, fundo);
        view.setGravity(Gravity.CENTER);
        view.setOnClickListener(listener);
        return view;
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

    private static class Oportunidade {
        String tipo;
        String titulo;
        String local;
        String valor;
        String descricao;
        String contato;

        Oportunidade(String tipo, String titulo, String local, String valor, String descricao, String contato) {
            this.tipo = tipo;
            this.titulo = titulo;
            this.local = local;
            this.valor = valor;
            this.descricao = descricao;
            this.contato = contato;
        }
    }
}
