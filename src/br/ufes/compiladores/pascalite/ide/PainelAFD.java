package br.ufes.compiladores.pascalite.ide;

import br.ufes.compiladores.pascalite.lexico.afd.EstadoAFD;
import br.ufes.compiladores.pascalite.lexico.afd.ModeloAFD;
import br.ufes.compiladores.pascalite.lexico.afd.TransicaoAFD;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;

/**
 * Painel interativo para visualização do Autômato Finito Determinístico (AFD) do PascaLite.
 * Exibe tanto o grafo visual das transições quanto a tabela formal de transição de estados.
 */
public class PainelAFD extends JPanel {
    private final ModeloAFD modeloAfd;

    public PainelAFD() {
        this.modeloAfd = new ModeloAFD();
        setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.65);

        // Painel Superior: Grafo 2D do AFD
        CanvasAFD canvas = new CanvasAFD(modeloAfd);
        JScrollPane scrollCanvas = new JScrollPane(canvas);
        scrollCanvas.setBorder(BorderFactory.createTitledBorder("Diagrama de Estados do AFD (Visualizador Gráfico Java 2D)"));

        // Painel Inferior: Tabela de Transições e Descrição Formal
        JPanel painelInferior = new JPanel(new BorderLayout());
        painelInferior.setBorder(BorderFactory.createTitledBorder("Tabela Formal de Transições de Estados (δ) e 5-Tupla M = (Q, Σ, δ, q0, F)"));

        String[] colunas = {"Estado Origem", "Símbolo de Entrada", "Estado Destino", "Tipo de Token Produzido / Ação"};
        DefaultTableModel modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        for (TransicaoAFD t : modeloAfd.getTransicoes()) {
            String token = t.getEstadoDestino().ehFinal() ? t.getEstadoDestino().getTokenProduzido() : "(Transitório)";
            modeloTabela.addRow(new Object[]{
                    t.getEstadoOrigem().getId() + " (" + t.getEstadoOrigem().getNome() + ")",
                    t.getRotulo(),
                    t.getEstadoDestino().getId() + " (" + t.getEstadoDestino().getNome() + ")",
                    token
            });
        }

        JTable tabela = new JTable(modeloTabela);
        tabela.setRowHeight(22);
        tabela.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane scrollTabela = new JScrollPane(tabela);

        painelInferior.add(scrollTabela, BorderLayout.CENTER);

        splitPane.setTopComponent(scrollCanvas);
        splitPane.setBottomComponent(painelInferior);
        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Canvas gráfico que desenha os nós e transições do autômato determinístico.
     */
    private static class CanvasAFD extends JPanel {
        private final ModeloAFD modelo;
        private final Map<String, Point> posicoesNos = new HashMap<>();

        public CanvasAFD(ModeloAFD modelo) {
            this.modelo = modelo;
            setBackground(new Color(24, 24, 24));
            setPreferredSize(new Dimension(1400, 750));
            calcularPosicionamento();
        }

        private void calcularPosicionamento() {
            // Posicionamento espacial de cada estado
            posicoesNos.put("q0", new Point(100, 360));

            // Ramo Identificadores
            posicoesNos.put("q_id", new Point(340, 100));

            // Ramo Números
            posicoesNos.put("q_int", new Point(340, 200));
            posicoesNos.put("q_ponto_num", new Point(540, 200));
            posicoesNos.put("q_real", new Point(740, 200));

            // Ramo Literais (Strings)
            posicoesNos.put("q_str", new Point(340, 290));
            posicoesNos.put("q_str_aspa", new Point(540, 290));

            // Ramo Relacionais
            posicoesNos.put("q_menor", new Point(340, 370));
            posicoesNos.put("q_menor_igual", new Point(540, 340));
            posicoesNos.put("q_diferente", new Point(540, 400));
            posicoesNos.put("q_maior", new Point(340, 470));
            posicoesNos.put("q_maior_igual", new Point(540, 470));
            posicoesNos.put("q_igual", new Point(340, 540));

            // Ramo Atribuição e Dois Pontos
            posicoesNos.put("q_dois_pontos", new Point(340, 620));
            posicoesNos.put("q_atribuicao", new Point(540, 620));

            // Ramo Aritméticos
            posicoesNos.put("q_soma", new Point(740, 360));
            posicoesNos.put("q_sub", new Point(740, 420));
            posicoesNos.put("q_mult", new Point(740, 480));
            posicoesNos.put("q_barra", new Point(740, 540));

            // Ramo Comentários
            posicoesNos.put("q_com_linha", new Point(940, 540));
            posicoesNos.put("q_com_chaves", new Point(940, 290));
            posicoesNos.put("q_fim_chaves", new Point(1140, 290));
            posicoesNos.put("q_abre_par", new Point(940, 380));
            posicoesNos.put("q_com_parest", new Point(1100, 380));
            posicoesNos.put("q_com_partalvez", new Point(1250, 380));
            posicoesNos.put("q_fim_parest", new Point(1250, 460));

            // Delimitadores restantes
            posicoesNos.put("q_fecha_par", new Point(940, 440));
            posicoesNos.put("q_abre_col", new Point(940, 140));
            posicoesNos.put("q_fecha_col", new Point(1100, 140));
            posicoesNos.put("q_pt_virgula", new Point(940, 210));
            posicoesNos.put("q_virgula", new Point(1100, 210));
            posicoesNos.put("q_ponto", new Point(1250, 210));

            // Erro
            posicoesNos.put("q_erro", new Point(340, 690));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 1. Desenha transições (arestas com setas)
            for (TransicaoAFD t : modelo.getTransicoes()) {
                Point p1 = posicoesNos.get(t.getEstadoOrigem().getId());
                Point p2 = posicoesNos.get(t.getEstadoDestino().getId());
                if (p1 != null && p2 != null) {
                    desenharTransicao(g2d, p1, p2, t);
                }
            }

            // 2. Desenha nós (estados)
            for (EstadoAFD s : modelo.getEstados().values()) {
                Point p = posicoesNos.get(s.getId());
                if (p != null) {
                    desenharNoEstado(g2d, p, s);
                }
            }

            // 3. Seta de estado inicial
            Point pontoInicio = posicoesNos.get("q0");
            if (pontoInicio != null) {
                g2d.setColor(new Color(60, 180, 75));
                g2d.setStroke(new BasicStroke(2.5f));
                g2d.drawLine(pontoInicio.x - 50, pontoInicio.y, pontoInicio.x - 22, pontoInicio.y);
                desenharPontaSeta(g2d, pontoInicio.x - 22, pontoInicio.y, 0);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2d.drawString("Início", pontoInicio.x - 55, pontoInicio.y - 8);
            }
        }

        private void desenharNoEstado(Graphics2D g2d, Point p, EstadoAFD estado) {
            int raio = 22;
            int diametro = raio * 2;

            // Cor de fundo
            if ("q0".equals(estado.getId())) {
                g2d.setColor(new Color(40, 80, 120));
            } else if ("q_erro".equals(estado.getId())) {
                g2d.setColor(new Color(130, 40, 40));
            } else if (estado.ehFinal()) {
                g2d.setColor(new Color(35, 75, 50));
            } else {
                g2d.setColor(new Color(50, 50, 55));
            }
            g2d.fillOval(p.x - raio, p.y - raio, diametro, diametro);

            // Borda
            g2d.setColor(estado.ehFinal() ? new Color(100, 220, 120) : new Color(180, 180, 180));
            g2d.setStroke(new BasicStroke(estado.ehFinal() ? 2.5f : 1.5f));
            g2d.drawOval(p.x - raio, p.y - raio, diametro, diametro);

            // Círculo duplo para aceitação
            if (estado.ehFinal()) {
                g2d.drawOval(p.x - raio + 4, p.y - raio + 4, diametro - 8, diametro - 8);
            }

            // Identificador do estado
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Consolas", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            int strW = fm.stringWidth(estado.getId());
            g2d.drawString(estado.getId(), p.x - strW / 2, p.y + fm.getAscent() / 2 - 2);

            // Legenda abaixo do estado
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2d.setColor(new Color(190, 190, 190));
            FontMetrics fmL = g2d.getFontMetrics();
            int strWL = fmL.stringWidth(estado.getNome());
            g2d.drawString(estado.getNome(), p.x - strWL / 2, p.y + raio + 13);
        }

        private void desenharTransicao(Graphics2D g2d, Point p1, Point p2, TransicaoAFD t) {
            g2d.setColor(new Color(150, 150, 160));
            g2d.setStroke(new BasicStroke(1.2f));

            if (p1.equals(p2)) {
                // Auto-loop
                int largLoop = 28;
                int altLoop = 26;
                g2d.drawOval(p1.x - 14, p1.y - 22 - altLoop, largLoop, altLoop);
                g2d.setColor(new Color(255, 215, 0));
                g2d.setFont(new Font("Consolas", Font.BOLD, 10));
                g2d.drawString(t.getRotulo(), p1.x - 10, p1.y - 26 - altLoop);
                return;
            }

            double angulo = Math.atan2(p2.y - p1.y, p2.x - p1.x);
            int raioNo = 22;
            int startX = (int) (p1.x + raioNo * Math.cos(angulo));
            int startY = (int) (p1.y + raioNo * Math.sin(angulo));
            int endX = (int) (p2.x - raioNo * Math.cos(angulo));
            int endY = (int) (p2.y - raioNo * Math.sin(angulo));

            g2d.drawLine(startX, startY, endX, endY);
            desenharPontaSeta(g2d, endX, endY, angulo);

            // Rótulo da transição
            int midX = (startX + endX) / 2;
            int midY = (startY + endY) / 2 - 4;
            g2d.setColor(new Color(255, 215, 0));
            g2d.setFont(new Font("Consolas", Font.BOLD, 10));
            g2d.drawString(t.getRotulo(), midX, midY);
        }

        private void desenharPontaSeta(Graphics2D g2d, int x, int y, double angulo) {
            AffineTransform tx = g2d.getTransform();
            g2d.translate(x, y);
            g2d.rotate(angulo);
            int tamSeta = 6;
            Polygon seta = new Polygon();
            seta.addPoint(0, 0);
            seta.addPoint(-tamSeta, -tamSeta / 2);
            seta.addPoint(-tamSeta, tamSeta / 2);
            g2d.fill(seta);
            g2d.setTransform(tx);
        }
    }
}
