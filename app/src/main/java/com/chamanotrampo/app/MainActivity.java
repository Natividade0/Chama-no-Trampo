package com.chamanotrampo.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private final List<Opportunity> opportunities = new ArrayList<>();
    private LinearLayout listContainer;
    private String currentFilter = "Todos";

    private static final int BRAND_DARK = Color.rgb(18, 18, 18);
    private static final int BRAND_YELLOW = Color.rgb(255, 179, 0);
    private static final int BRAND_GREEN = Color.rgb(46, 125, 50);
    private static final int BACKGROUND = Color.rgb(247, 247, 247);
    private static final int CARD = Color.WHITE;
    private static final int TEXT_PRIMARY = Color.rgb(27, 27, 27);
    private static final int TEXT_SECONDARY = Color.rgb(98, 98, 98);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        seedFakeData();
        renderHome();
    }

    private void renderHome() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(BACKGROUND);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(24));
        scrollView.addView(root);

        root.addView(header());
        root.addView(categoryBar());
        root.addView(primaryButton("Publicar oportunidade", view -> renderPublishForm()));

        TextView sectionTitle = title("Oportunidades perto de você", 22);
        sectionTitle.setPadding(0, dp(20), 0, dp(8));
        root.addView(sectionTitle);

        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(listContainer);

        setContentView(scrollView);
        refreshList();
    }

    private View header() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(18), dp(18), dp(18));
        box.setBackgroundColor(BRAND_DARK);

        TextView appName = title("Chama no Trampo", 30);
        appName.setTextColor(BRAND_YELLOW);
        box.addView(appName);

        TextView slogan = text("Empregos, bicos e serviços perto de você.", 16, Color.WHITE);
        slogan.setPadding(0, dp(8), 0, 0);
        box.addView(slogan);

        TextView phrase = text("Postou, chamou, fechou.", 14, BRAND_YELLOW);
        phrase.setPadding(0, dp(12), 0, 0);
        box.addView(phrase);

        return box;
    }

    private View categoryBar() {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(0, dp(16), 0, dp(8));

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.addView(filterButton("Todos"));
        row1.addView(filterButton("Vaga"));

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setPadding(0, dp(8), 0, 0);
        row2.addView(filterButton("Bico"));
        row2.addView(filterButton("Serviço"));
        row2.addView(filterButton("Urgente"));

        wrap.addView(row1);
        wrap.addView(row2);
        return wrap;
    }

    private Button filterButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(label.equals(currentFilter) ? BRAND_GREEN : BRAND_DARK);
        button.setOnClickListener(view -> {
            currentFilter = label;
            renderHome();
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(dp(3), 0, dp(3), 0);
        button.setLayoutParams(params);
        return button;
    }

    private void renderPublishForm() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(BACKGROUND);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(24));
        scrollView.addView(root);

        root.addView(title("Publicar oportunidade", 26));
        root.addView(text("Cadastre uma vaga, bico ou serviço para aparecer na lista do app.", 15, TEXT_SECONDARY));

        EditText titleInput = input("Título. Ex: Pedreiro para reforma");
        EditText cityInput = input("Cidade");
        EditText neighborhoodInput = input("Bairro");
        EditText priceInput = input("Valor ou salário. Opcional");
        EditText phoneInput = input("WhatsApp para contato. Ex: 16999999999");
        EditText descriptionInput = input("Descrição do que precisa ser feito");
        descriptionInput.setMinLines(4);
        descriptionInput.setGravity(Gravity.TOP);

        Spinner typeSpinner = new Spinner(this);
        String[] types = {"Vaga", "Bico", "Serviço", "Urgente"};
        typeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types));
        typeSpinner.setPadding(0, dp(8), 0, dp(8));

        root.addView(label("Tipo de oportunidade"));
        root.addView(typeSpinner);
        root.addView(titleInput);
        root.addView(cityInput);
        root.addView(neighborhoodInput);
        root.addView(priceInput);
        root.addView(phoneInput);
        root.addView(descriptionInput);

        root.addView(primaryButton("Publicar", view -> {
            String title = titleInput.getText().toString().trim();
            String city = cityInput.getText().toString().trim();
            String neighborhood = neighborhoodInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String price = priceInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String type = typeSpinner.getSelectedItem().toString();

            if (title.isEmpty() || city.isEmpty() || description.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Preencha título, cidade, descrição e WhatsApp.", Toast.LENGTH_LONG).show();
                return;
            }

            opportunities.add(0, new Opportunity(title, type, city, neighborhood, price, description, phone, now()));
            currentFilter = "Todos";
            Toast.makeText(this, "Oportunidade publicada no protótipo.", Toast.LENGTH_SHORT).show();
            renderHome();
        }));

        root.addView(secondaryButton("Voltar", view -> renderHome()));
        setContentView(scrollView);
    }

    private void refreshList() {
        listContainer.removeAllViews();

        for (Opportunity opportunity : opportunities) {
            boolean show = currentFilter.equals("Todos") || opportunity.type.equals(currentFilter);
            if (show) {
                listContainer.addView(opportunityCard(opportunity));
            }
        }
    }

    private View opportunityCard(Opportunity opportunity) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackgroundColor(CARD);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);

        TextView type = text(opportunity.type.toUpperCase(Locale.ROOT), 12, BRAND_GREEN);
        card.addView(type);

        TextView title = title(opportunity.title, 20);
        title.setPadding(0, dp(4), 0, 0);
        card.addView(title);

        String location = opportunity.city;
        if (!opportunity.neighborhood.isEmpty()) {
            location += " - " + opportunity.neighborhood;
        }
        card.addView(text(location, 14, TEXT_SECONDARY));

        if (!opportunity.price.isEmpty()) {
            TextView price = text(opportunity.price, 16, BRAND_DARK);
            price.setPadding(0, dp(8), 0, 0);
            card.addView(price);
        }

        TextView description = text(opportunity.description, 15, TEXT_PRIMARY);
        description.setPadding(0, dp(10), 0, dp(10));
        card.addView(description);

        card.addView(text("Publicado: " + opportunity.createdAt, 12, TEXT_SECONDARY));
        card.addView(primaryButton("Chamar no WhatsApp", view -> openWhatsApp(opportunity)));

        return card;
    }

    private void openWhatsApp(Opportunity opportunity) {
        String phone = opportunity.phone.replaceAll("[^0-9]", "");
        String message = "Olá! Vi no Chama no Trampo e tenho interesse: " + opportunity.title;
        String url = "https://wa.me/55" + phone + "?text=" + Uri.encode(message);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            startActivity(intent);
        } catch (Exception exception) {
            Toast.makeText(this, "Não foi possível abrir o WhatsApp.", Toast.LENGTH_LONG).show();
        }
    }

    private Button primaryButton(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(BRAND_GREEN);
        button.setOnClickListener(listener);
        button.setPadding(dp(8), dp(8), dp(8), dp(8));
        return button;
    }

    private Button secondaryButton(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(BRAND_DARK);
        button.setOnClickListener(listener);
        return button;
    }

    private EditText input(String hint) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setSingleLine(false);
        editText.setTextColor(TEXT_PRIMARY);
        editText.setHintTextColor(TEXT_SECONDARY);
        editText.setPadding(dp(10), dp(10), dp(10), dp(10));
        return editText;
    }

    private TextView label(String value) {
        TextView label = text(value, 14, TEXT_SECONDARY);
        label.setPadding(0, dp(16), 0, 0);
        return label;
    }

    private TextView title(String value, int sp) {
        TextView textView = text(value, sp, TEXT_PRIMARY);
        textView.setTypeface(null, 1);
        return textView;
    }

    private TextView text(String value, int sp, int color) {
        TextView textView = new TextView(this);
        textView.setText(value);
        textView.setTextSize(sp);
        textView.setTextColor(color);
        textView.setLineSpacing(0, 1.1f);
        return textView;
    }

    private String now() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void seedFakeData() {
        if (!opportunities.isEmpty()) {
            return;
        }

        opportunities.add(new Opportunity(
                "Auxiliar de produção",
                "Vaga",
                "Guariba",
                "Centro",
                "Salário a combinar",
                "Empresa procura pessoa disponível para início imediato. Não exige experiência.",
                "16999999999",
                "Hoje"
        ));

        opportunities.add(new Opportunity(
                "Pedreiro para reforma de banheiro",
                "Serviço",
                "Jardinópolis",
                "Jardim Primavera",
                "Enviar orçamento",
                "Preciso de profissional para avaliar e executar uma pequena reforma residencial.",
                "16999999999",
                "Hoje"
        ));

        opportunities.add(new Opportunity(
                "Ajudante para descarregar caminhão",
                "Bico",
                "Ribeirão Preto",
                "Distrito Industrial",
                "R$ 120,00 no dia",
                "Serviço rápido para amanhã cedo. Precisa ter disposição e pontualidade.",
                "16999999999",
                "Ontem"
        ));

        opportunities.add(new Opportunity(
                "Eletricista urgente",
                "Urgente",
                "Jaboticabal",
                "Nova Jaboticabal",
                "A combinar",
                "Preciso de eletricista ainda hoje para verificar queda de energia em uma casa.",
                "16999999999",
                "Agora"
        ));
    }

    private static class Opportunity {
        final String title;
        final String type;
        final String city;
        final String neighborhood;
        final String price;
        final String description;
        final String phone;
        final String createdAt;

        Opportunity(String title, String type, String city, String neighborhood, String price,
                    String description, String phone, String createdAt) {
            this.title = title;
            this.type = type;
            this.city = city;
            this.neighborhood = neighborhood;
            this.price = price;
            this.description = description;
            this.phone = phone;
            this.createdAt = createdAt;
        }
    }
}
