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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
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
    private static final String KEY_FAVORITES = "favorites";
    private static final String KEY_PROFILE = "profile_name";
    private static final String KEY_PROFILE_CITY = "profile_city";
    private static final String KEY_PROFILE_PHONE = "profile_phone";
    private static final String KEY_PROFILE_KIND = "profile_kind";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_HIDDEN = "hidden_listing_ids";
    private static final String KEY_SUSPICIOUS = "suspicious_listing_ids";
    private static final String KEY_BLOCKED_AUTHORS = "blocked_author_keys";
    private static final String KEY_ENTRY_SEEN = "entry_seen";
    private static final String KEY_APP_MODE = "app_mode";

    private static final String APP_MODE_LOCAL = "local";
    private static final String APP_MODE_LOGGED = "logged";

    private static final String TYPE_DEMAND = "demanda";
    private static final String TYPE_OFFER = "oferta";

    private static final String STATUS_ACTIVE = "ativo";
    private static final String STATUS_DONE = "concluido";
    private static final String STATUS_EXPIRED = "expirado";

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
    private TextView emptyText;
    private SharedPreferences prefs;
    private ArrayList<Opportunity> opportunities;
    private HashSet<String> favoriteIds;
    private HashSet<String> hiddenListingIds;
    private HashSet<String> suspiciousListingIds;
    private HashSet<String> blockedAuthorKeys;
    private String deviceId;
    private String currentScreen = "home";
    private String lastListScreen = "home";
    private String activeFilter = "Todas";
    private String typeFilter = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootContainer = findViewById(R.id.rootContainer);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        ensureDeviceId();
        loadData();
        buildMainShell();
        if (prefs.getBoolean(KEY_ENTRY_SEEN, false)) showHome();
        else showEntryScreen();
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

        opportunities = new ArrayList<Opportunity>();
        String saved = prefs.getString(KEY_OPPORTUNITIES, "");
        if (saved.length() > 0) {
            String[] rows = saved.split("\\n");
            for (int i = 0; i < rows.length; i++) {
                Opportunity item = Opportunity.fromStorage(rows[i], deviceId);
                if (item != null) opportunities.add(item);
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
        } else {
            boolean changed = fixLegacyVisibleText();
            if (!hasListing("seed5")) {
                opportunities.add(Opportunity.seed("seed5", TYPE_OFFER, "Servico", false, "Pedreiro disponível para reformas", "Jardinópolis", "4,2 km", now - 2L * HOUR, "A combinar", "Faço pequenos reparos, pisos, pintura e reformas. Atendo na região com combinação por WhatsApp.", "João Carlos", "5516988888888", 0.0, 0));
                changed = true;
            }
            if (changed) saveOpportunities();
        }

        SharedPreferences.Editor editor = prefs.edit();
        if (prefs.getString(KEY_PROFILE, "").length() == 0) editor.putString(KEY_PROFILE, "Visitante do Chama no Trampo");
        if (prefs.getString(KEY_PROFILE_KIND, "").length() == 0) editor.putString(KEY_PROFILE_KIND, "Trabalhador/Contratante");
        if (prefs.getString(KEY_APP_MODE, "").length() == 0) editor.putString(KEY_APP_MODE, APP_MODE_LOCAL);
        editor.apply();
    }

    private boolean fixLegacyVisibleText() {
        boolean changed = false;
        for (int i = 0; i < opportunities.size(); i++) {
            Opportunity item = opportunities.get(i);
            String old = item.toStorage();
            item.category = normalizeCategory(item.category);
            item.title = fixText(item.title);
            item.place = fixText(item.place);
            item.distance = fixText(item.distance);
            if (normalize(item.distance).contains("perto de voce")) item.distance = "distância não informada";
            item.value = fixText(item.value);
            item.description = fixText(item.description);
            item.author = fixText(item.author);
            item.phone = normalizePhoneForWhatsApp(item.phone);
            if (!old.equals(item.toStorage())) changed = true;
        }
        return changed;
    }

    private String fixText(String value) {
        if (value == null) return "";
        return value
                .replace("producao", "produção")
                .replace("Producao", "Produção")
                .replace("Salario", "Salário")
                .replace("organizacao", "organização")
                .replace("separacao", "separação")
                .replace("orcamento", "orçamento")
                .replace("Orcamento", "Orçamento")
                .replace("Preferencia", "Preferência")
                .replace("regiao", "região")
                .replace("caminhao", "caminhão")
                .replace("Ribeirao", "Ribeirão")
                .replace("servico", "serviço")
                .replace("Servico", "Serviço")
                .replace("avaliacao", "avaliação")
                .replace("disponivel", "disponível")
                .replace("disponiveis", "disponíveis")
                .replace("Faco", "Faço")
                .replace("combinacao", "combinação")
                .replace("Jardinopolis", "Jardinópolis")
                .replace("Sao", "São")
                .replace("Jose", "José")
                .replace("Joao", "João")
                .replace("voce", "você");
    }

    private boolean hasListing(String id) {
        for (int i = 0; i < opportunities.size(); i++) if (opportunities.get(i).id.equals(id)) return true;
        return false;
    }

    private HashSet<String> readSet(String key) {
        HashSet<String> set = new HashSet<String>();
        String raw = prefs.getString(key, "");
        if (raw.length() > 0) {
            String[] parts = raw.split(",");
            for (int i = 0; i < parts.length; i++) {
                String value = parts[i].trim();
                if (value.length() > 0) set.add(value);
            }
        }
        return set;
    }

    private void saveSet(String key, HashSet<String> set) {
        StringBuilder builder = new StringBuilder();
        for (String value : set) {
            if (builder.length() > 0) builder.append(',');
            builder.append(value);
        }
        prefs.edit().putString(key, builder.toString()).apply();
    }

    private void buildMainShell() {
        rootContainer.removeAllViews();
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundColor(CREAM);
        rootContainer.addView(main, new FrameLayout.LayoutParams(-1, -1));

        ScrollView scrollView = new ScrollView(this);
        contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dp(16), dp(14), dp(16), dp(14));
        scrollView.addView(contentLayout, new ScrollView.LayoutParams(-1, -2));
        main.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 1));

        bottomNav = new LinearLayout(this);
        bottomNav.setOrientation(LinearLayout.HORIZONTAL);
        bottomNav.setGravity(Gravity.CENTER);
        bottomNav.setPadding(dp(10), dp(8), dp(10), dp(10));
        bottomNav.setBackgroundColor(CREAM);
        main.addView(bottomNav, new LinearLayout.LayoutParams(-1, dp(84)));
        buildBottomNav();
    }

    private void showEntryScreen() {
        currentScreen = "entry";
        lastListScreen = "home";
        if (bottomNav != null) bottomNav.setVisibility(View.GONE);
        contentLayout.removeAllViews();
        addHeader("Chama no Trampo", "Entre com telefone ou continue no modo local de teste.");
        contentLayout.addView(detailText("O login real com telefone será ativado com Firebase Auth Phone. Por enquanto, o modo local permite testar publicação, busca, perfil e gerenciamento neste aparelho.", 16, TEXT, Typeface.NORMAL));
        contentLayout.addView(detailText("Modo local: dados salvos só neste aparelho.\nTelefone verificado: não.", 15, MUTED, Typeface.BOLD));
        Button phoneLogin = bigButton("Entrar com telefone", ORANGE, Color.WHITE);
        Button local = bigButton("Continuar testando localmente", GREEN, Color.WHITE);
        contentLayout.addView(phoneLogin);
        contentLayout.addView(local);
        phoneLogin.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showLoginInfoDialog(); } });
        local.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { continueLocalMode(); } });
    }

    private void showLoginInfoDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Login com telefone")
                .setMessage("O login real será feito com Firebase Auth Phone. Nesta versão, não vou simular uma conta falsa nem dizer que o telefone foi verificado. Use o modo local para testar o app até a integração com Firebase.")
                .setNegativeButton("Fechar", null)
                .setPositiveButton("Continuar localmente", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) { continueLocalMode(); }
                }).show();
    }

    private void continueLocalMode() {
        prefs.edit().putBoolean(KEY_ENTRY_SEEN, true).putString(KEY_APP_MODE, APP_MODE_LOCAL).apply();
        Toast.makeText(this, "Modo local de teste ativado.", Toast.LENGTH_LONG).show();
        showHome();
    }

    private void showHome() {
        currentScreen = "home";
        lastListScreen = "home";
        renderListScreen("Oportunidades perto de você", "Veja quem precisa contratar e quem está disponível.", false, "", false);
    }

    private void showSearch() {
        currentScreen = "search";
        lastListScreen = "search";
        String query = searchEditText == null ? "" : searchEditText.getText().toString();
        renderListScreen("Buscar oportunidades", "Combine texto, categoria e tipo.", false, query, false);
        focusSearchField();
    }

    private void showFavorites() {
        currentScreen = "favorites";
        lastListScreen = "favorites";
        String query = searchEditText == null ? "" : searchEditText.getText().toString();
        renderListScreen("Salvos", "Oportunidades que você salvou para ver depois.", true, query, false);
    }

    private void showMyPublications() {
        currentScreen = "mine";
        lastListScreen = "mine";
        contentLayout.removeAllViews();
        addHeader("Minhas publicações", "Edite, renove, conclua ou exclua anúncios deste aparelho.");
        int active = 0, expired = 0, done = 0;
        long now = System.currentTimeMillis();
        for (int i = 0; i < opportunities.size(); i++) {
            Opportunity item = opportunities.get(i);
            if (!isMine(item)) continue;
            String status = item.resolvedStatus(now);
            if (STATUS_ACTIVE.equals(status)) active++;
            else if (STATUS_EXPIRED.equals(status)) expired++;
            else done++;
        }
        contentLayout.addView(detailText("Ativos: " + active + "   Expirados: " + expired + "   Concluídos: " + done, 15, MUTED, Typeface.BOLD));
        emptyText = text("", 16, MUTED, Typeface.NORMAL);
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setPadding(dp(12), dp(24), dp(12), dp(24));
        contentLayout.addView(emptyText, new LinearLayout.LayoutParams(-1, -2));

        int count = 0;
        count += addMineSection("Ativos", STATUS_ACTIVE);
        count += addMineSection("Expirados", STATUS_EXPIRED);
        count += addMineSection("Concluídos", STATUS_DONE);

        if (count == 0) {
            emptyText.setText("Você ainda não publicou nenhuma oportunidade.");
            emptyText.setVisibility(View.VISIBLE);
        } else {
            emptyText.setVisibility(View.GONE);
        }
        buildBottomNav();
    }

    private int addMineSection(String title, String statusWanted) {
        int count = 0;
        long now = System.currentTimeMillis();
        for (int i = 0; i < opportunities.size(); i++) {
            Opportunity item = opportunities.get(i);
            if (!isMine(item)) continue;
            if (!statusWanted.equals(item.resolvedStatus(now))) continue;
            if (count == 0) contentLayout.addView(detailText(title, 18, TEXT, Typeface.BOLD));
            contentLayout.addView(cardView(item, true));
            count++;
        }
        return count;
    }

    private void renderListScreen(String title, String subtitle, boolean onlyFavorites, String query, boolean includeExpired) {
        contentLayout.removeAllViews();
        addHeader(title, subtitle);
        addSearchBox(query, onlyFavorites);
        addCategoryFilters(onlyFavorites);
        addTypeFilters(onlyFavorites);
        emptyText = text("", 16, MUTED, Typeface.NORMAL);
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setPadding(dp(12), dp(24), dp(12), dp(24));
        contentLayout.addView(emptyText, new LinearLayout.LayoutParams(-1, -2));
        renderCards(onlyFavorites, includeExpired);
        buildBottomNav();
    }

    private void addHeader(String title, String subtitle) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(18), dp(16), dp(18), dp(16));
        header.setBackground(roundedGradient(YELLOW, ORANGE, 24));
        header.setElevation(dp(2));
        contentLayout.addView(header, new LinearLayout.LayoutParams(-1, -2));
        TextView titleView = text(title, 25, TEXT, Typeface.BOLD);
        header.addView(titleView);
        TextView sub = text(subtitle, 15, TEXT, Typeface.NORMAL);
        sub.setPadding(0, dp(6), 0, 0);
        header.addView(sub);
    }

    private void addSearchBox(String query, final boolean onlyFavorites) {
        searchEditText = new EditText(this);
        searchEditText.setSingleLine(true);
        searchEditText.setHint("Buscar por título, cidade, categoria, valor ou autor");
        searchEditText.setText(query);
        searchEditText.setTextColor(TEXT);
        searchEditText.setHintTextColor(MUTED);
        searchEditText.setTextSize(15);
        searchEditText.setPadding(dp(14), 0, dp(14), 0);
        searchEditText.setBackground(roundedStroke(Color.WHITE, 18, BORDER, 1));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(52));
        params.setMargins(0, dp(14), 0, dp(8));
        contentLayout.addView(searchEditText, params);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { renderCards(onlyFavorites, false); }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private void addCategoryFilters(final boolean onlyFavorites) {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.setPadding(0, 0, 0, dp(2));
        scroll.addView(chips, new HorizontalScrollView.LayoutParams(-2, -2));
        String[] keys = new String[]{"Todas", "Vaga", "Servico", "Bico", "Urgente"};
        for (int i = 0; i < keys.length; i++) {
            final String key = keys[i];
            TextView chip = text(filterLabel(key), 13, key.equals(activeFilter) ? Color.WHITE : TEXT, Typeface.BOLD);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(13), dp(7), dp(13), dp(7));
            chip.setBackground(key.equals(activeFilter) ? rounded(Color.rgb(33, 33, 33), 18) : roundedStroke(Color.WHITE, 18, BORDER, 1));
            LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(-2, -2);
            chipParams.setMargins(0, 0, dp(8), 0);
            chips.addView(chip, chipParams);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    activeFilter = key;
                    if ("favorites".equals(currentScreen)) showFavorites();
                    else if ("search".equals(currentScreen)) showSearch();
                    else showHomeWithCurrentQuery();
                }
            });
        }
        contentLayout.addView(scroll, new LinearLayout.LayoutParams(-1, -2));
    }

    private void addTypeFilters(final boolean onlyFavorites) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(8), 0, dp(2));
        String[] types = new String[]{"Todos", "Preciso", "Ofereco"};
        for (int i = 0; i < types.length; i++) {
            final String key = types[i];
            TextView chip = text(typeLabel(key), 13, key.equals(typeFilter) ? Color.WHITE : TEXT, Typeface.BOLD);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(12), dp(7), dp(12), dp(7));
            chip.setBackground(key.equals(typeFilter) ? rounded(ORANGE, 18) : roundedStroke(Color.WHITE, 18, BORDER, 1));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2, 1);
            params.setMargins(0, 0, dp(8), 0);
            row.addView(chip, params);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    typeFilter = key;
                    if ("favorites".equals(currentScreen)) showFavorites();
                    else if ("search".equals(currentScreen)) showSearch();
                    else showHomeWithCurrentQuery();
                }
            });
        }
        contentLayout.addView(row, new LinearLayout.LayoutParams(-1, -2));
    }

    private void showHomeWithCurrentQuery() {
        String query = searchEditText == null ? "" : searchEditText.getText().toString();
        currentScreen = "home";
        lastListScreen = "home";
        renderListScreen("Oportunidades perto de você", "Veja quem precisa contratar e quem está disponível.", false, query, false);
    }

    private void renderCards(boolean onlyFavorites, boolean includeExpired) {
        while (contentLayout.getChildCount() > 5) contentLayout.removeViewAt(5);
        String query = normalize(searchEditText == null ? "" : searchEditText.getText().toString());
        int count = 0;
        long now = System.currentTimeMillis();
        for (int i = 0; i < opportunities.size(); i++) {
            final Opportunity item = opportunities.get(i);
            String visibleStatus = item.resolvedStatus(now);
            if (onlyFavorites && !favoriteIds.contains(item.id)) continue;
            if (!includeExpired && !STATUS_ACTIVE.equals(visibleStatus)) continue;
            if (hiddenListingIds.contains(item.id)) continue;
            if (blockedAuthorKeys.contains(item.authorKey)) continue;
            if (!matchesFilter(item)) continue;
            if (!matchesTypeFilter(item)) continue;
            if (query.length() > 0 && !item.matches(query)) continue;
            contentLayout.addView(cardView(item));
            count++;
        }
        if (count == 0) {
            if (onlyFavorites && query.length() == 0 && "Todas".equals(activeFilter)) emptyText.setText("Você ainda não salvou nenhuma oportunidade.");
            else emptyText.setText("Nenhuma oportunidade encontrada para esse filtro.");
            emptyText.setVisibility(View.VISIBLE);
        } else {
            emptyText.setVisibility(View.GONE);
        }
    }

    private View cardView(final Opportunity item) { return cardView(item, false); }

    private View cardView(final Opportunity item, final boolean managementMode) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(roundedStroke(Color.WHITE, 20, Color.rgb(238, 231, 219), 1));
        card.setElevation(dp(1));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.setMargins(0, dp(10), 0, dp(4));
        card.setLayoutParams(cardParams);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(badgeText(categoryIcon(item.category) + " " + displayCategory(item.category).toUpperCase(), TEXT, SOFT, 14));
        TextView typeBadge = badgeText(typeBadgeText(item.listingType), item.isOffer() ? Color.WHITE : TEXT, item.isOffer() ? GREEN : Color.rgb(255, 244, 225), 14);
        LinearLayout.LayoutParams typeParams = new LinearLayout.LayoutParams(-2, -2);
        typeParams.setMargins(dp(6), 0, 0, 0);
        top.addView(typeBadge, typeParams);
        if (item.urgent) {
            TextView urgent = badgeText("! URGENTE", Color.WHITE, RED, 14);
            LinearLayout.LayoutParams urgentParams = new LinearLayout.LayoutParams(-2, -2);
            urgentParams.setMargins(dp(6), 0, 0, 0);
            top.addView(urgent, urgentParams);
        }
        String status = item.resolvedStatus(System.currentTimeMillis());
        if (!STATUS_ACTIVE.equals(status)) {
            TextView statusBadge = badgeText(statusLabel(status).toUpperCase(), Color.WHITE, STATUS_EXPIRED.equals(status) ? MUTED : GREEN, 14);
            LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-2, -2);
            statusParams.setMargins(dp(6), 0, 0, 0);
            top.addView(statusBadge, statusParams);
        }
        card.addView(top);

        TextView time = text("Publicado " + item.timeAgo(System.currentTimeMillis()) + " · " + item.expiryLabel(System.currentTimeMillis()), 12, MUTED, Typeface.NORMAL);
        time.setPadding(0, dp(7), 0, 0);
        card.addView(time);

        TextView title = text(item.title, 20, TEXT, Typeface.BOLD);
        title.setPadding(0, dp(7), 0, 0);
        card.addView(title);
        card.addView(text(locationText(item), 14, MUTED, Typeface.NORMAL));
        TextView value = text(item.value, 16, GREEN, Typeface.BOLD);
        value.setPadding(0, dp(6), 0, 0);
        card.addView(value);
        TextView desc = text(item.description, 14, TEXT, Typeface.NORMAL);
        desc.setPadding(0, dp(8), 0, 0);
        card.addView(desc);

        TextView trust = text(trustText(item), 13, MUTED, Typeface.BOLD);
        trust.setPadding(0, dp(10), 0, 0);
        card.addView(trust);

        LinearLayout smallBadges = new LinearLayout(this);
        smallBadges.setOrientation(LinearLayout.HORIZONTAL);
        smallBadges.setPadding(0, dp(8), 0, 0);
        smallBadges.addView(tinyBadge(item.isOffer() ? "Profissional disponível" : "Contato direto"));
        if (isMine(item)) smallBadges.addView(tinyBadge("Minha publicação"));
        if (suspiciousListingIds.contains(item.id)) smallBadges.addView(tinyBadge("Marcado como suspeito"));
        card.addView(smallBadges);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(10), 0, 0);

        if (managementMode) {
            Button details = smallButton("Detalhes", SOFT, TEXT);
            Button edit = smallButton("Editar", ORANGE, Color.WHITE);
            Button remove = smallButton("Excluir", RED, Color.WHITE);
            actions.addView(details, new LinearLayout.LayoutParams(0, dp(44), 1));
            LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(0, dp(44), 1);
            editParams.setMargins(dp(8), 0, 0, 0);
            actions.addView(edit, editParams);
            LinearLayout.LayoutParams removeParams = new LinearLayout.LayoutParams(0, dp(44), 1);
            removeParams.setMargins(dp(8), 0, 0, 0);
            actions.addView(remove, removeParams);
            card.addView(actions);

            LinearLayout actions2 = new LinearLayout(this);
            actions2.setOrientation(LinearLayout.HORIZONTAL);
            actions2.setPadding(0, dp(8), 0, 0);
            Button renew = smallButton(STATUS_EXPIRED.equals(status) ? "Renovar" : "Atualizar prazo", SOFT, TEXT);
            Button done = smallButton(STATUS_ACTIVE.equals(status) ? "Concluir" : "Reativar", SOFT, TEXT);
            actions2.addView(renew, new LinearLayout.LayoutParams(0, dp(42), 1));
            LinearLayout.LayoutParams doneParams = new LinearLayout.LayoutParams(0, dp(42), 1);
            doneParams.setMargins(dp(8), 0, 0, 0);
            actions2.addView(done, doneParams);
            card.addView(actions2);

            details.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showDetails(item); } });
            edit.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showListingDialog(item); } });
            remove.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { deleteListing(item); } });
            renew.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { renewListing(item); } });
            done.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { if (STATUS_ACTIVE.equals(status)) markDone(item); else renewListing(item); } });
        } else {
            Button whatsapp = smallButton("WhatsApp", GREEN, Color.WHITE);
            boolean saved = favoriteIds.contains(item.id);
            Button save = smallButton(saved ? "★ Salvo" : "☆ Salvar", saved ? Color.rgb(255, 244, 225) : Color.WHITE, saved ? ORANGE : TEXT);
            save.setBackground(saved ? roundedStroke(Color.rgb(255, 244, 225), 16, ORANGE, 1) : roundedStroke(Color.WHITE, 16, BORDER, 1));
            actions.addView(whatsapp, new LinearLayout.LayoutParams(0, dp(44), 1));
            LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(0, dp(44), 1);
            saveParams.setMargins(dp(8), 0, 0, 0);
            actions.addView(save, saveParams);
            card.addView(actions);
            whatsapp.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { abrirWhatsapp(item); } });
            save.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { toggleFavorite(item.id); refreshCurrentScreen(); } });
        }
        card.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showDetails(item); } });
        return card;
    }

    private void showDetails(final Opportunity item) {
        if (!"details".equals(currentScreen)) lastListScreen = currentScreen;
        currentScreen = "details";
        contentLayout.removeAllViews();
        addHeader("Detalhes da oportunidade", item.isOffer() ? "Veja o perfil antes de chamar." : "Veja as informações antes de chamar.");

        LinearLayout badges = new LinearLayout(this);
        badges.setOrientation(LinearLayout.HORIZONTAL);
        badges.setPadding(0, dp(16), 0, 0);
        badges.addView(badgeText(categoryIcon(item.category) + " " + displayCategory(item.category).toUpperCase(), TEXT, SOFT, 14));
        TextView typeBadge = badgeText(typeBadgeText(item.listingType), item.isOffer() ? Color.WHITE : TEXT, item.isOffer() ? GREEN : Color.rgb(255, 244, 225), 14);
        LinearLayout.LayoutParams typeParams = new LinearLayout.LayoutParams(-2, -2);
        typeParams.setMargins(dp(6), 0, 0, 0);
        badges.addView(typeBadge, typeParams);
        if (item.urgent) {
            TextView urgent = badgeText("! URGENTE", Color.WHITE, RED, 14);
            LinearLayout.LayoutParams urgentParams = new LinearLayout.LayoutParams(-2, -2);
            urgentParams.setMargins(dp(6), 0, 0, 0);
            badges.addView(urgent, urgentParams);
        }
        String status = item.resolvedStatus(System.currentTimeMillis());
        if (!STATUS_ACTIVE.equals(status)) {
            TextView statusBadge = badgeText(statusLabel(status).toUpperCase(), Color.WHITE, STATUS_EXPIRED.equals(status) ? MUTED : GREEN, 14);
            LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-2, -2);
            statusParams.setMargins(dp(6), 0, 0, 0);
            badges.addView(statusBadge, statusParams);
        }
        contentLayout.addView(badges);

        contentLayout.addView(detailText(item.title, 25, TEXT, Typeface.BOLD));
        contentLayout.addView(detailText(locationText(item), 16, MUTED, Typeface.NORMAL));
        contentLayout.addView(detailText("Publicado " + item.timeAgo(System.currentTimeMillis()) + "\n" + item.expiryLabel(System.currentTimeMillis()), 15, MUTED, Typeface.NORMAL));
        contentLayout.addView(detailText(item.value, 18, GREEN, Typeface.BOLD));
        contentLayout.addView(detailText(item.description, 16, TEXT, Typeface.NORMAL));
        contentLayout.addView(detailText(trustText(item), 15, MUTED, Typeface.BOLD));

        Button profile = bigButton(isMine(item) ? "Ver meu perfil público" : "Ver perfil do anunciante", SOFT, TEXT);
        contentLayout.addView(profile);
        profile.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showPublicProfile(item); } });

        if (STATUS_ACTIVE.equals(status)) {
            Button whatsapp = bigButton("Chamar no WhatsApp", GREEN, Color.WHITE);
            Button share = bigButton("Compartilhar anúncio", SOFT, TEXT);
            contentLayout.addView(whatsapp);
            contentLayout.addView(share);
            whatsapp.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { abrirWhatsapp(item); } });
            share.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { shareListing(item); } });
        }

        Button favorite = bigButton(favoriteIds.contains(item.id) ? "★ Remover dos salvos" : "☆ Salvar oportunidade", ORANGE, Color.WHITE);
        contentLayout.addView(favorite);
        favorite.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { toggleFavorite(item.id); showDetails(item); } });

        if (isMine(item)) {
            Button edit = bigButton("Editar anúncio", SOFT, TEXT);
            Button delete = bigButton("Excluir anúncio", RED, Color.WHITE);
            contentLayout.addView(edit);
            contentLayout.addView(delete);
            edit.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showListingDialog(item); } });
            delete.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { deleteListing(item); } });

            if (STATUS_EXPIRED.equals(status)) {
                Button renew = bigButton("Renovar anúncio", ORANGE, Color.WHITE);
                contentLayout.addView(renew);
                renew.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { renewListing(item); } });
            }
            if (STATUS_ACTIVE.equals(status)) {
                Button done = bigButton("Marcar como concluído", SOFT, TEXT);
                contentLayout.addView(done);
                done.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { markDone(item); } });
            }
        } else {
            Button hide = bigButton("Ocultar e marcar como suspeito", SOFT, TEXT);
            Button block = bigButton("Bloquear anunciante", SOFT, TEXT);
            contentLayout.addView(hide);
            contentLayout.addView(block);
            hide.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { hideSuspicious(item); } });
            block.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { blockAuthor(item); } });
        }

        Button back = bigButton("Voltar", SOFT, TEXT);
        contentLayout.addView(back);
        back.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { returnToLastListScreen(); } });
        buildBottomNav();
    }

    private void showPublicProfile(final Opportunity item) {
        currentScreen = "publicProfile";
        contentLayout.removeAllViews();
        addHeader("Perfil público", "Informações básicas do anunciante.");
        contentLayout.addView(detailText(item.author, 26, TEXT, Typeface.BOLD));
        contentLayout.addView(detailText("Cidade/local: " + item.place, 16, MUTED, Typeface.NORMAL));
        contentLayout.addView(detailText("Contato: " + (isValidWhatsAppNumber(item.phone) ? item.phone : "não informado"), 16, MUTED, Typeface.NORMAL));
        contentLayout.addView(detailText(item.isOffer() ? "Tipo: oferece trabalho" : "Tipo: publica demandas", 16, TEXT, Typeface.BOLD));
        contentLayout.addView(detailText("Anúncios ativos neste aparelho: " + countActiveByAuthor(item.authorKey), 16, TEXT, Typeface.NORMAL));
        contentLayout.addView(detailText(item.demo && item.rating > 0 ? "Reputação visual de exemplo: " + ratingText(item.rating) : "Sem avaliações reais ainda", 15, MUTED, Typeface.BOLD));
        Button whatsapp = bigButton("Chamar no WhatsApp", GREEN, Color.WHITE);
        Button back = bigButton("Voltar", SOFT, TEXT);
        contentLayout.addView(whatsapp);
        if (!isMine(item)) {
            Button block = bigButton("Bloquear anunciante", SOFT, TEXT);
            contentLayout.addView(block);
            block.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { blockAuthor(item); } });
        }
        contentLayout.addView(back);
        whatsapp.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { abrirWhatsapp(item); } });
        back.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showDetails(item); } });
        buildBottomNav();
    }

    private int countActiveByAuthor(String authorKey) {
        int total = 0;
        long now = System.currentTimeMillis();
        for (int i = 0; i < opportunities.size(); i++) {
            Opportunity item = opportunities.get(i);
            if (item.authorKey.equals(authorKey) && STATUS_ACTIVE.equals(item.resolvedStatus(now))) total++;
        }
        return total;
    }

    private void showPublishDialog() { showListingDialog(null); }

    private void showListingDialog(final Opportunity editItem) {
        final boolean editing = editItem != null;
        final LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(12), dp(4), dp(12), 0);

        final CheckBox urgentCheck = new CheckBox(this);
        urgentCheck.setText("Marcar como urgente");
        urgentCheck.setTextColor(TEXT);
        urgentCheck.setChecked(editing && editItem.urgent);

        final String[] selectedType = new String[]{editing ? editItem.listingType : TYPE_DEMAND};
        final ArrayList<Button> typeButtons = new ArrayList<Button>();
        form.addView(text("O que você quer fazer?", 14, TEXT, Typeface.BOLD));
        LinearLayout typeRow = new LinearLayout(this);
        typeRow.setOrientation(LinearLayout.HORIZONTAL);
        form.addView(typeRow);
        addTypeButton(typeRow, typeButtons, selectedType, TYPE_DEMAND, "Preciso de alguém", urgentCheck);
        addTypeButton(typeRow, typeButtons, selectedType, TYPE_OFFER, "Estou disponível", urgentCheck);
        refreshTypeButtons(typeButtons, selectedType[0]);

        final String[] selectedCategory = new String[]{editing ? editItem.category : "Vaga"};
        final ArrayList<Button> categoryButtons = new ArrayList<Button>();
        form.addView(text("Categoria", 14, TEXT, Typeface.BOLD));
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        form.addView(row1);
        addCategoryButton(row1, categoryButtons, selectedCategory, "Vaga");
        addCategoryButton(row1, categoryButtons, selectedCategory, "Bico");
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        form.addView(row2);
        addCategoryButton(row2, categoryButtons, selectedCategory, "Servico");
        refreshCategoryButtons(categoryButtons, selectedCategory[0]);

        form.addView(urgentCheck);
        updateUrgentVisibility(urgentCheck, selectedType[0]);

        final EditText title = field("Título: o que você precisa ou oferece");
        final EditText place = field("Cidade/local");
        final EditText value = field("Valor");
        final EditText desc = field("Descrição");
        final EditText author = field("Publicado por");
        final EditText phone = field("WhatsApp com DDD, ex: 5516999999999");
        if (editing) {
            title.setText(editItem.title);
            place.setText(editItem.place);
            value.setText(editItem.value);
            desc.setText(editItem.description);
            author.setText(editItem.author);
            phone.setText(editItem.phone);
        } else {
            author.setText(prefs.getString(KEY_PROFILE, ""));
            phone.setText(prefs.getString(KEY_PROFILE_PHONE, ""));
        }
        form.addView(title); form.addView(place); form.addView(value); form.addView(desc); form.addView(author); form.addView(phone);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(editing ? "Editar anúncio" : "Publicar oportunidade")
                .setView(form)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(editing ? "Salvar" : "Publicar", null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        if (title.getText().toString().trim().length() == 0 || place.getText().toString().trim().length() == 0) {
                            Toast.makeText(MainActivity.this, "Informe título e local.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        String authorName = textOr(author, prefs.getString(KEY_PROFILE, "Visitante"));
                        String phoneNumber = normalizePhoneForWhatsApp(phone.getText().toString());
                        if (!isValidWhatsAppNumber(phoneNumber)) {
                            Toast.makeText(MainActivity.this, "Informe um WhatsApp válido com DDD. Ex: 5516999999999 ou 16999999999.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        long now = System.currentTimeMillis();
                        if (editing) {
                            editItem.listingType = selectedType[0];
                            editItem.category = selectedCategory[0];
                            editItem.urgent = TYPE_DEMAND.equals(selectedType[0]) && urgentCheck.isChecked();
                            editItem.title = title.getText().toString();
                            editItem.place = place.getText().toString();
                            editItem.value = textOr(value, "A combinar");
                            editItem.description = textOr(desc, "Sem descrição informada.");
                            editItem.author = authorName;
                            editItem.phone = phoneNumber;
                            editItem.authorKey = authorKeyFor(phoneNumber, authorName);
                            saveOpportunities();
                            Toast.makeText(MainActivity.this, "Anúncio atualizado. A edição não renova o prazo.", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            showMyPublications();
                        } else {
                            Opportunity item = new Opportunity("local" + now, deviceId, authorKeyFor(phoneNumber, authorName), selectedType[0], selectedCategory[0], TYPE_DEMAND.equals(selectedType[0]) && urgentCheck.isChecked(), title.getText().toString(), place.getText().toString(), "distância não informada", textOr(value, "A combinar"), textOr(desc, "Sem descrição informada."), authorName, phoneNumber, STATUS_ACTIVE, now, now + ttlFor(selectedType[0], urgentCheck.isChecked()), false, 0.0, 0);
                            opportunities.add(0, item);
                            saveOpportunities();
                            Toast.makeText(MainActivity.this, "Oportunidade publicada localmente.", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            showHome();
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    private void showProfile() {
        currentScreen = "profile";
        contentLayout.removeAllViews();
        addHeader("Perfil", "Seus dados ficam salvos apenas neste aparelho.");
        contentLayout.addView(detailText(appModeText(), 15, MUTED, Typeface.BOLD));
        Button login = bigButton("Entrar com telefone", ORANGE, Color.WHITE);
        contentLayout.addView(login);
        login.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showLoginInfoDialog(); } });

        final EditText name = field("Nome do perfil");
        final EditText city = field("Cidade principal");
        final EditText phone = field("WhatsApp com DDD");
        final EditText kind = field("Tipo: Trabalhador, Contratante ou Empresa");
        name.setText(prefs.getString(KEY_PROFILE, ""));
        city.setText(prefs.getString(KEY_PROFILE_CITY, ""));
        phone.setText(prefs.getString(KEY_PROFILE_PHONE, ""));
        kind.setText(prefs.getString(KEY_PROFILE_KIND, "Trabalhador/Contratante"));
        contentLayout.addView(name);
        contentLayout.addView(city);
        contentLayout.addView(phone);
        contentLayout.addView(kind);
        Button save = bigButton("Salvar perfil", ORANGE, Color.WHITE);
        Button mine = bigButton("Minhas publicações", SOFT, TEXT);
        contentLayout.addView(save);
        contentLayout.addView(mine);
        contentLayout.addView(detailText("Publicações locais: " + countLocal() + "\nOportunidades salvas: " + favoriteIds.size(), 16, TEXT, Typeface.NORMAL));
        save.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String normalizedPhone = normalizePhoneForWhatsApp(phone.getText().toString());
                prefs.edit()
                        .putString(KEY_PROFILE, name.getText().toString())
                        .putString(KEY_PROFILE_CITY, city.getText().toString())
                        .putString(KEY_PROFILE_PHONE, normalizedPhone)
                        .putString(KEY_PROFILE_KIND, kind.getText().toString())
                        .apply();
                Toast.makeText(MainActivity.this, "Perfil salvo.", Toast.LENGTH_LONG).show();
            }
        });
        mine.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showMyPublications(); } });
        buildBottomNav();
    }

    private String appModeText() {
        String mode = prefs.getString(KEY_APP_MODE, APP_MODE_LOCAL);
        if (APP_MODE_LOGGED.equals(mode)) return "Modo atual: Conta logada\nTelefone verificado: sim";
        return "Modo atual: Local de teste\nTelefone verificado: não";
    }

    private void buildBottomNav() {
        if (bottomNav == null) return;
        bottomNav.setVisibility(View.VISIBLE);
        bottomNav.removeAllViews();
        LinearLayout dock = new LinearLayout(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER);
        dock.setPadding(dp(8), dp(6), dp(8), dp(6));
        dock.setBackground(roundedStroke(Color.WHITE, 26, Color.rgb(235, 225, 210), 1));
        dock.setElevation(dp(5));
        addNavItem(dock, "Início", "home", false);
        addNavItem(dock, "Buscar", "search", false);
        addNavItem(dock, "+ Publicar", "publish", true);
        addNavItem(dock, "Salvos", "favorites", false);
        addNavItem(dock, "Perfil", "profile", false);
        bottomNav.addView(dock, new LinearLayout.LayoutParams(-1, -1));
    }

    private void addNavItem(LinearLayout dock, String label, final String target, boolean primary) {
        boolean selected = currentScreen.equals(target) || ("profile".equals(target) && ("mine".equals(currentScreen) || "publicProfile".equals(currentScreen)));
        TextView item = text(label, primary ? 13 : 11, primary ? Color.WHITE : (selected ? ORANGE : MUTED), Typeface.BOLD);
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(4), dp(4), dp(4), dp(4));
        if (primary) {
            item.setBackground(rounded(ORANGE, 22));
            item.setElevation(dp(4));
        } else if (selected) {
            item.setBackground(rounded(Color.rgb(255, 244, 225), 20));
        } else {
            item.setBackground(rounded(Color.TRANSPARENT, 20));
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, primary ? dp(58) : dp(54), primary ? 1.25f : 1f);
        params.setMargins(dp(2), 0, dp(2), 0);
        dock.addView(item, params);
        item.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if ("home".equals(target)) showHome();
                if ("search".equals(target)) showSearch();
                if ("publish".equals(target)) showPublishDialog();
                if ("favorites".equals(target)) showFavorites();
                if ("profile".equals(target)) showProfile();
            }
        });
    }

    private void focusSearchField() {
        if (searchEditText == null) return;
        searchEditText.requestFocus();
        searchEditText.setSelection(searchEditText.getText().length());
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onBackPressed() {
        if ("entry".equals(currentScreen)) { super.onBackPressed(); return; }
        if ("details".equals(currentScreen)) { returnToLastListScreen(); return; }
        if ("publicProfile".equals(currentScreen)) { returnToLastListScreen(); return; }
        if ("mine".equals(currentScreen)) { showProfile(); return; }
        if ("profile".equals(currentScreen) || "favorites".equals(currentScreen) || "search".equals(currentScreen)) { showHome(); return; }
        if (searchEditText != null && searchEditText.getText().toString().trim().length() > 0) { showHome(); return; }
        if (!"Todas".equals(activeFilter) || !"Todos".equals(typeFilter)) { activeFilter = "Todas"; typeFilter = "Todos"; showHome(); return; }
        if (!"home".equals(currentScreen)) { showHome(); return; }
        super.onBackPressed();
    }

    private void refreshCurrentScreen() {
        if ("favorites".equals(currentScreen)) showFavorites();
        else if ("search".equals(currentScreen)) showSearch();
        else if ("profile".equals(currentScreen)) showProfile();
        else if ("mine".equals(currentScreen)) showMyPublications();
        else showHomeWithCurrentQuery();
    }

    private void returnToLastListScreen() {
        if ("favorites".equals(lastListScreen)) showFavorites();
        else if ("search".equals(lastListScreen)) showSearch();
        else if ("mine".equals(lastListScreen)) showMyPublications();
        else showHomeWithCurrentQuery();
    }

    private void toggleFavorite(String id) {
        if (favoriteIds.contains(id)) {
            favoriteIds.remove(id);
            Toast.makeText(this, "Removido dos salvos.", Toast.LENGTH_SHORT).show();
        } else {
            favoriteIds.add(id);
            Toast.makeText(this, "Oportunidade salva.", Toast.LENGTH_SHORT).show();
        }
        saveSet(KEY_FAVORITES, favoriteIds);
    }

    private void hideSuspicious(final Opportunity item) {
        new AlertDialog.Builder(this)
                .setTitle("Ocultar anúncio?")
                .setMessage("Isto apenas remove este anúncio deste aparelho e marca como suspeito localmente. Não envia denúncia para uma central.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Ocultar", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        hiddenListingIds.add(item.id);
                        suspiciousListingIds.add(item.id);
                        saveSet(KEY_HIDDEN, hiddenListingIds);
                        saveSet(KEY_SUSPICIOUS, suspiciousListingIds);
                        Toast.makeText(MainActivity.this, "Anúncio ocultado neste aparelho.", Toast.LENGTH_LONG).show();
                        showHome();
                    }
                }).show();
    }

    private void blockAuthor(final Opportunity item) {
        new AlertDialog.Builder(this)
                .setTitle("Bloquear anunciante?")
                .setMessage("Este bloqueio local usa o telefone do anunciante. Se a mesma pessoa publicar com outro número, este aparelho não consegue reconhecer automaticamente.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Bloquear", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        blockedAuthorKeys.add(item.authorKey);
                        saveSet(KEY_BLOCKED_AUTHORS, blockedAuthorKeys);
                        Toast.makeText(MainActivity.this, "Anunciante bloqueado neste aparelho.", Toast.LENGTH_LONG).show();
                        showHome();
                    }
                }).show();
    }

    private void markDone(Opportunity item) {
        item.status = STATUS_DONE;
        saveOpportunities();
        Toast.makeText(this, "Anúncio marcado como concluído.", Toast.LENGTH_LONG).show();
        showDetails(item);
    }

    private void renewListing(Opportunity item) {
        long now = System.currentTimeMillis();
        item.createdAt = now;
        item.expiresAt = now + ttlFor(item.listingType, item.urgent);
        item.status = STATUS_ACTIVE;
        saveOpportunities();
        Toast.makeText(this, "Anúncio renovado.", Toast.LENGTH_LONG).show();
        showMyPublications();
    }

    private void deleteListing(final Opportunity item) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir anúncio?")
                .setMessage("Esta ação remove este anúncio deste aparelho.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        opportunities.remove(item);
                        favoriteIds.remove(item.id);
                        hiddenListingIds.remove(item.id);
                        suspiciousListingIds.remove(item.id);
                        saveOpportunities();
                        saveSet(KEY_FAVORITES, favoriteIds);
                        saveSet(KEY_HIDDEN, hiddenListingIds);
                        saveSet(KEY_SUSPICIOUS, suspiciousListingIds);
                        Toast.makeText(MainActivity.this, "Anúncio excluído.", Toast.LENGTH_LONG).show();
                        showMyPublications();
                    }
                }).show();
    }

    private void saveOpportunities() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < opportunities.size(); i++) {
            if (i > 0) builder.append('\n');
            builder.append(opportunities.get(i).toStorage());
        }
        prefs.edit().putString(KEY_OPPORTUNITIES, builder.toString()).apply();
    }

    private void abrirWhatsapp(Opportunity item) {
        String phone = normalizePhoneForWhatsApp(item.phone);
        if (!isValidWhatsAppNumber(phone)) {
            Toast.makeText(this, "Número de WhatsApp inválido ou não informado neste anúncio.", Toast.LENGTH_LONG).show();
            return;
        }
        String mensagem = item.isOffer()
                ? "Olá! Vi seu anúncio '" + item.title + "' no Chama no Trampo e tenho interesse."
                : "Olá! Vi a oportunidade '" + item.title + "' no Chama no Trampo e tenho interesse.";
        String url = "https://wa.me/" + phone + "?text=" + Uri.encode(mensagem);
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (Exception erro) { Toast.makeText(this, "Não foi possível abrir. Verifique se o WhatsApp está instalado.", Toast.LENGTH_LONG).show(); }
    }

    private void shareListing(Opportunity item) {
        StringBuilder message = new StringBuilder();
        message.append(item.isOffer() ? "Oferta no Chama no Trampo" : "Oportunidade no Chama no Trampo");
        message.append("\n\n").append(item.title);
        message.append("\n").append(locationText(item));
        message.append("\n").append(item.value);
        message.append("\n\n").append(item.description);
        message.append("\n\nContato: ").append(isValidWhatsAppNumber(item.phone) ? item.phone : "não informado");
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, message.toString());
        try { startActivity(Intent.createChooser(sendIntent, "Compartilhar anúncio")); }
        catch (Exception e) { Toast.makeText(this, "Não foi possível compartilhar este anúncio.", Toast.LENGTH_LONG).show(); }
    }

    private TextView text(String value, int sp, int color, int style) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, style);
        return view;
    }

    private TextView detailText(String value, int sp, int color, int style) {
        TextView view = text(value, sp, color, style);
        view.setPadding(0, dp(12), 0, 0);
        return view;
    }

    private TextView badgeText(String value, int fg, int bg, int radius) {
        TextView view = text(value, 12, fg, Typeface.BOLD);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(10), dp(5), dp(10), dp(5));
        view.setBackground(rounded(bg, radius));
        return view;
    }

    private TextView tinyBadge(String value) {
        TextView view = text(value, 11, MUTED, Typeface.BOLD);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(8), dp(4), dp(8), dp(4));
        view.setBackground(rounded(SOFT, 12));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.setMargins(0, 0, dp(6), 0);
        view.setLayoutParams(params);
        return view;
    }

    private Button bigButton(String label, int bg, int fg) {
        Button button = smallButton(label, bg, fg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(52));
        params.setMargins(0, dp(12), 0, 0);
        button.setLayoutParams(params);
        return button;
    }

    private Button smallButton(String label, int bg, int fg) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(12);
        button.setTextColor(fg);
        button.setAllCaps(false);
        button.setMinHeight(0);
        button.setMinWidth(0);
        button.setPadding(dp(8), 0, dp(8), 0);
        button.setBackground(rounded(bg, 16));
        return button;
    }

    private EditText field(String hint) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setSingleLine(false);
        editText.setTextColor(TEXT);
        editText.setHintTextColor(MUTED);
        editText.setBackground(roundedStroke(Color.WHITE, 16, BORDER, 1));
        editText.setPadding(dp(12), 0, dp(12), 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(52));
        params.setMargins(0, dp(10), 0, 0);
        editText.setLayoutParams(params);
        return editText;
    }

    private void addTypeButton(LinearLayout row, final ArrayList<Button> buttons, final String[] selectedType, final String value, String label, final CheckBox urgentCheck) {
        final Button button = smallButton(label, Color.WHITE, TEXT);
        button.setTag(value);
        buttons.add(button);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(44), 1);
        params.setMargins(0, dp(8), dp(8), 0);
        row.addView(button, params);
        button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                selectedType[0] = value;
                refreshTypeButtons(buttons, selectedType[0]);
                updateUrgentVisibility(urgentCheck, selectedType[0]);
            }
        });
    }

    private void refreshTypeButtons(ArrayList<Button> buttons, String selected) {
        for (int i = 0; i < buttons.size(); i++) {
            Button b = buttons.get(i);
            String value = String.valueOf(b.getTag());
            boolean active = value.equals(selected);
            b.setText(TYPE_OFFER.equals(value) ? "Ofereço trabalho" : "Preciso de alguém");
            b.setTextColor(active ? Color.WHITE : TEXT);
            b.setBackground(active ? rounded(ORANGE, 16) : roundedStroke(Color.WHITE, 16, BORDER, 1));
        }
    }

    private void updateUrgentVisibility(CheckBox urgentCheck, String listingType) {
        if (urgentCheck == null) return;
        if (TYPE_OFFER.equals(listingType)) {
            urgentCheck.setChecked(false);
            urgentCheck.setVisibility(View.GONE);
        } else {
            urgentCheck.setVisibility(View.VISIBLE);
        }
    }

    private void addCategoryButton(LinearLayout row, final ArrayList<Button> buttons, final String[] selectedCategory, final String category) {
        final Button button = smallButton(displayCategory(category), Color.WHITE, TEXT);
        button.setTag(category);
        buttons.add(button);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(44), 1);
        params.setMargins(0, dp(8), dp(8), 0);
        row.addView(button, params);
        button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                selectedCategory[0] = category;
                refreshCategoryButtons(buttons, selectedCategory[0]);
            }
        });
    }

    private void refreshCategoryButtons(ArrayList<Button> buttons, String selected) {
        for (int i = 0; i < buttons.size(); i++) {
            Button b = buttons.get(i);
            String category = String.valueOf(b.getTag());
            boolean active = category.equals(selected);
            b.setText(categoryIcon(category) + " " + displayCategory(category));
            b.setTextColor(active ? Color.WHITE : TEXT);
            b.setBackground(active ? rounded(ORANGE, 16) : roundedStroke(Color.WHITE, 16, BORDER, 1));
        }
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private GradientDrawable roundedStroke(int color, int radius, int strokeColor, int strokeWidthDp) {
        GradientDrawable drawable = rounded(color, radius);
        drawable.setStroke(dp(strokeWidthDp), strokeColor);
        return drawable;
    }

    private GradientDrawable roundedGradient(int start, int end, int radius) {
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{start, end});
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private String normalize(String value) {
        if (value == null) return "";
        String lower = value.toLowerCase(Locale.US).trim();
        try {
            String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD);
            return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        } catch (Exception e) {
            return lower;
        }
    }

    private String textOr(EditText editText, String fallback) {
        String value = editText.getText().toString().trim();
        return value.length() == 0 ? fallback : value;
    }

    private int countLocal() {
        int total = 0;
        for (int i = 0; i < opportunities.size(); i++) if (isMine(opportunities.get(i))) total++;
        return total;
    }

    private boolean isMine(Opportunity item) {
        return item.id.startsWith("local") || deviceId.equals(item.ownerDeviceId);
    }

    private boolean matchesFilter(Opportunity item) {
        if ("Todas".equals(activeFilter)) return true;
        if ("Urgente".equals(activeFilter)) return item.urgent;
        return normalizeCategory(item.category).equals(activeFilter);
    }

    private boolean matchesTypeFilter(Opportunity item) {
        if ("Todos".equals(typeFilter)) return true;
        if ("Preciso".equals(typeFilter)) return TYPE_DEMAND.equals(item.listingType);
        if ("Ofereco".equals(typeFilter)) return TYPE_OFFER.equals(item.listingType);
        return true;
    }

    private String filterLabel(String key) {
        if ("Todas".equals(key)) return "Todas";
        if ("Servico".equals(key)) return "Serviços";
        return key;
    }

    private String typeLabel(String key) {
        if ("Preciso".equals(key)) return "Preciso contratar";
        if ("Ofereco".equals(key)) return "Ofereço trabalho";
        return "Todos os tipos";
    }

    private String statusLabel(String status) {
        if (STATUS_DONE.equals(status)) return "concluído";
        if (STATUS_EXPIRED.equals(status)) return "expirado";
        return "ativo";
    }

    private String normalizeCategory(String value) {
        String v = normalize(value);
        if (v.contains("vaga")) return "Vaga";
        if (v.contains("bico")) return "Bico";
        return "Servico";
    }

    private String displayCategory(String value) {
        String category = normalizeCategory(value);
        if ("Servico".equals(category)) return "Serviço";
        return category;
    }

    private String categoryIcon(String value) {
        String category = normalizeCategory(value);
        if ("Vaga".equals(category)) return "[V]";
        if ("Bico".equals(category)) return "[B]";
        return "[S]";
    }

    private String typeBadgeText(String listingType) {
        if (TYPE_OFFER.equals(listingType)) return "OFEREÇO";
        return "PRECISO";
    }

    private String trustText(Opportunity item) {
        if (item.demo && item.rating > 0) return "Publicado por " + item.author + "   ★ " + ratingText(item.rating) + " · " + item.posts + " publicações";
        return "Publicado por " + item.author + " · Novo anunciante · Sem avaliações ainda";
    }

    private String locationText(Opportunity item) {
        if (item.distance == null || item.distance.length() == 0) return item.place;
        return item.place + " · " + item.distance;
    }

    private String ratingText(double rating) {
        return String.format(Locale.US, "%.1f", rating);
    }

    private String onlyDigits(String value) {
        StringBuilder out = new StringBuilder();
        if (value == null) return "";
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c >= '0' && c <= '9') out.append(c);
        }
        return out.toString();
    }

    private String normalizePhoneForWhatsApp(String value) {
        String digits = onlyDigits(value);
        if (digits.length() == 10 || digits.length() == 11) return "55" + digits;
        return digits;
    }

    private boolean isValidWhatsAppNumber(String value) {
        String digits = normalizePhoneForWhatsApp(value);
        return (digits.length() == 12 || digits.length() == 13) && digits.startsWith("55");
    }

    private String authorKeyFor(String phone, String author) {
        String digits = normalizePhoneForWhatsApp(phone);
        if (digits.length() > 0) return "phone_" + digits;
        return "unknown_" + normalize(author).replace(" ", "_");
    }

    private long ttlFor(String listingType, boolean urgent) {
        if (TYPE_OFFER.equals(listingType)) return OFFER_TTL;
        return urgent ? URGENT_TTL : NORMAL_TTL;
    }

    private static class Opportunity {
        String id; String ownerDeviceId; String authorKey; String listingType; String category; boolean urgent; String title; String place; String distance; String value; String description; String author; String phone; String status; long createdAt; long expiresAt; boolean demo; double rating; int posts;

        Opportunity(String id, String ownerDeviceId, String authorKey, String listingType, String category, boolean urgent, String title, String place, String distance, String value, String description, String author, String phone, String status, long createdAt, long expiresAt, boolean demo, double rating, int posts) {
            this.id = id; this.ownerDeviceId = ownerDeviceId; this.authorKey = authorKey; this.listingType = listingType; this.category = category; this.urgent = urgent; this.title = title; this.place = place; this.distance = distance; this.value = value; this.description = description; this.author = author; this.phone = phone; this.status = status; this.createdAt = createdAt; this.expiresAt = expiresAt; this.demo = demo; this.rating = rating; this.posts = posts;
        }

        static Opportunity seed(String id, String listingType, String category, boolean urgent, String title, String place, String distance, long createdAt, String value, String description, String author, String phone, double rating, int posts) {
            long ttl = TYPE_OFFER.equals(listingType) ? OFFER_TTL : (urgent ? URGENT_TTL : NORMAL_TTL);
            return new Opportunity(id, "seed", "phone_" + phone, listingType, category, urgent, title, place, distance, value, description, author, phone, STATUS_ACTIVE, createdAt, createdAt + ttl, true, rating, posts);
        }

        boolean isOffer() { return TYPE_OFFER.equals(listingType); }

        String resolvedStatus(long now) {
            if (STATUS_DONE.equals(status)) return STATUS_DONE;
            if (expiresAt > 0 && expiresAt <= now) return STATUS_EXPIRED;
            return STATUS_ACTIVE;
        }

        String timeAgo(long now) {
            long diff = Math.max(0, now - createdAt);
            if (diff < 60L * 1000L) return "agora";
            if (diff < HOUR) return "há " + (diff / (60L * 1000L)) + " min";
            if (diff < DAY) return "há " + (diff / HOUR) + "h";
            return "há " + (diff / DAY) + " dia" + ((diff / DAY) > 1 ? "s" : "");
        }

        String expiryLabel(long now) {
            if (STATUS_DONE.equals(status)) return "Concluído";
            long diff = expiresAt - now;
            if (diff <= 0) return "Expirado";
            if (diff < HOUR) return "Expira em menos de 1h";
            if (diff < DAY) return "Expira em " + (diff / HOUR) + "h";
            return (isOffer() ? "Disponível por " : "Expira em ") + (diff / DAY) + " dia" + ((diff / DAY) > 1 ? "s" : "");
        }

        boolean matches(String query) {
            String typeWords = isOffer() ? "oferta ofereco disponível profissional" : "demanda preciso contratar vaga";
            String data = title + " " + description + " " + place + " " + category + " " + value + " " + author + " " + typeWords + " " + (urgent ? "urgente prioridade hoje" : "");
            return normalizeStatic(data).contains(query);
        }

        String toStorage() {
            return clean(id) + "|" + clean(ownerDeviceId) + "|" + clean(authorKey) + "|" + clean(listingType) + "|" + clean(category) + "|" + urgent + "|" + clean(title) + "|" + clean(place) + "|" + clean(distance) + "|" + clean(value) + "|" + clean(description) + "|" + clean(author) + "|" + clean(phone) + "|" + clean(status) + "|" + createdAt + "|" + expiresAt + "|" + demo + "|" + rating + "|" + posts;
        }

        static Opportunity fromStorage(String row, String deviceId) {
            String[] p = row.split("\\|", -1);
            if (p.length >= 19) {
                return new Opportunity(p[0], p[1], p[2], p[3].length() == 0 ? TYPE_DEMAND : p[3], p[4], "true".equals(p[5]), p[6], p[7], p[8], p[9], p[10], p[11], p[12], p[13], parseLong(p[14], System.currentTimeMillis()), parseLong(p[15], System.currentTimeMillis() + NORMAL_TTL), "true".equals(p[16]), parseDouble(p[17], 0.0), parseInt(p[18], 0));
            }
            if (p.length >= 18) {
                return new Opportunity(p[0], p[1], p[2], TYPE_DEMAND, p[3], "true".equals(p[4]), p[5], p[6], p[7], p[8], p[9], p[10], p[11], p[12], parseLong(p[13], System.currentTimeMillis()), parseLong(p[14], System.currentTimeMillis() + NORMAL_TTL), "true".equals(p[15]), parseDouble(p[16], 0.0), parseInt(p[17], 0));
            }
            if (p.length >= 13) {
                long now = System.currentTimeMillis();
                boolean urgent = "true".equals(p[2]);
                return new Opportunity(p[0], p[0].startsWith("local") ? deviceId : "seed", "phone_" + p[10], TYPE_DEMAND, p[1], urgent, p[3], p[4], p[5], p[7], p[8], p[9], p[10], STATUS_ACTIVE, now - HOUR, now + (urgent ? URGENT_TTL : NORMAL_TTL), !p[0].startsWith("local"), parseDouble(p[11], 0.0), parseInt(p[12], 0));
            }
            if (p.length >= 9) {
                long now = System.currentTimeMillis();
                boolean urgent = p[1].toLowerCase(Locale.US).contains("urgent");
                String category = urgent ? "Servico" : p[1];
                return new Opportunity(p[0], p[0].startsWith("local") ? deviceId : "seed", "phone_" + p[8], TYPE_DEMAND, category, urgent, p[2], p[3], p[4], p[5], p[6], p[7], p[8], STATUS_ACTIVE, now - HOUR, now + (urgent ? URGENT_TTL : NORMAL_TTL), !p[0].startsWith("local"), p[0].startsWith("local") ? 0.0 : 4.4, p[0].startsWith("local") ? 0 : 5);
            }
            return null;
        }

        static long parseLong(String value, long fallback) { try { return Long.parseLong(value); } catch (Exception e) { return fallback; } }
        static double parseDouble(String value, double fallback) { try { return Double.parseDouble(value); } catch (Exception e) { return fallback; } }
        static int parseInt(String value, int fallback) { try { return Integer.parseInt(value); } catch (Exception e) { return fallback; } }
        static String clean(String value) { return value == null ? "" : value.replace("|", " ").replace("\n", " "); }

        static String normalizeStatic(String value) {
            if (value == null) return "";
            String lower = value.toLowerCase(Locale.US).trim();
            try {
                String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD);
                return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            } catch (Exception e) {
                return lower;
            }
        }
    }
}
