package br.ufes.compiladores.pascalite.lexico;

import java.util.ArrayList;
import java.util.List;

/**
 * Analisador Léxico para o dialeto PascaLite (dialeto da linguagem Pascal).
 * Implementa máquina de estados finitos determinística com:
 * - Insensibilidade a maiúsculas e minúsculas (case-insensitive)
 * - Identificadores de até 15 caracteres (com erro léxico se exceder)
 * - Números inteiros e reais
 * - Literais de texto delimitados por aspas simples com escape ('')
 * - Comentários de linha (//) e bloco ({}, (* *)) removidos do fluxo
 * - Recuperação de erros por Modo Pânico (descarte e sincronização)
 * - Integração completa com a Tabela de Símbolos
 */
public class AnalisadorLexico {
    private final String codigoFonte;
    private final int tamanhoFonte;
    private final TabelaSimbolos tabelaSimbolos;
    private final List<Token> tokens;
    private final List<ErroLexico> erros;

    private int indiceAtual;
    private int linhaAtual;
    private int colunaAtual;

    public AnalisadorLexico(String codigoFonte, TabelaSimbolos tabelaSimbolos) {
        this.codigoFonte = (codigoFonte != null) ? codigoFonte : "";
        this.tamanhoFonte = this.codigoFonte.length();
        this.tabelaSimbolos = (tabelaSimbolos != null) ? tabelaSimbolos : new TabelaSimbolos();
        this.tokens = new ArrayList<>();
        this.erros = new ArrayList<>();
        this.indiceAtual = 0;
        this.linhaAtual = 1;
        this.colunaAtual = 1;
    }

    public AnalisadorLexico(String codigoFonte) {
        this(codigoFonte, new TabelaSimbolos());
    }

    /**
     * Executa a análise léxica completa de todo o código fonte.
     */
    public List<Token> analisar() {
        tokens.clear();
        erros.clear();
        indiceAtual = 0;
        linhaAtual = 1;
        colunaAtual = 1;

        while (!ehFim()) {
            pularEspacosEmBranco();
            if (ehFim()) break;

            int inicioToken = indiceAtual;
            int linhaToken = linhaAtual;
            int colunaToken = colunaAtual;

            char c = olharAtual();

            // 1. Comentários (devem ser removidos do fluxo)
            if (c == '/' && olharProximo() == '/') {
                tratarComentarioLinha();
                continue;
            } else if (c == '{') {
                tratarComentarioChaves(inicioToken, linhaToken, colunaToken);
                continue;
            } else if (c == '(' && olharProximo() == '*') {
                tratarComentarioParentesesAsterisco(inicioToken, linhaToken, colunaToken);
                continue;
            }

            // 2. Identificadores ou Palavras Reservadas
            if (ehLetra(c)) {
                analisarIdentificadorOuPalavraChave(inicioToken, linhaToken, colunaToken);
            }
            // 3. Literais Numéricos (Inteiro ou Real)
            else if (ehDigito(c)) {
                analisarNumero(inicioToken, linhaToken, colunaToken);
            }
            // 4. Literais de Texto (Strings)
            else if (c == '\'') {
                analisarLiteralTexto(inicioToken, linhaToken, colunaToken);
            }
            // 5. Operador de Atribuição ou Dois Pontos
            else if (c == ':') {
                avancar();
                if (combinar('=')) {
                    adicionarToken(TipoToken.OP_ATRIBUICAO, ":=", linhaToken, colunaToken, inicioToken, indiceAtual, null);
                } else {
                    adicionarToken(TipoToken.DOIS_PONTOS, ":", linhaToken, colunaToken, inicioToken, indiceAtual, null);
                }
            }
            // 6. Operadores Relacionais (<, <=, <>, >, >=, =)
            else if (c == '<') {
                avancar();
                if (combinar('=')) {
                    adicionarToken(TipoToken.OP_MENOR_IGUAL, "<=", linhaToken, colunaToken, inicioToken, indiceAtual, null);
                } else if (combinar('>')) {
                    adicionarToken(TipoToken.OP_DIFERENTE, "<>", linhaToken, colunaToken, inicioToken, indiceAtual, null);
                } else {
                    adicionarToken(TipoToken.OP_MENOR, "<", linhaToken, colunaToken, inicioToken, indiceAtual, null);
                }
            } else if (c == '>') {
                avancar();
                if (combinar('=')) {
                    adicionarToken(TipoToken.OP_MAIOR_IGUAL, ">=", linhaToken, colunaToken, inicioToken, indiceAtual, null);
                } else {
                    adicionarToken(TipoToken.OP_MAIOR, ">", linhaToken, colunaToken, inicioToken, indiceAtual, null);
                }
            } else if (c == '=') {
                avancar();
                adicionarToken(TipoToken.OP_IGUAL, "=", linhaToken, colunaToken, inicioToken, indiceAtual, null);
            }
            // 7. Operadores Aritméticos (+, -, *, /)
            else if (c == '+') {
                avancar();
                adicionarToken(TipoToken.OP_SOMA, "+", linhaToken, colunaToken, inicioToken, indiceAtual, null);
            } else if (c == '-') {
                avancar();
                adicionarToken(TipoToken.OP_SUBTRACAO, "-", linhaToken, colunaToken, inicioToken, indiceAtual, null);
            } else if (c == '*') {
                avancar();
                adicionarToken(TipoToken.OP_MULTIPLICACAO, "*", linhaToken, colunaToken, inicioToken, indiceAtual, null);
            } else if (c == '/') {
                avancar();
                adicionarToken(TipoToken.OP_DIVISAO, "/", linhaToken, colunaToken, inicioToken, indiceAtual, null);
            }
            // 8. Delimitadores e Pontuações (; , . ( ) [ ])
            else if (c == ';') {
                avancar();
                adicionarToken(TipoToken.PONTO_E_VIRGULA, ";", linhaToken, colunaToken, inicioToken, indiceAtual, null);
            } else if (c == ',') {
                avancar();
                adicionarToken(TipoToken.VIRGULA, ",", linhaToken, colunaToken, inicioToken, indiceAtual, null);
            } else if (c == '.') {
                avancar();
                adicionarToken(TipoToken.PONTO_FINAL, ".", linhaToken, colunaToken, inicioToken, indiceAtual, null);
            } else if (c == '(') {
                avancar();
                adicionarToken(TipoToken.ABRE_PARENTESES, "(", linhaToken, colunaToken, inicioToken, indiceAtual, null);
            } else if (c == ')') {
                avancar();
                adicionarToken(TipoToken.FECHA_PARENTESES, ")", linhaToken, colunaToken, inicioToken, indiceAtual, null);
            } else if (c == '[') {
                avancar();
                adicionarToken(TipoToken.ABRE_COLCHETES, "[", linhaToken, colunaToken, inicioToken, indiceAtual, null);
            } else if (c == ']') {
                avancar();
                adicionarToken(TipoToken.FECHA_COLCHETES, "]", linhaToken, colunaToken, inicioToken, indiceAtual, null);
            }
            // 9. Caractere Inválido / Desconhecido (Erro Léxico com Modo Pânico)
            else {
                char caractereInvalido = avancar();
                reportarErro(linhaToken, colunaToken, inicioToken, 1,
                        String.valueOf(caractereInvalido),
                        "Caractere não reconhecido pela linguagem: '" + caractereInvalido + "'",
                        "Modo Pânico: caractere inválido descartado para continuar a análise");
            }
        }

        // Token de fim de arquivo (EOF)
        tokens.add(new Token(TipoToken.FIM_DE_ARQUIVO, "", linhaAtual, colunaAtual, indiceAtual, indiceAtual, null));
        return tokens;
    }

    /**
     * Identificadores e Palavras Reservadas:
     * Regra d): identificadores têm até 15 caracteres, iniciando por letra e seguidos por letras, dígitos ou '_'.
     * Regra e): palavras-chave são reservadas e case-insensitive.
     */
    private void analisarIdentificadorOuPalavraChave(int inicioIdx, int linha, int coluna) {
        while (!ehFim() && (ehLetra(olharAtual()) || ehDigito(olharAtual()) || olharAtual() == '_')) {
            avancar();
        }

        String lexemaBruto = codigoFonte.substring(inicioIdx, indiceAtual);
        int tamanho = lexemaBruto.length();

        // Verifica se é palavra reservada (case-insensitive)
        TipoToken tipoPalavraChave = tabelaSimbolos.buscarPalavraChave(lexemaBruto);
        if (tipoPalavraChave != null) {
            adicionarToken(tipoPalavraChave, lexemaBruto, linha, coluna, inicioIdx, indiceAtual, lexemaBruto.toUpperCase());
            return;
        }

        // Se não for palavra reservada, é um identificador.
        // Validação da regra d: tamanho máximo de 15 caracteres.
        if (tamanho > 15) {
            reportarErro(linha, coluna, inicioIdx, tamanho, lexemaBruto,
                    String.format("Identificador '%s' excede o limite máximo permitido de 15 caracteres (tamanho: %d)",
                            lexemaBruto, tamanho),
                    "Modo Pânico: identificador registrado na tabela com aviso de erro léxico");
            tabelaSimbolos.registrarIdentificador(lexemaBruto, linha);
            tokens.add(new Token(TipoToken.IDENTIFICADOR, lexemaBruto, linha, coluna, inicioIdx, indiceAtual, lexemaBruto.toUpperCase()));
        } else {
            // Identificador válido
            tabelaSimbolos.registrarIdentificador(lexemaBruto, linha);
            tokens.add(new Token(TipoToken.IDENTIFICADOR, lexemaBruto, linha, coluna, inicioIdx, indiceAtual, lexemaBruto.toUpperCase()));
        }
    }

    /**
     * Literais Numéricos (Inteiros e Reais):
     * NUM -> digitos | digitos.digitos
     */
    private void analisarNumero(int inicioIdx, int linha, int coluna) {
        while (!ehFim() && ehDigito(olharAtual())) {
            avancar();
        }

        boolean ehReal = false;

        // Verifica se há ponto seguido de dígitos
        if (!ehFim() && olharAtual() == '.') {
            char proximo = olharProximo();
            if (ehDigito(proximo)) {
                ehReal = true;
                avancar(); // consome o '.'
                while (!ehFim() && ehDigito(olharAtual())) {
                    avancar();
                }
            } else if (ehLetra(proximo) || proximo == '_') {
                // Erro: ex. 12.abc ou 12.
                avancar(); // consome '.'
                while (!ehFim() && (ehLetra(olharAtual()) || ehDigito(olharAtual()) || olharAtual() == '_')) {
                    avancar();
                }
                String lexemaRuim = codigoFonte.substring(inicioIdx, indiceAtual);
                reportarErro(linha, coluna, inicioIdx, lexemaRuim.length(), lexemaRuim,
                        "Número real malformado: esperado dígito após o ponto decimal",
                        "Modo Pânico: número malformado descartado");
                return;
            }
        }

        // Verifica se há letras grudadas no número (ex: 123abc)
        if (!ehFim() && (ehLetra(olharAtual()) || olharAtual() == '_')) {
            while (!ehFim() && (ehLetra(olharAtual()) || ehDigito(olharAtual()) || olharAtual() == '_')) {
                avancar();
            }
            String lexemaRuim = codigoFonte.substring(inicioIdx, indiceAtual);
            reportarErro(linha, coluna, inicioIdx, lexemaRuim.length(), lexemaRuim,
                    "Identificador inválido iniciando por dígitos: '" + lexemaRuim + "'",
                    "Modo Pânico: sequência inválida descartada");
            return;
        }

        String lexema = codigoFonte.substring(inicioIdx, indiceAtual);
        if (ehReal) {
            try {
                double val = Double.parseDouble(lexema);
                adicionarToken(TipoToken.NUMERO_REAL, lexema, linha, coluna, inicioIdx, indiceAtual, val);
            } catch (NumberFormatException e) {
                reportarErro(linha, coluna, inicioIdx, lexema.length(), lexema,
                        "Valor real inválido ou fora dos limites: " + lexema,
                        "Modo Pânico: valor descartado");
            }
        } else {
            try {
                long val = Long.parseLong(lexema);
                adicionarToken(TipoToken.NUMERO_INTEIRO, lexema, linha, coluna, inicioIdx, indiceAtual, val);
            } catch (NumberFormatException e) {
                reportarErro(linha, coluna, inicioIdx, lexema.length(), lexema,
                        "Valor inteiro fora dos limites numéricos: " + lexema,
                        "Modo Pânico: valor descartado");
            }
        }
    }

    /**
     * Literais de texto (Strings):
     * LITERAL -> '[letra | dig | CARACTER_ESPECIAL]*'
     * Suporta escape de aspas simples duplicadas: ''
     */
    private void analisarLiteralTexto(int inicioIdx, int linha, int coluna) {
        avancar(); // Consome a aspa inicial '\''
        StringBuilder sb = new StringBuilder();
        boolean fechado = false;

        while (!ehFim()) {
            char c = olharAtual();
            if (c == '\n' || c == '\r') {
                // Erro: quebra de linha antes de fechar a aspa
                String naoFechado = codigoFonte.substring(inicioIdx, indiceAtual);
                reportarErro(linha, coluna, inicioIdx, naoFechado.length(), naoFechado,
                        "Literal de texto não fechado antes do fim da linha",
                        "Modo Pânico: literal fechado implicitamente no final da linha");
                adicionarToken(TipoToken.LITERAL_TEXTO, naoFechado, linha, coluna, inicioIdx, indiceAtual, sb.toString());
                return;
            }

            if (c == '\'') {
                avancar(); // consome a aspa
                if (!ehFim() && olharAtual() == '\'') {
                    // Aspa duplicada: caractere de escape para incluir uma aspa simples
                    sb.append('\'');
                    avancar();
                } else {
                    // Fim do literal
                    fechado = true;
                    break;
                }
            } else {
                sb.append(c);
                avancar();
            }
        }

        if (!fechado) {
            String naoFechado = codigoFonte.substring(inicioIdx, indiceAtual);
            reportarErro(linha, coluna, inicioIdx, naoFechado.length(), naoFechado,
                    "Literal de texto não fechado antes do fim do arquivo (EOF)",
                    "Modo Pânico: fechamento forçado no fim do arquivo");
            adicionarToken(TipoToken.LITERAL_TEXTO, naoFechado, linha, coluna, inicioIdx, indiceAtual, sb.toString());
            return;
        }

        String lexema = codigoFonte.substring(inicioIdx, indiceAtual);
        adicionarToken(TipoToken.LITERAL_TEXTO, lexema, linha, coluna, inicioIdx, indiceAtual, sb.toString());
    }

    /**
     * Comentários de linha única (// ...)
     */
    private void tratarComentarioLinha() {
        avancar(); // /
        avancar(); // /
        while (!ehFim() && olharAtual() != '\n' && olharAtual() != '\r') {
            avancar();
        }
    }

    /**
     * Comentários de múltiplas linhas ({ ... })
     */
    private void tratarComentarioChaves(int inicioIdx, int linha, int coluna) {
        avancar(); // consome '{'
        while (!ehFim() && olharAtual() != '}') {
            avancar();
        }

        if (ehFim()) {
            reportarErro(linha, coluna, inicioIdx, indiceAtual - inicioIdx, "{...",
                    "Comentário de bloco '{' não fechado antes do fim do arquivo",
                    "Modo Pânico: comentário encerrado no fim do arquivo");
        } else {
            avancar(); // consome '}'
        }
    }

    /**
     * Comentários de múltiplas linhas (* ... *)
     */
    private void tratarComentarioParentesesAsterisco(int inicioIdx, int linha, int coluna) {
        avancar(); // consome '('
        avancar(); // consome '*'

        while (!ehFim()) {
            if (olharAtual() == '*' && olharProximo() == ')') {
                avancar(); // *
                avancar(); // )
                return;
            }
            avancar();
        }

        // Se chegou ao EOF sem fechar
        reportarErro(linha, coluna, inicioIdx, indiceAtual - inicioIdx, "(*...",
                "Comentário de bloco '(*' não fechado antes do fim do arquivo",
                "Modo Pânico: comentário encerrado no fim do arquivo");
    }

    /**
     * Ignora espaços em branco atualizando linha e coluna.
     */
    private void pularEspacosEmBranco() {
        while (!ehFim()) {
            char c = olharAtual();
            if (c == ' ' || c == '\t') {
                avancar();
            } else if (c == '\r') {
                avancar();
                if (!ehFim() && olharAtual() == '\n') {
                    avancar();
                }
                linhaAtual++;
                colunaAtual = 1;
            } else if (c == '\n') {
                avancar();
                linhaAtual++;
                colunaAtual = 1;
            } else {
                break;
            }
        }
    }

    private void adicionarToken(TipoToken tipo, String lexema, int linha, int col, int inicio, int fim, Object val) {
        tokens.add(new Token(tipo, lexema, linha, col, inicio, fim, val));
        tabelaSimbolos.registrarUso(lexema, linha);
    }

    private void reportarErro(int linha, int col, int inicio, int tam, String lexema, String msg, String rec) {
        erros.add(new ErroLexico(linha, col, inicio, tam, lexema, msg, rec));
    }

    private boolean ehLetra(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean ehDigito(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean ehFim() {
        return indiceAtual >= tamanhoFonte;
    }

    private char avancar() {
        if (ehFim()) return '\0';
        char c = codigoFonte.charAt(indiceAtual++);
        colunaAtual++;
        return c;
    }

    private char olharAtual() {
        if (ehFim()) return '\0';
        return codigoFonte.charAt(indiceAtual);
    }

    private char olharProximo() {
        if (indiceAtual + 1 >= tamanhoFonte) return '\0';
        return codigoFonte.charAt(indiceAtual + 1);
    }

    private boolean combinar(char esperado) {
        if (ehFim()) return false;
        if (codigoFonte.charAt(indiceAtual) != esperado) return false;
        indiceAtual++;
        colunaAtual++;
        return true;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public List<ErroLexico> getErros() {
        return erros;
    }

    public TabelaSimbolos getTabelaSimbolos() {
        return tabelaSimbolos;
    }

    public boolean possuiErros() {
        return !erros.isEmpty();
    }
}
