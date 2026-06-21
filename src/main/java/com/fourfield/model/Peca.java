package com.fourfield.model;

/**
 * Representa uma peça no tabuleiro do Four Field Kono.
 */
public class Peca {

    private boolean ativa;
    private int linha;
    private int coluna;
    private int jogadorId; // 1 ou 2

    /**
     * Cria uma peça ativa na posição indicada, pertencente ao jogador dado.
     *
     * @param linha linha do tabuleiro onde a peça é colocada
     * @param coluna coluna do tabuleiro onde a peça é colocada
     * @param jogadorId identificador do jogador a quem a peça pertence (1 ou 2)
     */
    public Peca(int linha, int coluna, int jogadorId) {
        this.linha = linha;
        this.coluna = coluna;
        this.jogadorId = jogadorId;
        this.ativa = true;
    }

    /** Move a peça para uma nova posição. */
    public void mover(int novaLinha, int novaColuna) {
        this.linha = novaLinha;
        this.coluna = novaColuna;
    }

    /** Elimina a peça do jogo. */
    public void eliminar() {
        this.ativa = false;
    }

    // Getters e Setters

    /** Diz se a peça ainda está em jogo. */
    public boolean isAtiva() { return ativa; }

    /** Linha onde a peça está. */
    public int getLinha() { return linha; }

    /** Coluna onde a peça está. */
    public int getColuna() { return coluna; }

    /** Id do jogador a quem a peça pertence (1 ou 2). */
    public int getJogadorId() { return jogadorId; }

    /** Texto com o estado da peça, usado para debug. */
    @Override
    public String toString() {
        return "Peca{jogador=" + jogadorId + ", linha=" + linha + ", coluna=" + coluna + ", ativa=" + ativa + "}";
    }
}
