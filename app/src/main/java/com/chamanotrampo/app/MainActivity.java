package com.chamanotrampo.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
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

    private static final String PREFS = "chama_no_trampo_prefs";
    private static final String KEY_NOME = "perfil_nome";
    private static final String KEY_CIDADE = "perfil_cidade";
    private static final String KEY_TELEFONE = "perfil_telefone";
    private static final String KEY_TIPO_USUARIO = "perfil_tipo_usuario";
    private static final String KEY_OPORTUNIDADES = "oportunidades_salvas";

    private LinearLayout conteudoContainer;
    private String filtroAtual = "TODOS";
    private ArrayList<Oportunidade> oportunidades = new ArrayList<Oportunidade>();

    private String perfilNome = "";
    private String perfilCidade = "";
    private String perfilTelefone = "";
    private String perfilTipoUsuario = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            carregarPerfil();
            carregarOportunidades();
            montarTelaPrincipal();
        } catch (Throwable erro) {
            mostrarTelaErro(erro);
        }
    }

    private void carregarPerfil() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        perfilNome = prefs.getString(KEY_NOME, "");
        perfilCidade = prefs.getString(KEY_CIDADE, "");
        perfilTelefone = prefs.getString(KEY_TELEFONE, "");
        perfilTipoUsuario = prefs.getString(KEY_TIPO_USUARIO, "");
    }

    private void salvarPerfil(String nome, String cidade, String telefone, String tipoUsuario) {
        perfilNome = nome;
        perfilCidade = cidade;
        perfilTelefone = telefone;
        perfilTipoUsuario = tipoUsuario;

        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putString(KEY_NOME, perfilNome)
                .putString(KEY_CIDADE, perfilCidade)
                .putString(KEY_TELEFONE, perfilTelefone)
                .putString(KEY_TIPO_USUARIO, perfilTipoUsuario)
                .apply();
    }

    private boolean perfilCompleto() {
        return perfilNome.trim().length() > 0 && perfilTelefone.trim().length() > 0;
    }

    private void carregarOportunidades() {
        oportunidades.clear();
        String salvo = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_OPORTUNIDADES, "");

        if (salvo != null && salvo.trim().length() > 0) {
            String[] linhas = salvo.split("\\n");
            for (int i = 0; i < linhas.length; i++) {
                String[] campos = linhas[i].split("\\|", -1);
                if (campos.length >= 7) {
                    oportunidades.add(new Oportunidade(
                            decodificar(campos[0]),
                            decodificar(campos[1]),
                            decodificar(campos[2]),
                            decodificar(campos[3]),
                            decodificar(campos[4]),
                            decodificar(campos[5]),
                            decodificar(campos[6])
                    ));
                }
            }
        }

        if (oportunidades.isEmpty()) {
            oportunidades.add(new Oportunidade("VAGA", "Auxiliar de producao", "Guariba - Centro", "Salario a combinar", "Empresa local buscando inicio imediato.", "16999999999", "Equipe Chama no Trampo"));
            oportunidades.add(new Oportunidade("SERVICO", "Pedreiro para reforma", "Jardinopolis", "Enviar orcamento", "Cliente precisa reformar uma area pequena.", "16999999999", "Equipe Chama no Trampo"));
            oportunidades.add(new Oportunidade("BICO", "Ajudante para descarregar caminhao", "Ribeirao Preto", "R$ 120,00 no dia", "Servico rapido, pagamento no mesmo dia.", "16999999999", "Equipe Chama no Trampo"));
            oportunidades.add(new Oportunidade("URGENTE", "Eletricista hoje", "Jaboticabal", "Valor a combinar", "Atendimento ainda hoje.", "16999999999", "Equipe Chama no Trampo"));
            oportunidades.add(new Oportunidade("VAGA", "Tecnico de seguranca do trabalho", "Ribeirao Preto e regiao", "Enviar pretensao", "Acompanhamento de obra e documentacao.", "16999999999", "Equipe Chama no Trampo"));
        }
    }

    private void salvarOportunidades() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < oportunidades.size(); i++) {
            Oportunidade item = oportunidades.get(i);
            builder.append(codificar(item.tipo)).append("|")
                    .append(codificar(item.titulo)).append("|")
                    .append(codificar(item.local)).append("|")
                    .append(codificar(item.valor)).append("|")
                    .append(codificar(item.descricao)).append("|")
                    .append(codificar(item.contato)).append("|")
                    .append(codificar(item.autor));
            if (i < oportunidades.size() - 1) {
                builder.append("\n");
            }
        }

        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putString(KEY_OPORTUNIDADES, builder.toString())
                .apply();
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

        if (perfilCompleto()) {
            raiz.addView(bloco("Perfil ativo: " + perfilNome + "\n" + textoPerfilResumo(), 15, Color.rgb(15, 23, 42), true, Color.WHITE));
        } else {
            TextView aviso = bloco("Complete seu perfil para publicar oportunidades com mais confianca.", 15, Color.WHITE, true, Color.rgb(217, 119, 6));
            aviso.setGravity(Gravity.CENTER);
            aviso.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    montarTelaPerfil();
                }
            });
            raiz.addView(aviso);
        }

        raiz.addView(espaco(12));
        raiz.addView(linhaAcoesPrincipais());
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

    private LinearLayout linhaAcoesPrincipais() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linha1 = new LinearLayout(this);
        linha1.setOrientation(LinearLayout.HORIZONTAL);
        linha1.addView(botaoTexto("Ver oportunidades", Color.rgb(22, 163, 74), Color.WHITE, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                montarTelaPrincipal();
            }
        }), peso());
        linha1.addView(espacoLargura(8));
        linha1.addView(botaoTexto("Publicar", Color.rgb(15, 23, 42), Color.WHITE, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                montarTelaPublicar();
            }
        }), peso());
        box.addView(linha1);

        box.addView(espaco(8));

        box.addView(botaoTexto("Meu perfil", Color.WHITE, Color.rgb(15, 23, 42), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                montarTelaPerfil();
            }
        }), larguraAltura(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));

        return box;
    }

    private String textoPerfilResumo() {
        String cidade = perfilCidade.trim().length() == 0 ? "Cidade nao informada" : perfilCidade;
        String tipo = perfilTipoUsuario.trim().length() == 0 ? "Usuario local" : perfilTipoUsuario;
        return cidade + " - " + tipo;
    }

    private void montarTelaPerfil() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(241, 245, 249));

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(16), dp(16), dp(16), dp(24));

        raiz.addView(bloco("Meu perfil\n\nCrie uma identidade para publicar e negociar com mais confianca.", 23, Color.WHITE, true, Color.rgb(15, 23, 42)), larguraAltura(LinearLayout.LayoutParams.MATCH_PARENT, dp(150)));
        raiz.addView(espaco(12));

        final EditText nome = campo("Seu nome ou nome da empresa");
        final EditText cidade = campo("Cidade / bairro");
        final EditText telefone = campo("WhatsApp para contato");
        final EditText tipoUsuario = campo("Tipo: Trabalhador, Contratante, Empresa ou Autonomo");

        nome.setText(perfilNome);
        cidade.setText(perfilCidade);
        telefone.setText(perfilTelefone);
        tipoUsuario.setText(perfilTipoUsuario);

        raiz.addView(nome);
        raiz.addView(espaco(8));
        raiz.addView(cidade);
        raiz.addView(espaco(8));
        raiz.addView(telefone);
        raiz.addView(espaco(8));
        raiz.addView(tipoUsuario);
        raiz.addView(espaco(14));

        raiz.addView(botaoTexto("Salvar perfil", Color.rgb(22, 163, 74), Color.WHITE, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nomeTexto = nome.getText().toString().trim();
                String cidadeTexto = cidade.getText().toString().trim();
                String telefoneTexto = telefone.getText().toString().trim();
                String tipoTexto = tipoUsuario.getText().toString().trim();

                if (nomeTexto.length() == 0 || telefoneTexto.length() == 0) {
                    Toast.makeText(MainActivity.this, "Preencha pelo menos nome e WhatsApp.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (tipoTexto.length() == 0) {
                    tipoTexto = "Usuario local";
                }

                salvarPerfil(nomeTexto, cidadeTexto, telefoneTexto, tipoTexto);
                Toast.makeText(MainActivity.this, "Perfil salvo com sucesso.", Toast.LENGTH_LONG).show();
                montarTelaPrincipal();
            }
        }), larguraAltura(LinearLayout.LayoutParams.MATCH_PARENT, dp(50)));

        raiz.addView(espaco(10));
        raiz.addView(bloco("Suas oportunidades publicadas ficam salvas neste aparelho. Na proxima etapa elas vao para um banco online.", 14, Color.rgb(71, 85, 105), false, Color.WHITE));
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

    private void montarTelaPublicar() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(241, 245, 249));

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(16), dp(16), dp(16), dp(24));

        raiz.addView(bloco("Publicar oportunidade\n\nCadastre uma vaga, bico ou servico.", 24, Color.WHITE, true, Color.rgb(15, 23, 42)), larguraAltura(LinearLayout.LayoutParams.MATCH_PARENT, dp(140)));
        raiz.addView(espaco(12));

        if (!perfilCompleto()) {
            raiz.addView(bloco("Dica: cadastre seu perfil antes de publicar para passar mais confianca.", 14, Color.WHITE, true, Color.rgb(217, 119, 6)));
            raiz.addView(espaco(10));
        }

        final EditText tipo = campo("Tipo: VAGA, BICO, SERVICO ou URGENTE");
        final EditText titulo = campo("Titulo da oportunidade");
        final EditText cidade = campo("Cidade / bairro");
        final EditText valor = campo("Valor ou salario");
        final EditText descricao = campo("Descricao");
        final EditText contato = campo("WhatsApp para contato");

        if (perfilTelefone.trim().length() > 0) {
            contato.setText(perfilTelefone);
        }
        if (perfilCidade.trim().length() > 0) {
            cidade.setText(perfilCidade);
        }

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

                String autor = perfilNome.trim().length() == 0 ? "Usuario local" : perfilNome;
                oportunidades.add(0, new Oportunidade(tipoTexto, tituloTexto, cidadeTexto, valorTexto, descricaoTexto, contatoTexto, autor));
                salvarOportunidades();
                Toast.makeText(MainActivity.this, "Oportunidade publicada e salva neste aparelho.", Toast.LENGTH_LONG).show();
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
        card.addView(texto("Publicado por: " + item.autor, 13, Color.rgb(100, 116, 139), false));
        card.addView(texto(item.valor.length() == 0 ? "Valor a combinar" : item.valor, 16, Color.rgb(22, 163, 74), true));
        card.addView(texto(item.descricao.length() == 0 ? "Sem descricao informada." : item.descricao, 14, Color.rgb(51, 65, 85), false));

        TextView interesse = bloco("Chamar no WhatsApp", 15, Color.WHITE, true, Color.rgb(15, 23, 42));
        interesse.setGravity(Gravity.CENTER);
        interesse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirWhatsApp(item);
            }
        });
        card.addView(espaco(8));
        card.addView(interesse, larguraAltura(LinearLayout.LayoutParams.MATCH_PARENT, dp(44)));

        conteudoContainer.addView(card);
        conteudoContainer.addView(espaco(10));
    }

    private void abrirWhatsApp(Oportunidade item) {
        String numero = limparNumero(item.contato);
        if (numero.length() == 0) {
            Toast.makeText(this, "Contato nao informado.", Toast.LENGTH_LONG).show();
            return;
        }

        String mensagem = "Ola, vi sua oportunidade no Chama no Trampo e tenho interesse.\n\n" +
                "Oportunidade: " + item.titulo + "\n" +
                "Local: " + item.local;

        String url = "https://wa.me/" + numero + "?text=" + Uri.encode(mensagem);

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception erro) {
            Toast.makeText(this, "Nao consegui abrir o WhatsApp neste aparelho.", Toast.LENGTH_LONG).show();
        }
    }

    private String limparNumero(String contato) {
        StringBuilder numeros = new StringBuilder();
        for (int i = 0; i < contato.length(); i++) {
            char c = contato.charAt(i);
            if (c >= '0' && c <= '9') {
                numeros.append(c);
            }
        }

        String numero = numeros.toString();
        if (numero.length() == 10 || numero.length() == 11) {
            numero = "55" + numero;
        }
        return numero;
    }

    private String codificar(String texto) {
        if (texto == null) return "";
        return Uri.encode(texto);
    }

    private String decodificar(String texto) {
        if (texto == null) return "";
        return Uri.decode(texto);
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
        String autor;

        Oportunidade(String tipo, String titulo, String local, String valor, String descricao, String contato, String autor) {
            this.tipo = tipo;
            this.titulo = titulo;
            this.local = local;
            this.valor = valor;
            this.descricao = descricao;
            this.contato = contato;
            this.autor = autor;
        }
    }
}
