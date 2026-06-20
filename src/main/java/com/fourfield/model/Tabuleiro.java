package com.fourfield.model;

/**
 * Representa o tabuleiro 4x4 do Four Field Kono.
 */
public class Tabuleiro {

    /** Número de linhas do tabuleiro. */
    public static final int TOTAL_LINHAS = 4;
    /** Número de colunas do tabuleiro. */
    public static final int TOTAL_COLUNAS = 4;

    // Grelha: null = vazio, caso contrário contém a peça
    private Peca[][] grelha;

    /** Cria um tabuleiro vazio. */
    public Tabuleiro() {
        grelha = new Peca[TOTAL_LINHAS][TOTAL_COLUNAS];
    }

    /** Inicializa as posições de início do jogo. */
    public void inicializarPosicoesIniciais(Jogador j1, Jogador j2) {
        // Limpa a grelha
        for (int l = 0; l < TOTAL_LINHAS; l++)
            for (int c = 0; c < TOTAL_COLUNAS; c++)
                grelha[l][c] = null;

        j1.getPecas().clear();
        j2.getPecas().clear();

        // J2 ocupa filas 0 e 1 (topo)
        for (int l = 0; l < 2; l++) {
            for (int c = 0; c < TOTAL_COLUNAS; c++) {
                Peca p = new Peca(l, c, 2);
                grelha[l][c] = p;
                j2.adicionarPeca(p);
            }
        }

        // J1 ocupa filas 2 e 3 (base)
        for (int l = 2; l < TOTAL_LINHAS; l++) {
            for (int c = 0; c < TOTAL_COLUNAS; c++) {
                Peca p = new Peca(l, c, 1);
                grelha[l][c] = p;
                j1.adicionarPeca(p);
            }
        }
    }

    /** Verifica se uma casa está dentro dos limites. */
    public boolean dentroDosTabuleiro(int linha, int coluna) {
        return linha >= 0 && linha < TOTAL_LINHAS && coluna >= 0 && coluna < TOTAL_COLUNAS;
    }

    /** Verifica se uma casa está vazia. */
    public boolean seCasaVazia(int linha, int coluna) {
        return grelha[linha][coluna] == null;
    }

    /** Obtém a peça numa determinada casa. */
    public Peca getPeca(int linha, int coluna) {
        return grelha[linha][coluna];
    }

    /** Coloca uma peça numa casa. */
    public void recebePecaNaCasa(int linha, int coluna, Peca peca) {
        grelha[linha][coluna] = peca;
    }

    /** Remove a peça de uma casa. */
    public void removerPeca(int linha, int coluna) {
        grelha[linha][coluna] = null;
    }

    /** Obtém a grelha completa (para renderização). */
    public Peca[][] getGrelha() {
        return grelha;
    }
}
