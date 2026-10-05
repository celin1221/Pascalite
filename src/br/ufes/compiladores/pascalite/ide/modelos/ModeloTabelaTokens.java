package br.ufes.compiladores.pascalite.ide.modelos;

import br.ufes.compiladores.pascalite.lexico.Token;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de tabela para visualização do fluxo de tokens gerados pelo compilador.
 */
public class ModeloTabelaTokens extends AbstractTableModel {
    private final String[] colunas = {"#", "Linha", "Coluna", "Token (Tipo)", "Categoria", "Lexema", "Atributo/Valor", "Tamanho"};
    private final List<Token> tokens = new ArrayList<>();

    public void definirTokens(List<Token> novosTokens) {
        tokens.clear();
        if (novosTokens != null) {
            tokens.addAll(novosTokens);
        }
        fireTableDataChanged();
    }

    public Token obterTokenNaLinha(int indiceLinha) {
        if (indiceLinha >= 0 && indiceLinha < tokens.size()) {
            return tokens.get(indiceLinha);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return tokens.size();
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
        Token t = tokens.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> rowIndex + 1;
            case 1 -> t.getLinha();
            case 2 -> t.getColuna();
            case 3 -> t.getTipo().name();
            case 4 -> t.getTipo().getCategoria().getDescricao();
            case 5 -> t.getLexema();
            case 6 -> (t.getValorAtributo() != null) ? t.getValorAtributo().toString() : "-";
            case 7 -> t.getTamanho();
            default -> "";
        };
    }
}
