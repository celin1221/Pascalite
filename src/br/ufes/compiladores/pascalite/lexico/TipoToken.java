package br.ufes.compiladores.pascalite.lexico;

/**
 * Tipos de tokens reconhecidos pelo analisador léxico do dialeto PascaLite.
 */
public enum TipoToken {
    // Palavras reservadas da gramática formal (Anexo I)
    PR_PROGRAM("PROGRAM", CategoriaToken.PALAVRA_RESERVADA),
    PR_BEGIN("BEGIN", CategoriaToken.PALAVRA_RESERVADA),
    PR_END("END", CategoriaToken.PALAVRA_RESERVADA),
    PR_CONST("CONST", CategoriaToken.PALAVRA_RESERVADA),
    PR_VAR("VAR", CategoriaToken.PALAVRA_RESERVADA),
    PR_INTEGER("INTEGER", CategoriaToken.PALAVRA_RESERVADA),
    PR_REAL("REAL", CategoriaToken.PALAVRA_RESERVADA),
    PR_CHAR("CHAR", CategoriaToken.PALAVRA_RESERVADA),
    PR_STRING("STRING", CategoriaToken.PALAVRA_RESERVADA),
    PR_PROCEDURE("PROCEDURE", CategoriaToken.PALAVRA_RESERVADA),
    PR_FUNCTION("FUNCTION", CategoriaToken.PALAVRA_RESERVADA),
    PR_IF("IF", CategoriaToken.PALAVRA_RESERVADA),
    PR_THEN("THEN", CategoriaToken.PALAVRA_RESERVADA),
    PR_ELSE("ELSE", CategoriaToken.PALAVRA_RESERVADA),
    PR_WHILE("WHILE", CategoriaToken.PALAVRA_RESERVADA),
    PR_DO("DO", CategoriaToken.PALAVRA_RESERVADA),
    PR_REPEAT("REPEAT", CategoriaToken.PALAVRA_RESERVADA),
    PR_UNTIL("UNTIL", CategoriaToken.PALAVRA_RESERVADA),
    PR_BREAK("BREAK", CategoriaToken.PALAVRA_RESERVADA),
    PR_CONTINUE("CONTINUE", CategoriaToken.PALAVRA_RESERVADA),

    // Operadores lógicos da gramática
    PR_OU("OU", CategoriaToken.OPERADOR_LOGICO),
    PR_E("E", CategoriaToken.OPERADOR_LOGICO),

    // Extensões e observações (laço FOR e tipos RECORD/ENUM)
    PR_FOR("FOR", CategoriaToken.PALAVRA_RESERVADA),
    PR_TO("TO", CategoriaToken.PALAVRA_RESERVADA),
    PR_DOWNTO("DOWNTO", CategoriaToken.PALAVRA_RESERVADA),
    PR_RECORD("RECORD", CategoriaToken.PALAVRA_RESERVADA),
    PR_ENUM("ENUM", CategoriaToken.PALAVRA_RESERVADA),
    PR_TYPE("TYPE", CategoriaToken.PALAVRA_RESERVADA),
    PR_OF("OF", CategoriaToken.PALAVRA_RESERVADA),

    // Identificador
    IDENTIFICADOR("ID", CategoriaToken.IDENTIFICADOR),

    // Literais numéricos e texto
    NUMERO_INTEIRO("NUM_INT", CategoriaToken.LITERAL_NUMERICO),
    NUMERO_REAL("NUM_REAL", CategoriaToken.LITERAL_NUMERICO),
    LITERAL_TEXTO("LITERAL", CategoriaToken.LITERAL_TEXTO),

    // Operador de Atribuição
    OP_ATRIBUICAO(":=", CategoriaToken.OPERADOR_ATRIBUICAO),

    // Operadores Aritméticos
    OP_SOMA("+", CategoriaToken.OPERADOR_ARITMETICO),
    OP_SUBTRACAO("-", CategoriaToken.OPERADOR_ARITMETICO),
    OP_MULTIPLICACAO("*", CategoriaToken.OPERADOR_ARITMETICO),
    OP_DIVISAO("/", CategoriaToken.OPERADOR_ARITMETICO),

    // Operadores Relacionais
    OP_IGUAL("=", CategoriaToken.OPERADOR_RELACIONAL),
    OP_DIFERENTE("<>", CategoriaToken.OPERADOR_RELACIONAL),
    OP_MENOR("<", CategoriaToken.OPERADOR_RELACIONAL),
    OP_MENOR_IGUAL("<=", CategoriaToken.OPERADOR_RELACIONAL),
    OP_MAIOR(">", CategoriaToken.OPERADOR_RELACIONAL),
    OP_MAIOR_IGUAL(">=", CategoriaToken.OPERADOR_RELACIONAL),

    // Delimitadores e Sinais de Pontuação
    PONTO_E_VIRGULA(";", CategoriaToken.DELIMITADOR),
    VIRGULA(",", CategoriaToken.DELIMITADOR),
    DOIS_PONTOS(":", CategoriaToken.DELIMITADOR),
    PONTO_FINAL(".", CategoriaToken.DELIMITADOR),
    ABRE_PARENTESES("(", CategoriaToken.DELIMITADOR),
    FECHA_PARENTESES(")", CategoriaToken.DELIMITADOR),
    ABRE_COLCHETES("[", CategoriaToken.DELIMITADOR),
    FECHA_COLCHETES("]", CategoriaToken.DELIMITADOR),

    // Tokens Especiais
    COMENTARIO("COMENTARIO", CategoriaToken.COMENTARIO),
    FIM_DE_ARQUIVO("EOF", CategoriaToken.FIM_ARQUIVO),
    ERRO("ERRO", CategoriaToken.ERRO);

    private final String representacao;
    private final CategoriaToken categoria;

    TipoToken(String representacao, CategoriaToken categoria) {
        this.representacao = representacao;
        this.categoria = categoria;
    }

    public String getRepresentacao() {
        return representacao;
    }

    public CategoriaToken getCategoria() {
        return categoria;
    }

    public boolean ehPalavraReservada() {
        return categoria == CategoriaToken.PALAVRA_RESERVADA || categoria == CategoriaToken.OPERADOR_LOGICO;
    }
}
