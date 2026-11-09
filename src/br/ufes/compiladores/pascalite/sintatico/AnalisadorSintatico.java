package br.ufes.compiladores.pascalite.sintatico;

import br.ufes.compiladores.pascalite.lexico.TipoToken;
import br.ufes.compiladores.pascalite.lexico.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AnalisadorSintatico {
    private static final int LIMITE_ERROS = 30;

    private final List<Token> tokens;
    private final List<ErroSintatico> erros;
    private int pos;

    public AnalisadorSintatico(List<Token> tokens) {
        this.tokens = tokens != null ? tokens : new ArrayList<>();
        this.erros = new ArrayList<>();
        this.pos = 0;
    }

    public List<ErroSintatico> analisar() {
        erros.clear();
        pos = 0;
        if (!tokens.isEmpty()) {
            programa();
        }
        return erros;
    }

    private void programa() {
        consumir(TipoToken.PR_PROGRAM, "Esperado 'PROGRAM' no início do programa");
        consumir(TipoToken.IDENTIFICADOR, "Esperado nome do programa após 'PROGRAM'");
        consumir(TipoToken.PONTO_E_VIRGULA, "Esperado ';' após o cabeçalho do programa");

        declaracoes();

        if (!verificar(TipoToken.PR_BEGIN)) {
            registrarErro("Esperado 'BEGIN' para iniciar o bloco principal");
            sincronizar(Set.of(TipoToken.PR_BEGIN, TipoToken.PR_END));
        }
        combinar(TipoToken.PR_BEGIN);
        instrucoes();
        if (!verificar(TipoToken.PR_END)) {
            registrarErro("Esperado 'END' para finalizar o programa");
            sincronizar(Set.of(TipoToken.PR_END, TipoToken.PONTO_FINAL));
        }
        combinar(TipoToken.PR_END);
        consumir(TipoToken.PONTO_FINAL, "Esperado '.' após o 'END' final do programa");
    }

    private void bloco() {
        consumir(TipoToken.PR_BEGIN, "Esperado 'BEGIN'");
        instrucoes();
        consumir(TipoToken.PR_END, "Esperado 'END' para fechar o bloco");
        consumir(TipoToken.PONTO_E_VIRGULA, "Esperado ';' após 'END' do bloco");
    }

    private void declaracoes() {
        while (!ehFim() && !acimaDoLimite()) {
            if (verificar(TipoToken.PR_TYPE)) {
                declaracaoTipo();
            } else if (verificar(TipoToken.PR_VAR)) {
                declaracaoVariavel();
            } else if (verificar(TipoToken.PR_CONST)) {
                declaracaoConstante();
            } else if (verificar(TipoToken.PR_PROCEDURE) || verificar(TipoToken.PR_FUNCTION)) {
                declProcedimento();
            } else {
                break;
            }
        }
    }

    private void declaracaoTipo() {
        avancar();
        while (verificar(TipoToken.IDENTIFICADOR) && !acimaDoLimite()) {
            avancar();
            consumir(TipoToken.OP_IGUAL, "Esperado '=' na definição de tipo");
            if (combinar(TipoToken.PR_RECORD)) {
                declVarList();
                consumir(TipoToken.PR_END, "Esperado 'END' ao final do RECORD");
                consumir(TipoToken.PONTO_E_VIRGULA, "Esperado ';' após definição de RECORD");
            } else if (combinar(TipoToken.PR_ENUM)) {
                combinar(TipoToken.PR_OF);
                consumir(TipoToken.ABRE_PARENTESES, "Esperado '(' na lista de ENUM");
                if (consumir(TipoToken.IDENTIFICADOR, "Esperado identificador no ENUM")) {
                    while (combinar(TipoToken.VIRGULA)) {
                        consumir(TipoToken.IDENTIFICADOR, "Esperado identificador após ',' no ENUM");
                    }
                }
                consumir(TipoToken.FECHA_PARENTESES, "Esperado ')' após valores do ENUM");
                consumir(TipoToken.PONTO_E_VIRGULA, "Esperado ';' após definição de ENUM");
            } else {
                registrarErro("Esperado 'RECORD' ou 'ENUM' na declaração de tipo");
                sincronizar(Set.of(TipoToken.PONTO_E_VIRGULA, TipoToken.PR_VAR, TipoToken.PR_CONST, TipoToken.PR_BEGIN));
                combinar(TipoToken.PONTO_E_VIRGULA);
            }
        }
    }

    private void declaracaoConstante() {
        if (combinar(TipoToken.PR_CONST)) {
            declConsList();
        }
    }

    private void declConsList() {
        while (verificar(TipoToken.IDENTIFICADOR) && !acimaDoLimite()) {
            avancar();
            if (combinar(TipoToken.DOIS_PONTOS)) {
                tipo();
                consumir(TipoToken.OP_IGUAL, "Esperado '=' na declaração de constante");
                valor();
                consumir(TipoToken.PONTO_E_VIRGULA, "Esperado ';' ao final da constante");
            } else if (combinar(TipoToken.OP_IGUAL)) {
                valor();
                consumir(TipoToken.PONTO_E_VIRGULA, "Esperado ';' ao final da constante");
            } else {
                registrarErro("Esperado ':' ou '=' após nome da constante");
                sincronizar(Set.of(TipoToken.PONTO_E_VIRGULA, TipoToken.PR_VAR, TipoToken.PR_BEGIN));
                combinar(TipoToken.PONTO_E_VIRGULA);
            }
        }
    }

    private void declaracaoVariavel() {
        if (combinar(TipoToken.PR_VAR)) {
            declVarList();
        }
    }

    private void declVarList() {
        Set<TipoToken> fimSecao = Set.of(
                TipoToken.PR_CONST,
                TipoToken.PR_PROCEDURE,
                TipoToken.PR_FUNCTION,
                TipoToken.PR_BEGIN,
                TipoToken.PR_END,
                TipoToken.PR_TYPE,
                TipoToken.PR_VAR
        );
        while (!ehFim() && !acimaDoLimite() && !fimSecao.contains(atual().getTipo())) {
            if (verificar(TipoToken.IDENTIFICADOR)) {
                declVar();
            } else {
                registrarErro("Esperado identificador na declaração de variável");
                sincronizar(Set.of(
                        TipoToken.PONTO_E_VIRGULA,
                        TipoToken.PR_CONST,
                        TipoToken.PR_PROCEDURE,
                        TipoToken.PR_FUNCTION,
                        TipoToken.PR_BEGIN,
                        TipoToken.PR_END
                ));
                combinar(TipoToken.PONTO_E_VIRGULA);
            }
        }
    }

    private void declVar() {
        variavel();
        conjuntoIds();
        if (!consumir(TipoToken.DOIS_PONTOS, "Esperado ':' após lista de variáveis")) {
            sincronizar(Set.of(TipoToken.PONTO_E_VIRGULA, TipoToken.PR_CONST, TipoToken.PR_PROCEDURE, TipoToken.PR_FUNCTION, TipoToken.PR_BEGIN));
            combinar(TipoToken.PONTO_E_VIRGULA);
            return;
        }
        tipo();
        if (!consumir(TipoToken.PONTO_E_VIRGULA, "Esperado ';' após declaração de variável")) {
            sincronizar(Set.of(TipoToken.PONTO_E_VIRGULA, TipoToken.PR_CONST, TipoToken.PR_PROCEDURE, TipoToken.PR_FUNCTION, TipoToken.PR_BEGIN));
            combinar(TipoToken.PONTO_E_VIRGULA);
        }
    }

    private void conjuntoIds() {
        while (combinar(TipoToken.VIRGULA)) {
            variavel();
        }
    }

    private void tipo() {
        if (combinar(TipoToken.PR_INTEGER)
                || combinar(TipoToken.PR_REAL)
                || combinar(TipoToken.PR_CHAR)
                || combinar(TipoToken.PR_STRING)
                || combinar(TipoToken.IDENTIFICADOR)) {
            return;
        }
        registrarErro("Tipo inválido. Esperado INTEGER, REAL, CHAR ou STRING");
    }

    private void valor() {
        if (combinar(TipoToken.LITERAL_TEXTO)) {
            return;
        }
        unario();
    }

    private void declProcedimento() {
        while ((verificar(TipoToken.PR_PROCEDURE) || verificar(TipoToken.PR_FUNCTION)) && !acimaDoLimite()) {
            declProc();
        }
    }

    private void declProc() {
        if (combinar(TipoToken.PR_PROCEDURE)) {
            consumir(TipoToken.IDENTIFICADOR, "Esperado nome do procedimento");
            consumir(TipoToken.ABRE_PARENTESES, "Esperado '(' após nome do procedimento");
            parametros();
            consumir(TipoToken.FECHA_PARENTESES, "Esperado ')' após parâmetros");
            consumir(TipoToken.PONTO_E_VIRGULA, "Esperado ';' após cabeçalho do procedimento");
            declaracaoVariavel();
            bloco();
        } else if (combinar(TipoToken.PR_FUNCTION)) {
            consumir(TipoToken.IDENTIFICADOR, "Esperado nome da função");
            consumir(TipoToken.ABRE_PARENTESES, "Esperado '(' após nome da função");
            parametros();
            consumir(TipoToken.FECHA_PARENTESES, "Esperado ')' após parâmetros");
            consumir(TipoToken.DOIS_PONTOS, "Esperado ':' para definir retorno da função");
            tipo();
            consumir(TipoToken.PONTO_E_VIRGULA, "Esperado ';' após cabeçalho da função");
            declaracaoVariavel();
            bloco();
        }
    }

    private void parametros() {
        while (verificar(TipoToken.IDENTIFICADOR) && !acimaDoLimite()) {
            variavel();
            conjuntoIds();
            consumir(TipoToken.DOIS_PONTOS, "Esperado ':' na declaração de parâmetro");
            tipo();
            if (!combinar(TipoToken.PONTO_E_VIRGULA)) {
                break;
            }
        }
    }

    private void instrucoes() {
        while (inicioDeInstrucao() && !acimaDoLimite()) {
            inst();
        }
    }

    private boolean inicioDeInstrucao() {
        TipoToken t = atual().getTipo();
        return t == TipoToken.IDENTIFICADOR
                || t == TipoToken.PR_IF
                || t == TipoToken.PR_WHILE
                || t == TipoToken.PR_REPEAT
                || t == TipoToken.PR_FOR
                || t == TipoToken.PR_BREAK
                || t == TipoToken.PR_CONTINUE
                || t == TipoToken.PR_BEGIN;
    }

    private void inst() {
        if (acimaDoLimite()) return;

        if (verificar(TipoToken.IDENTIFICADOR)) {
            Token idToken = avancar();
            if (combinar(TipoToken.ABRE_COLCHETES)) {
                expr();
                consumir(TipoToken.FECHA_COLCHETES, "Esperado ']' após índice do vetor");
                consumir(TipoToken.OP_ATRIBUICAO, "Esperado ':=' na atribuição");
                expr();
                consumirFimInstrucao();
            } else if (combinar(TipoToken.OP_ATRIBUICAO)) {
                expr();
                consumirFimInstrucao();
            } else if (combinar(TipoToken.ABRE_PARENTESES)) {
                parametros2();
                consumir(TipoToken.FECHA_PARENTESES, "Esperado ')' na chamada de sub-rotina");
                consumirFimInstrucao();
            } else if (combinar(TipoToken.PONTO_E_VIRGULA)) {
                erros.add(new ErroSintatico(
                        idToken.getLinha(),
                        idToken.getColuna(),
                        idToken.getIndiceInicio(),
                        idToken.getTamanho(),
                        idToken.getLexema(),
                        "Instrução com somente um ID não possui efeito nenhum",
                        true
                ));
            } else {
                registrarErro("Esperado ':=', '(' ou '[' após identificador");
                sincronizarInstrucao();
            }
        } else if (combinar(TipoToken.PR_IF)) {
            expr();
            consumir(TipoToken.PR_THEN, "Esperado 'THEN' após condição do IF");
            inst();
            if (combinar(TipoToken.PR_ELSE)) {
                inst();
            }
        } else if (combinar(TipoToken.PR_WHILE)) {
            expr();
            consumir(TipoToken.PR_DO, "Esperado 'DO' após condição do WHILE");
            inst();
        } else if (combinar(TipoToken.PR_REPEAT)) {
            instrucoes();
            consumir(TipoToken.PR_UNTIL, "Esperado 'UNTIL' para fechar o bloco REPEAT");
            expr();
            consumirFimInstrucao();
        } else if (combinar(TipoToken.PR_FOR)) {
            consumir(TipoToken.IDENTIFICADOR, "Esperado variável de controle no FOR");
            consumir(TipoToken.OP_ATRIBUICAO, "Esperado ':=' na inicialização do FOR");
            expr();
            if (!combinar(TipoToken.PR_TO) && !combinar(TipoToken.PR_DOWNTO)) {
                registrarErro("Esperado 'TO' ou 'DOWNTO' na estrutura FOR");
            }
            expr();
            consumir(TipoToken.PR_DO, "Esperado 'DO' após limite do FOR");
            inst();
        } else if (combinar(TipoToken.PR_BREAK)) {
            consumirFimInstrucao();
        } else if (combinar(TipoToken.PR_CONTINUE)) {
            consumirFimInstrucao();
        } else if (verificar(TipoToken.PR_BEGIN)) {
            bloco();
        } else {
            registrarErro("Instrução inválida");
            sincronizarInstrucao();
        }
    }

    private void consumirFimInstrucao() {
        if (verificar(TipoToken.PR_ELSE)) {
            return;
        }
        consumir(TipoToken.PONTO_E_VIRGULA, "Esperado ';' ao final da instrução");
    }

    private void parametros2() {
        if (inicioDeExpressao()) {
            expr();
            while (combinar(TipoToken.VIRGULA)) {
                expr();
            }
        }
    }

    private void expr() {
        exprComparacao();
        expr2();
    }

    private void expr2() {
        while (combinar(TipoToken.PR_OU) || combinar(TipoToken.PR_E)) {
            exprComparacao();
        }
    }

    private void exprComparacao() {
        exprOp();
        exprComparacao2();
    }

    private void exprComparacao2() {
        while (combinar(TipoToken.OP_IGUAL)
                || combinar(TipoToken.OP_DIFERENTE)
                || combinar(TipoToken.OP_MENOR)
                || combinar(TipoToken.OP_MENOR_IGUAL)
                || combinar(TipoToken.OP_MAIOR)
                || combinar(TipoToken.OP_MAIOR_IGUAL)) {
            exprOp();
        }
    }

    private void exprOp() {
        termo();
        exprOp2();
    }

    private void exprOp2() {
        while (combinar(TipoToken.OP_SOMA) || combinar(TipoToken.OP_SUBTRACAO)) {
            termo();
        }
    }

    private void termo() {
        unario();
        termo2();
    }

    private void termo2() {
        while (combinar(TipoToken.OP_MULTIPLICACAO) || combinar(TipoToken.OP_DIVISAO)) {
            unario();
        }
    }

    private void unario() {
        if (combinar(TipoToken.OP_SOMA) || combinar(TipoToken.OP_SUBTRACAO)) {
            fator();
        } else {
            fator();
        }
    }

    private void fator() {
        if (combinar(TipoToken.ABRE_PARENTESES)) {
            expr();
            consumir(TipoToken.FECHA_PARENTESES, "Esperado ')' após expressão");
        } else if (verificar(TipoToken.IDENTIFICADOR)) {
            Token id = avancar();
            if (combinar(TipoToken.ABRE_COLCHETES)) {
                exprOp();
                consumir(TipoToken.FECHA_COLCHETES, "Esperado ']' após índice");
            } else if (combinar(TipoToken.ABRE_PARENTESES)) {
                parametros2();
                consumir(TipoToken.FECHA_PARENTESES, "Esperado ')' após argumentos da função '" + id.getLexema() + "'");
            }
        } else if (combinar(TipoToken.NUMERO_INTEIRO)
                || combinar(TipoToken.NUMERO_REAL)
                || combinar(TipoToken.LITERAL_TEXTO)) {
            // literal consumido
        } else {
            registrarErro("Expressão malformada ou operando esperado");
            if (!ehFim() && !ehDelimitadorSincronismo(atual().getTipo())) {
                avancar();
            }
        }
    }

    private boolean ehDelimitadorSincronismo(TipoToken t) {
        return t == TipoToken.PONTO_E_VIRGULA
                || t == TipoToken.PR_THEN
                || t == TipoToken.PR_DO
                || t == TipoToken.PR_END
                || t == TipoToken.PR_ELSE
                || t == TipoToken.PR_UNTIL
                || t == TipoToken.FECHA_PARENTESES
                || t == TipoToken.FECHA_COLCHETES;
    }

    private void variavel() {
        consumir(TipoToken.IDENTIFICADOR, "Esperado identificador de variável");
        if (combinar(TipoToken.ABRE_COLCHETES)) {
            exprOp();
            consumir(TipoToken.FECHA_COLCHETES, "Esperado ']'");
        }
    }

    private boolean inicioDeExpressao() {
        TipoToken t = atual().getTipo();
        return t == TipoToken.ABRE_PARENTESES
                || t == TipoToken.IDENTIFICADOR
                || t == TipoToken.NUMERO_INTEIRO
                || t == TipoToken.NUMERO_REAL
                || t == TipoToken.LITERAL_TEXTO
                || t == TipoToken.OP_SOMA
                || t == TipoToken.OP_SUBTRACAO;
    }

    private boolean consumir(TipoToken esperado, String mensagemErro) {
        if (verificar(esperado)) {
            avancar();
            return true;
        }
        registrarErro(mensagemErro);
        return false;
    }

    private void registrarErro(String mensagem) {
        if (acimaDoLimite()) return;
        Token t = atual();
        String lex = t.getTipo() == TipoToken.FIM_DE_ARQUIVO ? "EOF" : t.getLexema();
        erros.add(new ErroSintatico(
                t.getLinha(),
                t.getColuna(),
                t.getIndiceInicio(),
                t.getTamanho(),
                lex,
                mensagem,
                false
        ));
    }

    private void sincronizarInstrucao() {
        sincronizar(Set.of(
                TipoToken.PONTO_E_VIRGULA,
                TipoToken.PR_END,
                TipoToken.PR_ELSE,
                TipoToken.PR_UNTIL,
                TipoToken.PR_IF,
                TipoToken.PR_WHILE,
                TipoToken.PR_REPEAT,
                TipoToken.PR_FOR
        ));
        combinar(TipoToken.PONTO_E_VIRGULA);
    }

    private void sincronizar(Set<TipoToken> tokensSincronismo) {
        while (!ehFim() && !tokensSincronismo.contains(atual().getTipo())) {
            avancar();
        }
    }

    private boolean combinar(TipoToken tipo) {
        if (verificar(tipo)) {
            avancar();
            return true;
        }
        return false;
    }

    private boolean verificar(TipoToken tipo) {
        return atual().getTipo() == tipo;
    }

    private Token avancar() {
        if (!ehFim()) pos++;
        return tokens.get(pos - 1);
    }

    private Token atual() {
        return tokens.get(pos);
    }

    private boolean ehFim() {
        return atual().getTipo() == TipoToken.FIM_DE_ARQUIVO;
    }

    private boolean acimaDoLimite() {
        return erros.size() >= LIMITE_ERROS;
    }

    public List<ErroSintatico> getErros() {
        return erros;
    }

    public boolean possuiErros() {
        return erros.stream().anyMatch(e -> !e.ehAviso());
    }
}
