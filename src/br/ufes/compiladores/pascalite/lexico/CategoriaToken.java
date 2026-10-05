package br.ufes.compiladores.pascalite.lexico;

/**
 * Categorias semânticas dos tokens do dialeto PascaLite.
 */
public enum CategoriaToken {
    PALAVRA_RESERVADA("Palavra Reservada"),
    IDENTIFICADOR("Identificador"),
    LITERAL_NUMERICO("Literal Numérico"),
    LITERAL_TEXTO("Literal de Texto"),
    OPERADOR_ARITMETICO("Operador Aritmético"),
    OPERADOR_RELACIONAL("Operador Relacional"),
    OPERADOR_LOGICO("Operador Lógico"),
    OPERADOR_ATRIBUICAO("Operador de Atribuição"),
    DELIMITADOR("Delimitador/Pontuação"),
    COMENTARIO("Comentário"),
    FIM_ARQUIVO("Fim de Arquivo"),
    ERRO("Erro Léxico");

    private final String descricao;

    CategoriaToken(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
