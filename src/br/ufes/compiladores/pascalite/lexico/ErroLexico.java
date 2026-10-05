package br.ufes.compiladores.pascalite.lexico;

/**
 * Representa um erro léxico identificado durante a análise do código fonte.
 */
public class ErroLexico {
    private final int linha;
    private final int coluna;
    private final int indiceInicio;
    private final int tamanho;
    private final String lexemaInvalido;
    private final String mensagem;
    private final String estrategiaRecuperacao;

    public ErroLexico(int linha, int coluna, int indiceInicio, int tamanho, String lexemaInvalido, String mensagem, String estrategiaRecuperacao) {
        this.linha = linha;
        this.coluna = coluna;
        this.indiceInicio = indiceInicio;
        this.tamanho = Math.max(1, tamanho);
        this.lexemaInvalido = lexemaInvalido;
        this.mensagem = mensagem;
        this.estrategiaRecuperacao = estrategiaRecuperacao;
    }

    public int getLinha() {
        return linha;
    }

    public int getColuna() {
        return coluna;
    }

    public int getIndiceInicio() {
        return indiceInicio;
    }

    public int getIndiceFim() {
        return indiceInicio + tamanho;
    }

    public int getTamanho() {
        return tamanho;
    }

    public String getLexemaInvalido() {
        return lexemaInvalido;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getEstrategiaRecuperacao() {
        return estrategiaRecuperacao;
    }

    @Override
    public String toString() {
        return String.format("[Erro Léxico] Linha %d, Coluna %d: %s (Trecho: '%s') - Recuperação: %s",
                linha, coluna, mensagem, lexemaInvalido, estrategiaRecuperacao);
    }
}
