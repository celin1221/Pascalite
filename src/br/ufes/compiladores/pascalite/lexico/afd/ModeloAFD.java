package br.ufes.compiladores.pascalite.lexico.afd;

import java.util.*;

/**
 * Modelo formal do Autômato Finito Determinístico (AFD) do dialeto PascaLite.
 * Define a 5-tupla M = (Q, Sigma, delta, q0, F), permitindo visualização em grafo,
 * tabela de transições e geração de relatório.
 */
public class ModeloAFD {
    private final Map<String, EstadoAFD> estados;
    private final EstadoAFD estadoInicial;
    private final List<TransicaoAFD> transicoes;

    public ModeloAFD() {
        this.estados = new LinkedHashMap<>();
        this.transicoes = new ArrayList<>();
        this.estadoInicial = construirModelo();
    }

    private EstadoAFD construirModelo() {
        // 1. Estados
        EstadoAFD q0 = adicionarEstado("q0", "Início", "Estado inicial aguardando caracteres", false, null);

        // Identificadores e Palavras-chave
        EstadoAFD qId = adicionarEstado("q_id", "Identificador/PR", "Lendo letras, dígitos ou underscore", true, "IDENTIFICADOR ou PALAVRA_RESERVADA");

        // Literais Numéricos
        EstadoAFD qNumInt = adicionarEstado("q_int", "Número Inteiro", "Lendo sequência de dígitos inteiros", true, "NUMERO_INTEIRO");
        EstadoAFD qNumPonto = adicionarEstado("q_ponto_num", "Ponto em Número", "Lendo ponto após dígitos", false, null);
        EstadoAFD qNumReal = adicionarEstado("q_real", "Número Real", "Lendo dígitos fracionários após o ponto", true, "NUMERO_REAL");

        // Literal de Texto (String)
        EstadoAFD qStr = adicionarEstado("q_str", "Lendo String", "Dentro de aspas simples", false, null);
        EstadoAFD qStrAspa = adicionarEstado("q_str_aspa", "Aspa em String", "Fechamento de string ou escape de aspa ('')", true, "LITERAL_TEXTO");

        // Operadores de Atribuição e Dois Pontos
        EstadoAFD qDoisPontos = adicionarEstado("q_dois_pontos", "Dois Pontos", "Lido caractere ':'", true, "DOIS_PONTOS (:)");
        EstadoAFD qAtribuicao = adicionarEstado("q_atribuicao", "Atribuição", "Lido sequência ':='", true, "OP_ATRIBUICAO (:=)");

        // Operadores Relacionais
        EstadoAFD qMenor = adicionarEstado("q_menor", "Menor que", "Lido caractere '<'", true, "OP_MENOR (<)");
        EstadoAFD qMenorIgual = adicionarEstado("q_menor_igual", "Menor ou Igual", "Lido sequência '<='", true, "OP_MENOR_IGUAL (<=)");
        EstadoAFD qDiferente = adicionarEstado("q_diferente", "Diferente", "Lido sequência '<>'", true, "OP_DIFERENTE (<>)");
        EstadoAFD qMaior = adicionarEstado("q_maior", "Maior que", "Lido caractere '>'", true, "OP_MAIOR (>)");
        EstadoAFD qMaiorIgual = adicionarEstado("q_maior_igual", "Maior ou Igual", "Lido sequência '>='", true, "OP_MAIOR_IGUAL (>=)");
        EstadoAFD qIgual = adicionarEstado("q_igual", "Igual", "Lido caractere '='", true, "OP_IGUAL (=)");

        // Operadores Aritméticos
        EstadoAFD qSoma = adicionarEstado("q_soma", "Adição", "Lido caractere '+'", true, "OP_SOMA (+)");
        EstadoAFD qSub = adicionarEstado("q_sub", "Subtração", "Lido caractere '-'", true, "OP_SUBTRACAO (-)");
        EstadoAFD qMult = adicionarEstado("q_mult", "Multiplicação", "Lido caractere '*'", true, "OP_MULTIPLICACAO (*)");
        EstadoAFD qBarra = adicionarEstado("q_barra", "Barra/Divisão", "Lido caractere '/'", true, "OP_DIVISAO (/)");

        // Delimitadores
        EstadoAFD qPontoVirgula = adicionarEstado("q_pt_virgula", "Ponto e Vírgula", "Lido caractere ';'", true, "PONTO_E_VIRGULA (;)");
        EstadoAFD qVirgula = adicionarEstado("q_virgula", "Vírgula", "Lido caractere ','", true, "VIRGULA (,)");
        EstadoAFD qPontoFinal = adicionarEstado("q_ponto", "Ponto Final", "Lido caractere '.'", true, "PONTO_FINAL (.)");
        EstadoAFD qAbrePar = adicionarEstado("q_abre_par", "Abre Parênteses", "Lido caractere '('", true, "ABRE_PARENTESES (() ");
        EstadoAFD qFechaPar = adicionarEstado("q_fecha_par", "Fecha Parênteses", "Lido caractere ')'", true, "FECHA_PARENTESES ())");
        EstadoAFD qAbreCol = adicionarEstado("q_abre_col", "Abre Colchetes", "Lido caractere '['", true, "ABRE_COLCHETES ([)");
        EstadoAFD qFechaCol = adicionarEstado("q_fecha_col", "Fecha Colchetes", "Lido caractere ']'", true, "FECHA_COLCHETES (])");

        // Comentários
        EstadoAFD qComLinha = adicionarEstado("q_com_linha", "Comentário Linha", "Lendo até quebra de linha", true, "COMENTARIO (//)");
        EstadoAFD qComChaves = adicionarEstado("q_com_chaves", "Comentário Chaves", "Lendo bloco entre chaves", false, null);
        EstadoAFD qFimChaves = adicionarEstado("q_fim_chaves", "Fim Comentário {}", "Fechou chave '}'", true, "COMENTARIO ({})");
        EstadoAFD qComParEst = adicionarEstado("q_com_parest", "Comentário (* *)", "Lendo bloco (* *)", false, null);
        EstadoAFD qComParTalvez = adicionarEstado("q_com_partalvez", "Possível Fim (* *)", "Lido '*' dentro de (* *)", false, null);
        EstadoAFD qFimParEst = adicionarEstado("q_fim_parest", "Fim Comentário (* *)", "Fechou '*)'", true, "COMENTARIO (* *)");

        // Erro Léxico
        EstadoAFD qErro = adicionarEstado("q_erro", "Erro Léxico", "Símbolo ou padrão inválido", true, "ERRO_LEXICO");

        // 2. Transições
        // Espaços em branco
        adicionarTransicao(q0, q0, "[ \\t\\r\\n]", "Consome espaços em branco");

        // Identificadores e Palavras Reservadas
        adicionarTransicao(q0, qId, "[A-Za-z]", "Inicia identificador com letra");
        adicionarTransicao(qId, qId, "[A-Za-z0-9_]", "Continua letras, dígitos e underscore (até 15 caracteres)");

        // Números
        adicionarTransicao(q0, qNumInt, "[0-9]", "Inicia número inteiro");
        adicionarTransicao(qNumInt, qNumInt, "[0-9]", "Continua dígitos inteiros");
        adicionarTransicao(qNumInt, qNumPonto, "'.'", "Encontra ponto decimal");
        adicionarTransicao(qNumPonto, qNumReal, "[0-9]", "Dígitos decimais do número real");
        adicionarTransicao(qNumReal, qNumReal, "[0-9]", "Continua dígitos fracionários");

        // Literais (Strings)
        adicionarTransicao(q0, qStr, "'", "Abre aspas do literal");
        adicionarTransicao(qStr, qStr, "[^'\\n\\r]", "Caracteres do literal");
        adicionarTransicao(qStr, qStrAspa, "'", "Aspa de fechamento ou escape");
        adicionarTransicao(qStrAspa, qStr, "'", "Escape de aspa simples ('')");

        // Dois pontos e atribuição
        adicionarTransicao(q0, qDoisPontos, ":", "Caractere dois pontos");
        adicionarTransicao(qDoisPontos, qAtribuicao, "=", "Operador de atribuição :=");

        // Relacionais
        adicionarTransicao(q0, qMenor, "<", "Operador menor");
        adicionarTransicao(qMenor, qMenorIgual, "=", "Operador menor ou igual <=");
        adicionarTransicao(qMenor, qDiferente, ">", "Operador diferente <>");
        adicionarTransicao(q0, qMaior, ">", "Operador maior");
        adicionarTransicao(qMaior, qMaiorIgual, "=", "Operador maior ou igual >=");
        adicionarTransicao(q0, qIgual, "=", "Operador igual");

        // Aritméticos
        adicionarTransicao(q0, qSoma, "+", "Operador soma");
        adicionarTransicao(q0, qSub, "-", "Operador subtração");
        adicionarTransicao(q0, qMult, "*", "Operador multiplicação");
        adicionarTransicao(q0, qBarra, "/", "Operador divisão ou início de comentário //");
        adicionarTransicao(qBarra, qComLinha, "/", "Comentário de linha //");
        adicionarTransicao(qComLinha, qComLinha, "[^\\n\\r]", "Consome comentário até fim da linha");

        // Comentários em bloco {}
        adicionarTransicao(q0, qComChaves, "{", "Início de comentário entre chaves");
        adicionarTransicao(qComChaves, qComChaves, "[^}]", "Conteúdo do comentário entre chaves");
        adicionarTransicao(qComChaves, qFimChaves, "}", "Fim de comentário entre chaves");

        // Parênteses e comentário (* *)
        adicionarTransicao(q0, qAbrePar, "(", "Abre parênteses ou início de (*");
        adicionarTransicao(qAbrePar, qComParEst, "*", "Início de comentário (* *)");
        adicionarTransicao(qComParEst, qComParEst, "[^*]", "Conteúdo do comentário (* *)");
        adicionarTransicao(qComParEst, qComParTalvez, "*", "Possível fim do comentário (* *)");
        adicionarTransicao(qComParTalvez, qComParEst, "[^\\)*]", "Retorna ao conteúdo do comentário");
        adicionarTransicao(qComParTalvez, qFimParEst, ")", "Fim de comentário (* *)");

        // Delimitadores restantes
        adicionarTransicao(q0, qFechaPar, ")", "Fecha parênteses");
        adicionarTransicao(q0, qAbreCol, "[", "Abre colchetes");
        adicionarTransicao(q0, qFechaCol, "]", "Fecha colchetes");
        adicionarTransicao(q0, qPontoVirgula, ";", "Ponto e vírgula");
        adicionarTransicao(q0, qVirgula, ",", "Vírgula");
        adicionarTransicao(q0, qPontoFinal, ".", "Ponto final");

        // Erro léxico
        adicionarTransicao(q0, qErro, "outro", "Qualquer símbolo inválido (@, $, #, !, ?, etc.)");
        adicionarTransicao(qNumPonto, qErro, "[^0-9]", "Ponto decimal sem dígitos subsequentes");

        return q0;
    }

    private EstadoAFD adicionarEstado(String id, String nome, String descricao, boolean ehFinal, String tokenProduzido) {
        EstadoAFD estado = new EstadoAFD(id, nome, descricao, ehFinal, tokenProduzido);
        estados.put(id, estado);
        return estado;
    }

    private void adicionarTransicao(EstadoAFD origem, EstadoAFD destino, String rotulo, String descricao) {
        transicoes.add(new TransicaoAFD(origem, destino, rotulo, descricao));
    }

    public EstadoAFD getEstadoInicial() {
        return estadoInicial;
    }

    public Map<String, EstadoAFD> getEstados() {
        return Collections.unmodifiableMap(estados);
    }

    public List<TransicaoAFD> getTransicoes() {
        return Collections.unmodifiableList(transicoes);
    }

    public List<EstadoAFD> getEstadosFinais() {
        List<EstadoAFD> lista = new ArrayList<>();
        for (EstadoAFD s : estados.values()) {
            if (s.ehFinal()) lista.add(s);
        }
        return lista;
    }
}
