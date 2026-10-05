package br.ufes.compiladores.pascalite.lexico;

import java.util.*;

/**
 * Tabela de Símbolos do compilador PascaLite.
 * Armazena previamente todos os tokens conhecidos (palavras reservadas, operadores, pontuações)
 * e registra dinamicamente os identificadores encontrados no programa objeto.
 * Suporta case-insensitivity conforme exigido pela especificação.
 */
public class TabelaSimbolos {
    private final Map<String, EntradaSimbolo> tabela;

    public TabelaSimbolos() {
        this.tabela = new LinkedHashMap<>();
        inicializarSimbolosPredefinidos();
    }

    /**
     * Inicializa a tabela de símbolos com todos os tokens conhecidos antecipadamente.
     */
    private void inicializarSimbolosPredefinidos() {
        // 1. Palavras Reservadas da Gramática (Anexo I) e Extensões
        adicionarPredefinido("PROGRAM", TipoToken.PR_PROGRAM);
        adicionarPredefinido("BEGIN", TipoToken.PR_BEGIN);
        adicionarPredefinido("END", TipoToken.PR_END);
        adicionarPredefinido("CONST", TipoToken.PR_CONST);
        adicionarPredefinido("VAR", TipoToken.PR_VAR);
        adicionarPredefinido("INTEGER", TipoToken.PR_INTEGER);
        adicionarPredefinido("REAL", TipoToken.PR_REAL);
        adicionarPredefinido("CHAR", TipoToken.PR_CHAR);
        adicionarPredefinido("STRING", TipoToken.PR_STRING);
        adicionarPredefinido("PROCEDURE", TipoToken.PR_PROCEDURE);
        adicionarPredefinido("FUNCTION", TipoToken.PR_FUNCTION);
        adicionarPredefinido("IF", TipoToken.PR_IF);
        adicionarPredefinido("THEN", TipoToken.PR_THEN);
        adicionarPredefinido("ELSE", TipoToken.PR_ELSE);
        adicionarPredefinido("WHILE", TipoToken.PR_WHILE);
        adicionarPredefinido("DO", TipoToken.PR_DO);
        adicionarPredefinido("REPEAT", TipoToken.PR_REPEAT);
        adicionarPredefinido("UNTIL", TipoToken.PR_UNTIL);
        adicionarPredefinido("BREAK", TipoToken.PR_BREAK);
        adicionarPredefinido("CONTINUE", TipoToken.PR_CONTINUE);

        // Operadores Lógicos
        adicionarPredefinido("OU", TipoToken.PR_OU);
        adicionarPredefinido("E", TipoToken.PR_E);

        // Palavras reservadas adicionais / extensões
        adicionarPredefinido("FOR", TipoToken.PR_FOR);
        adicionarPredefinido("TO", TipoToken.PR_TO);
        adicionarPredefinido("DOWNTO", TipoToken.PR_DOWNTO);
        adicionarPredefinido("RECORD", TipoToken.PR_RECORD);
        adicionarPredefinido("ENUM", TipoToken.PR_ENUM);
        adicionarPredefinido("TYPE", TipoToken.PR_TYPE);
        adicionarPredefinido("OF", TipoToken.PR_OF);

        // 2. Operador de Atribuição
        adicionarPredefinido(":=", TipoToken.OP_ATRIBUICAO);

        // 3. Operadores Aritméticos
        adicionarPredefinido("+", TipoToken.OP_SOMA);
        adicionarPredefinido("-", TipoToken.OP_SUBTRACAO);
        adicionarPredefinido("*", TipoToken.OP_MULTIPLICACAO);
        adicionarPredefinido("/", TipoToken.OP_DIVISAO);

        // 4. Operadores Relacionais
        adicionarPredefinido("=", TipoToken.OP_IGUAL);
        adicionarPredefinido("<>", TipoToken.OP_DIFERENTE);
        adicionarPredefinido("<", TipoToken.OP_MENOR);
        adicionarPredefinido("<=", TipoToken.OP_MENOR_IGUAL);
        adicionarPredefinido(">", TipoToken.OP_MAIOR);
        adicionarPredefinido(">=", TipoToken.OP_MAIOR_IGUAL);

        // 5. Delimitadores e Sinais de Pontuação
        adicionarPredefinido(";", TipoToken.PONTO_E_VIRGULA);
        adicionarPredefinido(",", TipoToken.VIRGULA);
        adicionarPredefinido(":", TipoToken.DOIS_PONTOS);
        adicionarPredefinido(".", TipoToken.PONTO_FINAL);
        adicionarPredefinido("(", TipoToken.ABRE_PARENTESES);
        adicionarPredefinido(")", TipoToken.FECHA_PARENTESES);
        adicionarPredefinido("[", TipoToken.ABRE_COLCHETES);
        adicionarPredefinido("]", TipoToken.FECHA_COLCHETES);
    }

    private void adicionarPredefinido(String lexema, TipoToken tipo) {
        String chave = normalizarChave(lexema);
        tabela.put(chave, new EntradaSimbolo(chave, lexema, tipo, tipo.getCategoria(), true));
    }

    /**
     * Normaliza a chave para comparação case-insensitive.
     */
    public static String normalizarChave(String lexema) {
        if (lexema == null) return "";
        return lexema.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Verifica se o lexema corresponde a uma palavra reservada pré-definida.
     */
    public TipoToken buscarPalavraChave(String lexema) {
        String chave = normalizarChave(lexema);
        EntradaSimbolo entrada = tabela.get(chave);
        if (entrada != null && entrada.getTipoToken().ehPalavraReservada()) {
            return entrada.getTipoToken();
        }
        return null;
    }

    /**
     * Registra ou atualiza um identificador encontrado no programa objeto.
     */
    public EntradaSimbolo registrarIdentificador(String lexema, int linha) {
        String chave = normalizarChave(lexema);
        EntradaSimbolo entrada = tabela.get(chave);
        if (entrada == null) {
            entrada = new EntradaSimbolo(chave, lexema, TipoToken.IDENTIFICADOR, CategoriaToken.IDENTIFICADOR, false);
            tabela.put(chave, entrada);
        }
        entrada.adicionarOcorrencia(linha);
        return entrada;
    }

    /**
     * Registra o uso de qualquer token pré-definido (para contabilizar ocorrências de palavras-chave, operadores, etc).
     */
    public void registrarUso(String lexema, int linha) {
        String chave = normalizarChave(lexema);
        EntradaSimbolo entrada = tabela.get(chave);
        if (entrada != null) {
            entrada.adicionarOcorrencia(linha);
        }
    }

    /**
     * Retorna a entrada associada à chave normalizada.
     */
    public EntradaSimbolo obter(String lexema) {
        return tabela.get(normalizarChave(lexema));
    }

    /**
     * Retorna todos os símbolos contidos na tabela.
     */
    public Collection<EntradaSimbolo> obterTodosSimbolos() {
        return Collections.unmodifiableCollection(tabela.values());
    }

    /**
     * Retorna os símbolos definidos pelo usuário (identificadores do programa objeto).
     */
    public List<EntradaSimbolo> obterSimbolosUsuario() {
        List<EntradaSimbolo> lista = new ArrayList<>();
        for (EntradaSimbolo entrada : tabela.values()) {
            if (!entrada.ehPredefinido()) {
                lista.add(entrada);
            }
        }
        return lista;
    }

    /**
     * Retorna os símbolos pré-definidos (palavras reservadas, operadores, pontuação).
     */
    public List<EntradaSimbolo> obterSimbolosPredefinidos() {
        List<EntradaSimbolo> lista = new ArrayList<>();
        for (EntradaSimbolo entrada : tabela.values()) {
            if (entrada.ehPredefinido()) {
                lista.add(entrada);
            }
        }
        return lista;
    }

    /**
     * Reinicializa a tabela para uma nova compilação, mantendo os pré-definidos e limpando identificadores.
     */
    public void reiniciar() {
        tabela.clear();
        inicializarSimbolosPredefinidos();
    }
}
