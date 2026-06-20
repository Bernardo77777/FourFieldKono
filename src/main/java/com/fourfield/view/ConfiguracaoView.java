package com.fourfield.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Ecrã de configuração antes de iniciar o jogo local.
 */
public class ConfiguracaoView {

    private final Stage stage;

    /** Cria o ecrã de configuração do jogo local. */
    public ConfiguracaoView(Stage stage) {
        this.stage = stage;
    }

    /** Mostra o formulário para escolher os nomes dos dois jogadores e iniciar o jogo local. */
    public void show() {
        StackPane root = new StackPane();
        root.setBackground(new Background(new BackgroundFill(
            new RadialGradient(0, 0, 0.5, 0.5, 1, true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#120e3a")),
                new Stop(1, Color.web("#060412"))),
            CornerRadii.EMPTY, Insets.EMPTY)));

        VBox container = new VBox(30);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(60));
        container.setMaxWidth(500);

        Label titulo = new Label("Configurar Jogo");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titulo.setTextFill(Color.WHITE);

        // Campo Jogador 1
        Label lblJ1 = new Label("Nome do Jogador 1");
        lblJ1.setTextFill(Color.web("#b3b0cb"));
        lblJ1.setFont(Font.font("Arial", 14));
        TextField campoJ1 = criarCampo("Jogador 1");

        // Campo Jogador 2
        Label lblJ2 = new Label("Nome do Jogador 2");
        lblJ2.setTextFill(Color.web("#b3b0cb"));
        lblJ2.setFont(Font.font("Arial", 14));
        TextField campoJ2 = criarCampo("Jogador 2");

        VBox formulario = new VBox(12, lblJ1, campoJ1, lblJ2, campoJ2);

        // Botões
        Button btnJogar = criarBotaoAcao("▶  Iniciar Jogo", "#0072ff", "#00c6ff");
        Button btnVoltar = criarBotaoVoltar();

        HBox botoes = new HBox(15, btnVoltar, btnJogar);
        botoes.setAlignment(Pos.CENTER);

        container.getChildren().addAll(titulo, formulario, botoes);
        root.getChildren().add(container);

        btnJogar.setOnAction(e -> {
            String nomeJ1 = campoJ1.getText().trim().isEmpty() ? "Jogador 1" : campoJ1.getText().trim();
            String nomeJ2 = campoJ2.getText().trim().isEmpty() ? "Jogador 2" : campoJ2.getText().trim();
            JogoView jogo = new JogoView(stage, nomeJ1, nomeJ2);
            jogo.show();
        });

        btnVoltar.setOnAction(e -> {
            MenuView menu = new MenuView(stage);
            menu.show();
        });

        stage.setScene(new Scene(root, 900, 600));
    }

    /** Cria um campo de texto com o estilo usado neste ecrã. */
    private TextField criarCampo(String placeholder) {
        TextField tf = new TextField();
        tf.setPromptText(placeholder);
        tf.setFont(Font.font("Arial", 15));
        tf.setPrefHeight(42);
        tf.setStyle("-fx-background-color: #1a1540; -fx-text-fill: white; "
                + "-fx-prompt-text-fill: #66628a; -fx-background-radius: 10; "
                + "-fx-border-color: #3a3570; -fx-border-radius: 10; -fx-padding: 8 14;");
        return tf;
    }

    /** Cria um botão de ação com gradiente de cor. */
    private Button criarBotaoAcao(String texto, String cor1, String cor2) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        btn.setTextFill(Color.WHITE);
        btn.setPrefWidth(200);
        btn.setPrefHeight(48);
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setBackground(new Background(new BackgroundFill(
            new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(cor1)), new Stop(1, Color.web(cor2))),
            new CornerRadii(12), Insets.EMPTY)));
        btn.setBorder(Border.EMPTY);
        return btn;
    }

    /** Cria o botão de voltar ao menu. */
    private Button criarBotaoVoltar() {
        Button btn = new Button("← Voltar");
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        btn.setTextFill(Color.web("#b3b0cb"));
        btn.setPrefWidth(130);
        btn.setPrefHeight(48);
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setBackground(new Background(new BackgroundFill(
            Color.web("#ffffff10"), new CornerRadii(12), Insets.EMPTY)));
        btn.setBorder(new Border(new BorderStroke(
            Color.web("#ffffff20"), BorderStrokeStyle.SOLID, new CornerRadii(12), BorderWidths.DEFAULT)));
        return btn;
    }
}
