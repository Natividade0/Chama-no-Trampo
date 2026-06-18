package com.chamanotrampo.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
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

    private static final int CREAM = Color.rgb(255, 248, 239);
    private static final int INK = Color.rgb(43, 34, 80);
    private static final int INK_LIGHT = Color.rgb(119, 110, 142);
    private static final int YELLOW = Color.rgb(255, 210, 63);
    private static final int TANGERINE = Color.rgb(255, 140, 66);
    private static final int CORAL = Color.rgb(255, 92, 92);
    private static final int GREEN = Color.rgb(6, 193, 103);
    private static final int WHATSAPP = Color.rgb(31, 184, 85);
    private static final int BLUE = Color.rgb(61, 174, 255);
    private static final int PURPLE = Color.rgb(155, 93, 229);
    private static final int WHITE = Color.WHITE;

    private static final int VAGA_TINT = Color.rgb(227, 251, 240);
    private static final int SERVICO_TINT = Color.rgb(230, 244, 255);
    private static final int BICO_TINT = Color.rgb(243, 234, 254);
    private static final int URGENTE_TINT = Color.rgb(255, 234, 234);
    private static final int SEGURANCA_TINT = Color.rgb(255, 241, 214);

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
            oportunidades.add(new Oportunidade("SEGURANCA", "Tecnico de seguranca do trabalho", "Ribeirao Preto e regiao", "Enviar pretensao", "Acompanhamento de obra e documentacao.", "16999999999", "Equipe Chama no Trampo"));
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
        scroll.setBackgroundColor(CREAM);

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(18), dp(18), dp(18), dp(128));

        raiz.addView(headerPrincipal());
        raiz.addView(espaco(12));
        raiz.addView(profileBanner());
        raiz.addView(espaco(12));
        raiz.addView(searchBar());
        raiz.addView(espaco(8));
        raiz.addView(chipsCategorias());
        raiz.addView(espaco(10));
        raiz.addView(sectionLabel());
        raiz.addView(espaco(8));

        conteudoContainer = new LinearLayout(this);
        conteudoContainer.setOrientation(LinearLayout.VERTICAL);
        raiz.addView(conteudoContainer);

        scroll.addView(raiz);
        setContentViewComNav(scroll, "inicio", true);

        atualizarLista(filtroAtual);
    }

    private View headerPrincipal() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(20), dp(18), dp(20), dp(18));
        header.setBackground(gradient(YELLOW, TANGERINE, dp(28)));
        header.setElevation(dp(4));

        TextView marca = texto("Chama no Trampo", 29, INK, true);
        marca.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        TextView subtitulo = texto("Empregos, bicos e servicos perto de voce", 15, Color.rgb(91, 62, 30), true);

        header.addView(marca);
        header.addView(subtitulo);

        return header;
    }

    private View profileBanner() {
        if (perfilCompleto()) {
            LinearLayout box = new LinearLayout(this);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setPadding(dp(16), dp(13), dp(16), dp(13));
            box.setBackground(bg(WHITE, dp(18)));
            box.setElevation(dp(3));

            box.addView(texto("Perfil ativo: " + perfilNome, 15, INK, true));
            box.addView(texto(textoPerfilResumo(), 13, INK_LIGHT, false));

            box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    montarTelaPerfil();
                }
            });
            return box;
        }

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(13), dp(16), dp(13));
        box.setBackground(bg(WHITE, dp(18)));
        box.setElevation(dp(3));

        TextView linha = texto("Falta pouco! Complete seu perfil e apareca mais nas buscas", 14, INK, true);
        box.addView(linha);
        box.addView(espaco(8));

        LinearLayout trilha = new LinearLayout(this);
        trilha.setBackground(bg(Color.rgb(241, 236, 223), dp(8)));
        LinearLayout preenchido = new LinearLayout(this);
        preenchido.setBackground(bg(GREEN, dp(8)));
        trilha.addView(preenchido, new LinearLayout.LayoutParams(0, dp(7), 60));
        box.addView(trilha, larguraAltura(LinearLayout.LayoutParams.MATCH_PARENT, dp(7)));

        box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                montarTelaPerfil();
            }
        });

        return box;
    }

    private View searchBar() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(dp(16), dp(12), dp(16), dp(12));
        box.setBackground(bg(WHITE, dp(999)));
        box.setElevation(dp(3));

        TextView icon = texto("🔎", 17, INK_LIGHT, false);
        TextView hint = texto("Buscar: pedreiro, ajudante, eletricista...", 14, INK_LIGHT, true);

        box.addView(icon);
        box.addView(espacoLargura(8));
        box.addView(hint, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Busca avancada entra na proxima etapa.", Toast.LENGTH_SHORT).show();
            }
        });

        return box;
    }

    private View chipsCategorias() {
        HorizontalScrollView horizontal = new HorizontalScrollView(this);
        horizontal.setHorizontalScrollBarEnabled(false);

        LinearLayout linha = new LinearLayout(this);
        linha.setOrientation(LinearLayout.HORIZONTAL);
        linha.setPadding(0, dp(4), 0, dp(4));

        linha.addView(chip("Todos", "TODOS"));
        linha.addView(chip("Vagas", "VAGA"));
        linha.addView(chip("Bicos", "BICO"));
        linha.addView(chip("Servicos", "SERVICO"));
        linha.addView(chip("Urgentes", "URGENTE"));
        linha.addView(chip("Seguranca", "SEGURANCA"));

        horizontal.addView(linha);
        return horizontal;
    }

    private TextView chip(String nome, final String filtro) {
        boolean ativo = filtro.equals(filtroAtual);
        int fundo = ativo ? INK : tintTipo(filtro);
        int texto = ativo ? WHITE : corTipo(filtro);

        TextView chip = texto(nome, 14, texto, true);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(17), dp(9), dp(17), dp(9));
        chip.setBackground(bg(fundo, dp(999)));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(42));
        lp.setMargins(0, 0, dp(9), 0);
        chip.setLayoutParams(lp);

        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filtroAtual = filtro;
                montarTelaPrincipal();
            }
        });

        return chip;
    }

    private View sectionLabel() {
        int total = contarFiltro(filtroAtual);
        String categoria = "TODOS".equals(filtroAtual) ? "perto de voce" : filtroAtual.toLowerCase();
        return texto(total + " oportunidades " + categoria, 14, INK_LIGHT, true);
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
                conteudoContainer.addView(cardOportunidade(item));
                conteudoContainer.addView(espaco(14));
                total++;
            }
        }

        if (total == 0) {
            conteudoContainer.addView(bloco("Nenhuma oportunidade encontrada nesta categoria.", 15, INK_LIGHT, false, WHITE, dp(18)));
        }
    }

    private View cardOportunidade(final Oportunidade item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(18));
        card.setBackground(bg(tintTipo(item.tipo), dp(24)));
        card.setElevation(dp(3));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.HORIZONTAL);
        left.setGravity(Gravity.CENTER_VERTICAL);

        TextView circle = texto(iconTipo(item.tipo), 15, WHITE, true);
        circle.setGravity(Gravity.CENTER);
        circle.setBackground(bg(corTipo(item.tipo), dp(999)));
        left.addView(circle, larguraAltura(dp(34), dp(34)));
        left.addView(espacoLargura(9));

        TextView badge = texto(labelTipo(item.tipo), 12, corTipo(item.tipo), true);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(10), dp(5), dp(10), dp(5));
        badge.setBackground(bg(Color.argb(105, 255, 255, 255), dp(999)));
        left.addView(badge);

        top.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView distancia = texto(distanciaFake(item), 12, INK_LIGHT, true);
        distancia.setGravity(Gravity.RIGHT);
        top.addView(distancia);

        card.addView(top);
        card.addView(espaco(9));

        TextView titulo = texto(item.titulo, 21, INK, true);
        card.addView(titulo);

        card.addView(texto(item.local, 14, INK_LIGHT, true));

        String autor = item.autor == null || item.autor.trim().length() == 0 ? "Usuario local" : item.autor;
        card.addView(texto("por " + autor, 12, INK_LIGHT, false));

        card.addView(espaco(4));
        card.addView(texto(item.valor.length() == 0 ? "Valor a combinar" : item.valor, 15, corPreco(item.valor), true));
        card.addView(texto(item.descricao.length() == 0 ? "Sem descricao informada." : item.descricao, 14, Color.rgb(86, 76, 114), false));
        card.addView(espaco(12));

        TextView cta = texto("☎  Chamar no WhatsApp", 15, WHITE, true);
        cta.setGravity(Gravity.CENTER);
        cta.setPadding(0, dp(12), 0, dp(12));
        cta.setBackground(bg(WHATSAPP, dp(999)));
        cta.setElevation(dp(3));
        cta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirWhatsApp(item);
            }
        });

        card.addView(cta, larguraAltura(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));

        return card;
    }

    private void montarTelaPublicar() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(CREAM);

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(18), dp(18), dp(18), dp(128));

        raiz.addView(headerSecundario("Publicar oportunidade", "Cadastre uma vaga, bico ou servico."));
        raiz.addView(espaco(12));

        if (!perfilCompleto()) {
            raiz.addView(bloco("Dica: cadastre seu perfil antes de publicar para passar mais confianca.", 14, WHITE, true, TANGERINE, dp(18)));
            raiz.addView(espaco(10));
        }

        final EditText tipo = campo("Tipo: VAGA, BICO, SERVICO, URGENTE ou SEGURANCA");
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

        raiz.addView(botaoTexto("Salvar oportunidade", GREEN, WHITE, new View.OnClickListener() {
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
                filtroAtual = "TODOS";
                Toast.makeText(MainActivity.this, "Oportunidade publicada e salva neste aparelho.", Toast.LENGTH_LONG).show();
                montarTelaPrincipal();
            }
        }), larguraAltura(LinearLayout.LayoutParams.MATCH_PARENT, dp(50)));

        raiz.addView(espaco(10));
        raiz.addView(botaoTexto("Voltar", WHITE, INK, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                montarTelaPrincipal();
            }
        }), larguraAltura(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));

        scroll.addView(raiz);
        setContentViewComNav(scroll, "publicar", false);
    }

    private void montarTelaPerfil() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(CREAM);

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(18), dp(18), dp(18), dp(128));

        raiz.addView(headerSecundario("Meu perfil", "Crie uma identidade para publicar e negociar com mais confianca."));
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

        raiz.addView(botaoTexto("Salvar perfil", GREEN, WHITE, new View.OnClickListener() {
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
        raiz.addView(bloco("Suas oportunidades publicadas ficam salvas neste aparelho. Na proxima etapa elas vao para um banco online.", 14, INK_LIGHT, false, WHITE, dp(18)));
        raiz.addView(espaco(10));
        raiz.addView(botaoTexto("Voltar", WHITE, INK, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                montarTelaPrincipal();
            }
        }), larguraAltura(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));

        scroll.addView(raiz);
        setContentViewComNav(scroll, "perfil", false);
    }

    private View headerSecundario(String titulo, String subtitulo) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(20), dp(18), dp(20), dp(18));
        header.setBackground(gradient(YELLOW, TANGERINE, dp(28)));
        header.setElevation(dp(4));

        header.addView(texto(titulo, 26, INK, true));
        header.addView(texto(subtitulo, 15, Color.rgb(91, 62, 30), true));

        return header;
    }

    private void setContentViewComNav(View conteudo, String ativo, boolean mostrarFab) {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(CREAM);

        root.addView(conteudo, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        LinearLayout nav = bottomNav(ativo);
        FrameLayout.LayoutParams navLp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(72), Gravity.BOTTOM);
        root.addView(nav, navLp);

        if (mostrarFab) {
            TextView fab = texto("+", 30, WHITE, true);
            fab.setGravity(Gravity.CENTER);
            fab.setBackground(bg(CORAL, dp(999)));
            fab.setElevation(dp(8));
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    montarTelaPublicar();
                }
            });

            FrameLayout.LayoutParams fabLp = new FrameLayout.LayoutParams(dp(58), dp(58), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            fabLp.setMargins(0, 0, 0, dp(42));
            root.addView(fab, fabLp);
        }

        setContentView(root);
    }

    private LinearLayout bottomNav(String ativo) {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(14), dp(6), dp(14), dp(6));
        nav.setBackgroundColor(WHITE);
        nav.setElevation(dp(8));

        nav.addView(navItem("⌂\nInicio", "inicio", ativo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filtroAtual = "TODOS";
                montarTelaPrincipal();
            }
        }), pesoNav());

        nav.addView(navItem("⌕\nBuscar", "buscar", ativo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Busca avancada entra na proxima etapa.", Toast.LENGTH_SHORT).show();
            }
        }), pesoNav());

        TextView espacoFab = new TextView(this);
        nav.addView(espacoFab, pesoNav());

        nav.addView(navItem("☏\nChat", "chat", ativo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Chat interno entra depois do banco online.", Toast.LENGTH_SHORT).show();
            }
        }), pesoNav());

        nav.addView(navItem("♙\nPerfil", "perfil", ativo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                montarTelaPerfil();
            }
        }), pesoNav());

        return nav;
    }

    private TextView navItem(String texto, String chave, String ativo, View.OnClickListener listener) {
        boolean isAtivo = chave.equals(ativo);
        TextView item = texto(texto, 11, isAtivo ? INK : INK_LIGHT, true);
        item.setGravity(Gravity.CENTER);
        item.setSingleLine(false);
        item.setBackground(isAtivo ? bg(Color.rgb(255, 241, 214), dp(18)) : bg(Color.TRANSPARENT, dp(18)));
        item.setOnClickListener(listener);
        return item;
    }

    private LinearLayout.LayoutParams pesoNav() {
        return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
    }

    private int contarFiltro(String filtro) {
        int total = 0;
        for (int i = 0; i < oportunidades.size(); i++) {
            Oportunidade item = oportunidades.get(i);
            if ("TODOS".equals(filtro) || item.tipo.equals(filtro)) {
                total++;
            }
        }
        return total;
    }

    private String textoPerfilResumo() {
        String cidade = perfilCidade.trim().length() == 0 ? "Cidade nao informada" : perfilCidade;
        String tipo = perfilTipoUsuario.trim().length() == 0 ? "Usuario local" : perfilTipoUsuario;
        return cidade + " - " + tipo;
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

    private int tintTipo(String tipo) {
        if ("URGENTE".equals(tipo)) return URGENTE_TINT;
        if ("SERVICO".equals(tipo)) return SERVICO_TINT;
        if ("BICO".equals(tipo)) return BICO_TINT;
        if ("SEGURANCA".equals(tipo)) return SEGURANCA_TINT;
        return VAGA_TINT;
    }

    private int corTipo(String tipo) {
        if ("URGENTE".equals(tipo)) return CORAL;
        if ("SERVICO".equals(tipo)) return BLUE;
        if ("BICO".equals(tipo)) return PURPLE;
        if ("SEGURANCA".equals(tipo)) return TANGERINE;
        return GREEN;
    }

    private String iconTipo(String tipo) {
        if ("URGENTE".equals(tipo)) return "!";
        if ("SERVICO".equals(tipo)) return "⚒";
        if ("BICO".equals(tipo)) return "⚡";
        if ("SEGURANCA".equals(tipo)) return "✓";
        return "▣";
    }

    private String labelTipo(String tipo) {
        if ("SERVICO".equals(tipo)) return "SERVICO";
        if ("BICO".equals(tipo)) return "BICO";
        if ("URGENTE".equals(tipo)) return "URGENTE";
        if ("SEGURANCA".equals(tipo)) return "SEGURANCA";
        return "VAGA";
    }

    private int corPreco(String valor) {
        if (valor == null) return INK_LIGHT;
        if (valor.contains("R$")) return Color.rgb(10, 138, 77);
        return INK_LIGHT;
    }

    private String distanciaFake(Oportunidade item) {
        int n = Math.abs((item.titulo + item.local).hashCode()) % 48;
        if (n < 3) n = n + 1;
        return n + " km";
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
        editText.setPadding(dp(14), dp(12), dp(14), dp(12));
        editText.setBackground(bg(WHITE, dp(18)));
        editText.setTextColor(INK);
        editText.setHintTextColor(INK_LIGHT);
        return editText;
    }

    private TextView botaoTexto(String texto, int fundo, int corTexto, View.OnClickListener listener) {
        TextView view = bloco(texto, 15, corTexto, true, fundo, dp(999));
        view.setGravity(Gravity.CENTER);
        view.setOnClickListener(listener);
        view.setElevation(dp(2));
        return view;
    }

    private TextView bloco(String texto, int tamanho, int corTexto, boolean negrito, int fundo, int raio) {
        TextView view = texto(texto, tamanho, corTexto, negrito);
        view.setPadding(dp(16), dp(13), dp(16), dp(13));
        view.setBackground(bg(fundo, raio));
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

    private GradientDrawable bg(int cor, int raio) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(cor);
        drawable.setCornerRadius(raio);
        return drawable;
    }

    private GradientDrawable gradient(int inicio, int fim, int raio) {
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{inicio, fim});
        drawable.setCornerRadius(raio);
        return drawable;
    }

    private void mostrarTelaErro(Throwable erro) {
        TextView view = new TextView(this);
        view.setText("Chama no Trampo\n\nO app abriu, mas houve erro na tela.\n\n" + erro.getClass().getSimpleName() + "\n" + erro.getMessage());
        view.setTextSize(18);
        view.setTextColor(WHITE);
        view.setBackgroundColor(Color.rgb(127, 29, 29));
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(24), dp(24), dp(24), dp(24));
        setContentView(view);
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
