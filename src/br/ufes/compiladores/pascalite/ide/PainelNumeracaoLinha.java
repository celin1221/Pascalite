package br.ufes.compiladores.pascalite.ide;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import java.awt.*;

/**
 * Componente de barra lateral que exibe os números de linha sincronizados com o editor.
 */
public class PainelNumeracaoLinha extends JPanel {
    private final JTextComponent editor;
    private int ultimosDigitos = 0;

    public PainelNumeracaoLinha(JTextComponent editor) {
        this.editor = editor;
        setBackground(new Color(30, 30, 30));
        setForeground(new Color(130, 130, 130));
        setFont(new Font("Consolas", Font.PLAIN, 14));

        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { atualizarLargura(); }
            @Override
            public void removeUpdate(DocumentEvent e) { atualizarLargura(); }
            @Override
            public void changedUpdate(DocumentEvent e) { atualizarLargura(); }
        });
        atualizarLargura();
    }

    private void atualizarLargura() {
        Element raiz = editor.getDocument().getDefaultRootElement();
        int totalLinhas = raiz.getElementCount();
        int digitos = Math.max(2, String.valueOf(totalLinhas).length());
        if (digitos != ultimosDigitos) {
            ultimosDigitos = digitos;
            FontMetrics fm = getFontMetrics(getFont());
            int largura = fm.charWidth('0') * digitos + 16;
            setPreferredSize(new Dimension(largura, Integer.MAX_VALUE - 1000));
            revalidate();
            repaint();
        } else {
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        FontMetrics fm = g2d.getFontMetrics(getFont());
        Rectangle clip = g2d.getClipBounds();
        Element raiz = editor.getDocument().getDefaultRootElement();

        int inicioOffset = editor.viewToModel2D(new Point(0, clip.y));
        int fimOffset = editor.viewToModel2D(new Point(0, clip.y + clip.height));

        int linhaInicial = raiz.getElementIndex(inicioOffset);
        int linhaFinal = raiz.getElementIndex(fimOffset);

        for (int i = linhaInicial; i <= linhaFinal; i++) {
            Element elementoLinha = raiz.getElement(i);
            try {
                Rectangle r = (Rectangle) editor.modelToView2D(elementoLinha.getStartOffset());
                if (r != null) {
                    String strNumLinha = String.valueOf(i + 1);
                    int larguraTexto = fm.stringWidth(strNumLinha);
                    int x = getWidth() - larguraTexto - 8;
                    int y = r.y + fm.getAscent();

                    g2d.setColor(getForeground());
                    g2d.drawString(strNumLinha, x, y);
                }
            } catch (Exception ignored) {
            }
        }
    }
}
