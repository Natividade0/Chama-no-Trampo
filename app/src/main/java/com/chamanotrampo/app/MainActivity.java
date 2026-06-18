package com.chamanotrampo.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends Activity {
    private static final String PREFS = "chama_no_trampo_prefs";
    private static final String KEY_OPPORTUNITIES = "opportunities";
    private static final String KEY_FAVORITES = "favorites";
    private static final String KEY_PROFILE = "profile_name";

    private static final int CREAM = Color.rgb(255, 248, 235);
    private static final int ORANGE = Color.rgb(255, 154, 36);
    private static final int YELLOW = Color.rgb(255, 202, 74);
    private static final int GREEN = Color.rgb(25, 170, 91);
    private static final int TEXT = Color.rgb(37, 32, 24);
    private static final int MUTED = Color.rgb(116, 98, 77);

    private FrameLayout rootContainer;
    private LinearLayout contentLayout;
    private LinearLayout bottomNav;
    private EditText searchEditText;
    private TextView screenTitle;
    private TextView emptyText;
    private SharedPreferences prefs;
    private ArrayList<Opportunity> opportunities;
    private HashSet<String> favoriteIds;
    private String currentScreen = "home";
    private String lastListScreen = "home";

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
                if (ids[i].trim().length() > 0) {
                    favoriteIds.add(ids[i].trim());
                }
            }
        }

        opportunities = new ArrayList<Opportunity>();
        String saved = prefs.getString(KEY_OPPORTUNITIES, "");
        if (saved.length() > 0) {
            String[] rows = saved.split("\\n");
            for (int i = 0; i < rows.length; i++) {
                Opportunity item = Opportunity.fromStorage(rows[i]);
                if (item != null) {
                    opportunities.add(item);
                }
            }
        }
        if (opportunities.size() == 0) {
            opportunities.add(new Opportunity("seed1", "Vaga", "Auxiliar de producao", "Guariba - Centro", "1,8 km", "Salario a combinar", "Ajudar na organizacao da linha, separacao de produtos e apoio geral na producao.", "Mercado Sao Jose", "5516999999999"));
            opportunities.add(new Opportunity("seed2", "Servico", "Pedreiro para reforma", "Jardinopolis - Jardim Primavera", "4,2 km", "Enviar orcamento", "Reforma pequena em banheiro, troca de piso e acabamento. Preferencia para profissionais da regiao.", "Dona Maria", "5516999999999"));
            opportunities.add(new Opportunity("seed3", "Bico", "Ajudante para descarregar caminhao", "Ribeirao Preto - Distrito Industrial", "7,5 km", "R$ 120,00 no dia", "Preciso de ajudante hoje para descarregar mercadorias. Pagamento no final do servico.", "Carlos Fretes", "5516999999999"));
            opportunities.add(new Opportunity("seed4", "Urgente", "Eletricista hoje", "Jaboticabal - Nova Jaboticabal", "2,3 km", "A combinar", "Tomadas pararam de funcionar. Preciso de avaliacao e reparo ainda hoje.", "Ana Paula", "5516999999999"));
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
        bottomNav.setPadding(dp(8), dp(8), dp(8), dp(8));
        bottomNav.setBackgroundColor(Color.WHITE);
        main.addView(bottomNav, new LinearLayout.LayoutParams(-1, dp(72)));
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
        renderListScreen("Buscar oportunidades", "Digite titulo, cidade, categoria, valor ou autor.", false, searchEditText == null ? "" : searchEditText.getText().toString());
        searchEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void showFavorites() {
        currentScreen = "favorites";
        lastListScreen = "favorites";
        renderListScreen("Salvos", "Oportunidades que voce salvou para ver depois.", true, searchEditText == null ? "" : searchEditText.getText().toString());
    }

    private void renderListScreen(String title, String subtitle, boolean onlyFavorites, String query) {
        contentLayout.removeAllViews();
        addHeader(title, subtitle);
        addSearchBox(query, onlyFavorites);
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
        contentLayout.addView(header, new LinearLayout.LayoutParams(-1, -2));
        screenTitle = text(title, 26, TEXT, Typeface.BOLD);
        header.addView(screenTitle);
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
        searchEditText.setBackground(rounded(Color.WHITE, 18));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(52));
        params.setMargins(0, dp(14), 0, dp(10));
        contentLayout.addView(searchEditText, params);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { renderCards(onlyFavorites); }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private void renderCards(boolean onlyFavorites) {
        while (contentLayout.getChildCount() > 3) {
            contentLayout.removeViewAt(3);
        }
        String query = normalize(searchEditText == null ? "" : searchEditText.getText().toString());
        int count = 0;
        for (int i = 0; i < opportunities.size(); i++) {
            final Opportunity item = opportunities.get(i);
            if (onlyFavorites && !favoriteIds.contains(item.id)) {
                continue;
            }
            if (query.length() > 0 && !item.matches(query)) {
                continue;
            }
            contentLayout.addView(cardView(item));
            count++;
        }
        if (count == 0) {
            if (onlyFavorites && query.length() == 0) {
                emptyText.setText("Voce ainda nao salvou nenhuma oportunidade.");
            } else {
                emptyText.setText("Nenhuma oportunidade encontrada para essa busca.");
            }
            emptyText.setVisibility(View.VISIBLE);
        } else {
            emptyText.setVisibility(View.GONE);
        }
    }

    private View cardView(final Opportunity item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(rounded(Color.WHITE, 20));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.setMargins(0, dp(10), 0, dp(4));
        card.setLayoutParams(cardParams);

        TextView badge = text(item.category.toUpperCase(), 12, Color.WHITE, Typeface.BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(10), dp(4), dp(10), dp(4));
        badge.setBackground(rounded(ORANGE, 14));
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(-2, -2);
        card.addView(badge, badgeParams);

        TextView title = text(item.title, 20, TEXT, Typeface.BOLD);
        title.setPadding(0, dp(8), 0, 0);
        card.addView(title);
        card.addView(text(item.place + " • " + item.distance, 14, MUTED, Typeface.NORMAL));
        TextView value = text(item.value, 16, GREEN, Typeface.BOLD);
        value.setPadding(0, dp(6), 0, 0);
        card.addView(value);
        TextView desc = text(item.description, 14, TEXT, Typeface.NORMAL);
        desc.setPadding(0, dp(8), 0, 0);
        card.addView(desc);
        card.addView(text("Publicado por " + item.author, 13, MUTED, Typeface.NORMAL));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(10), 0, 0);
        Button whatsapp = smallButton("WhatsApp", GREEN, Color.WHITE);
        Button save = smallButton(favoriteIds.contains(item.id) ? "Salvo" : "Salvar", favoriteIds.contains(item.id) ? ORANGE : Color.rgb(241, 236, 226), favoriteIds.contains(item.id) ? Color.WHITE : TEXT);
        actions.addView(whatsapp, new LinearLayout.LayoutParams(0, dp(44), 1));
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(0, dp(44), 1);
        saveParams.setMargins(dp(8), 0, 0, 0);
        actions.addView(save, saveParams);
        card.addView(actions);

        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showDetails(item); }
        });
        whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { abrirWhatsapp(item); }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { toggleFavorite(item.id); refreshCurrentScreen(); }
        });
        return card;
    }

    private void showDetails(final Opportunity item) {
        if (!"details".equals(currentScreen)) {
            lastListScreen = currentScreen;
        }
        currentScreen = "details";
        contentLayout.removeAllViews();
        addHeader("Detalhes da oportunidade", "Veja as informacoes antes de chamar.");
        TextView badge = text(item.category.toUpperCase(), 12, Color.WHITE, Typeface.BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(10), dp(5), dp(10), dp(5));
        badge.setBackground(rounded(ORANGE, 14));
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(-2, -2);
        badgeParams.setMargins(0, dp(16), 0, 0);
        contentLayout.addView(badge, badgeParams);
        contentLayout.addView(detailText(item.title, 25, TEXT, Typeface.BOLD));
        contentLayout.addView(detailText(item.place + " • " + item.distance, 16, MUTED, Typeface.NORMAL));
        contentLayout.addView(detailText(item.value, 18, GREEN, Typeface.BOLD));
        contentLayout.addView(detailText(item.description, 16, TEXT, Typeface.NORMAL));
        contentLayout.addView(detailText("Publicado por " + item.author, 15, MUTED, Typeface.NORMAL));
        Button whatsapp = bigButton("Chamar no WhatsApp", GREEN, Color.WHITE);
        Button favorite = bigButton(favoriteIds.contains(item.id) ? "Remover dos salvos" : "Salvar oportunidade", ORANGE, Color.WHITE);
        Button back = bigButton("Voltar", Color.rgb(241, 236, 226), TEXT);
        contentLayout.addView(whatsapp);
        contentLayout.addView(favorite);
        contentLayout.addView(back);
        whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { abrirWhatsapp(item); }
        });
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { toggleFavorite(item.id); showDetails(item); }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { returnToLastListScreen(); }
        });
        buildBottomNav();
    }

    private void showPublishDialog() {
        final LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(12), dp(4), dp(12), 0);
        final EditText title = field("Titulo");
        final EditText category = field("Categoria");
        final EditText place = field("Cidade/local");
        final EditText value = field("Valor");
        final EditText desc = field("Descricao");
        final EditText author = field("Publicado por");
        form.addView(title); form.addView(category); form.addView(place); form.addView(value); form.addView(desc); form.addView(author);
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
                        Opportunity item = new Opportunity("local" + System.currentTimeMillis(), textOr(category, "Oportunidade"), title.getText().toString(), place.getText().toString(), "perto de voce", textOr(value, "A combinar"), textOr(desc, "Sem descricao informada."), textOr(author, prefs.getString(KEY_PROFILE, "Visitante")), "5516999999999");
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
        TextView stats = detailText("Publicacoes locais: " + countLocal() + "\nOportunidades salvas: " + favoriteIds.size(), 16, TEXT, Typeface.NORMAL);
        contentLayout.addView(stats);
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
        addNavButton("Inicio", "home");
        addNavButton("Buscar", "search");
        addNavButton("+ Publicar", "publish");
        addNavButton("Salvos", "favorites");
        addNavButton("Perfil", "profile");
    }

    private void addNavButton(String label, final String target) {
        boolean selected = currentScreen.equals(target);
        Button button = smallButton(label, selected ? ORANGE : Color.WHITE, selected ? Color.WHITE : TEXT);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1, 1);
        params.setMargins(dp(2), 0, dp(2), 0);
        bottomNav.addView(button, params);
        button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if ("home".equals(target)) showHome();
                if ("search".equals(target)) showSearch();
                if ("publish".equals(target)) showPublishDialog();
                if ("favorites".equals(target)) showFavorites();
                if ("profile".equals(target)) showProfile();
            }
        });
    }

    private void refreshCurrentScreen() {
        if ("favorites".equals(currentScreen)) showFavorites();
        else if ("search".equals(currentScreen)) showSearch();
        else if ("profile".equals(currentScreen)) showProfile();
        else showHome();
    }

    private void returnToLastListScreen() {
        if ("favorites".equals(lastListScreen)) showFavorites();
        else if ("search".equals(lastListScreen)) showSearch();
        else showHome();
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
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception erro) {
            Toast.makeText(this, "Nao foi possivel abrir o WhatsApp.", Toast.LENGTH_LONG).show();
        }
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
        button.setBackground(rounded(bg, 16));
        return button;
    }

    private EditText field(String hint) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setSingleLine(false);
        editText.setTextColor(TEXT);
        editText.setHintTextColor(MUTED);
        editText.setBackground(rounded(Color.WHITE, 16));
        editText.setPadding(dp(12), 0, dp(12), 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(52));
        params.setMargins(0, dp(10), 0, 0);
        editText.setLayoutParams(params);
        return editText;
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
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
        for (int i = 0; i < opportunities.size(); i++) {
            if (opportunities.get(i).id.startsWith("local")) total++;
        }
        return total;
    }

    private static class Opportunity {
        String id; String category; String title; String place; String distance; String value; String description; String author; String phone;
        Opportunity(String id, String category, String title, String place, String distance, String value, String description, String author, String phone) {
            this.id = id; this.category = category; this.title = title; this.place = place; this.distance = distance; this.value = value; this.description = description; this.author = author; this.phone = phone;
        }
        boolean matches(String query) {
            String data = (title + " " + description + " " + place + " " + category + " " + value + " " + author).toLowerCase();
            return data.contains(query);
        }
        String toStorage() {
            return clean(id) + "|" + clean(category) + "|" + clean(title) + "|" + clean(place) + "|" + clean(distance) + "|" + clean(value) + "|" + clean(description) + "|" + clean(author) + "|" + clean(phone);
        }
        static Opportunity fromStorage(String row) {
            String[] parts = row.split("\\|", -1);
            if (parts.length < 9) return null;
            return new Opportunity(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], parts[8]);
        }
        static String clean(String value) {
            if (value == null) return "";
            return value.replace("|", " ").replace("\n", " ");
        }
    }
}
