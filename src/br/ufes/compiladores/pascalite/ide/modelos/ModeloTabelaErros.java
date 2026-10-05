package br.ufes.compiladores.pascalite.ide.modelos;

import br.ufes.compiladores.pascalite.lexico.ErroLexico;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de tabela para visualização de erros léxicos e estratégias de recuperação.
 */
public class ModeloTabelaErros extends AbstractTableModel {
    private final String[] colunas = {"#", "Linha", "Coluna", "Trecho com Erro", "Mensagem de Erro", "Estratégia de Recuperação"};
    private final List<ErroLexico> erros = new ArrayList<>();

    public void definirErros(List<ErroLexico> novosErros) {
        erros.clear();
        if (novosErros != null) {
            erros.addAll(novosErros);
        }
        fireTableDataChanged();
    }

    public ErroLexico obterErroNaLinha(int indiceLinha) {
        if (indiceLinha >= 0 && indiceLinha < erros.size()) {
            return erros.get(indiceLinha);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return erros.size();
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
        ErroLexico err = erros.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> rowIndex + 1;
            case 1 -> err.getLinha();
            case 2 -> err.getColuna();
            case 3 -> err.getLexemaInvalido();
            case 4 -> err.getMensagem();
            case 5 -> err.getEstrategiaRecuperacao();
            default -> "";
        };
    }
}
