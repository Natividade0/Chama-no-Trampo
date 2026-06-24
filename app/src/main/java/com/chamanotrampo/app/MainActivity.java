package com.chamanotrampo.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String PREFS = "chama_no_trampo_prefs";
    private static final String KEY_OPPORTUNITIES = "opportunities";
    private static final String KEY_PROPOSALS = "proposals";
    private static final String KEY_MESSAGES = "proposal_messages";
    private static final String KEY_FAVORITES = "favorites";
    private static final String KEY_PROFILE = "profile_name";
    private static final String KEY_PROFILE_CITY = "profile_city";
    private static final String KEY_PROFILE_PHONE = "profile_phone";
    private static final String KEY_PROFILE_KIND = "profile_kind";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_HIDDEN = "hidden_listing_ids";
    private static final String KEY_SUSPICIOUS = "suspicious_listing_ids";
    private static final String KEY_BLOCKED_AUTHORS = "blocked_author_keys";
    private static final String KEY_INTERESTED_LISTINGS = "interested_listing_ids";
    private static final String KEY_ENTRY_SEEN = "entry_seen";
    private static final String KEY_APP_MODE = "app_mode";

    private static final String APP_MODE_LOCAL = "local";
    private static final String TYPE_DEMAND = "demanda";
    private static final String TYPE_OFFER = "oferta";
    private static final String STATUS_ACTIVE = "ativo";
    private static final String STATUS_DONE = "concluido";
    private static final String STATUS_EXPIRED = "expirado";

    private static final String PROP_SENT = "enviada";
    private static final String PROP_ACCEPTED = "aceita";
    private static final String PROP_DECLINED = "recusada";
    private static final String PROP_DONE = "concluida";

    private static final String ROLE_OWNER = "owner";
    private static final String ROLE_PROPOSER = "proposer";

    private static final long HOUR = 60L * 60L * 1000L;
    private static final long DAY = 24L * HOUR;
    private static final long URGENT_TTL = DAY;
    private static final long NORMAL_TTL = 7L * DAY;
    private static final long OFFER_TTL = 60L * DAY;

    private static final int CREAM = Color.rgb(255, 248, 235);
    private static final int ORANGE = Color.rgb(255, 132, 24);
    private static final int YELLOW = Color.rgb(255, 207, 79);
    private static final int GREEN = Color.rgb(25, 170, 91);
    private static final int RED = Color.rgb(229, 57, 53);
    private static final int TEXT = Color.rgb(37, 32, 24);
    private static final int MUTED = Color.rgb(116, 98, 77);
    private static final int SOFT = Color.rgb(246, 240, 230);
    private static final int BORDER = Color.rgb(228, 218, 202);

    private FrameLayout rootContainer;
    private LinearLayout contentLayout;
    private LinearLayout bottomNav;
    private EditText searchEditText;
    private SharedPreferences prefs;
    private ArrayList<Opportunity> opportunities;
    private ArrayList<Proposal> proposals;
    private ArrayList<ChatMessage> messages;
    private HashSet<String> favoriteIds;
    private HashSet<String> hiddenListingIds;
    private HashSet<String> suspiciousListingIds;
    private HashSet<String> blockedAuthorKeys;
    private HashSet<String> interestedListingIds;
    private String deviceId;
    private String currentScreen = "home";
    private String lastListScreen = "home";
    private String activeType = "todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootContainer = findViewById(R.id.rootContainer);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        ensureDeviceId();
        loadData();
        buildMainShell();
        if (prefs.getBoolean(KEY_ENTRY_SEEN, false)) showHome(); else showEntryScreen();
    }

    private void ensureDeviceId() {
        deviceId = prefs.getString(KEY_DEVICE_ID, "");
        if (deviceId.length() == 0) {
            deviceId = "device_" + System.currentTimeMillis();
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply();
        }
    }

    private void loadData() {
        favoriteIds = readSet(KEY_FAVORITES);
        hiddenListingIds = readSet(KEY_HIDDEN);
        suspiciousListingIds = readSet(KEY_SUSPICIOUS);
        blockedAuthorKeys = readSet(KEY_BLOCKED_AUTHORS);
        interestedListingIds = readSet(KEY_INTERESTED_LISTINGS);
        opportunities = new ArrayList<Opportunity>();
        proposals = new ArrayList<Proposal>();
        messages = new ArrayList<ChatMessage>();

        String saved = prefs.getString(KEY_OPPORTUNITIES, "");
        if (saved.length() > 0) {
            String[] rows = saved.split("\\n");
            for (int i = 0; i < rows.length; i++) {
                Opportunity item = Opportunity.fromStorage(rows[i], deviceId);
                if (item != null) opportunities.add(item);
            }
        }

        String savedProposals = prefs.getString(KEY_PROPOSALS, "");
        if (savedProposals.length() > 0) {
            String[] rows = savedProposals.split("\\n");
            for (int i = 0; i < rows.length; i++) {
                Proposal proposal = Proposal.fromStorage(rows[i]);
                if (proposal != null) proposals.add(proposal);
            }
        }

        String savedMessages = prefs.getString(KEY_MESSAGES, "");
        if (savedMessages.length() > 0) {
            String[] rows = savedMessages.split("\\n");
            for (int i = 0; i < rows.length; i++) {
                ChatMessage msg = ChatMessage.fromStorage(rows[i]);
                if (msg != null) messages.add(msg);
            }
        }

        long now = System.currentTimeMillis();
        if (opportunities.size() == 0) {
            opportunities.add(Opportunity.seed("seed1", TYPE_DEMAND, "Vaga", false, "Auxiliar de produção", "Guariba - Centro", "1,8 km", now - 3L * HOUR, "Salário a combinar", "Ajudar na organização da linha, separação de produtos e apoio geral na produção.", "Mercado São José", "5516999999999", 4.6, 8));
            opportunities.add(Opportunity.seed("seed2", TYPE_DEMAND, "Servico", false, "Pedreiro para reforma", "Jardinópolis - Jardim Primavera", "4,2 km", now - DAY, "Enviar orçamento", "Reforma pequena em banheiro, troca de piso e acabamento. Preferência para profissionais da região.", "Dona Maria", "5516999999999", 4.9, 15));
            opportunities.add(Opportunity.seed("seed3", TYPE_DEMAND, "Bico", false, "Ajudante para descarregar caminhão", "Ribeirão Preto - Distrito Industrial", "7,5 km", now - 40L * 60L * 1000L, "R$ 120,00 no dia", "Preciso de ajudante hoje para descarregar mercadorias. Pagamento no final do serviço.", "Carlos Fretes", "5516999999999", 4.3, 5));
            opportunities.add(Opportunity.seed("seed4", TYPE_DEMAND, "Servico", true, "Eletricista hoje", "Jaboticabal - Nova Jaboticabal", "2,3 km", now - 12L * 60L * 1000L, "A combinar", "Tomadas pararam de funcionar. Preciso de avaliação e reparo ainda hoje.", "Ana Paula", "5516999999999", 4.7, 11));
            opportunities.add(Opportunity.seed("seed5", TYPE_OFFER, "Servico", false, "Pedreiro disponível para reformas", "Jardinópolis", "4,2 km", now - 2L * HOUR, "A combinar", "Faço pequenos reparos, pisos, pintura e reformas. Atendo na região com combinação por WhatsApp.", "João Carlos", "5516988888888", 0.0, 0));
            saveOpportunities();
        }

        SharedPreferences.Editor editor = prefs.edit();
        if (prefs.getString(KEY_PROFILE, "").length() == 0) editor.putString(KEY_PROFILE, "Visitante do Chama no Trampo");
        if (prefs.getString(KEY_PROFILE_KIND, "").length() == 0) editor.putString(KEY_PROFILE_KIND, "Trabalhador/Contratante");
        if (prefs.getString(KEY_APP_MODE, "").length() == 0) editor.putString(KEY_APP_MODE, APP_MODE_LOCAL);
        editor.apply();
    }

    private void buildMainShell() {
        rootContainer.removeAllViews();
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundColor(CREAM);
        rootContainer.addView(main, new FrameLayout.LayoutParams(-1, -1));

        ScrollView scroll = new ScrollView(this);
        contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dp(16), dp(14), dp(16), dp(14));
        scroll.addView(contentLayout, new ScrollView.LayoutParams(-1, -2));
        main.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        bottomNav = new LinearLayout(this);
        bottomNav.setOrientation(LinearLayout.HORIZONTAL);
        bottomNav.setGravity(Gravity.CENTER);
        bottomNav.setPadding(dp(10), dp(8), dp(10), dp(10));
        bottomNav.setBackgroundColor(CREAM);
        main.addView(bottomNav, new LinearLayout.LayoutParams(-1, dp(84)));
        buildBottomNav();
    }

    private void clear() { contentLayout.removeAllViews(); }

    private void showEntryScreen() {
        currentScreen = "entry";
        bottomNav.setVisibility(View.GONE);
        clear();
        addHeader("Chama no Trampo", "Agora com propostas e conversa local antes do WhatsApp.");
        contentLayout.addView(detailText("Teste o fluxo completo: publicar, receber proposta, conversar, aceitar, combinar e concluir. Tudo fica salvo apenas neste aparelho até a integração com Firebase.", 16, TEXT, Typeface.NORMAL));
        Button local = bigButton("Continuar testando localmente", GREEN, Color.WHITE);
        contentLayout.addView(local);
        local.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { prefs.edit().putBoolean(KEY_ENTRY_SEEN, true).apply(); showHome(); } });
    }

    private void showHome() {
        currentScreen = "home";
        lastListScreen = "home";
        clear();
        addHeader("Chama no Trampo", "Oportunidades, propostas e conversas locais.");
        addStatsPanel();
        addTypeFilters();
        addSearchBox("");
        renderHomeCards();
        buildBottomNav();
    }

    private void addStatsPanel() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(12), 0, dp(2));
        row.addView(statBox("Abertos", String.valueOf(countOpenListings())), new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(statBox("Propostas", String.valueOf(proposals.size())), new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(statBox("Mensagens", String.valueOf(messages.size())), new LinearLayout.LayoutParams(0, -2, 1));
        contentLayout.addView(row);
    }

    private View statBox(String label, String value) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(8), dp(10), dp(8), dp(10));
        box.setBackground(roundedStroke(Color.WHITE, 18, BORDER, 1));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1);
        p.setMargins(dp(3), 0, dp(3), 0);
        box.setLayoutParams(p);
        box.addView(text(value, 22, ORANGE, Typeface.BOLD));
        box.addView(text(label, 12, MUTED, Typeface.BOLD));
        return box;
    }

    private void addTypeFilters() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(10), 0, dp(4));
        addTypeFilter(row, "todos", "Todos");
        addTypeFilter(row, TYPE_DEMAND, "Preciso contratar");
        addTypeFilter(row, TYPE_OFFER, "Quero trabalhar");
        contentLayout.addView(row);
    }

    private void addTypeFilter(LinearLayout row, final String key, String label) {
        TextView chip = text(label, 12, key.equals(activeType) ? Color.WHITE : TEXT, Typeface.BOLD);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(8), dp(9), dp(8), dp(9));
        chip.setBackground(key.equals(activeType) ? rounded(ORANGE, 18) : roundedStroke(Color.WHITE, 18, BORDER, 1));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2, 1);
        params.setMargins(dp(3), 0, dp(3), 0);
        row.addView(chip, params);
        chip.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { activeType = key; showHome(); } });
    }

    private void addSearchBox(String query) {
        searchEditText = new EditText(this);
        searchEditText.setSingleLine(true);
        searchEditText.setHint("Buscar por título, cidade, serviço ou autor");
        searchEditText.setText(query);
        searchEditText.setTextColor(TEXT);
        searchEditText.setHintTextColor(MUTED);
        searchEditText.setTextSize(15);
        searchEditText.setPadding(dp(14), 0, dp(14), 0);
        searchEditText.setBackground(roundedStroke(Color.WHITE, 18, BORDER, 1));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(52));
        params.setMargins(0, dp(10), 0, dp(6));
        contentLayout.addView(searchEditText, params);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { renderHomeCards(); }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private void renderHomeCards() {
        while (contentLayout.getChildCount() > 4) contentLayout.removeViewAt(4);
        String query = normalize(searchEditText == null ? "" : searchEditText.getText().toString());
        int count = 0;
        long now = System.currentTimeMillis();
        for (int i = 0; i < opportunities.size(); i++) {
            Opportunity item = opportunities.get(i);
            if (hiddenListingIds.contains(item.id)) continue;
            if (blockedAuthorKeys.contains(item.authorKey)) continue;
            if (!"todos".equals(activeType) && !activeType.equals(item.listingType)) continue;
            if (!STATUS_ACTIVE.equals(item.resolvedStatus(now))) continue;
            if (query.length() > 0 && !item.matches(query)) continue;
            contentLayout.addView(cardView(item, false));
            count++;
        }
        if (count == 0) contentLayout.addView(empty("Nenhuma oportunidade encontrada."));
    }

    private View cardView(final Opportunity item, final boolean managementMode) {
        LinearLayout card = baseCard();
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.addView(badgeText(categoryIcon(item.category) + " " + displayCategory(item.category), TEXT, SOFT));
        top.addView(badgeText(dealStatusText(item), Color.WHITE, dealStatusColor(item)));
        if (item.urgent) top.addView(badgeText("Urgente", Color.WHITE, RED));
        card.addView(top);

        card.addView(detailText(item.title, 20, TEXT, Typeface.BOLD));
        card.addView(text(locationText(item), 14, MUTED, Typeface.NORMAL));
        card.addView(detailText(item.value, 16, GREEN, Typeface.BOLD));
        card.addView(detailText(item.description, 14, TEXT, Typeface.NORMAL));
        card.addView(detailText("Publicado por " + item.author + " · " + interestText(item), 13, MUTED, Typeface.BOLD));
        card.addView(detailText(proposalSummary(item) + " · " + messageSummary(item.id), 14, ORANGE, Typeface.BOLD));

        LinearLayout row = actionRow();
        Button details = smallButton("Detalhes", SOFT, TEXT);
        row.addView(details, new LinearLayout.LayoutParams(0, dp(44), 1));
        if (managementMode) {
            Button proposalsBtn = smallButton("Propostas", ORANGE, Color.WHITE);
            row.addView(proposalsBtn, sideParams());
            proposalsBtn.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showListingProposals(item); } });
        } else if (!isMine(item)) {
            Button propose = smallButton("Enviar proposta", ORANGE, Color.WHITE);
            row.addView(propose, sideParams());
            propose.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showProposalDialog(item); } });
        }
        card.addView(row);
        details.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showDetails(item); } });
        card.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showDetails(item); } });
        return card;
    }

    private void showDetails(final Opportunity item) {
        if (!"details".equals(currentScreen)) lastListScreen = currentScreen;
        currentScreen = "details";
        clear();
        addHeader("Detalhes", dealStatusText(item) + " · " + proposalSummary(item));
        contentLayout.addView(detailText(item.title, 26, TEXT, Typeface.BOLD));
        contentLayout.addView(detailText(locationText(item), 16, MUTED, Typeface.NORMAL));
        contentLayout.addView(detailText(item.value, 19, GREEN, Typeface.BOLD));
        contentLayout.addView(detailText(item.description, 16, TEXT, Typeface.NORMAL));
        contentLayout.addView(detailText("Anunciante: " + item.author + "\n" + interestText(item) + "\n" + proposalSummary(item) + "\n" + messageSummary(item.id), 15, MUTED, Typeface.BOLD));

        if (isMine(item)) {
            Button proposalsBtn = bigButton("Ver propostas recebidas", ORANGE, Color.WHITE);
            Button edit = bigButton("Editar anúncio", SOFT, TEXT);
            Button done = bigButton("Marcar como concluído", GREEN, Color.WHITE);
            contentLayout.addView(proposalsBtn);
            contentLayout.addView(edit);
            contentLayout.addView(done);
            proposalsBtn.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showListingProposals(item); } });
            edit.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showListingDialog(item); } });
            done.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { item.status = STATUS_DONE; saveOpportunities(); showDetails(item); } });
        } else {
            Button propose = bigButton("Enviar proposta", ORANGE, Color.WHITE);
            Button whatsapp = bigButton("Chamar no WhatsApp", GREEN, Color.WHITE);
            Button favorite = bigButton(favoriteIds.contains(item.id) ? "★ Remover dos salvos" : "☆ Salvar oportunidade", SOFT, TEXT);
            contentLayout.addView(propose);
            contentLayout.addView(whatsapp);
            contentLayout.addView(favorite);
            propose.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showProposalDialog(item); } });
            whatsapp.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { abrirWhatsapp(item); } });
            favorite.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { toggleFavorite(item.id); showDetails(item); } });
            addModerationActions(item);
        }
        Button back = bigButton("Voltar", SOFT, TEXT);
        contentLayout.addView(back);
        back.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { returnToLastScreen(); } });
        buildBottomNav();
    }

    private void showProposalDialog(final Opportunity item) {
        final LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(12), dp(4), dp(12), 0);
        final EditText name = field("Seu nome");
        final EditText phone = field("Seu WhatsApp com DDD");
        final EditText value = field(item.isOffer() ? "Quanto você quer combinar?" : "Sua proposta de valor");
        final EditText deadline = field("Prazo ou disponibilidade");
        final EditText message = field("Mensagem para o anunciante");
        name.setText(prefs.getString(KEY_PROFILE, ""));
        phone.setText(prefs.getString(KEY_PROFILE_PHONE, ""));
        form.addView(detailText("Envie uma proposta. Depois vocês podem conversar dentro do app e usar WhatsApp só para finalizar.", 14, MUTED, Typeface.NORMAL));
        form.addView(name); form.addView(phone); form.addView(value); form.addView(deadline); form.addView(message);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Enviar proposta")
                .setView(form)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Enviar", null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        String phoneNumber = normalizePhoneForWhatsApp(phone.getText().toString());
                        if (name.getText().toString().trim().length() == 0 || !isValidWhatsAppNumber(phoneNumber)) {
                            Toast.makeText(MainActivity.this, "Informe seu nome e WhatsApp válido.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        long now = System.currentTimeMillis();
                        Proposal proposal = new Proposal("prop" + now, item.id, item.title, deviceId, name.getText().toString().trim(), phoneNumber, textOr(value, "A combinar"), textOr(deadline, "A combinar"), textOr(message, "Tenho interesse e gostaria de combinar."), PROP_SENT, now);
                        proposals.add(0, proposal);
                        addMessage(proposal, ROLE_PROPOSER, proposal.fromName, proposal.message);
                        saveProposals();
                        saveMessages();
                        if (!interestedListingIds.contains(item.id)) {
                            interestedListingIds.add(item.id);
                            item.interestCount++;
                            saveOpportunities();
                            saveSet(KEY_INTERESTED_LISTINGS, interestedListingIds);
                        }
                        Toast.makeText(MainActivity.this, "Proposta enviada. A conversa foi aberta.", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        showProposalChat(proposal, item, false);
                    }
                });
            }
        });
        dialog.show();
    }

    private void showProposalCenter() {
        currentScreen = "proposals";
        clear();
        addHeader("Propostas", "Acompanhe negociações, conversas e acordos.");
        int received = 0;
        for (int i = 0; i < proposals.size(); i++) {
            Proposal p = proposals.get(i);
            Opportunity item = findOpportunity(p.listingId);
            if (item != null && isMine(item)) {
                if (received == 0) contentLayout.addView(detailText("Recebidas", 20, TEXT, Typeface.BOLD));
                contentLayout.addView(proposalCard(p, item, true));
                received++;
            }
        }
        if (received == 0) contentLayout.addView(empty("Nenhuma proposta recebida ainda."));

        int sent = 0;
        for (int i = 0; i < proposals.size(); i++) {
            Proposal p = proposals.get(i);
            if (deviceId.equals(p.fromDeviceId)) {
                if (sent == 0) contentLayout.addView(detailText("Enviadas por você", 20, TEXT, Typeface.BOLD));
                contentLayout.addView(proposalCard(p, findOpportunity(p.listingId), false));
                sent++;
            }
        }
        if (sent == 0) contentLayout.addView(empty("Você ainda não enviou propostas."));
        buildBottomNav();
    }

    private void showListingProposals(final Opportunity item) {
        currentScreen = "listingProposals";
        clear();
        addHeader("Propostas recebidas", item.title);
        int count = 0;
        for (int i = 0; i < proposals.size(); i++) {
            Proposal p = proposals.get(i);
            if (!item.id.equals(p.listingId)) continue;
            contentLayout.addView(proposalCard(p, item, true));
            count++;
        }
        if (count == 0) contentLayout.addView(empty("Este anúncio ainda não recebeu propostas."));
        Button back = bigButton("Voltar ao anúncio", SOFT, TEXT);
        contentLayout.addView(back);
        back.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showDetails(item); } });
        buildBottomNav();
    }

    private View proposalCard(final Proposal p, final Opportunity item, final boolean ownerMode) {
        LinearLayout card = baseCard();
        card.addView(badgeText(proposalStatusLabel(p), Color.WHITE, proposalStatusColor(p.status)));
        card.addView(detailText(p.listingTitle, 18, TEXT, Typeface.BOLD));
        card.addView(detailText("Proposta de " + p.fromName + "\nValor: " + p.value + "\nPrazo: " + p.deadline + "\nMensagem: " + p.message + "\n" + chatSummary(p.id), 15, TEXT, Typeface.NORMAL));
        LinearLayout row = actionRow();
        Button chat = smallButton("Conversa", ORANGE, Color.WHITE);
        Button contact = smallButton("WhatsApp", GREEN, Color.WHITE);
        row.addView(chat, new LinearLayout.LayoutParams(0, dp(44), 1));
        row.addView(contact, sideParams());
        chat.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showProposalChat(p, item, ownerMode); } });
        contact.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { openWhatsApp(p.phone, "Olá, vi sua proposta no Chama no Trampo sobre: " + p.listingTitle); } });
        if (ownerMode && PROP_SENT.equals(p.status)) {
            Button accept = smallButton("Aceitar", ORANGE, Color.WHITE);
            Button decline = smallButton("Recusar", SOFT, TEXT);
            row.addView(accept, sideParams());
            row.addView(decline, sideParams());
            accept.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { acceptProposal(p, item, true); } });
            decline.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { declineProposal(p, item, true); } });
        } else if (ownerMode && PROP_ACCEPTED.equals(p.status)) {
            Button done = smallButton("Concluir", ORANGE, Color.WHITE);
            row.addView(done, sideParams());
            done.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { concludeProposal(p, item, true); } });
        }
        card.addView(row);
        return card;
    }

    private void showProposalChat(final Proposal p, final Opportunity item, final boolean ownerMode) {
        currentScreen = "proposalChat";
        clear();
        addHeader("Conversa", proposalStatusLabel(p) + " · " + p.listingTitle);
        contentLayout.addView(detailText("Valor: " + p.value + "\nPrazo: " + p.deadline + "\nProposta inicial: " + p.message, 15, TEXT, Typeface.NORMAL));

        int count = 0;
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage msg = messages.get(i);
            if (!p.id.equals(msg.proposalId)) continue;
            contentLayout.addView(messageBubble(msg, ownerMode));
            count++;
        }
        if (count == 0) contentLayout.addView(empty("Nenhuma mensagem ainda. Use uma resposta rápida ou escreva abaixo."));

        addQuickMessages(p, item, ownerMode);

        final EditText input = field(ownerMode ? "Responder ao interessado" : "Responder ao anunciante");
        Button send = bigButton("Enviar mensagem", ORANGE, Color.WHITE);
        contentLayout.addView(input);
        contentLayout.addView(send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String body = input.getText().toString().trim();
                if (body.length() == 0) {
                    Toast.makeText(MainActivity.this, "Digite uma mensagem.", Toast.LENGTH_LONG).show();
                    return;
                }
                sendChatMessage(p, item, ownerMode, body);
            }
        });

        addProposalStateActions(p, item, ownerMode);

        Button back = bigButton("Voltar às propostas", SOFT, TEXT);
        contentLayout.addView(back);
        back.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showProposalCenter(); } });
        buildBottomNav();
    }

    private View messageBubble(ChatMessage msg, boolean ownerMode) {
        boolean mine = (ownerMode && ROLE_OWNER.equals(msg.role)) || (!ownerMode && ROLE_PROPOSER.equals(msg.role));
        LinearLayout card = baseCard();
        card.setBackground(roundedStroke(mine ? Color.rgb(255, 244, 225) : Color.WHITE, 18, BORDER, 1));
        card.addView(text((mine ? "Você" : msg.senderName) + " · " + timeAgo(msg.createdAt), 12, MUTED, Typeface.BOLD));
        card.addView(detailText(msg.body, 15, TEXT, Typeface.NORMAL));
        return card;
    }

    private void addQuickMessages(final Proposal p, final Opportunity item, final boolean ownerMode) {
        LinearLayout row1 = actionRow();
        Button q1 = smallButton("Qual horário?", SOFT, TEXT);
        Button q2 = smallButton("Pode por esse valor?", SOFT, TEXT);
        row1.addView(q1, new LinearLayout.LayoutParams(0, dp(42), 1));
        row1.addView(q2, sideParams());
        contentLayout.addView(row1);

        LinearLayout row2 = actionRow();
        Button q3 = smallButton("Me manda endereço", SOFT, TEXT);
        Button q4 = smallButton("Serviço combinado", GREEN, Color.WHITE);
        row2.addView(q3, new LinearLayout.LayoutParams(0, dp(42), 1));
        row2.addView(q4, sideParams());
        contentLayout.addView(row2);

        q1.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { sendChatMessage(p, item, ownerMode, "Qual é o melhor horário para combinarmos?"); } });
        q2.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { sendChatMessage(p, item, ownerMode, "Consegue fazer por esse valor?"); } });
        q3.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { sendChatMessage(p, item, ownerMode, "Pode me mandar o endereço ou ponto de referência?"); } });
        q4.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { sendChatMessage(p, item, ownerMode, "Combinado. Vou seguir conforme acertamos."); if (ownerMode && PROP_SENT.equals(p.status)) acceptProposal(p, item, false); } });
    }

    private void addProposalStateActions(final Proposal p, final Opportunity item, final boolean ownerMode) {
        LinearLayout row = actionRow();
        Button whatsapp = smallButton("WhatsApp", GREEN, Color.WHITE);
        row.addView(whatsapp, new LinearLayout.LayoutParams(0, dp(44), 1));
        whatsapp.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { openWhatsApp(p.phone, "Olá, vamos combinar pelo Chama no Trampo sobre: " + p.listingTitle); } });

        if (ownerMode && PROP_SENT.equals(p.status)) {
            Button accept = smallButton("Aceitar", ORANGE, Color.WHITE);
            Button decline = smallButton("Recusar", SOFT, TEXT);
            row.addView(accept, sideParams());
            row.addView(decline, sideParams());
            accept.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { acceptProposal(p, item, false); } });
            decline.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { declineProposal(p, item, false); } });
        } else if (ownerMode && PROP_ACCEPTED.equals(p.status)) {
            Button done = smallButton("Concluir", ORANGE, Color.WHITE);
            row.addView(done, sideParams());
            done.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { concludeProposal(p, item, false); } });
        }
        contentLayout.addView(row);
    }

    private void sendChatMessage(Proposal p, Opportunity item, boolean ownerMode, String body) {
        String role = ownerMode ? ROLE_OWNER : ROLE_PROPOSER;
        String fallbackName = ownerMode && item != null ? item.author : p.fromName;
        String sender = prefs.getString(KEY_PROFILE, fallbackName);
        if (sender == null || sender.trim().length() == 0) sender = fallbackName;
        addMessage(p, role, sender, body);
        saveMessages();
        Toast.makeText(this, "Mensagem enviada.", Toast.LENGTH_SHORT).show();
        showProposalChat(p, item, ownerMode);
    }

    private void addMessage(Proposal p, String role, String senderName, String body) {
        long now = System.currentTimeMillis();
        messages.add(new ChatMessage("msg" + now, p.id, role, senderName, body, now));
    }

    private void acceptProposal(Proposal p, Opportunity item, boolean backToCenter) {
        p.status = PROP_ACCEPTED;
        addMessage(p, ROLE_OWNER, item == null ? "Anunciante" : item.author, "Proposta aceita. Agora podemos combinar os detalhes.");
        saveProposals();
        saveMessages();
        Toast.makeText(this, "Proposta aceita.", Toast.LENGTH_LONG).show();
        if (backToCenter) showProposalCenter(); else showProposalChat(p, item, true);
    }

    private void declineProposal(Proposal p, Opportunity item, boolean backToCenter) {
        p.status = PROP_DECLINED;
        addMessage(p, ROLE_OWNER, item == null ? "Anunciante" : item.author, "Proposta recusada. Obrigado pelo interesse.");
        saveProposals();
        saveMessages();
        if (backToCenter) showProposalCenter(); else showProposalChat(p, item, true);
    }

    private void concludeProposal(Proposal p, Opportunity item, boolean backToCenter) {
        p.status = PROP_DONE;
        if (item != null) item.status = STATUS_DONE;
        addMessage(p, ROLE_OWNER, item == null ? "Anunciante" : item.author, "Negociação concluída.");
        saveProposals();
        saveMessages();
        saveOpportunities();
        if (backToCenter) showProposalCenter(); else showProposalChat(p, item, true);
    }

    private void showPublishDialog() { showListingDialog(null); }

    private void showListingDialog(final Opportunity editItem) {
        final boolean editing = editItem != null;
        final LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(12), dp(4), dp(12), 0);
        final String[] type = new String[]{editing ? editItem.listingType : TYPE_DEMAND};
        final TextView typeLabel = detailText("Tipo: " + (TYPE_OFFER.equals(type[0]) ? "Quero trabalhar / oferecer serviço" : "Preciso contratar"), 15, TEXT, Typeface.BOLD);
        Button changeType = smallButton("Alternar tipo", ORANGE, Color.WHITE);
        final EditText title = field("Título");
        final EditText place = field("Cidade/local");
        final EditText value = field("Valor");
        final EditText desc = field("Descrição");
        final EditText author = field("Publicado por");
        final EditText phone = field("WhatsApp com DDD");
        if (editing) {
            title.setText(editItem.title); place.setText(editItem.place); value.setText(editItem.value); desc.setText(editItem.description); author.setText(editItem.author); phone.setText(editItem.phone);
        } else {
            author.setText(prefs.getString(KEY_PROFILE, "")); phone.setText(prefs.getString(KEY_PROFILE_PHONE, ""));
        }
        form.addView(typeLabel); form.addView(changeType); form.addView(title); form.addView(place); form.addView(value); form.addView(desc); form.addView(author); form.addView(phone);
        changeType.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { type[0] = TYPE_OFFER.equals(type[0]) ? TYPE_DEMAND : TYPE_OFFER; typeLabel.setText("Tipo: " + (TYPE_OFFER.equals(type[0]) ? "Quero trabalhar / oferecer serviço" : "Preciso contratar")); } });

        final AlertDialog dialog = new AlertDialog.Builder(this).setTitle(editing ? "Editar anúncio" : "Publicar oportunidade").setView(form).setNegativeButton("Cancelar", null).setPositiveButton(editing ? "Salvar" : "Publicar", null).create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() { @Override public void onShow(DialogInterface di) { dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) {
            String phoneNumber = normalizePhoneForWhatsApp(phone.getText().toString());
            if (title.getText().toString().trim().length() == 0 || place.getText().toString().trim().length() == 0 || !isValidWhatsAppNumber(phoneNumber)) { Toast.makeText(MainActivity.this, "Informe título, local e WhatsApp válido.", Toast.LENGTH_LONG).show(); return; }
            long now = System.currentTimeMillis();
            String authorName = textOr(author, prefs.getString(KEY_PROFILE, "Visitante"));
            if (editing) {
                editItem.listingType = type[0]; editItem.category = TYPE_OFFER.equals(type[0]) ? "Servico" : editItem.category; editItem.title = title.getText().toString(); editItem.place = place.getText().toString(); editItem.value = textOr(value, "A combinar"); editItem.description = textOr(desc, "Sem descrição informada."); editItem.author = authorName; editItem.phone = phoneNumber; editItem.authorKey = authorKeyFor(phoneNumber, authorName); saveOpportunities(); showMyPublications();
            } else {
                Opportunity item = new Opportunity("local" + now, deviceId, authorKeyFor(phoneNumber, authorName), type[0], TYPE_OFFER.equals(type[0]) ? "Servico" : "Bico", false, title.getText().toString(), place.getText().toString(), "distância não informada", textOr(value, "A combinar"), textOr(desc, "Sem descrição informada."), authorName, phoneNumber, STATUS_ACTIVE, now, now + ttlFor(type[0], false), false, 0.0, 0, 0);
                opportunities.add(0, item); saveOpportunities(); showHome();
            }
            dialog.dismiss();
        }}); }});
        dialog.show();
    }

    private void showProfile() {
        currentScreen = "profile";
        clear();
        addHeader("Perfil", "Dados locais usados nas publicações e propostas.");
        final EditText name = field("Nome do perfil");
        final EditText city = field("Cidade principal");
        final EditText phone = field("WhatsApp com DDD");
        final EditText kind = field("Tipo: Trabalhador, Contratante ou Empresa");
        name.setText(prefs.getString(KEY_PROFILE, ""));
        city.setText(prefs.getString(KEY_PROFILE_CITY, ""));
        phone.setText(prefs.getString(KEY_PROFILE_PHONE, ""));
        kind.setText(prefs.getString(KEY_PROFILE_KIND, "Trabalhador/Contratante"));
        contentLayout.addView(name); contentLayout.addView(city); contentLayout.addView(phone); contentLayout.addView(kind);
        Button save = bigButton("Salvar perfil", ORANGE, Color.WHITE);
        Button mine = bigButton("Minhas publicações", SOFT, TEXT);
        Button moderation = bigButton("Bloqueios e ocultos", SOFT, TEXT);
        contentLayout.addView(save); contentLayout.addView(mine); contentLayout.addView(moderation);
        contentLayout.addView(detailText("Publicações: " + countLocal() + "\nPropostas recebidas: " + countReceivedProposals() + "\nPropostas aceitas: " + countAcceptedProposals() + "\nMensagens: " + messages.size(), 16, TEXT, Typeface.NORMAL));
        save.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { prefs.edit().putString(KEY_PROFILE, name.getText().toString()).putString(KEY_PROFILE_CITY, city.getText().toString()).putString(KEY_PROFILE_PHONE, normalizePhoneForWhatsApp(phone.getText().toString())).putString(KEY_PROFILE_KIND, kind.getText().toString()).apply(); Toast.makeText(MainActivity.this, "Perfil salvo.", Toast.LENGTH_LONG).show(); } });
        mine.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showMyPublications(); } });
        moderation.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showModerationControls(); } });
        buildBottomNav();
    }

    private void showMyPublications() {
        currentScreen = "mine";
        clear();
        addHeader("Minhas publicações", "Gerencie anúncios, propostas e conversas recebidas.");
        int count = 0;
        for (int i = 0; i < opportunities.size(); i++) {
            Opportunity item = opportunities.get(i);
            if (!isMine(item)) continue;
            contentLayout.addView(cardView(item, true));
            count++;
        }
        if (count == 0) contentLayout.addView(empty("Você ainda não publicou nenhuma oportunidade."));
        buildBottomNav();
    }

    private void showModerationControls() {
        currentScreen = "moderation";
        clear();
        addHeader("Bloqueios e ocultos", "Controle anúncios ocultados e anunciantes bloqueados.");
        contentLayout.addView(detailText("Anúncios ocultos: " + hiddenListingIds.size() + "\nAnunciantes bloqueados: " + blockedAuthorKeys.size(), 16, TEXT, Typeface.BOLD));
        for (int i = 0; i < opportunities.size(); i++) {
            final Opportunity item = opportunities.get(i);
            if (!hiddenListingIds.contains(item.id)) continue;
            LinearLayout card = baseCard();
            card.addView(detailText(item.title, 18, TEXT, Typeface.BOLD));
            Button restore = bigButton("Restaurar anúncio", GREEN, Color.WHITE);
            card.addView(restore);
            restore.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { hiddenListingIds.remove(item.id); suspiciousListingIds.remove(item.id); saveSet(KEY_HIDDEN, hiddenListingIds); saveSet(KEY_SUSPICIOUS, suspiciousListingIds); showModerationControls(); } });
            contentLayout.addView(card);
        }
        Button back = bigButton("Voltar ao perfil", SOFT, TEXT);
        contentLayout.addView(back);
        back.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showProfile(); } });
        buildBottomNav();
    }

    private void addModerationActions(final Opportunity item) {
        Button hide = bigButton("Ocultar anúncio", SOFT, TEXT);
        Button block = bigButton("Bloquear anunciante", SOFT, TEXT);
        contentLayout.addView(hide); contentLayout.addView(block);
        hide.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { hiddenListingIds.add(item.id); suspiciousListingIds.add(item.id); saveSet(KEY_HIDDEN, hiddenListingIds); saveSet(KEY_SUSPICIOUS, suspiciousListingIds); showHome(); } });
        block.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { blockedAuthorKeys.add(item.authorKey); saveSet(KEY_BLOCKED_AUTHORS, blockedAuthorKeys); showHome(); } });
    }

    private void abrirWhatsapp(Opportunity item) {
        String phone = normalizePhoneForWhatsApp(item.phone);
        if (!isValidWhatsAppNumber(phone)) { Toast.makeText(this, "Número de WhatsApp inválido ou não informado neste anúncio.", Toast.LENGTH_LONG).show(); return; }
        Toast.makeText(this, "Confira os dados do anúncio antes de combinar pelo WhatsApp.", Toast.LENGTH_LONG).show();
        if (!interestedListingIds.contains(item.id)) { interestedListingIds.add(item.id); item.interestCount++; saveOpportunities(); saveSet(KEY_INTERESTED_LISTINGS, interestedListingIds); }
        openWhatsApp(phone, item.isOffer() ? "Olá! Vi seu anúncio '" + item.title + "' no Chama no Trampo e tenho interesse." : "Olá! Vi a oportunidade '" + item.title + "' no Chama no Trampo e tenho interesse.");
    }

    private void openWhatsApp(String phone, String message) {
        String normalized = normalizePhoneForWhatsApp(phone);
        if (!isValidWhatsAppNumber(normalized)) { Toast.makeText(this, "WhatsApp inválido.", Toast.LENGTH_LONG).show(); return; }
        String url = "https://wa.me/" + normalized + "?text=" + Uri.encode(message);
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (Exception erro) { Toast.makeText(this, "Não foi possível abrir o WhatsApp. Verifique se ele está instalado.", Toast.LENGTH_LONG).show(); }
    }

    private void toggleFavorite(String id) { if (favoriteIds.contains(id)) favoriteIds.remove(id); else favoriteIds.add(id); saveSet(KEY_FAVORITES, favoriteIds); }

    private void returnToLastScreen() { if ("proposals".equals(lastListScreen)) showProposalCenter(); else if ("mine".equals(lastListScreen)) showMyPublications(); else showHome(); }

    private void buildBottomNav() {
        bottomNav.setVisibility(View.VISIBLE);
        bottomNav.removeAllViews();
        LinearLayout dock = new LinearLayout(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER);
        dock.setPadding(dp(8), dp(6), dp(8), dp(6));
        dock.setBackground(roundedStroke(Color.WHITE, 26, Color.rgb(235, 225, 210), 1));
        addNavItem(dock, "Início", "home");
        addNavItem(dock, "Publicar", "publish");
        addNavItem(dock, "Propostas", "proposals");
        addNavItem(dock, "Perfil", "profile");
        bottomNav.addView(dock, new LinearLayout.LayoutParams(-1, -1));
    }

    private void addNavItem(LinearLayout dock, String label, final String target) {
        boolean selected = currentScreen.equals(target) || ("proposals".equals(target) && ("proposalChat".equals(currentScreen) || "listingProposals".equals(currentScreen))) || ("profile".equals(target) && ("mine".equals(currentScreen) || "moderation".equals(currentScreen)));
        TextView item = text(label, 12, selected ? ORANGE : MUTED, Typeface.BOLD);
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(4), dp(4), dp(4), dp(4));
        item.setBackground(selected ? rounded(Color.rgb(255, 244, 225), 20) : rounded(Color.TRANSPARENT, 20));
        dock.addView(item, new LinearLayout.LayoutParams(0, dp(54), 1));
        item.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { if ("home".equals(target)) showHome(); else if ("publish".equals(target)) showPublishDialog(); else if ("proposals".equals(target)) showProposalCenter(); else showProfile(); } });
    }

    @Override public void onBackPressed() { if ("home".equals(currentScreen)) super.onBackPressed(); else showHome(); }

    private void saveOpportunities() { StringBuilder b = new StringBuilder(); for (int i = 0; i < opportunities.size(); i++) { if (i > 0) b.append('\n'); b.append(opportunities.get(i).toStorage()); } prefs.edit().putString(KEY_OPPORTUNITIES, b.toString()).apply(); }
    private void saveProposals() { StringBuilder b = new StringBuilder(); for (int i = 0; i < proposals.size(); i++) { if (i > 0) b.append('\n'); b.append(proposals.get(i).toStorage()); } prefs.edit().putString(KEY_PROPOSALS, b.toString()).apply(); }
    private void saveMessages() { StringBuilder b = new StringBuilder(); for (int i = 0; i < messages.size(); i++) { if (i > 0) b.append('\n'); b.append(messages.get(i).toStorage()); } prefs.edit().putString(KEY_MESSAGES, b.toString()).apply(); }
    private HashSet<String> readSet(String key) { HashSet<String> set = new HashSet<String>(); String raw = prefs.getString(key, ""); if (raw.length() > 0) { String[] parts = raw.split(","); for (int i = 0; i < parts.length; i++) if (parts[i].trim().length() > 0) set.add(parts[i].trim()); } return set; }
    private void saveSet(String key, HashSet<String> set) { StringBuilder b = new StringBuilder(); for (String value : set) { if (b.length() > 0) b.append(','); b.append(value); } prefs.edit().putString(key, b.toString()).apply(); }

    private int countOpenListings() { int total = 0; long now = System.currentTimeMillis(); for (int i = 0; i < opportunities.size(); i++) if (STATUS_ACTIVE.equals(opportunities.get(i).resolvedStatus(now))) total++; return total; }
    private int countLocal() { int total = 0; for (int i = 0; i < opportunities.size(); i++) if (isMine(opportunities.get(i))) total++; return total; }
    private int countReceivedProposals() { int total = 0; for (int i = 0; i < proposals.size(); i++) { Opportunity item = findOpportunity(proposals.get(i).listingId); if (item != null && isMine(item)) total++; } return total; }
    private int countAcceptedProposals() { int total = 0; for (int i = 0; i < proposals.size(); i++) if (PROP_ACCEPTED.equals(proposals.get(i).status)) total++; return total; }
    private Opportunity findOpportunity(String id) { for (int i = 0; i < opportunities.size(); i++) if (opportunities.get(i).id.equals(id)) return opportunities.get(i); return null; }
    private int proposalCount(String listingId) { int total = 0; for (int i = 0; i < proposals.size(); i++) if (listingId.equals(proposals.get(i).listingId)) total++; return total; }
    private int messageCountForProposal(String proposalId) { int total = 0; for (int i = 0; i < messages.size(); i++) if (proposalId.equals(messages.get(i).proposalId)) total++; return total; }
    private int messageCountForListing(String listingId) { int total = 0; for (int i = 0; i < proposals.size(); i++) if (listingId.equals(proposals.get(i).listingId)) total += messageCountForProposal(proposals.get(i).id); return total; }
    private boolean hasAcceptedProposal(String listingId) { for (int i = 0; i < proposals.size(); i++) if (listingId.equals(proposals.get(i).listingId) && PROP_ACCEPTED.equals(proposals.get(i).status)) return true; return false; }
    private boolean hasDoneProposal(String listingId) { for (int i = 0; i < proposals.size(); i++) if (listingId.equals(proposals.get(i).listingId) && PROP_DONE.equals(proposals.get(i).status)) return true; return false; }

    private String proposalSummary(Opportunity item) { int count = proposalCount(item.id); if (count == 0) return "Nenhuma proposta ainda"; if (count == 1) return "1 proposta recebida"; return count + " propostas recebidas"; }
    private String messageSummary(String listingId) { int count = messageCountForListing(listingId); if (count == 0) return "sem mensagens"; if (count == 1) return "1 mensagem"; return count + " mensagens"; }
    private String chatSummary(String proposalId) { int count = messageCountForProposal(proposalId); if (count == 0) return "Conversa: sem mensagens"; if (count == 1) return "Conversa: 1 mensagem"; return "Conversa: " + count + " mensagens"; }
    private String dealStatusText(Opportunity item) { if (STATUS_DONE.equals(item.status) || hasDoneProposal(item.id)) return "Concluído"; if (hasAcceptedProposal(item.id)) return "Combinado"; if (proposalCount(item.id) > 0) return "Em negociação"; return "Aberto"; }
    private int dealStatusColor(Opportunity item) { if (STATUS_DONE.equals(item.status) || hasDoneProposal(item.id)) return MUTED; if (hasAcceptedProposal(item.id)) return GREEN; if (proposalCount(item.id) > 0) return ORANGE; return Color.rgb(33, 33, 33); }
    private String interestText(Opportunity item) { if (item.interestCount <= 0) return "Nenhum interesse registrado"; if (item.interestCount == 1) return "1 pessoa demonstrou interesse"; return item.interestCount + " pessoas demonstraram interesse"; }
    private String proposalStatusLabel(Proposal p) { if (PROP_ACCEPTED.equals(p.status)) return "Aceita"; if (PROP_DECLINED.equals(p.status)) return "Recusada"; if (PROP_DONE.equals(p.status)) return "Concluída"; if (messageCountForProposal(p.id) > 1) return "Em conversa"; return "Enviada"; }
    private int proposalStatusColor(String status) { if (PROP_ACCEPTED.equals(status)) return GREEN; if (PROP_DECLINED.equals(status)) return RED; if (PROP_DONE.equals(status)) return MUTED; return ORANGE; }
    private String timeAgo(long createdAt) { long diff = Math.max(0, System.currentTimeMillis() - createdAt); if (diff < HOUR) return (diff / (60L * 1000L)) + " min"; if (diff < DAY) return (diff / HOUR) + "h"; return (diff / DAY) + " dia" + ((diff / DAY) > 1 ? "s" : ""); }

    private boolean isMine(Opportunity item) { return item.id.startsWith("local") || deviceId.equals(item.ownerDeviceId); }
    private String locationText(Opportunity item) { return item.place + (item.distance == null || item.distance.length() == 0 ? "" : " · " + item.distance); }
    private long ttlFor(String listingType, boolean urgent) { if (TYPE_OFFER.equals(listingType)) return OFFER_TTL; return urgent ? URGENT_TTL : NORMAL_TTL; }
    private String authorKeyFor(String phone, String author) { String digits = normalizePhoneForWhatsApp(phone); return digits.length() > 0 ? "phone_" + digits : "unknown_" + normalize(author).replace(" ", "_"); }
    private String displayCategory(String value) { String c = normalizeCategory(value); return "Servico".equals(c) ? "Serviço" : c; }
    private String categoryIcon(String value) { String c = normalizeCategory(value); if ("Vaga".equals(c)) return "💼"; if ("Bico".equals(c)) return "📦"; return "🛠"; }
    private String normalizeCategory(String value) { String v = normalize(value); if (v.contains("vaga")) return "Vaga"; if (v.contains("bico")) return "Bico"; return "Servico"; }
    private String normalize(String value) { if (value == null) return ""; String lower = value.toLowerCase(Locale.US).trim(); try { String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD); return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); } catch (Exception e) { return lower; } }
    private String onlyDigits(String value) { StringBuilder out = new StringBuilder(); if (value == null) return ""; for (int i = 0; i < value.length(); i++) { char c = value.charAt(i); if (c >= '0' && c <= '9') out.append(c); } return out.toString(); }
    private String normalizePhoneForWhatsApp(String value) { String digits = onlyDigits(value); if (digits.length() == 10 || digits.length() == 11) return "55" + digits; return digits; }
    private boolean isValidWhatsAppNumber(String value) { String digits = normalizePhoneForWhatsApp(value); return (digits.length() == 12 || digits.length() == 13) && digits.startsWith("55"); }
    private String textOr(EditText e, String fallback) { String v = e.getText().toString().trim(); return v.length() == 0 ? fallback : v; }

    private TextView text(String value, int sp, int color, int style) { TextView view = new TextView(this); view.setText(value); view.setTextSize(sp); view.setTextColor(color); view.setTypeface(Typeface.DEFAULT, style); return view; }
    private TextView detailText(String value, int sp, int color, int style) { TextView v = text(value, sp, color, style); v.setPadding(0, dp(10), 0, 0); return v; }
    private TextView empty(String msg) { TextView v = detailText(msg, 16, MUTED, Typeface.NORMAL); v.setGravity(Gravity.CENTER); v.setPadding(dp(12), dp(24), dp(12), dp(24)); return v; }
    private TextView badgeText(String value, int fg, int bg) { TextView v = text(value, 12, fg, Typeface.BOLD); v.setGravity(Gravity.CENTER); v.setPadding(dp(9), dp(5), dp(9), dp(5)); v.setBackground(rounded(bg, 14)); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, -2); p.setMargins(0, 0, dp(6), 0); v.setLayoutParams(p); return v; }
    private Button bigButton(String label, int bg, int fg) { Button b = smallButton(label, bg, fg); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(52)); p.setMargins(0, dp(12), 0, 0); b.setLayoutParams(p); return b; }
    private Button smallButton(String label, int bg, int fg) { Button b = new Button(this); b.setText(label); b.setTextSize(12); b.setTextColor(fg); b.setAllCaps(false); b.setMinHeight(0); b.setMinWidth(0); b.setPadding(dp(8), 0, dp(8), 0); b.setBackground(rounded(bg, 16)); return b; }
    private EditText field(String hint) { EditText e = new EditText(this); e.setHint(hint); e.setTextColor(TEXT); e.setHintTextColor(MUTED); e.setSingleLine(false); e.setBackground(roundedStroke(Color.WHITE, 16, BORDER, 1)); e.setPadding(dp(12), 0, dp(12), 0); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(52)); p.setMargins(0, dp(10), 0, 0); e.setLayoutParams(p); return e; }
    private LinearLayout baseCard() { LinearLayout c = new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(14), dp(14), dp(14), dp(14)); c.setBackground(roundedStroke(Color.WHITE, 20, Color.rgb(238, 231, 219), 1)); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.setMargins(0, dp(10), 0, dp(4)); c.setLayoutParams(p); return c; }
    private LinearLayout actionRow() { LinearLayout r = new LinearLayout(this); r.setOrientation(LinearLayout.HORIZONTAL); r.setPadding(0, dp(10), 0, 0); return r; }
    private LinearLayout.LayoutParams sideParams() { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(44), 1); p.setMargins(dp(8), 0, 0, 0); return p; }
    private void addHeader(String title, String subtitle) { LinearLayout h = new LinearLayout(this); h.setOrientation(LinearLayout.VERTICAL); h.setPadding(dp(18), dp(16), dp(18), dp(16)); h.setBackground(roundedGradient(YELLOW, ORANGE, 24)); contentLayout.addView(h, new LinearLayout.LayoutParams(-1, -2)); h.addView(text(title, 25, TEXT, Typeface.BOLD)); TextView sub = text(subtitle, 15, TEXT, Typeface.NORMAL); sub.setPadding(0, dp(6), 0, 0); h.addView(sub); }
    private GradientDrawable rounded(int color, int radius) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d; }
    private GradientDrawable roundedStroke(int color, int radius, int strokeColor, int strokeWidthDp) { GradientDrawable d = rounded(color, radius); d.setStroke(dp(strokeWidthDp), strokeColor); return d; }
    private GradientDrawable roundedGradient(int start, int end, int radius) { GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{start, end}); d.setCornerRadius(dp(radius)); return d; }
    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }

    private static class Opportunity {
        String id, ownerDeviceId, authorKey, listingType, category, title, place, distance, value, description, author, phone, status; boolean urgent, demo; long createdAt, expiresAt; double rating; int posts, interestCount;
        Opportunity(String id, String ownerDeviceId, String authorKey, String listingType, String category, boolean urgent, String title, String place, String distance, String value, String description, String author, String phone, String status, long createdAt, long expiresAt, boolean demo, double rating, int posts, int interestCount) { this.id = id; this.ownerDeviceId = ownerDeviceId; this.authorKey = authorKey; this.listingType = listingType; this.category = category; this.urgent = urgent; this.title = title; this.place = place; this.distance = distance; this.value = value; this.description = description; this.author = author; this.phone = phone; this.status = status; this.createdAt = createdAt; this.expiresAt = expiresAt; this.demo = demo; this.rating = rating; this.posts = posts; this.interestCount = Math.max(0, interestCount); }
        static Opportunity seed(String id, String listingType, String category, boolean urgent, String title, String place, String distance, long createdAt, String value, String description, String author, String phone, double rating, int posts) { long ttl = TYPE_OFFER.equals(listingType) ? OFFER_TTL : (urgent ? URGENT_TTL : NORMAL_TTL); return new Opportunity(id, "seed", "phone_" + phone, listingType, category, urgent, title, place, distance, value, description, author, phone, STATUS_ACTIVE, createdAt, createdAt + ttl, true, rating, posts, 0); }
        boolean isOffer() { return TYPE_OFFER.equals(listingType); }
        String resolvedStatus(long now) { if (STATUS_DONE.equals(status)) return STATUS_DONE; if (expiresAt > 0 && expiresAt <= now) return STATUS_EXPIRED; return STATUS_ACTIVE; }
        boolean matches(String query) { return normalizeStatic(title + " " + description + " " + place + " " + category + " " + value + " " + author).contains(query); }
        String toStorage() { return clean(id) + "|" + clean(ownerDeviceId) + "|" + clean(authorKey) + "|" + clean(listingType) + "|" + clean(category) + "|" + urgent + "|" + clean(title) + "|" + clean(place) + "|" + clean(distance) + "|" + clean(value) + "|" + clean(description) + "|" + clean(author) + "|" + clean(phone) + "|" + clean(status) + "|" + createdAt + "|" + expiresAt + "|" + demo + "|" + rating + "|" + posts + "|" + interestCount; }
        static Opportunity fromStorage(String row, String deviceId) { String[] p = row.split("\\|", -1); if (p.length >= 20) return new Opportunity(p[0], p[1], p[2], p[3].length() == 0 ? TYPE_DEMAND : p[3], p[4], "true".equals(p[5]), p[6], p[7], p[8], p[9], p[10], p[11], p[12], p[13], parseLong(p[14], System.currentTimeMillis()), parseLong(p[15], System.currentTimeMillis() + NORMAL_TTL), "true".equals(p[16]), parseDouble(p[17], 0.0), parseInt(p[18], 0), parseInt(p[19], 0)); if (p.length >= 19) return new Opportunity(p[0], p[1], p[2], p[3].length() == 0 ? TYPE_DEMAND : p[3], p[4], "true".equals(p[5]), p[6], p[7], p[8], p[9], p[10], p[11], p[12], p[13], parseLong(p[14], System.currentTimeMillis()), parseLong(p[15], System.currentTimeMillis() + NORMAL_TTL), "true".equals(p[16]), parseDouble(p[17], 0.0), parseInt(p[18], 0), 0); if (p.length >= 9) { long now = System.currentTimeMillis(); return new Opportunity(p[0], p[0].startsWith("local") ? deviceId : "seed", "phone_" + p[p.length - 1], TYPE_DEMAND, p[1], false, p[2], p[3], "", p.length > 5 ? p[5] : "A combinar", p.length > 6 ? p[6] : "", p.length > 7 ? p[7] : "Anunciante", p.length > 8 ? p[8] : "", STATUS_ACTIVE, now - HOUR, now + NORMAL_TTL, !p[0].startsWith("local"), 0.0, 0, 0); } return null; }
        static long parseLong(String value, long fallback) { try { return Long.parseLong(value); } catch (Exception e) { return fallback; } }
        static double parseDouble(String value, double fallback) { try { return Double.parseDouble(value); } catch (Exception e) { return fallback; } }
        static int parseInt(String value, int fallback) { try { return Integer.parseInt(value); } catch (Exception e) { return fallback; } }
        static String clean(String value) { return value == null ? "" : value.replace("|", " ").replace("\n", " "); }
        static String normalizeStatic(String value) { if (value == null) return ""; String lower = value.toLowerCase(Locale.US).trim(); try { String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD); return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); } catch (Exception e) { return lower; } }
    }

    private static class Proposal {
        String id, listingId, listingTitle, fromDeviceId, fromName, phone, value, deadline, message, status; long createdAt;
        Proposal(String id, String listingId, String listingTitle, String fromDeviceId, String fromName, String phone, String value, String deadline, String message, String status, long createdAt) { this.id = id; this.listingId = listingId; this.listingTitle = listingTitle; this.fromDeviceId = fromDeviceId; this.fromName = fromName; this.phone = phone; this.value = value; this.deadline = deadline; this.message = message; this.status = status; this.createdAt = createdAt; }
        String toStorage() { return Opportunity.clean(id) + "|" + Opportunity.clean(listingId) + "|" + Opportunity.clean(listingTitle) + "|" + Opportunity.clean(fromDeviceId) + "|" + Opportunity.clean(fromName) + "|" + Opportunity.clean(phone) + "|" + Opportunity.clean(value) + "|" + Opportunity.clean(deadline) + "|" + Opportunity.clean(message) + "|" + Opportunity.clean(status) + "|" + createdAt; }
        static Proposal fromStorage(String row) { String[] p = row.split("\\|", -1); if (p.length < 11) return null; return new Proposal(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[8], p[9], Opportunity.parseLong(p[10], System.currentTimeMillis())); }
    }

    private static class ChatMessage {
        String id, proposalId, role, senderName, body; long createdAt;
        ChatMessage(String id, String proposalId, String role, String senderName, String body, long createdAt) { this.id = id; this.proposalId = proposalId; this.role = role; this.senderName = senderName; this.body = body; this.createdAt = createdAt; }
        String toStorage() { return Opportunity.clean(id) + "|" + Opportunity.clean(proposalId) + "|" + Opportunity.clean(role) + "|" + Opportunity.clean(senderName) + "|" + Opportunity.clean(body) + "|" + createdAt; }
        static ChatMessage fromStorage(String row) { String[] p = row.split("\\|", -1); if (p.length < 6) return null; return new ChatMessage(p[0], p[1], p[2], p[3], p[4], Opportunity.parseLong(p[5], System.currentTimeMillis())); }
    }
}
