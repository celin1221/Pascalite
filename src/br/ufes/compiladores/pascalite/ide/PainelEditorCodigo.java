package br.ufes.compiladores.pascalite.ide;

import br.ufes.compiladores.pascalite.lexico.AnalisadorLexico;
import br.ufes.compiladores.pascalite.lexico.ErroLexico;
import br.ufes.compiladores.pascalite.lexico.Token;
import br.ufes.compiladores.pascalite.sintatico.AnalisadorSintatico;
import br.ufes.compiladores.pascalite.sintatico.ErroSintatico;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PainelEditorCodigo extends JTextPane {

    public interface RetornoAnalise {
        void aoFinalizarAnalise(List<Token> tokens, List<ErroLexico> errosLexicos, List<ErroSintatico> errosSintaticos);
    }

    public interface RetornoPosicaoCursor {
        void aoMoverCursor(int linha, int coluna);
    }

    private final Timer temporizadorDebounce;
    private final PintorSublinhadoOndulado pintorErro;
    private final PintorSublinhadoOndulado pintorAviso;
    private final List<Object> marcadoresDestaque = new ArrayList<>();
    private RetornoAnalise retornoAnalise;
    private RetornoPosicaoCursor retornoCursor;
    private boolean atualizandoEstilos = false;

    private final Style estiloNormal;
    private final Style estiloPalavraChave;
    private final Style estiloNumero;
    private final Style estiloTexto;
    private final Style estiloComentario;
    private final Style estiloOperador;

    public PainelEditorCodigo() {
        setFont(new Font("Consolas", Font.PLAIN, 15));
        setBackground(new Color(30, 30, 30));
        setForeground(new Color(220, 220, 220));
        setCaretColor(new Color(255, 255, 255));
        setMargin(new Insets(5, 8, 5, 8));

        this.pintorErro = new PintorSublinhadoOndulado(new Color(244, 75, 75));
        this.pintorAviso = new PintorSublinhadoOndulado(new Color(240, 180, 50));

        StyledDocument doc = getStyledDocument();
        estiloNormal = doc.addStyle("Normal", null);
        StyleConstants.setForeground(estiloNormal, new Color(220, 220, 220));

        estiloPalavraChave = doc.addStyle("PalavraChave", null);
        StyleConstants.setForeground(estiloPalavraChave, new Color(86, 156, 214));
        StyleConstants.setBold(estiloPalavraChave, true);

        estiloNumero = doc.addStyle("Numero", null);
        StyleConstants.setForeground(estiloNumero, new Color(181, 206, 168));

        estiloTexto = doc.addStyle("Texto", null);
        StyleConstants.setForeground(estiloTexto, new Color(206, 145, 120));

        estiloComentario = doc.addStyle("Comentario", null);
        StyleConstants.setForeground(estiloComentario, new Color(106, 153, 85));
        StyleConstants.setItalic(estiloComentario, true);

        estiloOperador = doc.addStyle("Operador", null);
        StyleConstants.setForeground(estiloOperador, new Color(218, 218, 160));

        temporizadorDebounce = new Timer(250, e -> executarAnalise());
        temporizadorDebounce.setRepeats(false);

        doc.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!atualizandoEstilos) temporizadorDebounce.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!atualizandoEstilos) temporizadorDebounce.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        addCaretListener(e -> atualizarPosicaoCursor());
    }

    public void definirRetornoAnalise(RetornoAnalise retorno) {
        this.retornoAnalise = retorno;
    }

    public void definirRetornoCursor(RetornoPosicaoCursor retorno) {
        this.retornoCursor = retorno;
    }

    public void executarAnalise() {
        String codigo = getText();
        AnalisadorLexico lexico = new AnalisadorLexico(codigo);
        List<Token> tokens = lexico.analisar();
        List<ErroLexico> errosLexicos = lexico.getErros();

        AnalisadorSintatico sintatico = new AnalisadorSintatico(tokens);
        List<ErroSintatico> errosSintaticos = sintatico.analisar();

        limparDestaquesErros();

        Highlighter highlighter = getHighlighter();
        for (ErroLexico err : errosLexicos) {
            adicionarSublinhado(highlighter, codigo.length(), err.getIndiceInicio(), err.getIndiceFim(), pintorErro);
        }

        for (ErroSintatico err : errosSintaticos) {
            PintorSublinhadoOndulado pintor = err.ehAviso() ? pintorAviso : pintorErro;
            adicionarSublinhado(highlighter, codigo.length(), err.getIndiceInicio(), err.getIndiceFim(), pintor);
        }

        aplicarDestaqueSintaxe(codigo, tokens);

        if (retornoAnalise != null) {
            retornoAnalise.aoFinalizarAnalise(tokens, errosLexicos, errosSintaticos);
        }
    }

    private void adicionarSublinhado(Highlighter highlighter, int tamCodigo, int inicioBruto, int fimBruto, PintorSublinhadoOndulado pintor) {
        int inicio = Math.max(0, Math.min(inicioBruto, tamCodigo));
        int fim = Math.max(inicio, Math.min(fimBruto, tamCodigo));
        if (inicio == fim && inicio > 0) {
            inicio = inicio - 1;
        }
        if (inicio < fim) {
            try {
                Object marcador = highlighter.addHighlight(inicio, fim, pintor);
                marcadoresDestaque.add(marcador);
            } catch (BadLocationException ignored) {
            }
        }
    }

    private void limparDestaquesErros() {
        Highlighter highlighter = getHighlighter();
        for (Object marcador : marcadoresDestaque) {
            highlighter.removeHighlight(marcador);
        }
        marcadoresDestaque.clear();
    }

    private void aplicarDestaqueSintaxe(String codigo, List<Token> tokens) {
        atualizandoEstilos = true;
        StyledDocument doc = getStyledDocument();

        doc.setCharacterAttributes(0, codigo.length(), estiloNormal, true);

        for (Token t : tokens) {
            int inicio = t.getIndiceInicio();
            int tamanho = t.getTamanho();
            if (inicio + tamanho <= codigo.length() && tamanho > 0) {
                Style estiloEscolhido = switch (t.getTipo().getCategoria()) {
                    case PALAVRA_RESERVADA, OPERADOR_LOGICO -> estiloPalavraChave;
                    case LITERAL_NUMERICO -> estiloNumero;
                    case LITERAL_TEXTO -> estiloTexto;
                    case OPERADOR_ARITMETICO, OPERADOR_RELACIONAL, OPERADOR_ATRIBUICAO -> estiloOperador;
                    default -> estiloNormal;
                };
                doc.setCharacterAttributes(inicio, tamanho, estiloEscolhido, false);
            }
        }

        destacarComentarios(codigo, doc);
        atualizandoEstilos = false;
    }

    private void destacarComentarios(String codigo, StyledDocument doc) {
        int tam = codigo.length();
        int i = 0;
        while (i < tam) {
            char c = codigo.charAt(i);

            if (c == '/' && i + 1 < tam && codigo.charAt(i + 1) == '/') {
                int inicio = i;
                while (i < tam && codigo.charAt(i) != '\n' && codigo.charAt(i) != '\r') {
                    i++;
                }
                doc.setCharacterAttributes(inicio, i - inicio, estiloComentario, false);
            } else if (c == '{') {
                int inicio = i;
                i++;
                while (i < tam && codigo.charAt(i) != '}') {
                    i++;
                }
                if (i < tam && codigo.charAt(i) == '}') {
                    i++;
                }
                doc.setCharacterAttributes(inicio, i - inicio, estiloComentario, false);
            } else if (c == '(' && i + 1 < tam && codigo.charAt(i + 1) == '*') {
                int inicio = i;
                i += 2;
                while (i < tam) {
                    if (codigo.charAt(i) == '*' && i + 1 < tam && codigo.charAt(i + 1) == ')') {
                        i += 2;
                        break;
                    }
                    i++;
                }
                doc.setCharacterAttributes(inicio, i - inicio, estiloComentario, false);
            } else if (c == '\'') {
                i++;
                while (i < tam && codigo.charAt(i) != '\'' && codigo.charAt(i) != '\n') {
                    i++;
                }
                if (i < tam && codigo.charAt(i) == '\'') {
                    i++;
                }
            } else {
                i++;
            }
        }
    }

    private void atualizarPosicaoCursor() {
        if (retornoCursor == null) return;
        int posicao = getCaretPosition();
        Element raiz = getDocument().getDefaultRootElement();
        int linha = raiz.getElementIndex(posicao);
        int coluna = posicao - raiz.getElement(linha).getStartOffset();
        retornoCursor.aoMoverCursor(linha + 1, coluna + 1);
    }

    public void irParaErro(int linha, int coluna, int tamanho) {
        try {
            Element raiz = getDocument().getDefaultRootElement();
            if (linha - 1 >= 0 && linha - 1 < raiz.getElementCount()) {
                Element elementoLinha = raiz.getElement(linha - 1);
                int deslocamento = elementoLinha.getStartOffset() + (coluna - 1);
                deslocamento = Math.min(deslocamento, elementoLinha.getEndOffset() - 1);
                deslocamento = Math.max(0, deslocamento);

                setCaretPosition(deslocamento);
                int fimSelecao = Math.min(deslocamento + tamanho, getDocument().getLength());
                select(deslocamento, fimSelecao);
                requestFocusInWindow();
            }
        } catch (Exception ignored) {
        }
    }
}
