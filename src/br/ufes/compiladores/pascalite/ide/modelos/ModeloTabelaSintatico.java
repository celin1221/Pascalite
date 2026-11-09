package br.ufes.compiladores.pascalite.ide.modelos;

import br.ufes.compiladores.pascalite.sintatico.ErroSintatico;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ModeloTabelaSintatico extends AbstractTableModel {
    private final String[] colunas = {"#", "Tipo", "Linha", "Coluna", "Token Próximo", "Mensagem"};
    private final List<ErroSintatico> erros = new ArrayList<>();

    public void definirErros(List<ErroSintatico> novosErros) {
        erros.clear();
        if (novosErros != null) {
            erros.addAll(novosErros);
        }
        fireTableDataChanged();
    }

    public ErroSintatico obterErroNaLinha(int indiceLinha) {
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
        ErroSintatico err = erros.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> rowIndex + 1;
            case 1 -> err.getTipoDescricao();
            case 2 -> err.getLinha();
            case 3 -> err.getColuna();
            case 4 -> err.getLexema();
            case 5 -> err.getMensagem();
            default -> "";
        };
    }
}
