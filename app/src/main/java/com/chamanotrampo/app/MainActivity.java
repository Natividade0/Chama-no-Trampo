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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String PREFS = "chama_no_trampo_prefs";
    private static final String KEY_OPPORTUNITIES = "opportunities";
    private static final String KEY_FAVORITES = "favorites";
    private static final String KEY_PROFILE = "profile_name";

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
    private String currentScreen = "home";
    private String lastListScreen = "home";
    private String activeFilter = "Todas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootContainer = findViewById(R.id.rootContainer);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        loadData();
        buildMainShell();
        showHome();
    }

    private void loadData() {
        favoriteIds = new HashSet<String>();
        String favorites = prefs.getString(KEY_FAVORITES, "");
        if (favorites.length() > 0) {
            String[] ids = favorites.split(",");
            for (int i = 0; i < ids.length; i++) {
                if (ids[i].trim().length() > 0) favoriteIds.add(ids[i].trim());
            }
        }

        opportunities = new ArrayList<Opportunity>();
        String saved = prefs.getString(KEY_OPPORTUNITIES, "");
        if (saved.length() > 0) {
            String[] rows = saved.split("\\n");
            for (int i = 0; i < rows.length; i++) {
                Opportunity item = Opportunity.fromStorage(rows[i]);
                if (item != null) opportunities.add(item);
            }
        }

        if (opportunities.size() == 0) {
            opportunities.add(new Opportunity("seed1", "Vaga", false, "Auxiliar de producao", "Guariba - Centro", "1,8 km", "ha 3h", "Salario a combinar", "Ajudar na organizacao da linha, separacao de produtos e apoio geral na producao.", "Mercado Sao Jose", "5516999999999", 4.6, 8));
            opportunities.add(new Opportunity("seed2", "Servico", false, "Pedreiro para reforma", "Jardinopolis - Jardim Primavera", "4,2 km", "ha 1 dia", "Enviar orcamento", "Reforma pequena em banheiro, troca de piso e acabamento. Preferencia para profissionais da regiao.", "Dona Maria", "5516999999999", 4.9, 15));
            opportunities.add(new Opportunity("seed3", "Bico", false, "Ajudante para descarregar caminhao", "Ribeirao Preto - Distrito Industrial", "7,5 km", "ha 40 min", "R$ 120,00 no dia", "Preciso de ajudante hoje para descarregar mercadorias. Pagamento no final do servico.", "Carlos Fretes", "5516999999999", 4.3, 5));
            opportunities.add(new Opportunity("seed4", "Servico", true, "Eletricista hoje", "Jaboticabal - Nova Jaboticabal", "2,3 km", "ha 12 min", "A combinar", "Tomadas pararam de funcionar. Preciso de avaliacao e reparo ainda hoje.", "Ana Paula", "5516999999999", 4.7, 11));
            saveOpportunities();
        }

        if (prefs.getString(KEY_PROFILE, "").length() == 0) {
            prefs.edit().putString(KEY_PROFILE, "Visitante do Chama no Trampo").apply();
        }
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

    private void showHome() {
        currentScreen = "home";
        lastListScreen = "home";
        renderListScreen("Oportunidades perto de voce", "Encontre vagas, bicos e servicos locais.", false, "");
    }

    private void showSearch() {
        currentScreen = "search";
        lastListScreen = "search";
        String query = searchEditText == null ? "" : searchEditText.getText().toString();
        renderListScreen("Buscar oportunidades", "Combine texto, categoria e prioridade.", false, query);
        focusSearchField();
    }

    private void showFavorites() {
        currentScreen = "favorites";
        lastListScreen = "favorites";
        String query = searchEditText == null ? "" : searchEditText.getText().toString();
        renderListScreen("Salvos", "Oportunidades que voce salvou para ver depois.", true, query);
    }

    private void renderListScreen(String title, String subtitle, boolean onlyFavorites, String query) {
        contentLayout.removeAllViews();
        addHeader(title, subtitle);
        addSearchBox(query, onlyFavorites);
        addQuickFilters(onlyFavorites);
        emptyText = text("", 16, MUTED, Typeface.NORMAL);
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setPadding(dp(12), dp(24), dp(12), dp(24));
        contentLayout.addView(emptyText, new LinearLayout.LayoutParams(-1, -2));
        renderCards(onlyFavorites);
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
        searchEditText.setHint("Buscar por titulo, cidade, categoria, valor ou autor");
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
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { renderCards(onlyFavorites); }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private void addQuickFilters(final boolean onlyFavorites) {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.setPadding(0, 0, 0, dp(2));
        scroll.addView(chips, new HorizontalScrollView.LayoutParams(-2, -2));

        String[] keys = new String[]{"Todas", "Vaga", "Servico", "Bico", "Seguranca", "Urgente"};
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

    private void showHomeWithCurrentQuery() {
        String query = searchEditText == null ? "" : searchEditText.getText().toString();
        currentScreen = "home";
        lastListScreen = "home";
        renderListScreen("Oportunidades perto de voce", "Encontre vagas, bicos e servicos locais.", false, query);
    }

    private void renderCards(boolean onlyFavorites) {
        while (contentLayout.getChildCount() > 4) contentLayout.removeViewAt(4);
        String query = normalize(searchEditText == null ? "" : searchEditText.getText().toString());
        int count = 0;
        for (int i = 0; i < opportunities.size(); i++) {
            final Opportunity item = opportunities.get(i);
            if (onlyFavorites && !favoriteIds.contains(item.id)) continue;
            if (!matchesFilter(item)) continue;
            if (query.length() > 0 && !item.matches(query)) continue;
            contentLayout.addView(cardView(item));
            count++;
        }
        if (count == 0) {
            if (onlyFavorites && query.length() == 0 && "Todas".equals(activeFilter)) emptyText.setText("Voce ainda nao salvou nenhuma oportunidade.");
            else emptyText.setText("Nenhuma oportunidade encontrada para esse filtro.");
            emptyText.setVisibility(View.VISIBLE);
        } else {
            emptyText.setVisibility(View.GONE);
        }
    }

    private View cardView(final Opportunity item) {
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
        if (item.urgent) {
            TextView urgent = badgeText("! URGENTE", Color.WHITE, RED, 14);
            LinearLayout.LayoutParams urgentParams = new LinearLayout.LayoutParams(-2, -2);
            urgentParams.setMargins(dp(6), 0, 0, 0);
            top.addView(urgent, urgentParams);
        }
        TextView time = text("Publicado " + item.time, 12, MUTED, Typeface.NORMAL);
        time.setGravity(Gravity.RIGHT);
        top.addView(time, new LinearLayout.LayoutParams(0, -2, 1));
        card.addView(top);

        TextView title = text(item.title, 20, TEXT, Typeface.BOLD);
        title.setPadding(0, dp(9), 0, 0);
        card.addView(title);
        card.addView(text(item.place + " · " + item.distance, 14, MUTED, Typeface.NORMAL));
        TextView value = text(item.value, 16, GREEN, Typeface.BOLD);
        value.setPadding(0, dp(6), 0, 0);
        card.addView(value);
        TextView desc = text(item.description, 14, TEXT, Typeface.NORMAL);
        desc.setPadding(0, dp(8), 0, 0);
        card.addView(desc);

        TextView trust = text("Publicado por " + item.author + "   * " + ratingText(item.rating) + " · " + item.posts + " publicacoes", 13, MUTED, Typeface.BOLD);
        trust.setPadding(0, dp(10), 0, 0);
        card.addView(trust);

        LinearLayout smallBadges = new LinearLayout(this);
        smallBadges.setOrientation(LinearLayout.HORIZONTAL);
        smallBadges.setPadding(0, dp(8), 0, 0);
        smallBadges.addView(tinyBadge("Contato direto"));
        smallBadges.addView(tinyBadge("Perto de voce"));
        if (item.id.startsWith("local")) smallBadges.addView(tinyBadge("Minha publicacao"));
        card.addView(smallBadges);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(10), 0, 0);
        Button whatsapp = smallButton("WhatsApp", GREEN, Color.WHITE);
        boolean saved = favoriteIds.contains(item.id);
        Button save = smallButton(saved ? "* Salvo" : "☆ Salvar", saved ? Color.rgb(255, 244, 225) : Color.WHITE, saved ? ORANGE : TEXT);
        save.setBackground(saved ? roundedStroke(Color.rgb(255, 244, 225), 16, ORANGE, 1) : roundedStroke(Color.WHITE, 16, BORDER, 1));
        actions.addView(whatsapp, new LinearLayout.LayoutParams(0, dp(44), 1));
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(0, dp(44), 1);
        saveParams.setMargins(dp(8), 0, 0, 0);
        actions.addView(save, saveParams);
        card.addView(actions);

        card.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showDetails(item); } });
        whatsapp.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { abrirWhatsapp(item); } });
        save.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { toggleFavorite(item.id); refreshCurrentScreen(); } });
        return card;
    }

    private void showDetails(final Opportunity item) {
        if (!"details".equals(currentScreen)) lastListScreen = currentScreen;
        currentScreen = "details";
        contentLayout.removeAllViews();
        addHeader("Detalhes da oportunidade", "Veja as informacoes antes de chamar.");

        LinearLayout badges = new LinearLayout(this);
        badges.setOrientation(LinearLayout.HORIZONTAL);
        badges.setPadding(0, dp(16), 0, 0);
        badges.addView(badgeText(categoryIcon(item.category) + " " + displayCategory(item.category).toUpperCase(), TEXT, SOFT, 14));
        if (item.urgent) {
            TextView urgent = badgeText("! URGENTE", Color.WHITE, RED, 14);
            LinearLayout.LayoutParams urgentParams = new LinearLayout.LayoutParams(-2, -2);
            urgentParams.setMargins(dp(6), 0, 0, 0);
            badges.addView(urgent, urgentParams);
        }
        contentLayout.addView(badges);

        contentLayout.addView(detailText(item.title, 25, TEXT, Typeface.BOLD));
        contentLayout.addView(detailText(item.place + " · " + item.distance, 16, MUTED, Typeface.NORMAL));
        contentLayout.addView(detailText("Publicado " + item.time, 15, MUTED, Typeface.NORMAL));
        contentLayout.addView(detailText(item.value, 18, GREEN, Typeface.BOLD));
        contentLayout.addView(detailText(item.description, 16, TEXT, Typeface.NORMAL));
        contentLayout.addView(detailText("Publicado por " + item.author + "\n* " + ratingText(item.rating) + " · " + item.posts + " publicacoes anteriores", 15, MUTED, Typeface.BOLD));

        Button whatsapp = bigButton("Chamar no WhatsApp", GREEN, Color.WHITE);
        Button favorite = bigButton(favoriteIds.contains(item.id) ? "* Remover dos salvos" : "☆ Salvar oportunidade", ORANGE, Color.WHITE);
        Button back = bigButton("Voltar", SOFT, TEXT);
        contentLayout.addView(whatsapp);
        contentLayout.addView(favorite);
        contentLayout.addView(back);
        whatsapp.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { abrirWhatsapp(item); } });
        favorite.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { toggleFavorite(item.id); showDetails(item); } });
        back.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { returnToLastListScreen(); } });
        buildBottomNav();
    }

    private void showPublishDialog() {
        final LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(12), dp(4), dp(12), 0);

        final String[] selectedCategory = new String[]{"Vaga"};
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
        addCategoryButton(row2, categoryButtons, selectedCategory, "Seguranca");
        refreshCategoryButtons(categoryButtons, selectedCategory[0]);

        final CheckBox urgentCheck = new CheckBox(this);
        urgentCheck.setText("Marcar como urgente");
        urgentCheck.setTextColor(TEXT);
        form.addView(urgentCheck);

        final EditText title = field("Titulo");
        final EditText place = field("Cidade/local");
        final EditText value = field("Valor");
        final EditText desc = field("Descricao");
        final EditText author = field("Publicado por");
        form.addView(title); form.addView(place); form.addView(value); form.addView(desc); form.addView(author);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Publicar oportunidade")
                .setView(form)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Publicar", null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        if (title.getText().toString().trim().length() == 0 || place.getText().toString().trim().length() == 0) {
                            Toast.makeText(MainActivity.this, "Informe titulo e local.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        String authorName = textOr(author, prefs.getString(KEY_PROFILE, "Visitante"));
                        Opportunity item = new Opportunity("local" + System.currentTimeMillis(), selectedCategory[0], urgentCheck.isChecked(), title.getText().toString(), place.getText().toString(), "perto de voce", "agora", textOr(value, "A combinar"), textOr(desc, "Sem descricao informada."), authorName, "5516999999999", 4.8, countAuthorPosts(authorName) + 1);
                        opportunities.add(0, item);
                        saveOpportunities();
                        Toast.makeText(MainActivity.this, "Oportunidade publicada localmente.", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        showHome();
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
        final EditText name = field("Nome do perfil");
        name.setText(prefs.getString(KEY_PROFILE, ""));
        contentLayout.addView(name);
        Button save = bigButton("Salvar perfil", ORANGE, Color.WHITE);
        contentLayout.addView(save);
        contentLayout.addView(detailText("Publicacoes locais: " + countLocal() + "\nOportunidades salvas: " + favoriteIds.size(), 16, TEXT, Typeface.NORMAL));
        save.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                prefs.edit().putString(KEY_PROFILE, name.getText().toString()).apply();
                Toast.makeText(MainActivity.this, "Perfil salvo.", Toast.LENGTH_LONG).show();
            }
        });
        buildBottomNav();
    }

    private void buildBottomNav() {
        if (bottomNav == null) return;
        bottomNav.removeAllViews();
        LinearLayout dock = new LinearLayout(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER);
        dock.setPadding(dp(8), dp(6), dp(8), dp(6));
        dock.setBackground(roundedStroke(Color.WHITE, 26, Color.rgb(235, 225, 210), 1));
        dock.setElevation(dp(5));
        addNavItem(dock, "Inicio", "home", false);
        addNavItem(dock, "Buscar", "search", false);
        addNavItem(dock, "+ Publicar", "publish", true);
        addNavItem(dock, "Salvos", "favorites", false);
        addNavItem(dock, "Perfil", "profile", false);
        bottomNav.addView(dock, new LinearLayout.LayoutParams(-1, -1));
    }

    private void addNavItem(LinearLayout dock, String label, final String target, boolean primary) {
        boolean selected = currentScreen.equals(target);
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
        if ("details".equals(currentScreen)) { returnToLastListScreen(); return; }
        if ("profile".equals(currentScreen) || "favorites".equals(currentScreen) || "search".equals(currentScreen)) { showHome(); return; }
        if (searchEditText != null && searchEditText.getText().toString().trim().length() > 0) { showHome(); return; }
        if (!"Todas".equals(activeFilter)) { activeFilter = "Todas"; showHome(); return; }
        if (!"home".equals(currentScreen)) { showHome(); return; }
        super.onBackPressed();
    }

    private void refreshCurrentScreen() {
        if ("favorites".equals(currentScreen)) showFavorites();
        else if ("search".equals(currentScreen)) showSearch();
        else if ("profile".equals(currentScreen)) showProfile();
        else showHomeWithCurrentQuery();
    }

    private void returnToLastListScreen() {
        if ("favorites".equals(lastListScreen)) showFavorites();
        else if ("search".equals(lastListScreen)) showSearch();
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
        StringBuilder builder = new StringBuilder();
        for (String favoriteId : favoriteIds) {
            if (builder.length() > 0) builder.append(',');
            builder.append(favoriteId);
        }
        prefs.edit().putString(KEY_FAVORITES, builder.toString()).apply();
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
        String mensagem = "Ola! Vi a oportunidade '" + item.title + "' no Chama no Trampo e tenho interesse.";
        String url = "https://wa.me/" + item.phone + "?text=" + Uri.encode(mensagem);
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (Exception erro) { Toast.makeText(this, "Nao foi possivel abrir o WhatsApp.", Toast.LENGTH_LONG).show(); }
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
        return value == null ? "" : value.toLowerCase().trim();
    }

    private String textOr(EditText editText, String fallback) {
        String value = editText.getText().toString().trim();
        return value.length() == 0 ? fallback : value;
    }

    private int countLocal() {
        int total = 0;
        for (int i = 0; i < opportunities.size(); i++) if (opportunities.get(i).id.startsWith("local")) total++;
        return total;
    }

    private int countAuthorPosts(String author) {
        int total = 0;
        for (int i = 0; i < opportunities.size(); i++) if (opportunities.get(i).author.equalsIgnoreCase(author)) total++;
        return total;
    }

    private boolean matchesFilter(Opportunity item) {
        if ("Todas".equals(activeFilter)) return true;
        if ("Urgente".equals(activeFilter)) return item.urgent;
        return normalizeCategory(item.category).equals(activeFilter);
    }

    private String filterLabel(String key) {
        if ("Todas".equals(key)) return "Todas";
        if ("Servico".equals(key)) return "Servicos";
        if ("Seguranca".equals(key)) return "Seguranca";
        return key;
    }

    private String normalizeCategory(String value) {
        String v = normalize(value);
        if (v.contains("vaga")) return "Vaga";
        if (v.contains("bico")) return "Bico";
        if (v.contains("segur")) return "Seguranca";
        return "Servico";
    }

    private String displayCategory(String value) {
        return normalizeCategory(value);
    }

    private String categoryIcon(String value) {
        String category = normalizeCategory(value);
        if ("Vaga".equals(category)) return "[V]";
        if ("Bico".equals(category)) return "[B]";
        if ("Seguranca".equals(category)) return "[S]";
        return "[T]";
    }

    private String ratingText(double rating) {
        return String.format(Locale.US, "%.1f", rating);
    }

    private static class Opportunity {
        String id; String category; boolean urgent; String title; String place; String distance; String time; String value; String description; String author; String phone; double rating; int posts;

        Opportunity(String id, String category, boolean urgent, String title, String place, String distance, String time, String value, String description, String author, String phone, double rating, int posts) {
            this.id = id; this.category = category; this.urgent = urgent; this.title = title; this.place = place; this.distance = distance; this.time = time; this.value = value; this.description = description; this.author = author; this.phone = phone; this.rating = rating; this.posts = posts;
        }

        boolean matches(String query) {
            String data = (title + " " + description + " " + place + " " + category + " " + value + " " + author + " " + (urgent ? "urgente prioridade hoje" : "")).toLowerCase();
            return data.contains(query);
        }

        String toStorage() {
            return clean(id) + "|" + clean(category) + "|" + urgent + "|" + clean(title) + "|" + clean(place) + "|" + clean(distance) + "|" + clean(time) + "|" + clean(value) + "|" + clean(description) + "|" + clean(author) + "|" + clean(phone) + "|" + rating + "|" + posts;
        }

        static Opportunity fromStorage(String row) {
            String[] parts = row.split("\\|", -1);
            if (parts.length >= 13) return new Opportunity(parts[0], parts[1], "true".equals(parts[2]), parts[3], parts[4], parts[5], parts[6], parts[7], parts[8], parts[9], parts[10], parseDouble(parts[11], 4.7), parseInt(parts[12], 5));
            if (parts.length >= 9) {
                boolean urgent = parts[1].toLowerCase().contains("urgent");
                String category = urgent ? "Servico" : parts[1];
                return new Opportunity(parts[0], category, urgent, parts[2], parts[3], parts[4], parts[0].startsWith("local") ? "agora" : "recentemente", parts[5], parts[6], parts[7], parts[8], 4.4, 5);
            }
            return null;
        }

        static double parseDouble(String value, double fallback) {
            try { return Double.parseDouble(value); } catch (Exception e) { return fallback; }
        }

        static int parseInt(String value, int fallback) {
            try { return Integer.parseInt(value); } catch (Exception e) { return fallback; }
        }

        static String clean(String value) {
            if (value == null) return "";
            return value.replace("|", " ").replace("\n", " ");
        }
    }
}
