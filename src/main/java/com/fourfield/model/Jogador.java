package com.fourfield.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa um jogador no Four Field Kono.
 */
public class Jogador {

    private String nome;
    private int pecasCapturadas;
    private List<Peca> pecas;

    /** Cria um jogador sem peças e sem capturas. */
    public Jogador(String nome) {
        this.nome = nome;
        this.pecasCapturadas = 0;
        this.pecas = new ArrayList<>();
    }

    /** Adiciona uma peça à lista do jogador. */
    public void adicionarPeca(Peca p) {
        pecas.add(p);
    }

    /** Regista uma captura feita por este jogador. */
    public void adicionarCaptura() {
        pecasCapturadas++;
    }

    /** Devolve o número de peças ainda ativas em jogo. */
    public int getPecasAtivas() {
        int count = 0;
        for (Peca p : pecas) {
            if (p.isAtiva()) count++;
        }
        return count;
    }

    /** Verifica se o jogador tem movimentos possíveis no tabuleiro (simples ou captura por salto). */
    public boolean temMovimentosPossiveis(Tabuleiro tabuleiro) {
        // Direções ortogonais: cima, baixo, esquerda, direita
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        for (Peca p : pecas) {
            if (!p.isAtiva()) continue;
            int meuId = p.getJogadorId();
            for (int[] d : dirs) {
                int nl = p.getLinha() + d[0];
                int nc = p.getColuna() + d[1];

                // Movimento simples para casa adjacente vazia
                if (tabuleiro.dentroDosTabuleiro(nl, nc) && tabuleiro.seCasaVazia(nl, nc)) {
                    return true;
                }

                // Captura por salto: peça aliada na casa intermédia, adversária no destino
                int jl = p.getLinha() + d[0] * 2;
                int jc = p.getColuna() + d[1] * 2;
                if (tabuleiro.dentroDosTabuleiro(jl, jc)) {
                    Peca meio = tabuleiro.getPeca(nl, nc);
                    Peca destino = tabuleiro.getPeca(jl, jc);
                    if (meio != null && meio.isAtiva() && meio.getJogadorId() == meuId
                            && destino != null && destino.isAtiva() && destino.getJogadorId() != meuId) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Getters

    /** Nome do jogador. */
    public String getNome() { return nome; }

    /** Número de peças que este jogador já capturou ao adversário. */
    public int getPecasCapturadas() { return pecasCapturadas; }

    /** Lista de todas as peças do jogador, ativas e eliminadas. */
    public List<Peca> getPecas() { return pecas; }

    /** Muda o nome do jogador. */
    public void setNome(String nome) { this.nome = nome; }
}
