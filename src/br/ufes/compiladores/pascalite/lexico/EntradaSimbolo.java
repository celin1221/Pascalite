package br.ufes.compiladores.pascalite.lexico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa uma entrada na Tabela de Símbolos do compilador PascaLite.
 */
public class EntradaSimbolo {
    private final String chaveCanonica;
    private final String lexema;
    private final TipoToken tipoToken;
    private final CategoriaToken categoria;
    private final boolean predefinido;
    private int ocorrencias;
    private final List<Integer> linhas;
    private Object valor;

    public EntradaSimbolo(String chaveCanonica, String lexema, TipoToken tipoToken, CategoriaToken categoria, boolean predefinido) {
        this.chaveCanonica = chaveCanonica;
        this.lexema = lexema;
        this.tipoToken = tipoToken;
        this.categoria = categoria;
        this.predefinido = predefinido;
        this.ocorrencias = 0;
        this.linhas = new ArrayList<>();
    }

    public String getChaveCanonica() {
        return chaveCanonica;
    }

    public String getLexema() {
        return lexema;
    }

    public TipoToken getTipoToken() {
        return tipoToken;
    }

    public CategoriaToken getCategoria() {
        return categoria;
    }

    public boolean ehPredefinido() {
        return predefinido;
    }

    public int getOcorrencias() {
        return ocorrencias;
    }

    public List<Integer> getLinhas() {
        return Collections.unmodifiableList(linhas);
    }

    public Object getValor() {
        return valor;
    }

    public void setValor(Object valor) {
        this.valor = valor;
    }

    public void adicionarOcorrencia(int linha) {
        this.ocorrencias++;
        if (!linhas.contains(linha)) {
            linhas.add(linha);
        }
    }

    public String getLinhasFormatadas() {
        if (linhas.isEmpty()) {
            return "-";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < linhas.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(linhas.get(i));
        }
        return sb.toString();
    }
}
