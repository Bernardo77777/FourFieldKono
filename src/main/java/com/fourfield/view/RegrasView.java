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
 * Ecrã de Regras do Four Field Kono.
 */
public class RegrasView {

    private final Stage stage;

    /** Cria o ecrã de regras. */
    public RegrasView(Stage stage) {
        this.stage = stage;
    }

    /** Mostra o texto com as regras do jogo. */
    public void show() {
        StackPane root = new StackPane();
        root.setBackground(new Background(new BackgroundFill(
            new RadialGradient(0, 0, 0.5, 0.5, 1, true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#120e3a")),
                new Stop(1, Color.web("#060412"))),
            CornerRadii.EMPTY, Insets.EMPTY)));

        VBox container = new VBox(25);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(50, 80, 50, 80));

        Label titulo = new Label("Regras do Jogo");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titulo.setTextFill(Color.WHITE);

        // Scroll com regras
        TextArea regras = new TextArea();
        regras.setEditable(false);
        regras.setWrapText(true);
        regras.setPrefHeight(360);
        regras.setFont(Font.font("Arial", 14));
        regras.setStyle("-fx-control-inner-background: #1a1540; -fx-text-fill: #d0cce8; "
                + "-fx-background-color: #1a1540; -fx-border-color: #3a3570; "
                + "-fx-border-radius: 12; -fx-background-radius: 12;");
        regras.setText(getTextoRegras());

        Button btnVoltar = new Button("← Voltar ao Menu");
        btnVoltar.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        btnVoltar.setTextFill(Color.WHITE);
        btnVoltar.setPrefWidth(200);
        btnVoltar.setPrefHeight(46);
        btnVoltar.setCursor(javafx.scene.Cursor.HAND);
        btnVoltar.setBackground(new Background(new BackgroundFill(
            new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#d81b60")), new Stop(1, Color.web("#ff4081"))),
            new CornerRadii(12), Insets.EMPTY)));
        btnVoltar.setBorder(Border.EMPTY);
        btnVoltar.setOnAction(e -> new MenuView(stage).show());

        container.getChildren().addAll(titulo, regras, btnVoltar);
        root.getChildren().add(container);

        stage.setScene(new Scene(root, 900, 600));
    }

    /** Texto com todas as regras do jogo. */
    private String getTextoRegras() {
        return """
TABULEIRO E PEÇAS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
• O jogo é disputado numa grelha 4×4 (16 casas).
• Cada jogador começa com 8 peças.
• O tabuleiro inicia completamente preenchido.

POSIÇÃO INICIAL
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
• Jogador 1 ocupa as duas filas inferiores (filas 3 e 4).
• Jogador 2 ocupa as duas filas superiores (filas 1 e 2).

MOVIMENTO SIMPLES
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
• Mover uma peça para uma casa adjacente vazia.
• Apenas movimentos ortogonais (horizontal ou vertical).
• Movimentos na diagonal são PROIBIDOS.

CAPTURA POR SALTO
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
• Para capturar: salta sobre uma peça ALIADA adjacente
  para aterrar numa casa com peça ADVERSÁRIA.
• A peça adversária é removida do jogo.
• O salto é sempre em linha reta (ortogonal).

CONDIÇÕES DE VITÓRIA
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
• Ganhas se o adversário ficar com apenas 1 peça
  (impossibilitado de capturar).
• Ganhas se bloqueares todas as peças do adversário,
  deixando-o sem jogadas possíveis.
""";
    }
}
