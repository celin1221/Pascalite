package br.ufes.compiladores.pascalite.ide;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;

/**
 * Desenha uma linha ondulada vermelha sob o trecho de código com erro léxico,
 * idêntico ao comportamento de IDEs profissionais (VS Code, IntelliJ).
 */
public class PintorSublinhadoOndulado extends DefaultHighlighter.DefaultHighlightPainter {
    private final Color corOnda;

    public PintorSublinhadoOndulado(Color corOnda) {
        super(corOnda);
        this.corOnda = corOnda;
    }

    public PintorSublinhadoOndulado() {
        this(new Color(230, 40, 40)); // Vermelho vibrante
    }

    @Override
    public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, javax.swing.text.View view) {
        g.setColor(corOnda);
        Rectangle r;

        try {
            Shape shape = view.modelToView(offs0, javax.swing.text.Position.Bias.Forward,
                    offs1, javax.swing.text.Position.Bias.Backward, bounds);
            r = (shape instanceof Rectangle) ? (Rectangle) shape : shape.getBounds();
        } catch (BadLocationException e) {
            return null;
        }

        int x1 = r.x;
        int x2 = r.x + r.width;
        int y = r.y + r.height - 2;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(1.2f));

        // Desenha a linha ondulada (senoidal/zigzag) sob o texto com erro
        int passoOnda = 3;
        int alturaOnda = 2;
        int xAtual = x1;
        int direcao = 1;

        while (xAtual < x2) {
            int proximoX = Math.min(xAtual + passoOnda, x2);
            int proximoY = (direcao > 0) ? (y - alturaOnda) : y;
            g2d.drawLine(xAtual, (direcao > 0) ? y : (y - alturaOnda), proximoX, proximoY);
            xAtual = proximoX;
            direcao = -direcao;
        }

        g2d.dispose();
        return r;
    }
}
