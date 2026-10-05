package br.ufes.compiladores.pascalite.ide.modelos;

import br.ufes.compiladores.pascalite.lexico.EntradaSimbolo;
import br.ufes.compiladores.pascalite.lexico.TabelaSimbolos;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Modelo de tabela para visualização da Tabela de Símbolos do compilador.
 */
public class ModeloTabelaSimbolos extends AbstractTableModel {
    private final String[] colunas = {
            "#", "Símbolo (Lexema)", "Chave Canônica", "Tipo de Token",
            "Categoria", "Origem", "Ocorrências", "Linhas de Ocorrência"
    };
    private final List<EntradaSimbolo> entradas = new ArrayList<>();

    public void atualizar(TabelaSimbolos tabelaSimbolos) {
        entradas.clear();
        if (tabelaSimbolos != null) {
            Collection<EntradaSimbolo> todos = tabelaSimbolos.obterTodosSimbolos();
            entradas.addAll(todos);
        }
        fireTableDataChanged();
    }

    public EntradaSimbolo obterEntradaNaLinha(int indiceLinha) {
        if (indiceLinha >= 0 && indiceLinha < entradas.size()) {
            return entradas.get(indiceLinha);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return entradas.size();
    }

    @Override
    public int getColumnCount() {
        return colunas.length;
    }

    @Override
    public String getColumnName(int column) {
        return colunas[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        EntradaSimbolo entrada = entradas.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> rowIndex + 1;
            case 1 -> entrada.getLexema();
            case 2 -> entrada.getChaveCanonica();
            case 3 -> entrada.getTipoToken().name();
            case 4 -> entrada.getCategoria().getDescricao();
            case 5 -> entrada.ehPredefinido() ? "Pré-definido" : "Programa Objeto";
            case 6 -> entrada.getOcorrencias();
            case 7 -> entrada.getLinhasFormatadas();
            default -> "";
        };
    }
}
