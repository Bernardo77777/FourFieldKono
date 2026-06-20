package com.fourfield.view;

import com.fourfield.model.*;
import com.fourfield.network.ClienteJogo;
import com.fourfield.network.ServidorJogo;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Ecrã principal do jogo Four Field Kono.
 * Contém o tabuleiro gráfico, painel de informação e controlos.
 */
public class JogoView {

    private static final int CELL_SIZE = 90;
    private static final int GRID = 4;

    private final Stage stage;
    private final String nomeJ1;
    private final String nomeJ2;

    private CondicoesJogo jogo;
    private StackPane[][] celulas;

    // Peça selecionada
    private int selLinha = -1;
    private int selColuna = -1;

    // Verdadeiro quando o jogo foi terminado manualmente (botão "Terminar Jogo")
    private boolean jogoEncerrado = false;

    // Banner de turno (destaque no topo do tabuleiro)
    private HBox bannerTurno;
    private Circle bannerIndicador;
    private Label bannerTexto;

    // Labels de informação
    private Label lblTurno;
    private Label lblJogadas;
    private Label lblPecasJ1;
    private Label lblPecasJ2;
    private Label lblCaptJ1;
    private Label lblCaptJ2;
    private Label lblMensagem;

    // Rede (opcional)
    private ServidorJogo servidor;
    private ClienteJogo cliente;
    // Em multiplayer: 1 ou 2, identifica de que lado é este utilizador local.
    // 0 = jogo local (sem rede), sem restrição — os dois jogadores partilham o ecrã.
    private int idLocal = 0;

    /** Cria o ecrã de jogo com os nomes dos dois jogadores. */
    public JogoView(Stage stage, String nomeJ1, String nomeJ2) {
        this.stage = stage;
        this.nomeJ1 = nomeJ1;
        this.nomeJ2 = nomeJ2;
    }

    /** Define o servidor de rede a usar, quando este ecrã é o anfitrião de uma partida multiplayer. */
    public void setServidor(ServidorJogo s) { this.servidor = s; }
    /** Define o cliente de rede a usar, quando este ecrã liga a um servidor remoto. */
    public void setCliente(ClienteJogo c) { this.cliente = c; }
    /** Define o id (1 ou 2) deste jogador local, para restringir que peças pode mover em multiplayer. */
    public void setIdLocal(int id) { this.idLocal = id; }

    /** Monta e mostra o ecrã de jogo (tabuleiro, painéis e banner de turno) e inicia a partida. */
    public void show() {
        jogo = new CondicoesJogo();
        jogo.setNomeJogador1(nomeJ1);
        jogo.setNomeJogador2(nomeJ2);
        jogo.iniciarPartida();

        // --- Layout principal ---
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(
            new RadialGradient(0, 0, 0.5, 0.5, 1, true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#120e3a")),
                new Stop(1, Color.web("#060412"))),
            CornerRadii.EMPTY, Insets.EMPTY)));

        // Topo: banner de turno em destaque
        VBox topoWrap = new VBox(construirBannerTurno());
        topoWrap.setAlignment(Pos.CENTER);
        topoWrap.setPadding(new Insets(24, 0, 0, 0));
        root.setTop(topoWrap);

        // Painel esquerdo: informação
        VBox painelInfo = construirPainelInfo();
        root.setLeft(painelInfo);

        // Centro: tabuleiro
        StackPane centroWrap = new StackPane(construirTabuleiro());
        centroWrap.setPadding(new Insets(30));
        root.setCenter(centroWrap);

        // Painel direito: ações
        VBox painelAcoes = construirPainelAcoes();
        root.setRight(painelAcoes);

        atualizarUI();

        // Ouvir jogadas da rede (a ligação já está ativa; só redireciona o callback)
        if (servidor != null) {
            servidor.setOnMensagemRecebida(msg -> Platform.runLater(() -> processarMensagemRede(msg)));
        }
        if (cliente != null) {
            cliente.setOnMensagemRecebida(msg -> Platform.runLater(() -> processarMensagemRede(msg)));
        }

        stage.setScene(new Scene(root, 900, 680));
    }

    // ===================== BANNER DE TURNO =====================

    /** Cria o banner no topo do ecrã que mostra de quem é o turno. */
    private HBox construirBannerTurno() {
        bannerIndicador = new Circle(9, Color.WHITE);

        bannerTexto = new Label();
        bannerTexto.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        bannerTexto.setTextFill(Color.WHITE);

        bannerTurno = new HBox(12, bannerIndicador, bannerTexto);
        bannerTurno.setAlignment(Pos.CENTER);
        bannerTurno.setPadding(new Insets(12, 28, 12, 28));
        bannerTurno.setBorder(new Border(new BorderStroke(
            Color.web("#ffffff40"), BorderStrokeStyle.SOLID, new CornerRadii(30), BorderWidths.DEFAULT)));
        return bannerTurno;
    }

    /** Atualiza a cor e o texto do banner de turno para o jogador atual. */
    private void atualizarBannerTurno() {
        boolean ehJ1 = getIdAtual() == 1;
        String nomeAtual = jogo.getTurnoAtual().getNome();

        bannerTurno.setBackground(new Background(new BackgroundFill(
            ehJ1
                ? new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#0072ff")), new Stop(1, Color.web("#00c6ff")))
                : new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#6200ea")), new Stop(1, Color.web("#9d4edd"))),
            new CornerRadii(30), Insets.EMPTY)));

        bannerTexto.setText("Vez de " + nomeAtual + "  (J" + getIdAtual() + ")");
    }

    // ===================== TABULEIRO =====================

    /** Cria a grelha 4x4 do tabuleiro, com uma célula clicável por casa. */
    private GridPane construirTabuleiro() {
        GridPane grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(6);
        grid.setAlignment(Pos.CENTER);
        celulas = new StackPane[GRID][GRID];

        for (int l = 0; l < GRID; l++) {
            for (int c = 0; c < GRID; c++) {
                StackPane celula = new StackPane();
                celula.setPrefSize(CELL_SIZE, CELL_SIZE);
                celula.setBackground(new Background(new BackgroundFill(
                    Color.web("#1e1a4a"), new CornerRadii(12), Insets.EMPTY)));
                celula.setBorder(new Border(new BorderStroke(
                    Color.web("#3a3570"), BorderStrokeStyle.SOLID, new CornerRadii(12), BorderWidths.DEFAULT)));
                celulas[l][c] = celula;

                final int fl = l, fc = c;
                celula.setOnMouseClicked(e -> clicarCelula(fl, fc));
                celula.setCursor(javafx.scene.Cursor.HAND);

                grid.add(celula, c, l);
            }
        }
        return grid;
    }

    /** Desenha as peças e as cores de destaque (seleção e destinos possíveis) em todas as casas. */
    private void renderizarTabuleiro() {
        Peca[][] grelha = jogo.getTabuleiro().getGrelha();
        for (int l = 0; l < GRID; l++) {
            for (int c = 0; c < GRID; c++) {
                StackPane celula = celulas[l][c];
                celula.getChildren().clear();

                // Cor de fundo da célula
                boolean selecionada = (l == selLinha && c == selColuna);
                boolean destaque = isDestaque(l, c);

                Color bgCor;
                if (selecionada) bgCor = Color.web("#ffd60099");
                else if (destaque) bgCor = Color.web("#00ff8844");
                else bgCor = Color.web("#1e1a4a");

                celula.setBackground(new Background(new BackgroundFill(bgCor, new CornerRadii(12), Insets.EMPTY)));
                celula.setBorder(new Border(new BorderStroke(
                    selecionada ? Color.web("#ffd600") : Color.web("#3a3570"),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(12),
                    selecionada ? new BorderWidths(4) : BorderWidths.DEFAULT)));

                // Peça
                Peca peca = grelha[l][c];
                if (peca != null && peca.isAtiva()) {
                    Circle circulo = new Circle(selecionada ? 33 : 30);
                    if (peca.getJogadorId() == 1) {
                        circulo.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                            new Stop(0, Color.web("#0072ff")), new Stop(1, Color.web("#00c6ff"))));
                    } else {
                        circulo.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                            new Stop(0, Color.web("#6200ea")), new Stop(1, Color.web("#9d4edd"))));
                    }

                    if (selecionada) {
                        circulo.setStroke(Color.web("#ffd600"));
                        circulo.setStrokeWidth(4);
                        DropShadow brilho = new DropShadow();
                        brilho.setColor(Color.web("#ffd600"));
                        brilho.setRadius(22);
                        brilho.setSpread(0.5);
                        circulo.setEffect(brilho);
                    } else {
                        circulo.setStroke(Color.web("#ffffff30"));
                        circulo.setStrokeWidth(2);
                    }

                    // Letra identificadora
                    Label lblP = new Label("J" + peca.getJogadorId());
                    lblP.setFont(Font.font("Arial", FontWeight.BOLD, 13));
                    lblP.setTextFill(Color.WHITE);

                    celula.getChildren().addAll(circulo, lblP);
                } else if (destaque) {
                    // Marcador de destino
                    Circle marca = new Circle(10, Color.web("#00ff88"));
                    celula.getChildren().add(marca);
                }
            }
        }
    }

    /** Diz se a casa (l, c) é um destino possível para a peça selecionada. */
    private boolean isDestaque(int l, int c) {
        if (selLinha < 0) return false;
        // Movimento simples
        if (jogo.validarMovimentoOrtogonal(selLinha, selColuna, l, c)) return true;
        // Captura
        if (jogo.validarCapturaPorSalto(selLinha, selColuna, l, c)) return true;
        return false;
    }

    // ===================== INTERAÇÃO =====================

    /** Trata o clique numa casa: seleciona uma peça, move-a ou faz uma captura. */
    private void clicarCelula(int l, int c) {
        if (jogo.getVencedor() != null || jogoEncerrado) return;

        Peca pecaClicada = jogo.getTabuleiro().getPeca(l, c);

        if (selLinha < 0) {
            // Selecionar peça própria
            if (ehPecaControlavelLocalmente(pecaClicada)) {
                selLinha = l;
                selColuna = c;
                lblMensagem.setText("Peça selecionada em (" + (l+1) + "," + (c+1) + ")");
            } else if (ehPecaDoAdversarioRemoto(pecaClicada)) {
                lblMensagem.setText("Essas são as peças do adversário. Não as podes mover.");
            }
        } else {
            // Tentar mover ou capturar
            if (jogo.validarMovimentoOrtogonal(selLinha, selColuna, l, c)) {
                enviarJogadaRede("MOV", selLinha, selColuna, l, c);
                jogo.executarMovimento(selLinha, selColuna, l, c);
                limparSelecao();
                atualizarUI();
                verificarVitoria();
            } else if (jogo.validarCapturaPorSalto(selLinha, selColuna, l, c)) {
                enviarJogadaRede("CAP", selLinha, selColuna, l, c);
                jogo.executarCaptura(selLinha, selColuna, l, c);
                limparSelecao();
                atualizarUI();
                verificarVitoria();
            } else {
                // Nova seleção ou deselecionar
                if (ehPecaControlavelLocalmente(pecaClicada)) {
                    selLinha = l;
                    selColuna = c;
                    lblMensagem.setText("Peça selecionada em (" + (l+1) + "," + (c+1) + ")");
                } else if (ehPecaDoAdversarioRemoto(pecaClicada)) {
                    limparSelecao();
                    lblMensagem.setText("Essas são as peças do adversário. Não as podes mover.");
                } else {
                    limparSelecao();
                    lblMensagem.setText("Jogada inválida. Tenta novamente.");
                }
            }
        }
        renderizarTabuleiro();
    }

    /** Verifica se a peça pode ser selecionada por este jogador local (turno + dono em multiplayer). */
    private boolean ehPecaControlavelLocalmente(Peca p) {
        if (p == null || !p.isAtiva() || p.getJogadorId() != getIdAtual()) return false;
        return idLocal == 0 || p.getJogadorId() == idLocal;
    }

    /** Verifica se a peça pertence ao adversário remoto (modo multiplayer). */
    private boolean ehPecaDoAdversarioRemoto(Peca p) {
        return idLocal != 0 && p != null && p.isAtiva() && p.getJogadorId() != idLocal;
    }

    /** Limpa a peça selecionada. */
    private void limparSelecao() {
        selLinha = -1;
        selColuna = -1;
    }

    /** Id (1 ou 2) do jogador a quem pertence o turno atual. */
    private int getIdAtual() {
        return jogo.getTurnoAtual() == jogo.getJogador1() ? 1 : 2;
    }

    /** Verifica se o jogo terminou e, se sim, mostra o vencedor. */
    private void verificarVitoria() {
        if (jogo.verificarCondicaoVitoria()) {
            Jogador v = jogo.getVencedor();
            lblMensagem.setText("🏆 " + v.getNome() + " venceu!");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Fim do Jogo");
            alert.setHeaderText("🏆 Temos um Vencedor!");
            alert.setContentText(v.getNome() + " ganhou o jogo!\n\nJogadas totais: " + jogo.getTotalJogadas());
            alert.showAndWait();
        }
    }

    /** Termina a partida antes do fim normal, depois de confirmação, decidindo o vencedor pelo estado atual. */
    private void terminarJogo() {
        if (jogo.getVencedor() != null || jogoEncerrado) return;

        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION);
        confirmar.setTitle("Terminar Jogo");
        confirmar.setHeaderText("Terminar a partida agora?");
        confirmar.setContentText("O vencedor é definido pelo número de peças (e capturas, em caso de empate) neste momento.");
        confirmar.showAndWait().filter(resp -> resp == ButtonType.OK).ifPresent(resp -> {
            Jogador v = jogo.terminarAntecipadamente();
            jogoEncerrado = true;
            limparSelecao();
            renderizarTabuleiro();

            Alert resultado = new Alert(Alert.AlertType.INFORMATION);
            resultado.setTitle("Fim do Jogo");
            if (v != null) {
                lblMensagem.setText("🏁 " + v.getNome() + " venceu (jogo terminado)!");
                resultado.setHeaderText("🏆 Temos um Vencedor!");
                resultado.setContentText(v.getNome() + " venceu por ter mais peças/capturas no momento em que o jogo terminou.");
            } else {
                lblMensagem.setText("🤝 Jogo terminado em empate.");
                resultado.setHeaderText("🤝 Empate!");
                resultado.setContentText("Ambos os jogadores tinham o mesmo número de peças e capturas.");
            }
            resultado.showAndWait();
        });
    }

    // ===================== UI =====================

    /** Atualiza todos os labels, o banner de turno e o tabuleiro com o estado atual do jogo. */
    private void atualizarUI() {
        Jogador atual = jogo.getTurnoAtual();
        lblTurno.setText("🎮 " + atual.getNome());
        lblTurno.setTextFill(getIdAtual() == 1 ? Color.web("#00c6ff") : Color.web("#9d4edd"));
        lblJogadas.setText(String.valueOf(jogo.getTotalJogadas()));
        lblPecasJ1.setText(String.valueOf(jogo.getJogador1().getPecasAtivas()));
        lblPecasJ2.setText(String.valueOf(jogo.getJogador2().getPecasAtivas()));
        lblCaptJ1.setText(String.valueOf(jogo.getJogador1().getPecasCapturadas()));
        lblCaptJ2.setText(String.valueOf(jogo.getJogador2().getPecasCapturadas()));
        lblMensagem.setText("Turno de " + atual.getNome());
        atualizarBannerTurno();
        renderizarTabuleiro();
    }

    /** Cria o painel da esquerda, com o turno, número de jogadas, peças e capturas. */
    private VBox construirPainelInfo() {
        VBox painel = new VBox(13);
        painel.setPadding(new Insets(20, 20, 20, 30));
        painel.setPrefWidth(210);
        painel.setAlignment(Pos.TOP_LEFT);

        Label titulo = new Label("Four Field Kono");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titulo.setTextFill(Color.WHITE);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #3a3570;");

        lblTurno = criarLabelInfo("Turno atual");
        lblJogadas = criarValorInfo("0");
        lblPecasJ1 = criarValorInfo("8");
        lblPecasJ2 = criarValorInfo("8");
        lblCaptJ1 = criarValorInfo("0");
        lblCaptJ2 = criarValorInfo("0");

        painel.getChildren().addAll(
            titulo, sep,
            criarLabelInfo("Turno"),
            lblTurno,
            criarLabelInfo("Jogadas"),
            lblJogadas,
            criarSeparadorFino(),
            criarLabelInfo("Peças " + nomeJ1),
            lblPecasJ1,
            criarLabelInfo("Peças " + nomeJ2),
            lblPecasJ2,
            criarSeparadorFino(),
            criarLabelInfo("Capturas " + nomeJ1),
            lblCaptJ1,
            criarLabelInfo("Capturas " + nomeJ2),
            lblCaptJ2
        );

        return painel;
    }

    /** Cria o painel da direita, com a mensagem do jogo, os botões de ação e a legenda. */
    private VBox construirPainelAcoes() {
        VBox painel = new VBox(14);
        painel.setPadding(new Insets(30, 30, 30, 20));
        painel.setPrefWidth(190);
        painel.setAlignment(Pos.TOP_CENTER);

        lblMensagem = new Label("Boa sorte!");
        lblMensagem.setWrapText(true);
        lblMensagem.setFont(Font.font("Arial", 13));
        lblMensagem.setTextFill(Color.web("#00c6ff"));
        lblMensagem.setPrefWidth(150);

        Button btnTerminar = criarBotaoAcao("🏁 Terminar Jogo", "#f57c00", "#ffb300");
        Button btnReiniciar = criarBotaoAcao("🔄 Novo Jogo", "#d81b60", "#ff4081");
        Button btnMenu = criarBotaoAcao("🏠 Menu", "#36454f", "#5a7a8a");

        btnTerminar.setOnAction(e -> terminarJogo());
        btnReiniciar.setOnAction(e -> { jogo.iniciarPartida(); limparSelecao(); jogoEncerrado = false; atualizarUI(); });
        btnMenu.setOnAction(e -> new MenuView(stage).show());

        // Legenda cores
        Label legenda = new Label("Legenda:");
        legenda.setTextFill(Color.web("#85829e"));
        legenda.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        HBox legJ1 = criarLegendaItem(Color.web("#0072ff"), nomeJ1 + " (J1)");
        HBox legJ2 = criarLegendaItem(Color.web("#6200ea"), nomeJ2 + " (J2)");
        HBox legDest = criarLegendaItem(Color.web("#00ff88"), "Destino possível");

        painel.getChildren().addAll(
            lblMensagem,
            new Separator(),
            btnTerminar, btnReiniciar, btnMenu,
            new Separator(),
            legenda, legJ1, legJ2, legDest
        );

        return painel;
    }

    /** Cria uma linha da legenda, com uma bolinha de cor e um texto. */
    private HBox criarLegendaItem(Color cor, String texto) {
        Circle c = new Circle(8, cor);
        Label l = new Label(texto);
        l.setTextFill(Color.web("#b3b0cb"));
        l.setFont(Font.font("Arial", 12));
        HBox h = new HBox(8, c, l);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    /** Cria um label pequeno e cinzento, usado como etiqueta no painel de informação. */
    private Label criarLabelInfo(String texto) {
        Label l = new Label(texto);
        l.setFont(Font.font("Arial", 11));
        l.setTextFill(Color.web("#85829e"));
        return l;
    }

    /** Cria um label maior e branco, usado para mostrar um valor no painel de informação. */
    private Label criarValorInfo(String texto) {
        Label l = new Label(texto);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        l.setTextFill(Color.WHITE);
        return l;
    }

    /** Cria um separador fino e escuro, usado entre secções do painel de informação. */
    private Separator criarSeparadorFino() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color: #2a2560;");
        return s;
    }

    /** Cria um botão de ação com gradiente de cor. */
    private Button criarBotaoAcao(String texto, String c1, String c2) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        btn.setPrefWidth(155);
        btn.setPrefHeight(40);
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setBackground(new Background(new BackgroundFill(
            new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(c1)), new Stop(1, Color.web(c2))),
            new CornerRadii(10), Insets.EMPTY)));
        btn.setBorder(Border.EMPTY);
        return btn;
    }

    // ===================== REDE =====================

    /** Envia uma jogada (movimento ou captura) ao adversário, se houver ligação de rede. */
    private void enviarJogadaRede(String tipo, int lO, int cO, int lD, int cD) {
        String msg = tipo + ":" + lO + ":" + cO + ":" + lD + ":" + cD;
        if (servidor != null) servidor.enviar(msg);
        if (cliente != null) cliente.enviar(msg);
    }

    /** Lê uma jogada recebida da rede e aplica-a no jogo local. */
    private void processarMensagemRede(String msg) {
        try {
            String[] p = msg.split(":");
            String tipo = p[0];
            int lO = Integer.parseInt(p[1]);
            int cO = Integer.parseInt(p[2]);
            int lD = Integer.parseInt(p[3]);
            int cD = Integer.parseInt(p[4]);

            if ("MOV".equals(tipo)) jogo.executarMovimento(lO, cO, lD, cD);
            else if ("CAP".equals(tipo)) jogo.executarCaptura(lO, cO, lD, cD);

            atualizarUI();
            verificarVitoria();
        } catch (Exception e) {
            // Mensagem inválida, ignorar
        }
    }
}
