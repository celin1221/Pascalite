package br.ufes.compiladores.pascalite.ide;

import br.ufes.compiladores.pascalite.ide.modelos.ModeloTabelaErros;
import br.ufes.compiladores.pascalite.ide.modelos.ModeloTabelaSimbolos;
import br.ufes.compiladores.pascalite.ide.modelos.ModeloTabelaSintatico;
import br.ufes.compiladores.pascalite.ide.modelos.ModeloTabelaTokens;
import br.ufes.compiladores.pascalite.lexico.AnalisadorLexico;
import br.ufes.compiladores.pascalite.lexico.ErroLexico;
import br.ufes.compiladores.pascalite.lexico.TabelaSimbolos;
import br.ufes.compiladores.pascalite.lexico.Token;
import br.ufes.compiladores.pascalite.sintatico.AnalisadorSintatico;
import br.ufes.compiladores.pascalite.sintatico.ErroSintatico;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class IDEPascalite extends JFrame {
    private final PainelEditorCodigo painelEditor;
    private final ModeloTabelaTokens modeloTabelaTokens;
    private final ModeloTabelaSimbolos modeloTabelaSimbolos;
    private final ModeloTabelaErros modeloTabelaErros;
    private final ModeloTabelaSintatico modeloTabelaSintatico;

    private final JTable tabelaTokens;
    private final JTable tabelaSimbolos;
    private final JTable tabelaErros;
    private final JTable tabelaSintatico;
    private final JTabbedPane abasInferiores;
    private final JTextArea areaConsole;

    private final JLabel rotuloPosicaoCursor;
    private final JLabel rotuloStatus;
    private final JComboBox<String> seletorExemplos;

    private File arquivoAtual = null;

    public IDEPascalite() {
        super("PascaLite IDE - Compilador (UFES)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 800);
        setLocationRelativeTo(null);

        modeloTabelaTokens = new ModeloTabelaTokens();
        modeloTabelaSimbolos = new ModeloTabelaSimbolos();
        modeloTabelaErros = new ModeloTabelaErros();
        modeloTabelaSintatico = new ModeloTabelaSintatico();

        tabelaTokens = criarTabelaEstilizada(modeloTabelaTokens);
        tabelaSimbolos = criarTabelaEstilizada(modeloTabelaSimbolos);
        tabelaErros = criarTabelaEstilizada(modeloTabelaErros);
        tabelaSintatico = criarTabelaEstilizada(modeloTabelaSintatico);

        tabelaErros.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int linhaSel = tabelaErros.getSelectedRow();
                    if (linhaSel >= 0) {
                        ErroLexico err = modeloTabelaErros.obterErroNaLinha(linhaSel);
                        if (err != null) {
                            painelEditor.irParaErro(err.getLinha(), err.getColuna(), err.getTamanho());
                        }
                    }
                }
            }
        });

        tabelaSintatico.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int linhaSel = tabelaSintatico.getSelectedRow();
                    if (linhaSel >= 0) {
                        ErroSintatico err = modeloTabelaSintatico.obterErroNaLinha(linhaSel);
                        if (err != null) {
                            painelEditor.irParaErro(err.getLinha(), err.getColuna(), err.getTamanho());
                        }
                    }
                }
            }
        });

        painelEditor = new PainelEditorCodigo();
        PainelNumeracaoLinha painelNumeros = new PainelNumeracaoLinha(painelEditor);

        JScrollPane scrollEditor = new JScrollPane(painelEditor);
        scrollEditor.setRowHeaderView(painelNumeros);
        scrollEditor.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 50)));

        abasInferiores = new JTabbedPane();
        abasInferiores.setFont(new Font("Segoe UI", Font.BOLD, 12));
        abasInferiores.addTab("Erros Léxicos (0)", new JScrollPane(tabelaErros));
        abasInferiores.addTab("Erros Sintáticos (0)", new JScrollPane(tabelaSintatico));
        abasInferiores.addTab("Fluxo de Tokens (0)", new JScrollPane(tabelaTokens));
        abasInferiores.addTab("Tabela de Símbolos", new JScrollPane(tabelaSimbolos));

        areaConsole = new JTextArea();
        areaConsole.setEditable(false);
        areaConsole.setBackground(new Color(24, 24, 24));
        areaConsole.setForeground(new Color(210, 210, 210));
        areaConsole.setFont(new Font("Consolas", Font.PLAIN, 12));
        abasInferiores.addTab("Console", new JScrollPane(areaConsole));

        JSplitPane splitPrincipal = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollEditor, abasInferiores);
        splitPrincipal.setResizeWeight(0.55);
        splitPrincipal.setDividerSize(6);

        JPanel barraStatus = new JPanel(new BorderLayout());
        barraStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 50, 50)),
                new EmptyBorder(4, 10, 4, 10)
        ));
        barraStatus.setBackground(new Color(30, 30, 30));

        rotuloStatus = new JLabel("Pronto | Dialeto PascaLite");
        rotuloStatus.setForeground(new Color(180, 180, 180));
        rotuloStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        rotuloPosicaoCursor = new JLabel("Linha: 1, Coluna: 1   |   UTF-8");
        rotuloPosicaoCursor.setForeground(new Color(180, 180, 180));
        rotuloPosicaoCursor.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        barraStatus.add(rotuloStatus, BorderLayout.WEST);
        barraStatus.add(rotuloPosicaoCursor, BorderLayout.EAST);

        painelEditor.definirRetornoCursor((linha, coluna) ->
                rotuloPosicaoCursor.setText(String.format("Linha: %d, Coluna: %d   |   UTF-8", linha, coluna)));

        painelEditor.definirRetornoAnalise(this::aoFinalizarAnalise);

        seletorExemplos = new JComboBox<>(new String[]{
                "-- Selecionar Exemplo --",
                "1. Programa Válido Completo (Anexo I)",
                "2. Programa com Erros Léxicos e Sintáticos",
                "3. Algoritmo Fatorial (Função e For)",
                "4. Registros e Enumerações"
        });
        seletorExemplos.addActionListener(this::tratarSelecaoExemplo);

        setJMenuBar(criarBarraMenu());
        add(criarBarraFerramentas(), BorderLayout.NORTH);
        add(splitPrincipal, BorderLayout.CENTER);
        add(barraStatus, BorderLayout.SOUTH);

        carregarExemploValido();
    }

    private JTable criarTabelaEstilizada(javax.swing.table.TableModel modelo) {
        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(24);
        tabela.setFont(new Font("Consolas", Font.PLAIN, 13));
        tabela.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabela.getTableHeader().setBackground(new Color(40, 40, 40));
        tabela.getTableHeader().setForeground(new Color(220, 220, 220));
        tabela.setBackground(new Color(28, 28, 28));
        tabela.setForeground(new Color(220, 220, 220));
        tabela.setGridColor(new Color(50, 50, 50));
        tabela.setSelectionBackground(new Color(60, 90, 140));
        tabela.setSelectionForeground(Color.WHITE);
        return tabela;
    }

    private JMenuBar criarBarraMenu() {
        JMenuBar mb = new JMenuBar();

        JMenu menuArquivo = new JMenu("Arquivo");
        JMenuItem itemNovo = new JMenuItem("Novo");
        itemNovo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        itemNovo.addActionListener(e -> novoArquivo());

        JMenuItem itemAbrir = new JMenuItem("Abrir...");
        itemAbrir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        itemAbrir.addActionListener(e -> abrirArquivo());

        JMenuItem itemSalvar = new JMenuItem("Salvar");
        itemSalvar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        itemSalvar.addActionListener(e -> salvarArquivo(false));

        JMenuItem itemSalvarComo = new JMenuItem("Salvar Como...");
        itemSalvarComo.addActionListener(e -> salvarArquivo(true));

        JMenuItem itemSair = new JMenuItem("Sair");
        itemSair.addActionListener(e -> System.exit(0));

        menuArquivo.add(itemNovo);
        menuArquivo.add(itemAbrir);
        menuArquivo.add(itemSalvar);
        menuArquivo.add(itemSalvarComo);
        menuArquivo.addSeparator();
        menuArquivo.add(itemSair);

        JMenu menuCompilar = new JMenu("Compilar");
        JMenuItem itemExecutar = new JMenuItem("Executar Análise (F5)");
        itemExecutar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        itemExecutar.addActionListener(e -> executarAnaliseLexica());

        JMenuItem itemLimpar = new JMenuItem("Limpar Saídas");
        itemLimpar.addActionListener(e -> limparSaidas());

        menuCompilar.add(itemExecutar);
        menuCompilar.add(itemLimpar);

        JMenu menuExemplos = new JMenu("Exemplos");
        JMenuItem ex1 = new JMenuItem("1. Programa Válido (Anexo I)");
        ex1.addActionListener(e -> carregarExemploValido());
        JMenuItem ex2 = new JMenuItem("2. Programa com Erros");
        ex2.addActionListener(e -> carregarExemploErros());
        JMenuItem ex3 = new JMenuItem("3. Fatorial (Função e For)");
        ex3.addActionListener(e -> carregarExemploFatorial());
        JMenuItem ex4 = new JMenuItem("4. Registros e Enumerações");
        ex4.addActionListener(e -> carregarExemploBonus());

        menuExemplos.add(ex1);
        menuExemplos.add(ex2);
        menuExemplos.add(ex3);
        menuExemplos.add(ex4);

        JMenu menuAjuda = new JMenu("Ajuda");
        JMenuItem itemGramatica = new JMenuItem("Gramática do Dialeto (Anexo I)");
        itemGramatica.addActionListener(e -> exibirDialogoGramatica());
        JMenuItem itemSobre = new JMenuItem("Sobre o PascaLite");
        itemSobre.addActionListener(e -> exibirDialogoSobre());

        menuAjuda.add(itemGramatica);
        menuAjuda.add(itemSobre);

        mb.add(menuArquivo);
        mb.add(menuCompilar);
        mb.add(menuExemplos);
        mb.add(menuAjuda);
        return mb;
    }

    private JToolBar criarBarraFerramentas() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        tb.setBackground(new Color(38, 38, 38));
        tb.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(55, 55, 55)));

        JButton btnNovo = new JButton("Novo");
        btnNovo.addActionListener(e -> novoArquivo());

        JButton btnAbrir = new JButton("Abrir");
        btnAbrir.addActionListener(e -> abrirArquivo());

        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.addActionListener(e -> salvarArquivo(false));

        JButton btnCompilar = new JButton("▶ Compilar (F5)");
        btnCompilar.setToolTipText("Executa a análise léxica e sintática");
        btnCompilar.setBackground(new Color(40, 120, 60));
        btnCompilar.setForeground(Color.WHITE);
        btnCompilar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCompilar.addActionListener(e -> executarAnaliseLexica());

        JButton btnLimpar = new JButton("Limpar");
        btnLimpar.addActionListener(e -> limparSaidas());

        tb.add(Box.createHorizontalStrut(5));
        tb.add(btnNovo);
        tb.add(btnAbrir);
        tb.add(btnSalvar);
        tb.addSeparator();
        tb.add(btnCompilar);
        tb.addSeparator();
        tb.add(btnLimpar);
        tb.add(Box.createHorizontalStrut(20));
        tb.add(new JLabel("Exemplo: "));
        tb.add(seletorExemplos);
        tb.add(Box.createHorizontalGlue());

        return tb;
    }

    public void executarAnaliseLexica() {
        String codigo = painelEditor.getText();
        TabelaSimbolos tabSimbolos = new TabelaSimbolos();
        AnalisadorLexico lexico = new AnalisadorLexico(codigo, tabSimbolos);

        long inicioTempo = System.currentTimeMillis();
        List<Token> tokens = lexico.analisar();
        List<ErroLexico> errosLexicos = lexico.getErros();

        AnalisadorSintatico sintatico = new AnalisadorSintatico(tokens);
        List<ErroSintatico> errosSintaticos = sintatico.analisar();
        long tempoDecorrido = System.currentTimeMillis() - inicioTempo;

        modeloTabelaTokens.definirTokens(tokens);
        modeloTabelaSimbolos.atualizar(tabSimbolos);
        modeloTabelaErros.definirErros(errosLexicos);
        modeloTabelaSintatico.definirErros(errosSintaticos);

        abasInferiores.setTitleAt(0, "Erros Léxicos (" + errosLexicos.size() + ")");
        abasInferiores.setTitleAt(1, "Erros Sintáticos (" + errosSintaticos.size() + ")");
        abasInferiores.setTitleAt(2, "Fluxo de Tokens (" + tokens.size() + ")");

        StringBuilder log = new StringBuilder();
        log.append("ANÁLISE LÉXICA E SINTÁTICA - PASCALITE\n");
        log.append("------------------------------------------------------------\n");
        log.append(String.format("Tempo de execução: %d ms | Tokens: %d | Símbolos: %d\n",
                tempoDecorrido, tokens.size(), tabSimbolos.obterTodosSimbolos().size()));
        log.append(String.format("Erros léxicos: %d | Ocorrências sintáticas: %d\n",
                errosLexicos.size(), errosSintaticos.size()));
        log.append("------------------------------------------------------------\n");

        if (errosLexicos.isEmpty() && !sintatico.possuiErros()) {
            log.append("Compilação concluída sem erros.\n");
            for (ErroSintatico aviso : errosSintaticos) {
                log.append(String.format("  [Aviso - Linha %d, Col %d] %s\n",
                        aviso.getLinha(), aviso.getColuna(), aviso.getMensagem()));
            }
            rotuloStatus.setText("Análise concluída sem erros.");
            rotuloStatus.setForeground(new Color(100, 220, 100));
            if (!errosSintaticos.isEmpty()) {
                abasInferiores.setSelectedIndex(1);
            } else {
                abasInferiores.setSelectedIndex(2);
            }
        } else {
            if (!errosLexicos.isEmpty()) {
                log.append("\nErros Léxicos:\n");
                for (ErroLexico err : errosLexicos) {
                    log.append(String.format("  [Linha %d, Coluna %d] %s ('%s')\n",
                            err.getLinha(), err.getColuna(), err.getMensagem(), err.getLexemaInvalido()));
                }
            }
            if (!errosSintaticos.isEmpty()) {
                log.append("\nAnálise Sintática:\n");
                for (ErroSintatico err : errosSintaticos) {
                    String prefixo = err.ehAviso() ? "Aviso" : "Erro";
                    log.append(String.format("  [%s - Linha %d, Coluna %d] %s (próximo a '%s')\n",
                            prefixo, err.getLinha(), err.getColuna(), err.getMensagem(), err.getLexema()));
                }
            }

            rotuloStatus.setText(String.format("Foram encontrados %d erro(s) léxico(s) e %d ocorrência(s) sintática(s).",
                    errosLexicos.size(), errosSintaticos.size()));
            rotuloStatus.setForeground(new Color(245, 100, 100));

            if (!errosLexicos.isEmpty()) {
                abasInferiores.setSelectedIndex(0);
            } else {
                abasInferiores.setSelectedIndex(1);
            }
        }

        areaConsole.setText(log.toString());
        painelEditor.executarAnalise();
    }

    private void aoFinalizarAnalise(List<Token> tokens, List<ErroLexico> errosLexicos, List<ErroSintatico> errosSintaticos) {
        abasInferiores.setTitleAt(0, "Erros Léxicos (" + errosLexicos.size() + ")");
        abasInferiores.setTitleAt(1, "Erros Sintáticos (" + errosSintaticos.size() + ")");
        modeloTabelaErros.definirErros(errosLexicos);
        modeloTabelaSintatico.definirErros(errosSintaticos);

        long qtdErrosSintaticos = errosSintaticos.stream().filter(e -> !e.ehAviso()).count();
        if (!errosLexicos.isEmpty() || qtdErrosSintaticos > 0) {
            rotuloStatus.setText(String.format("Erros detectados: %d léxico(s), %d sintático(s)",
                    errosLexicos.size(), qtdErrosSintaticos));
            rotuloStatus.setForeground(new Color(245, 100, 100));
        } else if (!errosSintaticos.isEmpty()) {
            rotuloStatus.setText("Código válido com avisos sintáticos.");
            rotuloStatus.setForeground(new Color(240, 190, 80));
        } else {
            rotuloStatus.setText("Código válido: nenhum erro léxico ou sintático.");
            rotuloStatus.setForeground(new Color(150, 220, 150));
        }
    }

    private void tratarSelecaoExemplo(ActionEvent e) {
        int idx = seletorExemplos.getSelectedIndex();
        switch (idx) {
            case 1 -> carregarExemploValido();
            case 2 -> carregarExemploErros();
            case 3 -> carregarExemploFatorial();
            case 4 -> carregarExemploBonus();
        }
    }

    private void carregarExemploValido() {
        String codigo = """
                PROGRAM ExemploValido;
                
                VAR
                   contador, limite : INTEGER;
                   media : REAL;
                   letra : CHAR;
                   mensagem : STRING;
                
                CONST
                   MAX_ITERACOES : INTEGER = 100;
                   PI = 3.14159;
                   TITULO = 'Compilador PascaLite';
                
                PROCEDURE ImprimeResult(valor : INTEGER);
                VAR
                   status : STRING;
                BEGIN
                   IF valor >= 50 THEN
                      status := 'Aprovado'
                   ELSE
                      status := 'Reprovado';
                END;
                
                BEGIN
                   contador := 0;
                   limite := 10;
                   media := 8.75;
                   mensagem := 'Bem-vindo ao dialeto Pascal!';
                
                   WHILE contador < limite DO
                      contador := contador + 1;
                
                   REPEAT
                      contador := contador - 1;
                   UNTIL contador = 0;
                
                   ImprimeResult(limite);
                END.
                """;
        painelEditor.setText(codigo);
        painelEditor.setCaretPosition(0);
        executarAnaliseLexica();
    }

    private void carregarExemploErros() {
        String codigo = """
                PROGRAM TesteErros;
                VAR
                   nomeValido : STRING;
                   esteIdentificadorTemMaisDeQuinzeCaracteres : INTEGER;
                   salario$Base : REAL;
                   taxa : REAL;
                BEGIN
                   taxa := 42.abc;
                   
                   // Aviso sintático: instrução apenas com identificador
                   nomeValido;
                   
                   // Erro sintático: falta o THEN no IF
                   IF taxa > 10
                      taxa := 0;
                      
                   nomeValido := 'Texto sem aspas no final;
                END.
                """;
        painelEditor.setText(codigo);
        painelEditor.setCaretPosition(0);
        executarAnaliseLexica();
    }

    private void carregarExemploFatorial() {
        String codigo = """
                PROGRAM CalculoFatorial;
                VAR
                   numero, resultado, i : INTEGER;
                
                FUNCTION Fatorial(n : INTEGER) : INTEGER;
                VAR
                   res : INTEGER;
                BEGIN
                   IF n <= 1 THEN
                      res := 1
                   ELSE
                      res := n * Fatorial(n - 1);
                END;
                
                BEGIN
                   numero := 5;
                   resultado := 1;
                   
                   FOR i := 1 TO numero DO
                      resultado := resultado * i;
                END.
                """;
        painelEditor.setText(codigo);
        painelEditor.setCaretPosition(0);
        executarAnaliseLexica();
    }

    private void carregarExemploBonus() {
        String codigo = """
                PROGRAM ExemploBonus;
                
                TYPE
                   Pessoa = RECORD
                      nome : STRING;
                      idade : INTEGER;
                      salario : REAL;
                   END;
                
                   DiasSemana = ENUM OF (Segunda, Terca, Quarta, Quinta, Sexta, Sabado, Domingo);
                
                VAR
                   funcionario : Pessoa;
                   diaTrabalho : DiasSemana;
                
                BEGIN
                   diaTrabalho := Segunda;
                   funcionario := 'Registrado';
                END.
                """;
        painelEditor.setText(codigo);
        painelEditor.setCaretPosition(0);
        executarAnaliseLexica();
    }

    private void novoArquivo() {
        painelEditor.setText("");
        arquivoAtual = null;
        setTitle("PascaLite IDE - [Novo Arquivo]");
        limparSaidas();
    }

    private void abrirArquivo() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                String conteudo = Files.readString(f.toPath());
                painelEditor.setText(conteudo);
                arquivoAtual = f;
                setTitle("PascaLite IDE - [" + f.getName() + "]");
                executarAnaliseLexica();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao abrir arquivo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void salvarArquivo(boolean salvarComo) {
        if (arquivoAtual == null || salvarComo) {
            JFileChooser fc = new JFileChooser();
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                arquivoAtual = fc.getSelectedFile();
            } else {
                return;
            }
        }
        try {
            Files.writeString(arquivoAtual.toPath(), painelEditor.getText());
            setTitle("PascaLite IDE - [" + arquivoAtual.getName() + "]");
            JOptionPane.showMessageDialog(this, "Arquivo salvo com sucesso!", "Salvar", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar arquivo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparSaidas() {
        modeloTabelaTokens.definirTokens(null);
        modeloTabelaErros.definirErros(null);
        modeloTabelaSintatico.definirErros(null);
        modeloTabelaSimbolos.atualizar(new TabelaSimbolos());
        areaConsole.setText("");
        abasInferiores.setTitleAt(0, "Erros Léxicos (0)");
        abasInferiores.setTitleAt(1, "Erros Sintáticos (0)");
        abasInferiores.setTitleAt(2, "Fluxo de Tokens (0)");
        rotuloStatus.setText("Pronto.");
    }

    private void exibirDialogoGramatica() {
        String msg = """
                DIALETO PASCALITE - GRAMÁTICA (ANEXO I):
                
                programa → PROGRAM ID; declaracoes BEGIN instrucoes END .
                bloco → BEGIN instrucoes END ;
                declaracoes → declaracaoVariavel declaracaoConstante declProcedimento
                declaracaoConstante → CONST declConsList | ε
                declConsList → ID : tipo = valor; declConsList | ID = valor; declConsList | ε
                declaracaoVariavel → VAR declVarList | ε
                declVarList → declVar declVarList | ε
                declVar → variavel conjuntoIds : tipo ;
                conjuntoIds → , variavel conjuntoIds | ε
                tipo → INTEGER | REAL | CHAR | STRING
                valor → unario | LITERAL
                declProcedimento → declProc declProcedimento | ε
                declProc → PROCEDURE ID(parametros); declaracaoVariavel bloco |
                            FUNCTION ID(parametros) : tipo; declaracaoVariavel bloco
                parametros → declVarList | ε
                instrucoes → inst instrucoes | ε
                inst → ID := expr ; | ID [expr] := expr ; | ID (parametros2) ; | ID ; |
                       IF expr THEN inst | IF expr THEN inst ELSE inst |
                       WHILE expr DO inst | REPEAT inst UNTIL expr ; |
                       FOR ID := expr TO/DOWNTO expr DO inst |
                       BREAK ; | CONTINUE ; | bloco
                expr → exprComparacao expr2
                expr2 → OU exprComparacao expr2 | E exprComparacao expr2 | ε
                exprComparacao → exprOp exprComparacao2
                exprComparacao2 → = exprOp exprComparacao2 | <> exprOp exprComparacao2 |
                                  < exprOp exprComparacao2 | <= exprOp exprComparacao2 |
                                  > exprOp exprComparacao2 | >= exprOp exprComparacao2 | ε
                exprOp → termo exprOp2
                exprOp2 → + termo exprOp2 | - termo exprOp2 | ε
                termo → unario termo2
                termo2 → * unario termo2 | / unario termo2 | ε
                unario → + fator | - fator | fator
                fator → (expr) | variavel | NUM | LITERAL
                variavel → ID | ID[exprOp]
                """;
        JTextArea ta = new JTextArea(msg);
        ta.setFont(new Font("Consolas", Font.PLAIN, 12));
        ta.setEditable(false);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(650, 480));
        JOptionPane.showMessageDialog(this, sp, "Gramática do Dialeto PascaLite", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exibirDialogoSobre() {
        String msg = """
                PascaLite IDE & Compilador
                Trabalho da Disciplina de Compiladores
                Ciência da Computação - UFES
                Professor: Rodrigo Freitas Silva
                """;
        JOptionPane.showMessageDialog(this, msg, "Sobre o PascaLite", JOptionPane.INFORMATION_MESSAGE);
    }
}
