package br.ufes.compiladores.pascalite.lexico;

/**
 * Representa um token léxico identificado pelo compilador PascaLite.
 */
public class Token {
    private final TipoToken tipo;
    private final String lexema;
    private final int linha;
    private final int coluna;
    private final int indiceInicio;
    private final int indiceFim;
    private final Object valorAtributo;

    public Token(TipoToken tipo, String lexema, int linha, int coluna, int indiceInicio, int indiceFim, Object valorAtributo) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linha = linha;
        this.coluna = coluna;
        this.indiceInicio = indiceInicio;
        this.indiceFim = indiceFim;
        this.valorAtributo = valorAtributo;
    }

    public Token(TipoToken tipo, String lexema, int linha, int coluna, int indiceInicio, int indiceFim) {
        this(tipo, lexema, linha, coluna, indiceInicio, indiceFim, null);
    }

    public TipoToken getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
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
        return indiceFim;
    }

    public int getTamanho() {
        return indiceFim - indiceInicio;
    }

    public Object getValorAtributo() {
        return valorAtributo;
    }

    @Override
    public String toString() {
        String atributo = (valorAtributo != null) ? " (Valor: " + valorAtributo + ")" : "";
        return String.format("<%s, '%s'%s> na Linha %d, Coluna %d",
                tipo.name(), lexema, atributo, linha, coluna);
    }
}
