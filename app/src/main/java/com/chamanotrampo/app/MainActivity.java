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

    private static final String AGUARDANDO = "AGUARDANDO_RESPOSTA";
    private static final String EM_CONVERSA = "EM_CONVERSA";
    private static final String COMBINADO = "COMBINADO";
    private static final String CONCLUIDO = "CONCLUIDO";

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
    private static final int CINZA_TINT = Color.rgb(238, 238, 242);

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
        SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
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
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
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
                String[] c = linhas[i].split("\\|", -1);
                if (c.length >= 7) oportunidades.add(new Oportunidade(dec(c[0]), dec(c[1]), dec(c[2]), dec(c[3]), dec(c[4]), dec(c[5]), dec(c[6])));
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
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < oportunidades.size(); i++) {
            Oportunidade o = oportunidades.get(i);
            b.append(enc(o.tipo)).append("|").append(enc(o.titulo)).append("|").append(enc(o.local)).append("|").append(enc(o.valor)).append("|").append(enc(o.descricao)).append("|").append(enc(o.contato)).append("|").append(enc(o.autor));
            if (i < oportunidades.size() - 1) b.append("\n");
        }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_OPORTUNIDADES, b.toString()).apply();
    }

    private void montarTelaPrincipal() {
        LinearLayout raiz = base();
        raiz.addView(header("Chama no Trampo", "Empregos, bicos e servicos perto de voce"));
        raiz.addView(espaco(12));
        raiz.addView(profileBanner());
        raiz.addView(espaco(12));
        raiz.addView(searchBar());
        raiz.addView(espaco(8));
        raiz.addView(chipsCategorias());
        raiz.addView(espaco(10));
        raiz.addView(texto(contarFiltro(filtroAtual) + " oportunidades " + ("TODOS".equals(filtroAtual) ? "perto de voce" : filtroAtual.toLowerCase()), 14, INK_LIGHT, true));
        raiz.addView(espaco(8));
        conteudoContainer = coluna();
        raiz.addView(conteudoContainer);
        setContentViewComNav(scroll(raiz), "inicio", true);
        atualizarLista(filtroAtual);
    }

    private View profileBanner() {
        LinearLayout box = colunaCard(WHITE);
        if (perfilCompleto()) {
            box.addView(texto("Perfil ativo: " + perfilNome, 15, INK, true));
            box.addView(texto(textoPerfilResumo(), 13, INK_LIGHT, false));
        } else {
            box.addView(texto("Falta pouco! Complete seu perfil e apareca mais nas buscas", 14, INK, true));
            box.addView(espaco(8));
            LinearLayout trilha = new LinearLayout(this);
            trilha.setBackground(bg(Color.rgb(241, 236, 223), dp(8)));
            LinearLayout preenchido = new LinearLayout(this);
            preenchido.setBackground(bg(GREEN, dp(8)));
            trilha.addView(preenchido, new LinearLayout.LayoutParams(0, dp(7), 60));
            box.addView(trilha, lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(7)));
        }
        box.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { montarTelaPerfil(); }});
        return box;
    }

    private View searchBar() {
        LinearLayout box = linha();
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(dp(16), dp(12), dp(16), dp(12));
        box.setBackground(bg(WHITE, dp(999)));
        box.setElevation(dp(3));
        box.addView(texto("Buscar: pedreiro, ajudante, eletricista...", 14, INK_LIGHT, true), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        box.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { Toast.makeText(MainActivity.this, "Busca avancada entra na proxima etapa.", Toast.LENGTH_SHORT).show(); }});
        return box;
    }

    private View chipsCategorias() {
        HorizontalScrollView h = new HorizontalScrollView(this);
        h.setHorizontalScrollBarEnabled(false);
        LinearLayout l = linha();
        l.addView(chip("Todos", "TODOS"));
        l.addView(chip("Vagas", "VAGA"));
        l.addView(chip("Bicos", "BICO"));
        l.addView(chip("Servicos", "SERVICO"));
        l.addView(chip("Urgentes", "URGENTE"));
        l.addView(chip("Seguranca", "SEGURANCA"));
        h.addView(l);
        return h;
    }

    private TextView chip(String nome, final String filtro) {
        boolean ativo = filtro.equals(filtroAtual);
        TextView c = texto(nome, 14, ativo ? WHITE : corTipo(filtro), true);
        c.setGravity(Gravity.CENTER);
        c.setPadding(dp(17), dp(9), dp(17), dp(9));
        c.setBackground(bg(ativo ? INK : tintTipo(filtro), dp(999)));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(42));
        p.setMargins(0, 0, dp(9), 0);
        c.setLayoutParams(p);
        c.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { filtroAtual = filtro; montarTelaPrincipal(); }});
        return c;
    }

    private void atualizarLista(String filtro) {
        filtroAtual = filtro;
        if (conteudoContainer == null) return;
        conteudoContainer.removeAllViews();
        int total = 0;
        for (int i = 0; i < oportunidades.size(); i++) {
            Oportunidade o = oportunidades.get(i);
            if ("TODOS".equals(filtroAtual) || o.tipo.equals(filtroAtual)) {
                conteudoContainer.addView(cardOportunidade(o));
                conteudoContainer.addView(espaco(14));
                total++;
            }
        }
        if (total == 0) conteudoContainer.addView(bloco("Nenhuma oportunidade encontrada nesta categoria.", 15, INK_LIGHT, false, WHITE, dp(18)));
    }

    private View cardOportunidade(final Oportunidade o) {
        LinearLayout card = colunaCard(tintTipo(o.tipo));
        LinearLayout top = linha();
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView badge = texto(labelTipo(o.tipo), 12, corTipo(o.tipo), true);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(10), dp(5), dp(10), dp(5));
        badge.setBackground(bg(Color.argb(105, 255, 255, 255), dp(999)));
        top.addView(badge, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        TextView dist = texto(distanciaFake(o), 12, INK_LIGHT, true);
        dist.setGravity(Gravity.RIGHT);
        top.addView(dist);
        card.addView(top);
        card.addView(espaco(9));
        card.addView(texto(o.titulo, 21, INK, true));
        card.addView(texto(o.local, 14, INK_LIGHT, true));
        card.addView(texto("por " + autor(o), 12, INK_LIGHT, false));
        card.addView(espaco(4));
        card.addView(texto(o.valor.length() == 0 ? "Valor a combinar" : o.valor, 15, corPreco(o.valor), true));
        card.addView(texto(o.descricao.length() == 0 ? "Sem descricao informada." : o.descricao, 14, Color.rgb(86, 76, 114), false));
        card.addView(espaco(10));
        card.addView(resumoConversa(o));
        if (temAvaliacao(o)) {
            card.addView(espaco(8));
            card.addView(resumoAvaliacao(o));
        }
        card.addView(espaco(12));
        card.addView(botao("Abrir conversa", INK, WHITE, new View.OnClickListener() { public void onClick(View v) { montarTelaChat(o); }}), lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
        card.addView(espaco(8));
        card.addView(botao("Chamar no WhatsApp", WHATSAPP, WHITE, new View.OnClickListener() { public void onClick(View v) { abrirWhatsApp(o); }}), lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
        return card;
    }

    private View resumoConversa(Oportunidade o) {
        LinearLayout box = coluna();
        box.setPadding(dp(13), dp(10), dp(13), dp(10));
        box.setBackground(bg(Color.argb(130, 255, 255, 255), dp(16)));
        box.addView(statusPill(statusConversa(o)));
        String ultima = ultimaMensagemResumo(o);
        box.addView(texto(ultima.length() > 0 ? ultima : "Sem mensagens ainda. Abra a conversa para negociar por aqui.", 12, INK_LIGHT, false));
        return box;
    }

    private void montarTelaChat(final Oportunidade o) {
        LinearLayout raiz = base();
        raiz.addView(header("Conversa da proposta", "Negocie dentro do app; WhatsApp fica como saida final."));
        raiz.addView(espaco(12));
        raiz.addView(cardResumo(o));
        raiz.addView(espaco(10));
        raiz.addView(statusBox(o));
        if (temAvaliacao(o)) {
            raiz.addView(espaco(10));
            raiz.addView(resumoAvaliacao(o));
        } else if (CONCLUIDO.equals(statusConversa(o))) {
            raiz.addView(espaco(10));
            raiz.addView(chamadaAvaliacao(o));
        }
        raiz.addView(espaco(12));
        ArrayList<Mensagem> msgs = carregarMensagens(o);
        if (msgs.isEmpty()) raiz.addView(bloco("Nenhuma mensagem ainda. Use uma mensagem rapida ou digite abaixo.", 14, INK_LIGHT, false, WHITE, dp(18)));
        for (int i = 0; i < msgs.size(); i++) {
            raiz.addView(cardMensagem(msgs.get(i)));
            raiz.addView(espaco(8));
        }
        raiz.addView(espaco(12));
        raiz.addView(texto("Mensagens rapidas", 14, INK, true));
        raiz.addView(espaco(6));
        raiz.addView(mensagensRapidas(o));
        raiz.addView(espaco(12));
        final EditText resposta = campo("Digite sua mensagem...");
        resposta.setMinLines(1);
        resposta.setMaxLines(4);
        raiz.addView(resposta);
        raiz.addView(espaco(10));
        raiz.addView(botao("Enviar mensagem", INK, WHITE, new View.OnClickListener() { public void onClick(View v) {
            String t = resposta.getText().toString().trim();
            if (t.length() == 0) { Toast.makeText(MainActivity.this, "Digite uma mensagem.", Toast.LENGTH_SHORT).show(); return; }
            addMensagem(o, "Voce", t, "texto");
            if (AGUARDANDO.equals(statusConversa(o))) setStatus(o, EM_CONVERSA);
            montarTelaChat(o);
        }}), lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(50)));
        raiz.addView(espaco(10));
        raiz.addView(botoesStatus(o));
        raiz.addView(espaco(10));
        raiz.addView(botao("Continuar pelo WhatsApp", WHATSAPP, WHITE, new View.OnClickListener() { public void onClick(View v) { abrirWhatsApp(o); }}), lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
        raiz.addView(espaco(10));
        raiz.addView(botao("Voltar para propostas", WHITE, INK, new View.OnClickListener() { public void onClick(View v) { montarTelaPrincipal(); }}), lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
        setContentViewComNav(scroll(raiz), "chat", false);
    }

    private View cardResumo(Oportunidade o) {
        LinearLayout box = colunaCard(tintTipo(o.tipo));
        box.addView(texto(o.titulo, 20, INK, true));
        box.addView(texto(o.local, 14, INK_LIGHT, true));
        box.addView(texto(o.valor.length() == 0 ? "Valor a combinar" : o.valor, 14, corPreco(o.valor), true));
        box.addView(texto("Proposta -> Conversa -> Combinado -> Concluido -> Avaliacao", 12, INK_LIGHT, false));
        return box;
    }

    private View statusBox(Oportunidade o) {
        LinearLayout box = linha();
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(dp(14), dp(12), dp(14), dp(12));
        box.setBackground(bg(WHITE, dp(18)));
        box.setElevation(dp(2));
        box.addView(texto("Status", 14, INK_LIGHT, true), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        box.addView(statusPill(statusConversa(o)));
        return box;
    }

    private View chamadaAvaliacao(final Oportunidade o) {
        LinearLayout box = colunaCard(WHITE);
        box.addView(texto("Servico concluido", 17, INK, true));
        box.addView(texto("Avalie a experiencia para montar a reputacao do perfil.", 13, INK_LIGHT, false));
        box.addView(espaco(8));
        box.addView(botao("Avaliar servico", TANGERINE, WHITE, new View.OnClickListener() { public void onClick(View v) { montarTelaAvaliacao(o); }}), lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(46)));
        return box;
    }

    private View cardMensagem(Mensagem m) {
        boolean minha = "Voce".equals(m.autor);
        LinearLayout linha = linha();
        linha.setGravity(minha ? Gravity.RIGHT : Gravity.LEFT);
        LinearLayout bolha = coluna();
        bolha.setPadding(dp(14), dp(10), dp(14), dp(10));
        bolha.setBackground(bg(minha ? INK : WHITE, dp(18)));
        bolha.setElevation(dp(1));
        bolha.addView(texto(m.autor + " - " + m.hora, 11, minha ? Color.rgb(220, 214, 236) : INK_LIGHT, true));
        bolha.addView(texto(m.texto, 15, minha ? WHITE : INK, false));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(minha ? dp(44) : 0, 0, minha ? 0 : dp(44), 0);
        linha.addView(bolha, p);
        return linha;
    }

    private View mensagensRapidas(final Oportunidade o) {
        HorizontalScrollView h = new HorizontalScrollView(this);
        h.setHorizontalScrollBarEnabled(false);
        LinearLayout l = linha();
        l.addView(botaoRapido(o, "Qual melhor horario?"));
        l.addView(botaoRapido(o, "Consegue fazer por esse valor?"));
        l.addView(botaoRapido(o, "Pode me mandar o endereco?"));
        l.addView(botaoRapido(o, "Servico combinado"));
        h.addView(l);
        return h;
    }

    private TextView botaoRapido(final Oportunidade o, final String msg) {
        TextView c = texto(msg, 13, INK, true);
        c.setGravity(Gravity.CENTER);
        c.setPadding(dp(14), dp(9), dp(14), dp(9));
        c.setBackground(bg(WHITE, dp(999)));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(42));
        p.setMargins(0, 0, dp(8), 0);
        c.setLayoutParams(p);
        c.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
            addMensagem(o, "Voce", msg, "rapida");
            setStatus(o, "Servico combinado".equals(msg) ? COMBINADO : EM_CONVERSA);
            montarTelaChat(o);
        }});
        return c;
    }

    private View botoesStatus(final Oportunidade o) {
        LinearLayout box = coluna();
        if (!CONCLUIDO.equals(statusConversa(o))) {
            box.addView(botao("Marcar como combinado", GREEN, WHITE, new View.OnClickListener() { public void onClick(View v) { setStatus(o, COMBINADO); addMensagem(o, "Sistema", "Servico combinado.", "status"); montarTelaChat(o); }}), lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
            box.addView(espaco(8));
            box.addView(botao("Concluir servico", Color.rgb(93, 93, 112), WHITE, new View.OnClickListener() { public void onClick(View v) { setStatus(o, CONCLUIDO); addMensagem(o, "Sistema", "Servico concluido.", "status"); montarTelaAvaliacao(o); }}), lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
        } else {
            box.addView(botao(temAvaliacao(o) ? "Editar avaliacao" : "Avaliar servico", TANGERINE, WHITE, new View.OnClickListener() { public void onClick(View v) { montarTelaAvaliacao(o); }}), lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
        }
        return box;
    }

    private void montarTelaAvaliacao(final Oportunidade o) {
        LinearLayout raiz = base();
        raiz.addView(header("Avaliar servico", "Salve uma nota local apos a conclusao."));
        raiz.addView(espaco(12));
        raiz.addView(cardResumo(o));
        raiz.addView(espaco(12));

        Avaliacao atual = carregarAvaliacao(o);
        final int[] nota = new int[]{atual.nota};
        final TextView notaAtual = texto("Nota selecionada: " + labelNota(nota[0]), 16, INK, true);
        raiz.addView(notaAtual);
        raiz.addView(espaco(8));
        raiz.addView(botoesNota(nota, notaAtual));
        raiz.addView(espaco(12));

        final EditText comentario = campo("Comentario sobre o servico");
        comentario.setMinLines(3);
        comentario.setMaxLines(6);
        if (atual.comentario.length() > 0) comentario.setText(atual.comentario);
        raiz.addView(comentario);
        raiz.addView(espaco(12));

        raiz.addView(botao("Salvar avaliacao", GREEN, WHITE, new View.OnClickListener() { public void onClick(View v) {
            if (nota[0] <= 0) { Toast.makeText(MainActivity.this, "Escolha uma nota de 1 a 5.", Toast.LENGTH_SHORT).show(); return; }
            salvarAvaliacao(o, nota[0], comentario.getText().toString().trim());
            addMensagem(o, "Sistema", "Avaliacao salva: " + nota[0] + "/5.", "avaliacao");
            Toast.makeText(MainActivity.this, "Avaliacao salva no aparelho.", Toast.LENGTH_LONG).show();
            montarTelaChat(o);
        }}), lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(50)));
        raiz.addView(espaco(10));
        raiz.addView(botao("Voltar para conversa", WHITE, INK, new View.OnClickListener() { public void onClick(View v) { montarTelaChat(o); }}), lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
        setContentViewComNav(scroll(raiz), "chat", false);
    }

    private View botoesNota(final int[] nota, final TextView notaAtual) {
        LinearLayout linha = linha();
        linha.setGravity(Gravity.CENTER);
        for (int i = 1; i <= 5; i++) {
            final int valor = i;
            TextView b = texto(String.valueOf(i), 18, INK, true);
            b.setGravity(Gravity.CENTER);
            b.setBackground(bg(WHITE, dp(999)));
            b.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { nota[0] = valor; notaAtual.setText("Nota selecionada: " + labelNota(valor)); }});
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(48), 1);
            p.setMargins(0, 0, dp(7), 0);
            linha.addView(b, p);
        }
        return linha;
    }

    private View resumoAvaliacao(Oportunidade o) {
        Avaliacao a = carregarAvaliacao(o);
        LinearLayout box = coluna();
        box.setPadding(dp(13), dp(10), dp(13), dp(10));
        box.setBackground(bg(Color.argb(145, 255, 255, 255), dp(16)));
        box.addView(texto("Avaliacao: " + a.nota + "/5", 14, TANGERINE, true));
        if (a.comentario.length() > 0) box.addView(texto(a.comentario, 13, INK_LIGHT, false));
        if (a.data.length() > 0) box.addView(texto("Salva em " + a.data, 11, INK_LIGHT, false));
        return box;
    }

    private Avaliacao carregarAvaliacao(Oportunidade o) {
        String salvo = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_AVALIACAO + chave(o), "");
        if (salvo == null || salvo.trim().length() == 0) return new Avaliacao(0, "", "");
        String[] c = salvo.split("\\|", -1);
        int nota = 0;
        try { nota = Integer.parseInt(c[0]); } catch (Exception e) { nota = 0; }
        String comentario = c.length > 1 ? dec(c[1]) : "";
        String data = c.length > 2 ? dec(c[2]) : "";
        return new Avaliacao(nota, comentario, data);
    }

    private void salvarAvaliacao(Oportunidade o, int nota, String comentario) {
        String valor = nota + "|" + enc(comentario) + "|" + enc(agora());
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_AVALIACAO + chave(o), valor).apply();
    }

    private boolean temAvaliacao(Oportunidade o) {
        return carregarAvaliacao(o).nota > 0;
    }

    private String labelNota(int nota) {
        if (nota <= 0) return "nenhuma";
        return nota + "/5";
    }

    private void montarTelaConversas() {
        LinearLayout raiz = base();
        raiz.addView(header("Conversas", "Acompanhe negociacoes locais por proposta."));
        raiz.addView(espaco(12));
        int ativas = 0;
        for (int i = 0; i < oportunidades.size(); i++) {
            if (temConversaAtiva(oportunidades.get(i))) {
                raiz.addView(cardConversa(oportunidades.get(i)));
                raiz.addView(espaco(10));
                ativas++;
            }
        }
        if (ativas == 0) {
            raiz.addView(bloco("Voce ainda nao tem conversas iniciadas. Toque em uma proposta abaixo para comecar.", 14, INK_LIGHT, false, WHITE, dp(18)));
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
        LinearLayout card = colunaCard(WHITE);
        LinearLayout top = linha();
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(texto(o.titulo, 17, INK, true), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        top.addView(statusPill(statusConversa(o)));
        card.addView(top);
        card.addView(texto(o.local, 13, INK_LIGHT, false));
        String ultima = ultimaMensagemResumo(o);
        card.addView(texto(ultima.length() == 0 ? "Toque para iniciar a negociacao local." : ultima, 13, INK_LIGHT, false));
        if (temAvaliacao(o)) card.addView(texto("Avaliacao: " + carregarAvaliacao(o).nota + "/5", 12, TANGERINE, true));
        card.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { montarTelaChat(o); }});
        return card;
    }

    private boolean temConversaAtiva(Oportunidade o) {
        return carregarMensagens(o).size() > 0 || !AGUARDANDO.equals(statusConversa(o)) || temAvaliacao(o);
    }

    private ArrayList<Mensagem> carregarMensagens(Oportunidade o) {
        ArrayList<Mensagem> ms = new ArrayList<Mensagem>();
        String salvo = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_CHAT + chave(o), "");
        if (salvo == null || salvo.trim().length() == 0) return ms;
        String[] linhas = salvo.split("\\n");
        for (int i = 0; i < linhas.length; i++) {
            String[] c = linhas[i].split("\\|", -1);
            if (c.length >= 4) ms.add(new Mensagem(dec(c[0]), dec(c[1]), dec(c[2]), dec(c[3])));
        }
        return ms;
    }

    private void addMensagem(Oportunidade o, String autor, String texto, String tipo) {
        ArrayList<Mensagem> ms = carregarMensagens(o);
        ms.add(new Mensagem(autor, texto, agora(), tipo));
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < ms.size(); i++) {
            Mensagem m = ms.get(i);
            b.append(enc(m.autor)).append("|").append(enc(m.texto)).append("|").append(enc(m.hora)).append("|").append(enc(m.tipo));
            if (i < ms.size() - 1) b.append("\n");
        }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_CHAT + chave(o), b.toString()).apply();
    }

    private String ultimaMensagemResumo(Oportunidade o) {
        ArrayList<Mensagem> ms = carregarMensagens(o);
        if (ms.isEmpty()) return "";
        Mensagem m = ms.get(ms.size() - 1);
        String t = m.autor + ": " + m.texto;
        return t.length() > 82 ? t.substring(0, 82) + "..." : t;
    }

    private String statusConversa(Oportunidade o) {
        String s = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_STATUS + chave(o), "");
        if (s == null || s.trim().length() == 0) return carregarMensagens(o).isEmpty() ? AGUARDANDO : EM_CONVERSA;
        return s;
    }

    private void setStatus(Oportunidade o, String s) {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_STATUS + chave(o), s).apply();
    }

    private String chave(Oportunidade o) {
        return String.valueOf(Math.abs((o.titulo + "|" + o.local + "|" + o.autor + "|" + o.contato).hashCode()));
    }

    private TextView statusPill(String s) {
        TextView v = texto(labelStatus(s), 12, corStatus(s), true);
        v.setGravity(Gravity.CENTER);
        v.setPadding(dp(10), dp(5), dp(10), dp(5));
        v.setBackground(bg(fundoStatus(s), dp(999)));
        return v;
    }

    private String labelStatus(String s) {
        if (EM_CONVERSA.equals(s)) return "Em conversa";
        if (COMBINADO.equals(s)) return "Combinado";
        if (CONCLUIDO.equals(s)) return "Concluido";
        return "Aguardando resposta";
    }

    private int corStatus(String s) {
        if (EM_CONVERSA.equals(s)) return BLUE;
        if (COMBINADO.equals(s)) return GREEN;
        if (CONCLUIDO.equals(s)) return Color.rgb(80, 80, 94);
        return TANGERINE;
    }

    private int fundoStatus(String s) {
        if (EM_CONVERSA.equals(s)) return Color.rgb(238, 244, 255);
        if (COMBINADO.equals(s)) return Color.rgb(226, 252, 240);
        if (CONCLUIDO.equals(s)) return CINZA_TINT;
        return SEGURANCA_TINT;
    }

    private void montarTelaPublicar() {
        LinearLayout raiz = base();
        raiz.addView(header("Publicar oportunidade", "Cadastre uma vaga, bico ou servico."));
        raiz.addView(espaco(12));
        if (!perfilCompleto()) {
            raiz.addView(bloco("Dica: cadastre seu perfil antes de publicar para passar mais confianca.", 14, WHITE, true, TANGERINE, dp(18)));
            raiz.addView(espaco(10));
        }
        final EditText tipo = campo("Tipo: VAGA, BICO, SERVICO, URGENTE ou SEGURANCA");
        final EditText titulo = campo("Titulo da oportunidade");
        final EditText cidade = campo("Cidade / bairro");
        final EditText valor = campo("Valor ou salario");
        final EditText desc = campo("Descricao");
        final EditText contato = campo("WhatsApp para contato");
        if (perfilTelefone.trim().length() > 0) contato.setText(perfilTelefone);
        if (perfilCidade.trim().length() > 0) cidade.setText(perfilCidade);
        raiz.addView(tipo); raiz.addView(espaco(8)); raiz.addView(titulo); raiz.addView(espaco(8)); raiz.addView(cidade); raiz.addView(espaco(8)); raiz.addView(valor); raiz.addView(espaco(8)); raiz.addView(desc); raiz.addView(espaco(8)); raiz.addView(contato); raiz.addView(espaco(14));
        raiz.addView(botao("Salvar oportunidade", GREEN, WHITE, new View.OnClickListener() { public void onClick(View v) {
            String tp = tipo.getText().toString().trim().toUpperCase();
            String ti = titulo.getText().toString().trim();
            String ci = cidade.getText().toString().trim();
            String va = valor.getText().toString().trim();
            String de = desc.getText().toString().trim();
            String co = contato.getText().toString().trim();
            if (tp.length() == 0) tp = "BICO";
            if (ti.length() == 0 || ci.length() == 0 || co.length() == 0) { Toast.makeText(MainActivity.this, "Preencha titulo, cidade e contato.", Toast.LENGTH_LONG).show(); return; }
            oportunidades.add(0, new Oportunidade(tp, ti, ci, va, de, co, perfilNome.trim().length() == 0 ? "Usuario local" : perfilNome));
            salvarOportunidades(); filtroAtual = "TODOS";
            Toast.makeText(MainActivity.this, "Oportunidade publicada e salva neste aparelho.", Toast.LENGTH_LONG).show();
            montarTelaPrincipal();
        }}), lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(50)));
        raiz.addView(espaco(10));
        raiz.addView(botao("Voltar", WHITE, INK, new View.OnClickListener() { public void onClick(View v) { montarTelaPrincipal(); }}), lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
        setContentViewComNav(scroll(raiz), "publicar", false);
    }

    private void montarTelaPerfil() {
        LinearLayout raiz = base();
        raiz.addView(header("Meu perfil", "Crie uma identidade para publicar e negociar com mais confianca."));
        raiz.addView(espaco(12));
        final EditText nome = campo("Seu nome ou nome da empresa");
        final EditText cidade = campo("Cidade / bairro");
        final EditText telefone = campo("WhatsApp para contato");
        final EditText tipo = campo("Tipo: Trabalhador, Contratante, Empresa ou Autonomo");
        nome.setText(perfilNome); cidade.setText(perfilCidade); telefone.setText(perfilTelefone); tipo.setText(perfilTipoUsuario);
        raiz.addView(nome); raiz.addView(espaco(8)); raiz.addView(cidade); raiz.addView(espaco(8)); raiz.addView(telefone); raiz.addView(espaco(8)); raiz.addView(tipo); raiz.addView(espaco(14));
        raiz.addView(botao("Salvar perfil", GREEN, WHITE, new View.OnClickListener() { public void onClick(View v) {
            String n = nome.getText().toString().trim();
            String c = cidade.getText().toString().trim();
            String t = telefone.getText().toString().trim();
            String tp = tipo.getText().toString().trim();
            if (n.length() == 0 || t.length() == 0) { Toast.makeText(MainActivity.this, "Preencha pelo menos nome e WhatsApp.", Toast.LENGTH_LONG).show(); return; }
            salvarPerfil(n, c, t, tp.length() == 0 ? "Usuario local" : tp);
            Toast.makeText(MainActivity.this, "Perfil salvo com sucesso.", Toast.LENGTH_LONG).show();
            montarTelaPrincipal();
        }}), lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(50)));
        raiz.addView(espaco(10));
        raiz.addView(bloco("Suas oportunidades, conversas, combinados e avaliacoes ficam salvos neste aparelho. Depois isso vai para um banco online.", 14, INK_LIGHT, false, WHITE, dp(18)));
        raiz.addView(espaco(10));
        raiz.addView(botao("Voltar", WHITE, INK, new View.OnClickListener() { public void onClick(View v) { montarTelaPrincipal(); }}), lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
        setContentViewComNav(scroll(raiz), "perfil", false);
    }

    private View header(String titulo, String subtitulo) {
        LinearLayout h = coluna();
        h.setGravity(Gravity.CENTER_VERTICAL);
        h.setPadding(dp(20), dp(18), dp(20), dp(18));
        h.setBackground(gradient(YELLOW, TANGERINE, dp(28)));
        h.setElevation(dp(4));
        h.addView(texto(titulo, 26, INK, true));
        h.addView(texto(subtitulo, 15, Color.rgb(91, 62, 30), true));
        return h;
    }

    private void setContentViewComNav(View conteudo, String ativo, boolean mostrarFab) {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(CREAM);
        root.addView(conteudo, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        FrameLayout.LayoutParams navLp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(72), Gravity.BOTTOM);
        root.addView(bottomNav(ativo), navLp);
        if (mostrarFab) {
            TextView fab = texto("+", 30, WHITE, true);
            fab.setGravity(Gravity.CENTER);
            fab.setBackground(bg(CORAL, dp(999)));
            fab.setElevation(dp(8));
            fab.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { montarTelaPublicar(); }});
            FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams(dp(58), dp(58), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            fp.setMargins(0, 0, 0, dp(42));
            root.addView(fab, fp);
        }
        setContentView(root);
    }

    private LinearLayout bottomNav(String ativo) {
        LinearLayout nav = linha();
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(14), dp(6), dp(14), dp(6));
        nav.setBackgroundColor(WHITE);
        nav.setElevation(dp(8));
        nav.addView(navItem("Inicio", "inicio", ativo, new View.OnClickListener() { public void onClick(View v) { filtroAtual = "TODOS"; montarTelaPrincipal(); }}), pesoNav());
        nav.addView(navItem("Buscar", "buscar", ativo, new View.OnClickListener() { public void onClick(View v) { Toast.makeText(MainActivity.this, "Busca avancada entra na proxima etapa.", Toast.LENGTH_SHORT).show(); }}), pesoNav());
        nav.addView(new TextView(this), pesoNav());
        nav.addView(navItem("Chat", "chat", ativo, new View.OnClickListener() { public void onClick(View v) { montarTelaConversas(); }}), pesoNav());
        nav.addView(navItem("Perfil", "perfil", ativo, new View.OnClickListener() { public void onClick(View v) { montarTelaPerfil(); }}), pesoNav());
        return nav;
    }

    private TextView navItem(String txt, String chave, String ativo, View.OnClickListener listener) {
        TextView item = texto(txt, 11, chave.equals(ativo) ? INK : INK_LIGHT, true);
        item.setGravity(Gravity.CENTER);
        item.setBackground(chave.equals(ativo) ? bg(Color.rgb(255, 241, 214), dp(18)) : bg(Color.TRANSPARENT, dp(18)));
        item.setOnClickListener(listener);
        return item;
    }

    private void abrirWhatsApp(Oportunidade o) {
        String numero = limparNumero(o.contato);
        if (numero.length() == 0) { Toast.makeText(this, "Contato nao informado.", Toast.LENGTH_LONG).show(); return; }
        String msg = "Ola, vi sua oportunidade no Chama no Trampo e tenho interesse.\n\nOportunidade: " + o.titulo + "\nLocal: " + o.local;
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + numero + "?text=" + Uri.encode(msg)))); }
        catch (Exception e) { Toast.makeText(this, "Nao consegui abrir o WhatsApp neste aparelho.", Toast.LENGTH_LONG).show(); }
    }

    private String limparNumero(String contato) {
        StringBuilder n = new StringBuilder();
        for (int i = 0; i < contato.length(); i++) { char c = contato.charAt(i); if (c >= '0' && c <= '9') n.append(c); }
        String s = n.toString();
        return (s.length() == 10 || s.length() == 11) ? "55" + s : s;
    }

    private int contarFiltro(String filtro) { int t = 0; for (int i = 0; i < oportunidades.size(); i++) if ("TODOS".equals(filtro) || oportunidades.get(i).tipo.equals(filtro)) t++; return t; }
    private String textoPerfilResumo() { return (perfilCidade.trim().length() == 0 ? "Cidade nao informada" : perfilCidade) + " - " + (perfilTipoUsuario.trim().length() == 0 ? "Usuario local" : perfilTipoUsuario); }
    private String autor(Oportunidade o) { return o.autor == null || o.autor.trim().length() == 0 ? "Usuario local" : o.autor; }
    private String agora() { return new SimpleDateFormat("dd/MM HH:mm", new Locale("pt", "BR")).format(new Date()); }
    private int tintTipo(String t) { if ("URGENTE".equals(t)) return URGENTE_TINT; if ("SERVICO".equals(t)) return SERVICO_TINT; if ("BICO".equals(t)) return BICO_TINT; if ("SEGURANCA".equals(t)) return SEGURANCA_TINT; return VAGA_TINT; }
    private int corTipo(String t) { if ("URGENTE".equals(t)) return CORAL; if ("SERVICO".equals(t)) return BLUE; if ("BICO".equals(t)) return PURPLE; if ("SEGURANCA".equals(t)) return TANGERINE; return GREEN; }
    private String labelTipo(String t) { if ("SERVICO".equals(t)) return "SERVICO"; if ("BICO".equals(t)) return "BICO"; if ("URGENTE".equals(t)) return "URGENTE"; if ("SEGURANCA".equals(t)) return "SEGURANCA"; return "VAGA"; }
    private int corPreco(String v) { return v != null && v.contains("R$") ? Color.rgb(10, 138, 77) : INK_LIGHT; }
    private String distanciaFake(Oportunidade o) { int n = Math.abs((o.titulo + o.local).hashCode()) % 48; return (n < 3 ? n + 1 : n) + " km"; }
    private String enc(String t) { return t == null ? "" : Uri.encode(t); }
    private String dec(String t) { return t == null ? "" : Uri.decode(t); }

    private LinearLayout base() { LinearLayout l = coluna(); l.setPadding(dp(18), dp(18), dp(18), dp(128)); return l; }
    private ScrollView scroll(View v) { ScrollView s = new ScrollView(this); s.setBackgroundColor(CREAM); s.addView(v); return s; }
    private LinearLayout coluna() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private LinearLayout linha() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); return l; }
    private LinearLayout colunaCard(int cor) { LinearLayout l = coluna(); l.setPadding(dp(18), dp(16), dp(18), dp(18)); l.setBackground(bg(cor, dp(24))); l.setElevation(dp(3)); return l; }
    private EditText campo(String dica) { EditText e = new EditText(this); e.setHint(dica); e.setTextSize(15); e.setSingleLine(false); e.setPadding(dp(14), dp(12), dp(14), dp(12)); e.setBackground(bg(WHITE, dp(18))); e.setTextColor(INK); e.setHintTextColor(INK_LIGHT); return e; }
    private TextView botao(String txt, int fundo, int cor, View.OnClickListener l) { TextView v = bloco(txt, 15, cor, true, fundo, dp(999)); v.setGravity(Gravity.CENTER); v.setOnClickListener(l); v.setElevation(dp(2)); return v; }
    private TextView bloco(String txt, int tam, int corTexto, boolean negrito, int fundo, int raio) { TextView v = texto(txt, tam, corTexto, negrito); v.setPadding(dp(16), dp(13), dp(16), dp(13)); v.setBackground(bg(fundo, raio)); return v; }
    private TextView texto(String txt, int tam, int cor, boolean negrito) { TextView v = new TextView(this); v.setText(txt); v.setTextSize(tam); v.setTextColor(cor); v.setPadding(0, dp(3), 0, dp(3)); if (negrito) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return v; }
    private GradientDrawable bg(int cor, int raio) { GradientDrawable d = new GradientDrawable(); d.setColor(cor); d.setCornerRadius(raio); return d; }
    private GradientDrawable gradient(int a, int b, int raio) { GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{a, b}); d.setCornerRadius(raio); return d; }
    private LinearLayout.LayoutParams lp(int w, int h) { return new LinearLayout.LayoutParams(w, h); }
    private LinearLayout.LayoutParams pesoNav() { return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1); }
    private View espaco(int h) { View v = new View(this); v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(h))); return v; }
    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }
    private void mostrarTelaErro(Throwable e) { TextView v = new TextView(this); v.setText("Chama no Trampo\n\nO app abriu, mas houve erro na tela.\n\n" + e.getClass().getSimpleName() + "\n" + e.getMessage()); v.setTextSize(18); v.setTextColor(WHITE); v.setBackgroundColor(Color.rgb(127, 29, 29)); v.setGravity(Gravity.CENTER); v.setPadding(dp(24), dp(24), dp(24), dp(24)); setContentView(v); }

    private static class Oportunidade {
        String tipo, titulo, local, valor, descricao, contato, autor;
        Oportunidade(String tipo, String titulo, String local, String valor, String descricao, String contato, String autor) { this.tipo = tipo; this.titulo = titulo; this.local = local; this.valor = valor; this.descricao = descricao; this.contato = contato; this.autor = autor; }
    }

    private static class Mensagem {
        String autor, texto, hora, tipo;
        Mensagem(String autor, String texto, String hora, String tipo) { this.autor = autor; this.texto = texto; this.hora = hora; this.tipo = tipo; }
    }

    private static class Avaliacao {
        int nota;
        String comentario, data;
        Avaliacao(int nota, String comentario, String data) { this.nota = nota; this.comentario = comentario; this.data = data; }
    }
}
