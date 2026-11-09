package br.ufes.compiladores.pascalite.lexico;

import java.util.*;

public class TabelaSimbolos {
    private final Map<String, EntradaSimbolo> tabela;

    public TabelaSimbolos() {
        this.tabela = new LinkedHashMap<>();
        inicializarSimbolosPredefinidos();
    }

    private void inicializarSimbolosPredefinidos() {
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

        adicionarPredefinido("OU", TipoToken.PR_OU);
        adicionarPredefinido("E", TipoToken.PR_E);

        adicionarPredefinido("FOR", TipoToken.PR_FOR);
        adicionarPredefinido("TO", TipoToken.PR_TO);
        adicionarPredefinido("DOWNTO", TipoToken.PR_DOWNTO);
        adicionarPredefinido("RECORD", TipoToken.PR_RECORD);
        adicionarPredefinido("ENUM", TipoToken.PR_ENUM);
        adicionarPredefinido("TYPE", TipoToken.PR_TYPE);
        adicionarPredefinido("OF", TipoToken.PR_OF);

        adicionarPredefinido(":=", TipoToken.OP_ATRIBUICAO);

        adicionarPredefinido("+", TipoToken.OP_SOMA);
        adicionarPredefinido("-", TipoToken.OP_SUBTRACAO);
        adicionarPredefinido("*", TipoToken.OP_MULTIPLICACAO);
        adicionarPredefinido("/", TipoToken.OP_DIVISAO);

        adicionarPredefinido("=", TipoToken.OP_IGUAL);
        adicionarPredefinido("<>", TipoToken.OP_DIFERENTE);
        adicionarPredefinido("<", TipoToken.OP_MENOR);
        adicionarPredefinido("<=", TipoToken.OP_MENOR_IGUAL);
        adicionarPredefinido(">", TipoToken.OP_MAIOR);
        adicionarPredefinido(">=", TipoToken.OP_MAIOR_IGUAL);

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

    public static String normalizarChave(String lexema) {
        if (lexema == null) return "";
        return lexema.trim().toUpperCase(Locale.ROOT);
    }

    public TipoToken buscarPalavraChave(String lexema) {
        String chave = normalizarChave(lexema);
        EntradaSimbolo entrada = tabela.get(chave);
        if (entrada != null && entrada.getTipoToken().ehPalavraReservada()) {
            return entrada.getTipoToken();
        }
        return null;
    }

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

    public void registrarUso(String lexema, int linha) {
        String chave = normalizarChave(lexema);
        EntradaSimbolo entrada = tabela.get(chave);
        if (entrada != null) {
            entrada.adicionarOcorrencia(linha);
        }
    }

    public EntradaSimbolo obter(String lexema) {
        return tabela.get(normalizarChave(lexema));
    }

    public Collection<EntradaSimbolo> obterTodosSimbolos() {
        return Collections.unmodifiableCollection(tabela.values());
    }

    public List<EntradaSimbolo> obterSimbolosUsuario() {
        List<EntradaSimbolo> lista = new ArrayList<>();
        for (EntradaSimbolo entrada : tabela.values()) {
            if (!entrada.ehPredefinido()) {
                lista.add(entrada);
            }
        }
        return lista;
    }

    public List<EntradaSimbolo> obterSimbolosPredefinidos() {
        List<EntradaSimbolo> lista = new ArrayList<>();
        for (EntradaSimbolo entrada : tabela.values()) {
            if (entrada.ehPredefinido()) {
                lista.add(entrada);
            }
        }
        return lista;
    }

    public void reiniciar() {
        tabela.clear();
        inicializarSimbolosPredefinidos();
    }
}
