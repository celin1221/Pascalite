package br.ufes.compiladores.pascalite.sintatico;

public class ErroSintatico {
    private final int linha;
    private final int coluna;
    private final int indiceInicio;
    private final int tamanho;
    private final String lexema;
    private final String mensagem;
    private final boolean aviso;

    public ErroSintatico(int linha, int coluna, int indiceInicio, int tamanho, String lexema, String mensagem, boolean aviso) {
        this.linha = linha;
        this.coluna = coluna;
        this.indiceInicio = indiceInicio;
        this.tamanho = Math.max(1, tamanho);
        this.lexema = lexema;
        this.mensagem = mensagem;
        this.aviso = aviso;
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

    public String getLexema() {
        return lexema;
    }

    public String getMensagem() {
        return mensagem;
    }

    public boolean ehAviso() {
        return aviso;
    }

    public String getTipoDescricao() {
        return aviso ? "Warning" : "Erro Sintático";
    }

    @Override
    public String toString() {
        return String.format("[%s] Linha %d, Coluna %d: %s (próximo a '%s')",
                getTipoDescricao(), linha, coluna, mensagem, lexema);
    }
}
