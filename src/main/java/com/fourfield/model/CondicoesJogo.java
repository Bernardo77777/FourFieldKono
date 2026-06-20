package com.fourfield.model;

/**
 * Gere as condições e regras do jogo Four Field Kono.
 * Contém a lógica central: validação de movimentos, capturas e vitória.
 */
public class CondicoesJogo {

    private Jogador jogador1;
    private Jogador jogador2;
    private Tabuleiro tabuleiro;
    private Jogador turnoAtual;
    private int totalJogadas;
    private Jogador vencedor;

    /** Cria um novo jogo, com os dois jogadores e o tabuleiro ainda vazios. */
    public CondicoesJogo() {
        jogador1 = new Jogador("Jogador 1");
        jogador2 = new Jogador("Jogador 2");
        tabuleiro = new Tabuleiro();
        totalJogadas = 0;
        vencedor = null;
    }

    /** Inicia uma nova partida, posicionando as peças. */
    public void iniciarPartida() {
        totalJogadas = 0;
        vencedor = null;
        jogador1 = new Jogador(jogador1.getNome());
        jogador2 = new Jogador(jogador2.getNome());
        tabuleiro = new Tabuleiro();
        tabuleiro.inicializarPosicoesIniciais(jogador1, jogador2);
        turnoAtual = jogador1;
    }

    /**
     * Valida um movimento simples (ortogonal para casa vazia).
     * @param linhaOrig linha da peça que se quer mover
     * @param colOrig coluna da peça que se quer mover
     * @param linhaD linha de destino
     * @param colD coluna de destino
     * @return true se o movimento é válido
     */
    public boolean validarMovimentoOrtogonal(int linhaOrig, int colOrig, int linhaD, int colD) {
        // A peça origem deve pertencer ao jogador atual
        Peca peca = tabuleiro.getPeca(linhaOrig, colOrig);
        if (peca == null || !peca.isAtiva()) return false;
        if (peca.getJogadorId() != getIdJogadorAtual()) return false;

        // Destino dentro do tabuleiro
        if (!tabuleiro.dentroDosTabuleiro(linhaD, colD)) return false;

        // Destino deve estar vazio
        if (!tabuleiro.seCasaVazia(linhaD, colD)) return false;

        // Movimento ortogonal de exatamente 1 casa
        int dLinha = Math.abs(linhaD - linhaOrig);
        int dCol = Math.abs(colD - colOrig);
        return (dLinha == 1 && dCol == 0) || (dLinha == 0 && dCol == 1);
    }

    /**
     * Valida uma captura por salto.
     * O jogador salta sobre uma peça aliada para capturar uma peça inimiga.
     * @param linhaOrig linha da peça que ataca
     * @param colOrig coluna da peça que ataca
     * @param linhaD linha de destino (onde está a peça a capturar)
     * @param colD coluna de destino (onde está a peça a capturar)
     * @return true se a captura é válida
     */
    public boolean validarCapturaPorSalto(int linhaOrig, int colOrig, int linhaD, int colD) {
        Peca pecaAtacante = tabuleiro.getPeca(linhaOrig, colOrig);
        if (pecaAtacante == null || !pecaAtacante.isAtiva()) return false;
        if (pecaAtacante.getJogadorId() != getIdJogadorAtual()) return false;

        // Destino dentro do tabuleiro
        if (!tabuleiro.dentroDosTabuleiro(linhaD, colD)) return false;

        // Movimento de exatamente 2 casas em linha reta
        int dLinha = linhaD - linhaOrig;
        int dCol = colD - colOrig;
        if (!((Math.abs(dLinha) == 2 && dCol == 0) || (dLinha == 0 && Math.abs(dCol) == 2))) return false;

        // Casa intermédia: deve ter uma peça aliada
        int lMeio = linhaOrig + dLinha / 2;
        int cMeio = colOrig + dCol / 2;
        Peca pecaMeio = tabuleiro.getPeca(lMeio, cMeio);
        if (pecaMeio == null || !pecaMeio.isAtiva()) return false;
        if (pecaMeio.getJogadorId() != getIdJogadorAtual()) return false;

        // Destino: deve ter uma peça adversária
        Peca pecaDestino = tabuleiro.getPeca(linhaD, colD);
        if (pecaDestino == null || !pecaDestino.isAtiva()) return false;
        if (pecaDestino.getJogadorId() == getIdJogadorAtual()) return false;

        return true;
    }

    /**
     * Executa um movimento simples (depois de validado) e passa o turno ao outro jogador.
     * @param linhaOrig linha de origem
     * @param colOrig coluna de origem
     * @param linhaD linha de destino
     * @param colD coluna de destino
     */
    public void executarMovimento(int linhaOrig, int colOrig, int linhaD, int colD) {
        Peca peca = tabuleiro.getPeca(linhaOrig, colOrig);
        tabuleiro.removerPeca(linhaOrig, colOrig);
        peca.mover(linhaD, colD);
        tabuleiro.recebePecaNaCasa(linhaD, colD, peca);
        totalJogadas++;
        alterarTurno();
    }

    /**
     * Executa uma captura por salto (depois de validada) e passa o turno ao outro jogador.
     * @param linhaOrig linha de origem (peça atacante)
     * @param colOrig coluna de origem (peça atacante)
     * @param linhaD linha de destino (peça capturada)
     * @param colD coluna de destino (peça capturada)
     */
    public void executarCaptura(int linhaOrig, int colOrig, int linhaD, int colD) {
        int dLinha = linhaD - linhaOrig;
        int dCol = colD - colOrig;
        int lMeio = linhaOrig + dLinha / 2;
        int cMeio = colOrig + dCol / 2;

        // Capturar peça do destino
        Peca pecaCapturada = tabuleiro.getPeca(linhaD, colD);
        pecaCapturada.eliminar();
        tabuleiro.removerPeca(linhaD, colD);
        turnoAtual.adicionarCaptura();

        // Mover atacante para o destino
        Peca peca = tabuleiro.getPeca(linhaOrig, colOrig);
        tabuleiro.removerPeca(linhaOrig, colOrig);
        peca.mover(linhaD, colD);
        tabuleiro.recebePecaNaCasa(linhaD, colD, peca);

        totalJogadas++;
        alterarTurno();
    }

    /** Verifica se há uma condição de vitória após a última jogada. */
    public boolean verificarCondicaoVitoria() {
        // O turno já foi alternado: turnoAtual é quem vai jogar a seguir,
        // ou seja, é o adversário de quem acabou de jogar.
        Jogador quemJogou = (turnoAtual == jogador1) ? jogador2 : jogador1;

        // Condição 1: o jogador a seguir ficou com 1 peça (ou menos)
        if (turnoAtual.getPecasAtivas() <= 1) {
            vencedor = quemJogou;
            return true;
        }

        // Condição 2: o jogador a seguir não tem movimentos possíveis
        if (!turnoAtual.temMovimentosPossiveis(tabuleiro)) {
            vencedor = quemJogou;
            return true;
        }

        return false;
    }

    /**
     * Termina a partida de forma antecipada (sem aguardar pelas condições normais de vitória).
     * O vencedor é determinado pelo número de peças ativas e, em caso de empate, pelo número de capturas.
     * @return o vencedor, ou null em caso de empate total
     */
    public Jogador terminarAntecipadamente() {
        int p1 = jogador1.getPecasAtivas();
        int p2 = jogador2.getPecasAtivas();

        if (p1 > p2) {
            vencedor = jogador1;
        } else if (p2 > p1) {
            vencedor = jogador2;
        } else {
            int c1 = jogador1.getPecasCapturadas();
            int c2 = jogador2.getPecasCapturadas();
            if (c1 > c2) vencedor = jogador1;
            else if (c2 > c1) vencedor = jogador2;
            else vencedor = null;
        }

        return vencedor;
    }

    /** Alterna o turno entre os dois jogadores. */
    public void alterarTurno() {
        turnoAtual = (turnoAtual == jogador1) ? jogador2 : jogador1;
    }

    // --- Getters ---

    /** Jogador 1. */
    public Jogador getJogador1() { return jogador1; }

    /** Jogador 2. */
    public Jogador getJogador2() { return jogador2; }

    /** Tabuleiro da partida atual. */
    public Tabuleiro getTabuleiro() { return tabuleiro; }

    /** Jogador a quem pertence o turno atual. */
    public Jogador getTurnoAtual() { return turnoAtual; }

    /** Número de jogadas feitas até agora. */
    public int getTotalJogadas() { return totalJogadas; }

    /** Vencedor da partida, ou null se ainda não houver. */
    public Jogador getVencedor() { return vencedor; }

    /** Muda o nome do jogador 1. */
    public void setNomeJogador1(String nome) { jogador1.setNome(nome); }

    /** Muda o nome do jogador 2. */
    public void setNomeJogador2(String nome) { jogador2.setNome(nome); }

    /** Id (1 ou 2) do jogador a quem pertence o turno atual. */
    private int getIdJogadorAtual() {
        return (turnoAtual == jogador1) ? 1 : 2;
    }
}
