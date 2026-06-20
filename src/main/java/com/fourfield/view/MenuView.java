package com.fourfield.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Ecrã do Menu Inicial do Four Field Kono.
 * Design inspirado no mockup fornecido.
 */
public class MenuView {

    private final Stage stage;

    /** Cria o ecrã de menu para a janela indicada. */
    public MenuView(Stage stage) {
        this.stage = stage;
    }

    /** Mostra o menu inicial, com os botões para novo jogo, multiplayer e regras. */
    public void show() {
        stage.setTitle("Four Field Kono");
        stage.setWidth(900);
        stage.setHeight(600);
        stage.setResizable(false);

        // --- Fundo gradiente ---
        StackPane root = new StackPane();
        root.setBackground(new Background(new BackgroundFill(
            new RadialGradient(0, 0, 0.5, 0.5, 1, true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#120e3a")),
                new Stop(1, Color.web("#060412"))),
            CornerRadii.EMPTY, Insets.EMPTY)));

        VBox container = new VBox(40);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(50));
        container.setMaxWidth(820);

        // --- Título ---
        Label titulo = new Label("Four Field Kono");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        titulo.setTextFill(Color.WHITE);

        Label subtitulo = new Label("Escolhe uma opção para começar!");
        subtitulo.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitulo.setTextFill(Color.web("#b3b0cb"));

        VBox header = new VBox(8, titulo, subtitulo);
        header.setAlignment(Pos.CENTER);

        // --- Botões principais ---
        HBox botoes = new HBox(20);
        botoes.setAlignment(Pos.CENTER);

        Button btnNovoJogo = criarBotao("▶  Novo Jogo",
            new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0072ff")), new Stop(1, Color.web("#00c6ff"))));

        Button btnMultiplayer = criarBotao("⚡  Multi Player",
            new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#6200ea")), new Stop(1, Color.web("#9d4edd"))));

        Button btnRegras = criarBotao("📄  Regras",
            new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#d81b60")), new Stop(1, Color.web("#ff4081"))));

        botoes.getChildren().addAll(btnNovoJogo, btnMultiplayer, btnRegras);

        // --- Cartões informativos ---
        HBox infoCards = new HBox(20);
        infoCards.setAlignment(Pos.CENTER);
        infoCards.getChildren().addAll(
            criarInfoCard("👤", "JOGADORES", "2 Jogadores"),
            criarInfoCard("🏆", "OBJETIVO", "Bloquear ou Capturar"),
            criarInfoCard("⊞", "TABULEIRO", "Grelha 4 × 4")
        );

        // --- Texto rodapé ---
        Label footer = new Label("Escolhe uma opção para começar!");
        footer.setFont(Font.font("Arial", 14));
        footer.setTextFill(Color.web("#666280"));

        container.getChildren().addAll(header, botoes, infoCards, footer);
        root.getChildren().add(container);

        // --- Ações ---
        btnNovoJogo.setOnAction(e -> {
            ConfiguracaoView config = new ConfiguracaoView(stage);
            config.show();
        });

        btnMultiplayer.setOnAction(e -> {
            MultiplayerView mp = new MultiplayerView(stage);
            mp.show();
        });

        btnRegras.setOnAction(e -> {
            RegrasView regras = new RegrasView(stage);
            regras.show();
        });

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /** Cria um botão grande com o gradiente de cor indicado. */
    private Button criarBotao(String texto, Paint gradiente) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btn.setTextFill(Color.WHITE);
        btn.setPrefWidth(220);
        btn.setPrefHeight(55);
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setBackground(new Background(new BackgroundFill(gradiente, new CornerRadii(14), Insets.EMPTY)));
        btn.setBorder(Border.EMPTY);

        btn.setOnMouseEntered(e -> btn.setScaleX(1.04));
        btn.setOnMouseExited(e -> btn.setScaleX(1.0));

        return btn;
    }

    /** Cria um cartão pequeno com ícone, etiqueta e valor (usado para mostrar info do jogo). */
    private VBox criarInfoCard(String icone, String label, String valor) {
        Label ico = new Label(icone);
        ico.setFont(Font.font(28));

        Label lbl = new Label(label);
        lbl.setFont(Font.font("Arial", 11));
        lbl.setTextFill(Color.web("#85829e"));

        Label val = new Label(valor);
        val.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        val.setTextFill(Color.WHITE);

        VBox texto = new VBox(3, lbl, val);

        HBox card = new HBox(15, ico, texto);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setPrefWidth(230);

        card.setBackground(new Background(new BackgroundFill(
            Color.web("#ffffff08"), new CornerRadii(14), Insets.EMPTY)));
        card.setBorder(new Border(new BorderStroke(
            Color.web("#ffffff14"), BorderStrokeStyle.SOLID, new CornerRadii(14), BorderWidths.DEFAULT)));

        return new VBox(card);
    }
}
