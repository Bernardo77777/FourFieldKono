package com.fourfield.view;

import com.fourfield.network.ClienteJogo;
import com.fourfield.network.ServidorJogo;
import javafx.application.Platform;
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
 * Ecrã de configuração do modo Multiplayer (rede local).
 */
public class MultiplayerView {

    private final Stage stage;

    /** Cria o ecrã de configuração do multiplayer. */
    public MultiplayerView(Stage stage) {
        this.stage = stage;
    }

    /** Mostra o formulário para criar servidor ou ligar como cliente. */
    public void show() {
        StackPane root = new StackPane();
        root.setBackground(new Background(new BackgroundFill(
            new RadialGradient(0, 0, 0.5, 0.5, 1, true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#120e3a")),
                new Stop(1, Color.web("#060412"))),
            CornerRadii.EMPTY, Insets.EMPTY)));

        VBox container = new VBox(25);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(50));
        container.setMaxWidth(520);

        Label titulo = new Label("Multi Player");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titulo.setTextFill(Color.WHITE);

        Label subtitulo = new Label("Joga em rede local com outro utilizador");
        subtitulo.setFont(Font.font("Arial", 14));
        subtitulo.setTextFill(Color.web("#b3b0cb"));

        // Separador: Servidor vs Cliente
        HBox modos = new HBox(20);
        modos.setAlignment(Pos.CENTER);

        ToggleGroup grupo = new ToggleGroup();
        RadioButton rbServidor = new RadioButton("Criar Servidor (Anfitrião)");
        RadioButton rbCliente = new RadioButton("Ligar como Cliente");
        rbServidor.setToggleGroup(grupo);
        rbCliente.setToggleGroup(grupo);
        rbServidor.setSelected(true);
        estilizarRadio(rbServidor);
        estilizarRadio(rbCliente);
        modos.getChildren().addAll(rbServidor, rbCliente);

        // Campos
        Label lblNome = new Label("O teu nome");
        lblNome.setTextFill(Color.web("#b3b0cb"));
        TextField campoNome = criarCampo("O teu nome");

        Label lblPorta = new Label("Porta");
        lblPorta.setTextFill(Color.web("#b3b0cb"));
        TextField campoPorta = criarCampo("5000");

        Label lblIP = new Label("IP do Servidor");
        lblIP.setTextFill(Color.web("#b3b0cb"));
        TextField campoIP = criarCampo("192.168.1.X");

        // Mostra o IP deste computador quando é o anfitrião (é o que o outro jogador tem de escrever)
        Label lblMeuIp = new Label();
        lblMeuIp.setTextFill(Color.web("#00ff88"));
        lblMeuIp.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblMeuIp.setWrapText(true);

        // Como anfitrião: o campo já vem preenchido com o IP da máquina e bloqueado (não é para escrever lá).
        // Como cliente: o campo fica vazio e editável, para escrever o IP do anfitrião.
        Runnable atualizarModoIp = () -> {
            if (rbServidor.isSelected()) {
                String meuIp = ServidorJogo.obterIpLocal();
                lblMeuIp.setText("📡 O teu IP nesta rede: " + meuIp
                    + "  (dá este IP a quem vai ligar como cliente)");
                campoIP.setText(meuIp);
                campoIP.setDisable(true);
            } else {
                lblMeuIp.setText("");
                campoIP.clear();
                campoIP.setDisable(false);
            }
        };
        atualizarModoIp.run();

        // Atualiza o campo/label de IP conforme o modo escolhido
        grupo.selectedToggleProperty().addListener((obs, old, novo) -> atualizarModoIp.run());

        Label lblStatus = new Label("");
        lblStatus.setTextFill(Color.web("#00c6ff"));
        lblStatus.setFont(Font.font("Arial", 13));

        VBox formulario = new VBox(10, lblNome, campoNome, lblPorta, campoPorta, lblIP, campoIP);

        Button btnLigar = criarBotaoAcao("⚡  Ligar", "#6200ea", "#9d4edd");
        Button btnVoltar = criarBotaoVoltar();

        HBox botoes = new HBox(15, btnVoltar, btnLigar);
        botoes.setAlignment(Pos.CENTER);

        container.getChildren().addAll(titulo, subtitulo, modos, lblMeuIp, formulario, lblStatus, botoes);
        root.getChildren().add(container);

        // Ação ligar
        btnLigar.setOnAction(e -> {
            String nome = campoNome.getText().trim().isEmpty() ? "Jogador" : campoNome.getText().trim();
            int porta = 5000;
            try { porta = Integer.parseInt(campoPorta.getText().trim()); } catch (NumberFormatException ex) {}

            if (rbServidor.isSelected()) {
                // Iniciar servidor e esperar que o outro jogador se junte antes de abrir o tabuleiro
                try {
                    lblStatus.setText("⏳ À espera de ligação em " + ServidorJogo.obterIpLocal() + ":" + porta + "...");
                    btnLigar.setDisable(true);
                    ServidorJogo servidor = new ServidorJogo(msg -> {});
                    servidor.setOnLigado(() -> Platform.runLater(() -> {
                        lblStatus.setText("✅ O outro jogador juntou-se! A iniciar o jogo...");
                        // Só agora, com o outro jogador já ligado, abre o tabuleiro (anfitrião joga como J1)
                        JogoView jogo = new JogoView(stage, nome, "Adversário (remoto)");
                        jogo.setServidor(servidor);
                        jogo.setIdLocal(1);
                        jogo.show();
                    }));
                    servidor.iniciar(porta);
                } catch (Exception ex) {
                    btnLigar.setDisable(false);
                    lblStatus.setText("❌ Erro: " + ex.getMessage());
                }
            } else {
                // Ligar como cliente
                String ip = campoIP.getText().trim();
                try {
                    lblStatus.setText("⏳ A ligar a " + ip + ":" + porta + "...");
                    ClienteJogo cliente = new ClienteJogo(msg ->
                        Platform.runLater(() -> lblStatus.setText("✅ Ligado ao servidor!")));
                    cliente.ligar(ip, porta);
                    // O cliente joga como J2 (o anfitrião é sempre J1, e joga primeiro)
                    JogoView jogo = new JogoView(stage, "Adversário (remoto)", nome);
                    jogo.setCliente(cliente);
                    jogo.setIdLocal(2);
                    jogo.show();
                } catch (Exception ex) {
                    lblStatus.setText("❌ Erro ao ligar: " + ex.getMessage());
                }
            }
        });

        btnVoltar.setOnAction(e -> new MenuView(stage).show());

        stage.setScene(new Scene(root, 900, 600));
    }

    /** Cria um campo de texto com o estilo usado neste ecrã. */
    private TextField criarCampo(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setFont(Font.font("Arial", 14));
        tf.setPrefHeight(40);
        tf.setStyle("-fx-background-color: #1a1540; -fx-text-fill: white; "
                + "-fx-prompt-text-fill: #66628a; -fx-background-radius: 10; "
                + "-fx-border-color: #3a3570; -fx-border-radius: 10; -fx-padding: 6 12;");
        return tf;
    }

    /** Aplica o estilo visual (cor e fonte) a um radio button. */
    private void estilizarRadio(RadioButton rb) {
        rb.setTextFill(Color.web("#d0cce8"));
        rb.setFont(Font.font("Arial", 14));
    }

    /** Cria um botão de ação com gradiente de cor. */
    private Button criarBotaoAcao(String texto, String c1, String c2) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        btn.setTextFill(Color.WHITE);
        btn.setPrefWidth(180);
        btn.setPrefHeight(46);
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setBackground(new Background(new BackgroundFill(
            new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(c1)), new Stop(1, Color.web(c2))),
            new CornerRadii(12), Insets.EMPTY)));
        btn.setBorder(Border.EMPTY);
        return btn;
    }

    /** Cria o botão de voltar ao menu. */
    private Button criarBotaoVoltar() {
        Button btn = new Button("← Voltar");
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        btn.setTextFill(Color.web("#b3b0cb"));
        btn.setPrefWidth(120);
        btn.setPrefHeight(46);
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setBackground(new Background(new BackgroundFill(
            Color.web("#ffffff10"), new CornerRadii(12), Insets.EMPTY)));
        btn.setBorder(new Border(new BorderStroke(
            Color.web("#ffffff20"), BorderStrokeStyle.SOLID, new CornerRadii(12), BorderWidths.DEFAULT)));
        return btn;
    }
}
