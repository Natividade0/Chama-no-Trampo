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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String PREFS = "chama_no_trampo_prefs";

    private static final String KEY_NOME = "perfil_nome";
    private static final String KEY_CIDADE = "perfil_cidade";
    private static final String KEY_TELEFONE = "perfil_telefone";
    private static final String KEY_TIPO_USUARIO = "perfil_tipo_usuario";
    private static final String KEY_OPORTUNIDADES = "oportunidades_salvas";
    private static final String KEY_CHAT = "chat_proposta_";
    private static final String KEY_STATUS = "status_conversa_";
    private static final String KEY_AVALIACAO = "avaliacao_proposta_";
    private static final String KEY_FAVORITO = "favorito_proposta_";
    private static final String KEY_NOTIFICACOES = "notificacoes_internas";

    private static final String AGUARDANDO = "AGUARDANDO_RESPOSTA";
    private static final String EM_CONVERSA = "EM_CONVERSA";
    private static final String COMBINADO = "COMBINADO";
    private static final String CONCLUIDO = "CONCLUIDO";

    private static final String TELA_HOME = "home";
    private static final String TELA_BUSCA = "busca";
    private static final String TELA_RESULTADO_BUSCA = "resultado_busca";
    private static final String TELA_DETALHES = "detalhes";
    private static final String TELA_CHAT = "chat";
    private static final String TELA_AVALIACAO = "avaliacao";
    private static final String TELA_FAVORITOS = "favoritos";
    private static final String TELA_AVISOS = "avisos";
    private static final String TELA_PUBLICAR = "publicar";
    private static final String TELA_PERFIL = "perfil";
    private static final String TELA_CONVERSAS = "conversas";

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

    private final ArrayList<Oportunidade> oportunidades = new ArrayList<Oportunidade>();

    private LinearLayout listaContainer;
    private String filtroAtual = "TODOS";
    private String telaAtual = TELA_HOME;
    private String origemAtual = "home";
    private String termoBuscaAtual = "";
    private Oportunidade oportunidadeAtual;

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
            mostrarHome();
        } catch (Throwable erro) {
            mostrarTelaErro(erro);
        }
    }

    @Override
    public void onBackPressed() {
        if (TELA_HOME.equals(telaAtual)) {
            super.onBackPressed();
            return;
        }

        if (TELA_DETALHES.equals(telaAtual)) {
            voltarParaOrigem();
            return;
        }

        if (TELA_CHAT.equals(telaAtual) && oportunidadeAtual != null) {
            mostrarDetalhes(oportunidadeAtual, origemAtual);
            return;
        }

        if (TELA_AVALIACAO.equals(telaAtual) && oportunidadeAtual != null) {
            mostrarChat(oportunidadeAtual);
            return;
        }

        if (TELA_RESULTADO_BUSCA.equals(telaAtual)) {
            mostrarBusca(termoBuscaAtual);
            return;
        }

        mostrarHome();
    }

    private SharedPreferences prefs() {
        return getSharedPreferences(PREFS, MODE_PRIVATE);
    }

    private void carregarPerfil() {
        SharedPreferences p = prefs();
        perfilNome = p.getString(KEY_NOME, "");
        perfilCidade = p.getString(KEY_CIDADE, "");
        perfilTelefone = p.getString(KEY_TELEFONE, "");
        perfilTipoUsuario = p.getString(KEY_TIPO_USUARIO, "");
    }

    private void salvarPerfil(String nome, String cidade, String telefone, String tipoUsuario) {
        perfilNome = nome;
        perfilCidade = cidade;
        perfilTelefone = telefone;
        perfilTipoUsuario = tipoUsuario;

        prefs().edit()
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
        String salvo = prefs().getString(KEY_OPORTUNIDADES, "");

        if (salvo != null && salvo.trim().length() > 0) {
            String[] linhas = salvo.split("\\n");
            for (int i = 0; i < linhas.length; i++) {
                String[] campos = linhas[i].split("\\|", -1);
                if (campos.length >= 8) {
                    oportunidades.add(new Oportunidade(
                            dec(campos[0]),
                            dec(campos[1]),
                            dec(campos[2]),
                            dec(campos[3]),
                            dec(campos[4]),
                            dec(campos[5]),
                            dec(campos[6]),
                            dec(campos[7])
                    ));
                } else if (campos.length >= 7) {
                    Oportunidade antiga = new Oportunidade(
                            "",
                            dec(campos[0]),
                            dec(campos[1]),
                            dec(campos[2]),
                            dec(campos[3]),
                            dec(campos[4]),
                            dec(campos[5]),
                            dec(campos[6])
                    );
                    antiga.id = chaveLegada(antiga);
                    oportunidades.add(antiga);
                }
            }
        }

        if (oportunidades.isEmpty()) {
            adicionarExemplos();
        }
    }

    private void adicionarExemplos() {
        oportunidades.add(new Oportunidade("exemplo_auxiliar", "VAGA", "Auxiliar de producao", "Guariba - Centro", "Salario a combinar", "Empresa local buscando inicio imediato.", "16999999999", "Equipe Chama no Trampo"));
        oportunidades.add(new Oportunidade("exemplo_pedreiro", "SERVICO", "Pedreiro para reforma", "Jardinopolis", "Enviar orcamento", "Cliente precisa reformar uma area pequena.", "16999999999", "Equipe Chama no Trampo"));
        oportunidades.add(new Oportunidade("exemplo_ajudante", "BICO", "Ajudante para descarregar caminhao", "Ribeirao Preto", "R$ 120,00 no dia", "Servico rapido, pagamento no mesmo dia.", "16999999999", "Equipe Chama no Trampo"));
        oportunidades.add(new Oportunidade("exemplo_eletricista", "URGENTE", "Eletricista hoje", "Jaboticabal", "Valor a combinar", "Atendimento ainda hoje.", "16999999999", "Equipe Chama no Trampo"));
        oportunidades.add(new Oportunidade("exemplo_tst", "SEGURANCA", "Tecnico de seguranca do trabalho", "Ribeirao Preto e regiao", "Enviar pretensao", "Acompanhamento de obra e documentacao.", "16999999999", "Equipe Chama no Trampo"));
    }

    private void salvarOportunidades() {
        StringBuilder b = new StringBuilder();

        for (int i = 0; i < oportunidades.size(); i++) {
            Oportunidade o = oportunidades.get(i);
            if (o.id == null || o.id.trim().length() == 0) {
                o.id = chaveLegada(o);
            }

            b.append(enc(o.id)).append("|")
                    .append(enc(o.tipo)).append("|")
                    .append(enc(o.titulo)).append("|")
                    .append(enc(o.local)).append("|")
                    .append(enc(o.valor)).append("|")
                    .append(enc(o.descricao)).append("|")
                    .append(enc(o.contato)).append("|")
                    .append(enc(o.autor));

            if (i < oportunidades.size() - 1) {
                b.append("\n");
            }
        }

        prefs().edit().putString(KEY_OPORTUNIDADES, b.toString()).apply();
    }

    private void mostrarHome() {
        telaAtual = TELA_HOME;
        oportunidadeAtual = null;
        origemAtual = "home";

        LinearLayout raiz = base();
        raiz.addView(header("Chama no Trampo", "Empregos, bicos e servicos perto de voce"));
        raiz.addView(espaco(12));
        raiz.addView(cardPerfilResumo());
        raiz.addView(espaco(12));
        raiz.addView(cardBusca());
        raiz.addView(espaco(8));
        raiz.addView(atalhosHome());
        raiz.addView(espaco(8));
        raiz.addView(chipsCategorias());
        raiz.addView(espaco(10));
        raiz.addView(texto(contarFiltro(filtroAtual) + " oportunidades " + textoFiltroAtual(), 14, INK_LIGHT, true));
        raiz.addView(espaco(8));

        listaContainer = coluna();
        raiz.addView(listaContainer);

        setContentViewComNav(scroll(raiz), "inicio", true);
        atualizarListaHome();
    }

    private String textoFiltroAtual() {
        return "TODOS".equals(filtroAtual) ? "perto de voce" : filtroAtual.toLowerCase();
    }

    private View cardPerfilResumo() {
        LinearLayout card = card(WHITE);

        if (perfilCompleto()) {
            card.addView(texto("Perfil ativo: " + perfilNome, 15, INK, true));
            card.addView(texto(resumoPerfil(), 13, INK_LIGHT, false));
        } else {
            card.addView(texto("Complete seu perfil para publicar e negociar com mais confianca", 14, INK, true));
        }

        card.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mostrarPerfil();
            }
        });

        return card;
    }

    private String resumoPerfil() {
        String cidade = perfilCidade.trim().length() == 0 ? "Cidade nao informada" : perfilCidade;
        String tipo = perfilTipoUsuario.trim().length() == 0 ? "Usuario local" : perfilTipoUsuario;
        return cidade + " - " + tipo;
    }

    private View cardBusca() {
        LinearLayout card = linha();
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(12), dp(16), dp(12));
        card.setBackground(bg(WHITE, 999));
        card.setElevation(dp(3));

        card.addView(texto("Buscar: pedreiro, eletricista, cidade...", 14, INK_LIGHT, true), peso(1));
        card.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mostrarBusca("");
            }
        });

        return card;
    }

    private View atalhosHome() {
        LinearLayout linha = linha();
        linha.addView(botao("Favoritos", WHITE, INK, new View.OnClickListener() {
            public void onClick(View v) {
                mostrarFavoritos();
            }
        }), new LinearLayout.LayoutParams(0, dp(44), 1));

        linha.addView(espacoLargura(8));

        linha.addView(botao("Avisos internos", WHITE, INK, new View.OnClickListener() {
            public void onClick(View v) {
                mostrarAvisosInternos();
            }
        }), new LinearLayout.LayoutParams(0, dp(44), 1));

        return linha;
    }

    private View chipsCategorias() {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);

        LinearLayout linha = linha();
        linha.addView(chipCategoria("Todos", "TODOS"));
        linha.addView(chipCategoria("Vagas", "VAGA"));
        linha.addView(chipCategoria("Bicos", "BICO"));
        linha.addView(chipCategoria("Servicos", "SERVICO"));
        linha.addView(chipCategoria("Urgentes", "URGENTE"));
        linha.addView(chipCategoria("Seguranca", "SEGURANCA"));

        scroll.addView(linha);
        return scroll;
    }

    private TextView chipCategoria(String rotulo, final String filtro) {
        boolean ativo = filtro.equals(filtroAtual);
        TextView chip = texto(rotulo, 14, ativo ? WHITE : corTipo(filtro), true);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(17), dp(9), dp(17), dp(9));
        chip.setBackground(bg(ativo ? INK : tintTipo(filtro), 999));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(42));
        params.setMargins(0, 0, dp(9), 0);
        chip.setLayoutParams(params);

        chip.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                filtroAtual = filtro;
                mostrarHome();
            }
        });

        return chip;
    }

    private void atualizarListaHome() {
        listaContainer.removeAllViews();
        int total = 0;

        for (int i = 0; i < oportunidades.size(); i++) {
            Oportunidade o = oportunidades.get(i);
            if ("TODOS".equals(filtroAtual) || o.tipo.equals(filtroAtual)) {
                listaContainer.addView(cardOportunidadeResumo(o, "home"));
                listaContainer.addView(espaco(14));
                total++;
            }
        }

        if (total == 0) {
            listaContainer.addView(bloco("Nenhuma oportunidade nesta categoria.", 14, INK_LIGHT, false, WHITE, 18));
        }
    }

    private View cardOportunidadeResumo(final Oportunidade o, final String origem) {
        LinearLayout card = card(tintTipo(o.tipo));

        LinearLayout topo = linha();
        topo.setGravity(Gravity.CENTER_VERTICAL);
        topo.addView(pill(labelTipo(o.tipo), corTipo(o.tipo), Color.argb(105, 255, 255, 255)), peso(1));
        topo.addView(texto(isFavorito(o) ? "Favorito" : distanciaFake(o), 12, isFavorito(o) ? TANGERINE : INK_LIGHT, true));
        card.addView(topo);

        card.addView(espaco(8));
        card.addView(texto(o.titulo, 21, INK, true));
        card.addView(texto(o.local, 14, INK_LIGHT, true));
        card.addView(texto(valorVisivel(o), 15, corPreco(o.valor), true));
        card.addView(espaco(8));
        card.addView(statusPill(statusConversa(o)));
        card.addView(espaco(10));
        card.addView(botao("Abrir detalhes", INK, WHITE, new View.OnClickListener() {
            public void onClick(View v) {
                mostrarDetalhes(o, origem);
            }
        }), lpMatchAltura(48));

        card.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mostrarDetalhes(o, origem);
            }
        });

        return card;
    }

    private void mostrarDetalhes(final Oportunidade o, final String origem) {
        telaAtual = TELA_DETALHES;
        oportunidadeAtual = o;
        origemAtual = origem == null ? "home" : origem;

        LinearLayout raiz = base();
        raiz.addView(header("Detalhes da oportunidade", "Veja tudo antes de conversar."));
        raiz.addView(espaco(12));

        LinearLayout card = card(tintTipo(o.tipo));
        card.addView(pill(labelTipo(o.tipo), corTipo(o.tipo), Color.argb(105, 255, 255, 255)));
        card.addView(espaco(6));
        card.addView(texto(o.titulo, 24, INK, true));
        card.addView(texto(o.local, 15, INK_LIGHT, true));
        card.addView(texto("por " + autor(o), 13, INK_LIGHT, false));
        card.addView(espaco(8));
        card.addView(texto(valorVisivel(o), 16, corPreco(o.valor), true));
        card.addView(espaco(8));
        card.addView(texto(descricaoVisivel(o), 15, Color.rgb(86, 76, 114), false));
        card.addView(espaco(10));
        card.addView(statusPill(statusConversa(o)));

        if (temAvaliacao(o)) {
            card.addView(espaco(8));
            card.addView(resumoAvaliacao(o));
        }

        raiz.addView(card);
        raiz.addView(espaco(12));

        raiz.addView(botao(isFavorito(o) ? "Remover dos favoritos" : "Salvar favorito", isFavorito(o) ? CORAL : TANGERINE, WHITE, new View.OnClickListener() {
            public void onClick(View v) {
                alternarFavorito(o);
                if ("favoritos".equals(origemAtual) && !isFavorito(o)) {
                    mostrarFavoritos();
                } else {
                    mostrarDetalhes(o, origemAtual);
                }
            }
        }), lpMatchAltura(48));

        raiz.addView(espaco(8));
        raiz.addView(botao("Abrir conversa", INK, WHITE, new View.OnClickListener() {
            public void onClick(View v) {
                mostrarChat(o);
            }
        }), lpMatchAltura(48));

        raiz.addView(espaco(8));
        raiz.addView(botao("Chamar no WhatsApp", WHATSAPP, WHITE, new View.OnClickListener() {
            public void onClick(View v) {
                abrirWhatsApp(o);
            }
        }), lpMatchAltura(48));

        raiz.addView(espaco(8));
        raiz.addView(botao("Voltar", WHITE, INK, new View.OnClickListener() {
            public void onClick(View v) {
                voltarParaOrigem();
            }
        }), lpMatchAltura(48));

        setContentViewComNav(scroll(raiz), origemAtual.startsWith("busca:") ? "buscar" : "inicio", false);
    }

    private void voltarParaOrigem() {
        if ("favoritos".equals(origemAtual)) {
            mostrarFavoritos();
            return;
        }

        if (origemAtual != null && origemAtual.startsWith("busca:")) {
            mostrarResultadoBusca(origemAtual.substring(6));
            return;
        }

        mostrarHome();
    }

    private void mostrarBusca(String termoInicial) {
        telaAtual = TELA_BUSCA;
        oportunidadeAtual = null;
        termoBuscaAtual = termoInicial == null ? "" : termoInicial;

        LinearLayout raiz = base();
        raiz.addView(header("Buscar", "Encontre oportunidades pelo texto."));
        raiz.addView(espaco(12));

        final EditText campoBusca = campo("Digite: pedreiro, eletricista, Ribeirao...");
        campoBusca.setText(termoBuscaAtual);
        raiz.addView(campoBusca);

        raiz.addView(espaco(10));
        raiz.addView(botao("Buscar agora", INK, WHITE, new View.OnClickListener() {
            public void onClick(View v) {
                mostrarResultadoBusca(campoBusca.getText().toString().trim());
            }
        }), lpMatchAltura(50));

        raiz.addView(espaco(12));
        raiz.addView(texto("Sugestoes rapidas", 14, INK, true));
        raiz.addView(espaco(8));
        raiz.addView(chipsBusca());
        raiz.addView(espaco(12));
        raiz.addView(bloco("A busca procura em titulo, cidade, valor, descricao, autor e categoria.", 14, INK_LIGHT, false, WHITE, 18));

        setContentViewComNav(scroll(raiz), "buscar", false);
    }

    private View chipsBusca() {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);

        LinearLayout linha = linha();
        linha.addView(chipBusca("Pedreiro"));
        linha.addView(chipBusca("Eletricista"));
        linha.addView(chipBusca("Ribeirao"));
        linha.addView(chipBusca("Hoje"));
        linha.addView(chipBusca("Seguranca"));

        scroll.addView(linha);
        return scroll;
    }

    private TextView chipBusca(final String termo) {
        TextView chip = texto(termo, 13, INK, true);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(14), dp(9), dp(14), dp(9));
        chip.setBackground(bg(WHITE, 999));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(42));
        params.setMargins(0, 0, dp(8), 0);
        chip.setLayoutParams(params);

        chip.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mostrarResultadoBusca(termo);
            }
        });

        return chip;
    }

    private void mostrarResultadoBusca(final String termo) {
        telaAtual = TELA_RESULTADO_BUSCA;
        termoBuscaAtual = termo == null ? "" : termo;
        oportunidadeAtual = null;

        LinearLayout raiz = base();
        raiz.addView(header("Resultado da busca", termoBuscaAtual.length() == 0 ? "Mostrando todas." : "Busca por: " + termoBuscaAtual));
        raiz.addView(espaco(12));
        raiz.addView(botao("Nova busca", WHITE, INK, new View.OnClickListener() {
            public void onClick(View v) {
                mostrarBusca(termoBuscaAtual);
            }
        }), lpMatchAltura(46));
        raiz.addView(espaco(12));

        int total = 0;
        for (int i = 0; i < oportunidades.size(); i++) {
            Oportunidade o = oportunidades.get(i);
            if (termoBuscaAtual.length() == 0 || correspondeBusca(o, termoBuscaAtual)) {
                raiz.addView(cardOportunidadeResumo(o, "busca:" + termoBuscaAtual));
                raiz.addView(espaco(12));
                total++;
            }
        }

        if (total == 0) {
            raiz.addView(bloco("Nenhuma oportunidade encontrada.", 14, INK_LIGHT, false, WHITE, 18));
        }

        setContentViewComNav(scroll(raiz), "buscar", false);
    }

    private boolean correspondeBusca(Oportunidade o, String termo) {
        String alvo = (o.tipo + " " + o.titulo + " " + o.local + " " + o.valor + " " + o.descricao + " " + o.autor).toLowerCase();
        return alvo.contains(termo.toLowerCase());
    }

    private void mostrarFavoritos() {
        telaAtual = TELA_FAVORITOS;
        origemAtual = "favoritos";
        oportunidadeAtual = null;

        LinearLayout raiz = base();
        raiz.addView(header("Favoritos", "Oportunidades salvas neste aparelho."));
        raiz.addView(espaco(12));

        int total = 0;
        for (int i = 0; i < oportunidades.size(); i++) {
            Oportunidade o = oportunidades.get(i);
            if (isFavorito(o)) {
                raiz.addView(cardOportunidadeResumo(o, "favoritos"));
                raiz.addView(espaco(12));
                total++;
            }
        }

        if (total == 0) {
            raiz.addView(bloco("Nenhum favorito salvo ainda. Abra uma oportunidade e toque em Salvar favorito.", 14, INK_LIGHT, false, WHITE, 18));
        }

        raiz.addView(espaco(10));
        raiz.addView(botao("Voltar", WHITE, INK, new View.OnClickListener() {
            public void onClick(View v) {
                mostrarHome();
            }
        }), lpMatchAltura(48));

        setContentViewComNav(scroll(raiz), "inicio", false);
    }

    private void mostrarAvisosInternos() {
        telaAtual = TELA_AVISOS;
        oportunidadeAtual = null;

        LinearLayout raiz = base();
        raiz.addView(header("Avisos internos", "Historico local do que aconteceu no app."));
        raiz.addView(espaco(12));
        raiz.addView(bloco("Estes avisos ficam apenas dentro do app. Ainda nao sao notificacoes push do Android.", 13, INK_LIGHT, false, WHITE, 18));
        raiz.addView(espaco(12));

        ArrayList<Notificacao> avisos = carregarNotificacoes();
        if (avisos.isEmpty()) {
            raiz.addView(bloco("Nenhum aviso interno ainda.", 14, INK_LIGHT, false, WHITE, 18));
        } else {
            for (int i = avisos.size() - 1; i >= 0; i--) {
                raiz.addView(cardNotificacao(avisos.get(i)));
                raiz.addView(espaco(8));
            }
        }

        raiz.addView(espaco(10));
        raiz.addView(botao("Limpar avisos internos", CORAL, WHITE, new View.OnClickListener() {
            public void onClick(View v) {
                prefs().edit().remove(KEY_NOTIFICACOES).apply();
                mostrarAvisosInternos();
            }
        }), lpMatchAltura(48));

        raiz.addView(espaco(10));
        raiz.addView(botao("Voltar", WHITE, INK, new View.OnClickListener() {
            public void onClick(View v) {
                mostrarHome();
            }
        }), lpMatchAltura(48));

        setContentViewComNav(scroll(raiz), "inicio", false);
    }

    private View cardNotificacao(Notificacao notificacao) {
        LinearLayout card = card(WHITE);
        card.addView(texto(notificacao.titulo, 16, INK, true));
        card.addView(texto(notificacao.texto, 13, INK_LIGHT, false));
        card.addView(texto(notificacao.data, 11, INK_LIGHT, false));
        return card;
    }

    private void adicionarNotificacao(String titulo, String texto) {
        ArrayList<Notificacao> notificacoes = carregarNotificacoes();
        notificacoes.add(new Notificacao(titulo, texto, agora()));

        while (notificacoes.size() > 30) {
            notificacoes.remove(0);
        }

        StringBuilder b = new StringBuilder();
        for (int i = 0; i < notificacoes.size(); i++) {
            Notificacao n = notificacoes.get(i);
            b.append(enc(n.titulo)).append("|")
                    .append(enc(n.texto)).append("|")
                    .append(enc(n.data));

            if (i < notificacoes.size() - 1) {
                b.append("\n");
            }
        }

        prefs().edit().putString(KEY_NOTIFICACOES, b.toString()).apply();
    }

    private ArrayList<Notificacao> carregarNotificacoes() {
        ArrayList<Notificacao> notificacoes = new ArrayList<Notificacao>();
        String salvo = prefs().getString(KEY_NOTIFICACOES, "");

        if (salvo == null || salvo.trim().length() == 0) {
            return notificacoes;
        }

        String[] linhas = salvo.split("\\n");
        for (int i = 0; i < linhas.length; i++) {
            String[] campos = linhas[i].split("\\|", -1);
            if (campos.length >= 3) {
                notificacoes.add(new Notificacao(dec(campos[0]), dec(campos[1]), dec(campos[2])));
            }
        }

        return notificacoes;
    }

    private void mostrarChat(final Oportunidade o) {
        telaAtual = TELA_CHAT;
        oportunidadeAtual = o;

        LinearLayout raiz = base();
        raiz.addView(header("Conversa da proposta", "Negocie dentro do app; WhatsApp fica como saida final."));
        raiz.addView(espaco(12));
        raiz.addView(cardMiniOportunidade(o));
        raiz.addView(espaco(10));
        raiz.addView(cardStatus(o));

        if (temAvaliacao(o)) {
            raiz.addView(espaco(10));
            raiz.addView(resumoAvaliacao(o));
        } else if (CONCLUIDO.equals(statusConversa(o))) {
            raiz.addView(espaco(10));
            raiz.addView(chamadaAvaliacao(o));
        }

        raiz.addView(espaco(12));

        ArrayList<Mensagem> mensagens = carregarMensagens(o);
        if (mensagens.isEmpty()) {
            raiz.addView(bloco("Nenhuma mensagem ainda.", 14, INK_LIGHT, false, WHITE, 18));
        }

        for (int i = 0; i < mensagens.size(); i++) {
            raiz.addView(cardMensagem(mensagens.get(i)));
            raiz.addView(espaco(8));
        }

        raiz.addView(espaco(12));
        raiz.addView(texto("Mensagens rapidas", 14, INK, true));
        raiz.addView(espaco(6));
        raiz.addView(mensagensRapidas(o));
        raiz.addView(espaco(12));

        final EditText campoMensagem = campo("Digite sua mensagem...");
        campoMensagem.setMinLines(1);
        campoMensagem.setMaxLines(4);
        raiz.addView(campoMensagem);

        raiz.addView(espaco(10));
        raiz.addView(botao("Enviar mensagem", INK, WHITE, new View.OnClickListener() {
            public void onClick(View v) {
                String texto = campoMensagem.getText().toString().trim();
                if (texto.length() == 0) {
                    Toast.makeText(MainActivity.this, "Digite uma mensagem.", Toast.LENGTH_SHORT).show();
                    return;
                }

                adicionarMensagem(o, "Voce", texto, "texto");
                adicionarNotificacao("Mensagem registrada", o.titulo);
                if (AGUARDANDO.equals(statusConversa(o))) {
                    setStatusConversa(o, EM_CONVERSA);
                }
                mostrarChat(o);
            }
        }), lpMatchAltura(50));

        raiz.addView(espaco(10));
        raiz.addView(botoesStatus(o));
        raiz.addView(espaco(10));
        raiz.addView(botao("Continuar pelo WhatsApp", WHATSAPP, WHITE, new View.OnClickListener() {
            public void onClick(View v) {
                abrirWhatsApp(o);
            }
        }), lpMatchAltura(48));

        raiz.addView(espaco(10));
        raiz.addView(botao("Voltar aos detalhes", WHITE, INK, new View.OnClickListener() {
            public void onClick(View v) {
                mostrarDetalhes(o, origemAtual);
            }
        }), lpMatchAltura(48));

        setContentViewComNav(scroll(raiz), "chat", false);
    }

    private View cardMiniOportunidade(Oportunidade o) {
        LinearLayout card = card(tintTipo(o.tipo));
        card.addView(texto(o.titulo, 20, INK, true));
        card.addView(texto(o.local, 14, INK_LIGHT, true));
        card.addView(texto(valorVisivel(o), 14, corPreco(o.valor), true));
        card.addView(texto("Proposta -> Detalhes -> Conversa -> Combinado -> Concluido -> Avaliacao", 12, INK_LIGHT, false));
        return card;
    }

    private View cardStatus(Oportunidade o) {
        LinearLayout card = linha();
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(bg(WHITE, 18));
        card.setElevation(dp(2));
        card.addView(texto("Status", 14, INK_LIGHT, true), peso(1));
        card.addView(statusPill(statusConversa(o)));
        return card;
    }

    private View chamadaAvaliacao(final Oportunidade o) {
        LinearLayout card = card(WHITE);
        card.addView(texto("Servico concluido", 17, INK, true));
        card.addView(texto("Avalie a experiencia para registrar o historico local.", 13, INK_LIGHT, false));
        card.addView(espaco(8));
        card.addView(botao("Avaliar servico", TANGERINE, WHITE, new View.OnClickListener() {
            public void onClick(View v) {
                mostrarAvaliacao(o);
            }
        }), lpMatchAltura(46));
        return card;
    }

    private View cardMensagem(Mensagem mensagem) {
        boolean minhaMensagem = "Voce".equals(mensagem.autor);

        LinearLayout linha = linha();
        linha.setGravity(minhaMensagem ? Gravity.RIGHT : Gravity.LEFT);

        LinearLayout bolha = coluna();
        bolha.setPadding(dp(14), dp(10), dp(14), dp(10));
        bolha.setBackground(bg(minhaMensagem ? INK : WHITE, 18));
        bolha.setElevation(dp(1));
        bolha.addView(texto(mensagem.autor + " - " + mensagem.hora, 11, minhaMensagem ? Color.rgb(220, 214, 236) : INK_LIGHT, true));
        bolha.addView(texto(mensagem.texto, 15, minhaMensagem ? WHITE : INK, false));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(minhaMensagem ? dp(44) : 0, 0, minhaMensagem ? 0 : dp(44), 0);
        linha.addView(bolha, params);

        return linha;
    }

    private View mensagensRapidas(final Oportunidade o) {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);

        LinearLayout linha = linha();
        linha.addView(botaoMensagemRapida(o, "Qual melhor horario?"));
        linha.addView(botaoMensagemRapida(o, "Consegue fazer por esse valor?"));
        linha.addView(botaoMensagemRapida(o, "Pode me mandar o endereco?"));
        linha.addView(botaoMensagemRapida(o, "Servico combinado"));

        scroll.addView(linha);
        return scroll;
    }

    private TextView botaoMensagemRapida(final Oportunidade o, final String mensagem) {
        TextView botao = texto(mensagem, 13, INK, true);
        botao.setGravity(Gravity.CENTER);
        botao.setPadding(dp(14), dp(9), dp(14), dp(9));
        botao.setBackground(bg(WHITE, 999));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(42));
        params.setMargins(0, 0, dp(8), 0);
        botao.setLayoutParams(params);

        botao.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                adicionarMensagem(o, "Voce", mensagem, "rapida");
                setStatusConversa(o, "Servico combinado".equals(mensagem) ? COMBINADO : EM_CONVERSA);
                adicionarNotificacao("Mensagem rapida", mensagem);
                mostrarChat(o);
            }
        });

        return botao;
    }

    private View botoesStatus(final Oportunidade o) {
        LinearLayout box = coluna();

        if (!CONCLUIDO.equals(statusConversa(o))) {
            box.addView(botao("Marcar como combinado", GREEN, WHITE, new View.OnClickListener() {
                public void onClick(View v) {
                    setStatusConversa(o, COMBINADO);
                    adicionarMensagem(o, "Sistema", "Servico combinado.", "status");
                    adicionarNotificacao("Servico combinado", o.titulo);
                    mostrarChat(o);
                }
            }), lpMatchAltura(48));

            box.addView(espaco(8));
            box.addView(botao("Concluir servico", Color.rgb(93, 93, 112), WHITE, new View.OnClickListener() {
                public void onClick(View v) {
                    setStatusConversa(o, CONCLUIDO);
                    adicionarMensagem(o, "Sistema", "Servico concluido.", "status");
                    adicionarNotificacao("Servico concluido", o.titulo);
                    mostrarAvaliacao(o);
                }
            }), lpMatchAltura(48));
        } else {
            box.addView(botao(temAvaliacao(o) ? "Editar avaliacao" : "Avaliar servico", TANGERINE, WHITE, new View.OnClickListener() {
                public void onClick(View v) {
                    mostrarAvaliacao(o);
                }
            }), lpMatchAltura(48));
        }

        return box;
    }

    private void mostrarAvaliacao(final Oportunidade o) {
        telaAtual = TELA_AVALIACAO;
        oportunidadeAtual = o;

        LinearLayout raiz = base();
        raiz.addView(header("Avaliar servico", "Salve uma nota local apos a conclusao."));
        raiz.addView(espaco(12));
        raiz.addView(cardMiniOportunidade(o));
        raiz.addView(espaco(12));

        Avaliacao avaliacaoAtual = carregarAvaliacao(o);
        final int[] nota = new int[]{avaliacaoAtual.nota};
        final TextView labelNota = texto("Nota selecionada: " + textoNota(nota[0]), 16, INK, true);

        raiz.addView(labelNota);
        raiz.addView(espaco(8));
        raiz.addView(botoesNota(nota, labelNota));
        raiz.addView(espaco(12));

        final EditText comentario = campo("Comentario sobre o servico");
        comentario.setMinLines(3);
        comentario.setMaxLines(6);
        if (avaliacaoAtual.comentario.length() > 0) {
            comentario.setText(avaliacaoAtual.comentario);
        }
        raiz.addView(comentario);
        raiz.addView(espaco(12));

        raiz.addView(botao("Salvar avaliacao", GREEN, WHITE, new View.OnClickListener() {
            public void onClick(View v) {
                if (nota[0] <= 0) {
                    Toast.makeText(MainActivity.this, "Escolha uma nota de 1 a 5.", Toast.LENGTH_SHORT).show();
                    return;
                }

                salvarAvaliacao(o, nota[0], comentario.getText().toString().trim());
                adicionarMensagem(o, "Sistema", "Avaliacao salva: " + nota[0] + "/5.", "avaliacao");
                adicionarNotificacao("Avaliacao salva", o.titulo + " - " + nota[0] + "/5");
                Toast.makeText(MainActivity.this, "Avaliacao salva no aparelho.", Toast.LENGTH_LONG).show();
                mostrarChat(o);
            }
        }), lpMatchAltura(50));

        raiz.addView(espaco(10));
        raiz.addView(botao("Voltar para conversa", WHITE, INK, new View.OnClickListener() {
            public void onClick(View v) {
                mostrarChat(o);
            }
        }), lpMatchAltura(48));

        setContentViewComNav(scroll(raiz), "chat", false);
    }

    private View botoesNota(final int[] nota, final TextView labelNota) {
        LinearLayout linha = linha();
        linha.setGravity(Gravity.CENTER);

        for (int i = 1; i <= 5; i++) {
            final int valor = i;
            TextView botao = texto(String.valueOf(i), 18, INK, true);
            botao.setGravity(Gravity.CENTER);
            botao.setBackground(bg(WHITE, 999));
            botao.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    nota[0] = valor;
                    labelNota.setText("Nota selecionada: " + textoNota(valor));
                }
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(48), 1);
            params.setMargins(0, 0, dp(7), 0);
            linha.addView(botao, params);
        }

        return linha;
    }

    private String textoNota(int nota) {
        return nota <= 0 ? "nenhuma" : nota + "/5";
    }

    private View resumoAvaliacao(Oportunidade o) {
        Avaliacao avaliacao = carregarAvaliacao(o);

        LinearLayout box = coluna();
        box.setPadding(dp(13), dp(10), dp(13), dp(10));
        box.setBackground(bg(Color.argb(145, 255, 255, 255), 16));
        box.addView(texto("Avaliacao: " + avaliacao.nota + "/5", 14, TANGERINE, true));

        if (avaliacao.comentario.length() > 0) {
            box.addView(texto(avaliacao.comentario, 13, INK_LIGHT, false));
        }

        if (avaliacao.data.length() > 0) {
            box.addView(texto("Salva em " + avaliacao.data, 11, INK_LIGHT, false));
        }

        return box;
    }

    private Avaliacao carregarAvaliacao(Oportunidade o) {
        String salvo = prefs().getString(KEY_AVALIACAO + chave(o), "");
        if (salvo == null || salvo.trim().length() == 0) {
            return new Avaliacao(0, "", "");
        }

        String[] campos = salvo.split("\\|", -1);
        int nota = 0;
        try {
            nota = Integer.parseInt(campos[0]);
        } catch (Exception e) {
            nota = 0;
        }

        String comentario = campos.length > 1 ? dec(campos[1]) : "";
        String data = campos.length > 2 ? dec(campos[2]) : "";
        return new Avaliacao(nota, comentario, data);
    }

    private void salvarAvaliacao(Oportunidade o, int nota, String comentario) {
        String valor = nota + "|" + enc(comentario) + "|" + enc(agora());
        prefs().edit().putString(KEY_AVALIACAO + chave(o), valor).apply();
    }

    private boolean temAvaliacao(Oportunidade o) {
        return carregarAvaliacao(o).nota > 0;
    }

    private void mostrarConversas() {
        telaAtual = TELA_CONVERSAS;
        oportunidadeAtual = null;

        LinearLayout raiz = base();
        raiz.addView(header("Conversas", "Acompanhe negociacoes locais por proposta."));
        raiz.addView(espaco(12));

        int total = 0;
        for (int i = 0; i < oportunidades.size(); i++) {
            Oportunidade o = oportunidades.get(i);
            if (temConversaAtiva(o)) {
                raiz.addView(cardConversa(o));
                raiz.addView(espaco(10));
                total++;
            }
        }

        if (total == 0) {
            raiz.addView(bloco("Voce ainda nao tem conversas iniciadas.", 14, INK_LIGHT, false, WHITE, 18));
            raiz.addView(espaco(12));
            raiz.addView(texto("Propostas disponiveis", 14, INK, true));
            raiz.addView(espaco(8));

            for (int i = 0; i < oportunidades.size(); i++) {
                raiz.addView(cardConversa(oportunidades.get(i)));
                raiz.addView(espaco(10));
            }
        }

        setContentViewComNav(scroll(raiz), "chat", false);
    }

    private View cardConversa(final Oportunidade o) {
        LinearLayout card = card(WHITE);

        LinearLayout topo = linha();
        topo.setGravity(Gravity.CENTER_VERTICAL);
        topo.addView(texto(o.titulo, 17, INK, true), peso(1));
        topo.addView(statusPill(statusConversa(o)));
        card.addView(topo);

        card.addView(texto(o.local, 13, INK_LIGHT, false));
        String ultima = ultimaMensagemResumo(o);
        card.addView(texto(ultima.length() == 0 ? "Toque para iniciar a negociacao local." : ultima, 13, INK_LIGHT, false));

        if (temAvaliacao(o)) {
            card.addView(texto("Avaliacao: " + carregarAvaliacao(o).nota + "/5", 12, TANGERINE, true));
        }

        card.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                origemAtual = "home";
                mostrarChat(o);
            }
        });

        return card;
    }

    private boolean temConversaAtiva(Oportunidade o) {
        return carregarMensagens(o).size() > 0 || !AGUARDANDO.equals(statusConversa(o)) || temAvaliacao(o);
    }

    private ArrayList<Mensagem> carregarMensagens(Oportunidade o) {
        ArrayList<Mensagem> mensagens = new ArrayList<Mensagem>();
        String salvo = prefs().getString(KEY_CHAT + chave(o), "");

        if (salvo == null || salvo.trim().length() == 0) {
            return mensagens;
        }

        String[] linhas = salvo.split("\\n");
        for (int i = 0; i < linhas.length; i++) {
            String[] campos = linhas[i].split("\\|", -1);
            if (campos.length >= 4) {
                mensagens.add(new Mensagem(dec(campos[0]), dec(campos[1]), dec(campos[2]), dec(campos[3])));
            }
        }

        return mensagens;
    }

    private void adicionarMensagem(Oportunidade o, String autor, String texto, String tipo) {
        ArrayList<Mensagem> mensagens = carregarMensagens(o);
        mensagens.add(new Mensagem(autor, texto, agora(), tipo));

        StringBuilder b = new StringBuilder();
        for (int i = 0; i < mensagens.size(); i++) {
            Mensagem m = mensagens.get(i);
            b.append(enc(m.autor)).append("|")
                    .append(enc(m.texto)).append("|")
                    .append(enc(m.hora)).append("|")
                    .append(enc(m.tipo));

            if (i < mensagens.size() - 1) {
                b.append("\n");
            }
        }

        prefs().edit().putString(KEY_CHAT + chave(o), b.toString()).apply();
    }

    private String ultimaMensagemResumo(Oportunidade o) {
        ArrayList<Mensagem> mensagens = carregarMensagens(o);
        if (mensagens.isEmpty()) {
            return "";
        }

        Mensagem ultima = mensagens.get(mensagens.size() - 1);
        String texto = ultima.autor + ": " + ultima.texto;
        return texto.length() > 82 ? texto.substring(0, 82) + "..." : texto;
    }

    private String statusConversa(Oportunidade o) {
        String status = prefs().getString(KEY_STATUS + chave(o), "");
        if (status == null || status.trim().length() == 0) {
            return carregarMensagens(o).isEmpty() ? AGUARDANDO : EM_CONVERSA;
        }
        return status;
    }

    private void setStatusConversa(Oportunidade o, String status) {
        prefs().edit().putString(KEY_STATUS + chave(o), status).apply();
    }

    private String chave(Oportunidade o) {
        if (o.id != null && o.id.trim().length() > 0) {
            return o.id;
        }
        return chaveLegada(o);
    }

    private String chaveLegada(Oportunidade o) {
        return String.valueOf(Math.abs((o.titulo + "|" + o.local + "|" + o.autor + "|" + o.contato).hashCode()));
    }

    private String novoId(String base) {
        return "local_" + System.currentTimeMillis() + "_" + Math.abs((base + System.nanoTime()).hashCode());
    }

    private TextView statusPill(String status) {
        return pill(labelStatus(status), corStatus(status), fundoStatus(status));
    }

    private String labelStatus(String status) {
        if (EM_CONVERSA.equals(status)) return "Em conversa";
        if (COMBINADO.equals(status)) return "Combinado";
        if (CONCLUIDO.equals(status)) return "Concluido";
        return "Aguardando resposta";
    }

    private int corStatus(String status) {
        if (EM_CONVERSA.equals(status)) return BLUE;
        if (COMBINADO.equals(status)) return GREEN;
        if (CONCLUIDO.equals(status)) return Color.rgb(80, 80, 94);
        return TANGERINE;
    }

    private int fundoStatus(String status) {
        if (EM_CONVERSA.equals(status)) return Color.rgb(238, 244, 255);
        if (COMBINADO.equals(status)) return Color.rgb(226, 252, 240);
        if (CONCLUIDO.equals(status)) return Color.rgb(238, 238, 242);
        return Color.rgb(255, 241, 214);
    }

    private void mostrarPublicar() {
        telaAtual = TELA_PUBLICAR;
        oportunidadeAtual = null;

        LinearLayout raiz = base();
        raiz.addView(header("Publicar oportunidade", "Cadastre uma vaga, bico ou servico."));
        raiz.addView(espaco(12));

        if (!perfilCompleto()) {
            raiz.addView(bloco("Dica: cadastre seu perfil antes de publicar.", 14, WHITE, true, TANGERINE, 18));
            raiz.addView(espaco(10));
        }

        final EditText tipo = campo("Tipo: VAGA, BICO, SERVICO, URGENTE ou SEGURANCA");
        final EditText titulo = campo("Titulo");
        final EditText local = campo("Cidade / bairro");
        final EditText valor = campo("Valor ou salario");
        final EditText descricao = campo("Descricao");
        final EditText contato = campo("WhatsApp");

        if (perfilTelefone.length() > 0) contato.setText(perfilTelefone);
        if (perfilCidade.length() > 0) local.setText(perfilCidade);

        raiz.addView(tipo);
        raiz.addView(espaco(8));
        raiz.addView(titulo);
        raiz.addView(espaco(8));
        raiz.addView(local);
        raiz.addView(espaco(8));
        raiz.addView(valor);
        raiz.addView(espaco(8));
        raiz.addView(descricao);
        raiz.addView(espaco(8));
        raiz.addView(contato);
        raiz.addView(espaco(14));

        raiz.addView(botao("Salvar oportunidade", GREEN, WHITE, new View.OnClickListener() {
            public void onClick(View v) {
                String tp = tipo.getText().toString().trim().toUpperCase();
                String ti = titulo.getText().toString().trim();
                String lo = local.getText().toString().trim();
                String va = valor.getText().toString().trim();
                String de = descricao.getText().toString().trim();
                String co = contato.getText().toString().trim();

                if (tp.length() == 0) tp = "BICO";
                if (ti.length() == 0 || lo.length() == 0 || co.length() == 0) {
                    Toast.makeText(MainActivity.this, "Preencha titulo, cidade e contato.", Toast.LENGTH_LONG).show();
                    return;
                }

                String autor = perfilNome.trim().length() == 0 ? "Usuario local" : perfilNome;
                oportunidades.add(0, new Oportunidade(novoId(ti), tp, ti, lo, va, de, co, autor));
                salvarOportunidades();
                filtroAtual = "TODOS";
                adicionarNotificacao("Oportunidade publicada", ti);
                Toast.makeText(MainActivity.this, "Oportunidade salva neste aparelho.", Toast.LENGTH_LONG).show();
                mostrarHome();
            }
        }), lpMatchAltura(50));

        raiz.addView(espaco(10));
        raiz.addView(botao("Voltar", WHITE, INK, new View.OnClickListener() {
            public void onClick(View v) {
                mostrarHome();
            }
        }), lpMatchAltura(48));

        setContentViewComNav(scroll(raiz), "publicar", false);
    }

    private void mostrarPerfil() {
        telaAtual = TELA_PERFIL;
        oportunidadeAtual = null;

        LinearLayout raiz = base();
        raiz.addView(header("Meu perfil", "Crie uma identidade para publicar."));
        raiz.addView(espaco(12));

        final EditText nome = campo("Seu nome ou empresa");
        final EditText cidade = campo("Cidade / bairro");
        final EditText telefone = campo("WhatsApp");
        final EditText tipo = campo("Tipo: Trabalhador, Contratante, Empresa ou Autonomo");

        nome.setText(perfilNome);
        cidade.setText(perfilCidade);
        telefone.setText(perfilTelefone);
        tipo.setText(perfilTipoUsuario);

        raiz.addView(nome);
        raiz.addView(espaco(8));
        raiz.addView(cidade);
        raiz.addView(espaco(8));
        raiz.addView(telefone);
        raiz.addView(espaco(8));
        raiz.addView(tipo);
        raiz.addView(espaco(14));

        raiz.addView(botao("Salvar perfil", GREEN, WHITE, new View.OnClickListener() {
            public void onClick(View v) {
                String n = nome.getText().toString().trim();
                String c = cidade.getText().toString().trim();
                String t = telefone.getText().toString().trim();
                String tp = tipo.getText().toString().trim();

                if (n.length() == 0 || t.length() == 0) {
                    Toast.makeText(MainActivity.this, "Preencha pelo menos nome e WhatsApp.", Toast.LENGTH_LONG).show();
                    return;
                }

                salvarPerfil(n, c, t, tp.length() == 0 ? "Usuario local" : tp);
                adicionarNotificacao("Perfil atualizado", n);
                Toast.makeText(MainActivity.this, "Perfil salvo.", Toast.LENGTH_LONG).show();
                mostrarHome();
            }
        }), lpMatchAltura(50));

        raiz.addView(espaco(10));
        raiz.addView(botao("Meus favoritos", TANGERINE, WHITE, new View.OnClickListener() {
            public void onClick(View v) {
                mostrarFavoritos();
            }
        }), lpMatchAltura(48));

        raiz.addView(espaco(8));
        raiz.addView(botao("Avisos internos", INK, WHITE, new View.OnClickListener() {
            public void onClick(View v) {
                mostrarAvisosInternos();
            }
        }), lpMatchAltura(48));

        raiz.addView(espaco(10));
        raiz.addView(bloco("Dados ficam salvos neste aparelho ate entrar Firebase.", 14, INK_LIGHT, false, WHITE, 18));
        raiz.addView(espaco(10));
        raiz.addView(botao("Voltar", WHITE, INK, new View.OnClickListener() {
            public void onClick(View v) {
                mostrarHome();
            }
        }), lpMatchAltura(48));

        setContentViewComNav(scroll(raiz), "perfil", false);
    }

    private View header(String titulo, String subtitulo) {
        LinearLayout header = coluna();
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(20), dp(18), dp(20), dp(18));
        header.setBackground(gradiente(YELLOW, TANGERINE, 28));
        header.setElevation(dp(4));
        header.addView(texto(titulo, 26, INK, true));
        header.addView(texto(subtitulo, 15, Color.rgb(91, 62, 30), true));
        return header;
    }

    private void setContentViewComNav(View conteudo, String ativo, boolean mostrarFab) {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(CREAM);
        root.addView(conteudo, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        FrameLayout.LayoutParams navParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(72), Gravity.BOTTOM);
        root.addView(bottomNav(ativo), navParams);

        if (mostrarFab) {
            TextView fab = texto("+", 30, WHITE, true);
            fab.setGravity(Gravity.CENTER);
            fab.setBackground(bg(CORAL, 999));
            fab.setElevation(dp(8));
            fab.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mostrarPublicar();
                }
            });

            FrameLayout.LayoutParams fabParams = new FrameLayout.LayoutParams(dp(58), dp(58), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            fabParams.setMargins(0, 0, 0, dp(42));
            root.addView(fab, fabParams);
        }

        setContentView(root);
    }

    private LinearLayout bottomNav(String ativo) {
        LinearLayout nav = linha();
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(14), dp(6), dp(14), dp(6));
        nav.setBackgroundColor(WHITE);
        nav.setElevation(dp(8));

        nav.addView(navItem("Inicio", "inicio", ativo, new View.OnClickListener() {
            public void onClick(View v) {
                filtroAtual = "TODOS";
                mostrarHome();
            }
        }), navPeso());

        nav.addView(navItem("Buscar", "buscar", ativo, new View.OnClickListener() {
            public void onClick(View v) {
                mostrarBusca("");
            }
        }), navPeso());

        nav.addView(new TextView(this), navPeso());

        nav.addView(navItem("Chat", "chat", ativo, new View.OnClickListener() {
            public void onClick(View v) {
                mostrarConversas();
            }
        }), navPeso());

        nav.addView(navItem("Perfil", "perfil", ativo, new View.OnClickListener() {
            public void onClick(View v) {
                mostrarPerfil();
            }
        }), navPeso());

        return nav;
    }

    private TextView navItem(String texto, String chave, String ativo, View.OnClickListener listener) {
        TextView item = texto(texto, 11, chave.equals(ativo) ? INK : INK_LIGHT, true);
        item.setGravity(Gravity.CENTER);
        item.setBackground(chave.equals(ativo) ? bg(Color.rgb(255, 241, 214), 18) : bg(Color.TRANSPARENT, 18));
        item.setOnClickListener(listener);
        return item;
    }

    private void abrirWhatsApp(Oportunidade o) {
        String numero = limparNumero(o.contato);
        if (numero.length() == 0) {
            Toast.makeText(this, "Contato nao informado.", Toast.LENGTH_LONG).show();
            return;
        }

        String mensagem = "Ola, vi sua oportunidade no Chama no Trampo e tenho interesse.\n\nOportunidade: " + o.titulo + "\nLocal: " + o.local;
        String base = "http" + "s://wa" + "." + "me/";

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(base + numero + "?text=" + Uri.encode(mensagem))));
        } catch (Exception e) {
            Toast.makeText(this, "Nao consegui abrir o WhatsApp.", Toast.LENGTH_LONG).show();
        }
    }

    private String limparNumero(String contato) {
        StringBuilder numero = new StringBuilder();
        for (int i = 0; i < contato.length(); i++) {
            char c = contato.charAt(i);
            if (c >= '0' && c <= '9') {
                numero.append(c);
            }
        }

        String limpo = numero.toString();
        return (limpo.length() == 10 || limpo.length() == 11) ? "55" + limpo : limpo;
    }

    private boolean isFavorito(Oportunidade o) {
        return prefs().getBoolean(KEY_FAVORITO + chave(o), false);
    }

    private void alternarFavorito(Oportunidade o) {
        boolean novoValor = !isFavorito(o);
        prefs().edit().putBoolean(KEY_FAVORITO + chave(o), novoValor).apply();
        adicionarNotificacao(novoValor ? "Favorito salvo" : "Favorito removido", o.titulo);
        Toast.makeText(this, novoValor ? "Salvo nos favoritos." : "Removido dos favoritos.", Toast.LENGTH_SHORT).show();
    }

    private int contarFiltro(String filtro) {
        int total = 0;
        for (int i = 0; i < oportunidades.size(); i++) {
            if ("TODOS".equals(filtro) || oportunidades.get(i).tipo.equals(filtro)) {
                total++;
            }
        }
        return total;
    }

    private String autor(Oportunidade o) {
        return o.autor == null || o.autor.trim().length() == 0 ? "Usuario local" : o.autor;
    }

    private String valorVisivel(Oportunidade o) {
        return o.valor == null || o.valor.length() == 0 ? "Valor a combinar" : o.valor;
    }

    private String descricaoVisivel(Oportunidade o) {
        return o.descricao == null || o.descricao.length() == 0 ? "Sem descricao informada." : o.descricao;
    }

    private String agora() {
        return new SimpleDateFormat("dd/MM HH:mm", new Locale("pt", "BR")).format(new Date());
    }

    private int tintTipo(String tipo) {
        if ("URGENTE".equals(tipo)) return Color.rgb(255, 234, 234);
        if ("SERVICO".equals(tipo)) return Color.rgb(230, 244, 255);
        if ("BICO".equals(tipo)) return Color.rgb(243, 234, 254);
        if ("SEGURANCA".equals(tipo)) return Color.rgb(255, 241, 214);
        return Color.rgb(227, 251, 240);
    }

    private int corTipo(String tipo) {
        if ("URGENTE".equals(tipo)) return CORAL;
        if ("SERVICO".equals(tipo)) return BLUE;
        if ("BICO".equals(tipo)) return PURPLE;
        if ("SEGURANCA".equals(tipo)) return TANGERINE;
        return GREEN;
    }

    private String labelTipo(String tipo) {
        if ("SERVICO".equals(tipo)) return "SERVICO";
        if ("BICO".equals(tipo)) return "BICO";
        if ("URGENTE".equals(tipo)) return "URGENTE";
        if ("SEGURANCA".equals(tipo)) return "SEGURANCA";
        return "VAGA";
    }

    private int corPreco(String valor) {
        return valor != null && valor.contains("R$") ? Color.rgb(10, 138, 77) : INK_LIGHT;
    }

    private String distanciaFake(Oportunidade o) {
        int distancia = Math.abs((o.titulo + o.local).hashCode()) % 48;
        return (distancia < 3 ? distancia + 1 : distancia) + " km";
    }

    private String enc(String texto) {
        return texto == null ? "" : Uri.encode(texto);
    }

    private String dec(String texto) {
        return texto == null ? "" : Uri.decode(texto);
    }

    private LinearLayout base() {
        LinearLayout layout = coluna();
        layout.setPadding(dp(18), dp(18), dp(18), dp(128));
        return layout;
    }

    private ScrollView scroll(View view) {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(CREAM);
        scroll.addView(view);
        return scroll;
    }

    private LinearLayout coluna() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout linha() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        return layout;
    }

    private LinearLayout card(int cor) {
        LinearLayout layout = coluna();
        layout.setPadding(dp(18), dp(16), dp(18), dp(18));
        layout.setBackground(bg(cor, 24));
        layout.setElevation(dp(3));
        return layout;
    }

    private EditText campo(String dica) {
        EditText campo = new EditText(this);
        campo.setHint(dica);
        campo.setTextSize(15);
        campo.setSingleLine(false);
        campo.setPadding(dp(14), dp(12), dp(14), dp(12));
        campo.setBackground(bg(WHITE, 18));
        campo.setTextColor(INK);
        campo.setHintTextColor(INK_LIGHT);
        return campo;
    }

    private TextView botao(String txt, int fundo, int corTexto, View.OnClickListener listener) {
        TextView botao = bloco(txt, 15, corTexto, true, fundo, 999);
        botao.setGravity(Gravity.CENTER);
        botao.setOnClickListener(listener);
        botao.setElevation(dp(2));
        return botao;
    }

    private TextView bloco(String txt, int tamanho, int corTexto, boolean negrito, int fundo, int raio) {
        TextView view = texto(txt, tamanho, corTexto, negrito);
        view.setPadding(dp(16), dp(13), dp(16), dp(13));
        view.setBackground(bg(fundo, raio));
        return view;
    }

    private TextView texto(String txt, int tamanho, int cor, boolean negrito) {
        TextView view = new TextView(this);
        view.setText(txt);
        view.setTextSize(tamanho);
        view.setTextColor(cor);
        view.setPadding(0, dp(3), 0, dp(3));
        if (negrito) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return view;
    }

    private TextView pill(String texto, int corTexto, int fundo) {
        TextView pill = texto(texto, 12, corTexto, true);
        pill.setGravity(Gravity.CENTER);
        pill.setPadding(dp(10), dp(5), dp(10), dp(5));
        pill.setBackground(bg(fundo, 999));
        return pill;
    }

    private GradientDrawable bg(int cor, int raio) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(cor);
        drawable.setCornerRadius(dp(raio));
        return drawable;
    }

    private GradientDrawable gradiente(int a, int b, int raio) {
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{a, b});
        drawable.setCornerRadius(dp(raio));
        return drawable;
    }

    private LinearLayout.LayoutParams lpMatchAltura(int alturaDp) {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(alturaDp));
    }

    private LinearLayout.LayoutParams peso(float peso) {
        return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, peso);
    }

    private LinearLayout.LayoutParams navPeso() {
        return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
    }

    private View espaco(int alturaDp) {
        View view = new View(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(1, dp(alturaDp)));
        return view;
    }

    private View espacoLargura(int larguraDp) {
        View view = new View(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(dp(larguraDp), 1));
        return view;
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void mostrarTelaErro(Throwable erro) {
        TextView view = new TextView(this);
        view.setText("Chama no Trampo\n\nErro na tela.\n\n" + erro.getClass().getSimpleName() + "\n" + erro.getMessage());
        view.setTextSize(18);
        view.setTextColor(WHITE);
        view.setBackgroundColor(Color.rgb(127, 29, 29));
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(24), dp(24), dp(24), dp(24));
        setContentView(view);
    }

    private static class Oportunidade {
        String id;
        String tipo;
        String titulo;
        String local;
        String valor;
        String descricao;
        String contato;
        String autor;

        Oportunidade(String id, String tipo, String titulo, String local, String valor, String descricao, String contato, String autor) {
            this.id = id;
            this.tipo = tipo;
            this.titulo = titulo;
            this.local = local;
            this.valor = valor;
            this.descricao = descricao;
            this.contato = contato;
            this.autor = autor;
        }
    }

    private static class Mensagem {
        String autor;
        String texto;
        String hora;
        String tipo;

        Mensagem(String autor, String texto, String hora, String tipo) {
            this.autor = autor;
            this.texto = texto;
            this.hora = hora;
            this.tipo = tipo;
        }
    }

    private static class Avaliacao {
        int nota;
        String comentario;
        String data;

        Avaliacao(int nota, String comentario, String data) {
            this.nota = nota;
            this.comentario = comentario;
            this.data = data;
        }
    }

    private static class Notificacao {
        String titulo;
        String texto;
        String data;

        Notificacao(String titulo, String texto, String data) {
            this.titulo = titulo;
            this.texto = texto;
            this.data = data;
        }
    }
}
