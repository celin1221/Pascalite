package br.ufes.compiladores.pascalite.ide;

import br.ufes.compiladores.pascalite.ide.modelos.ModeloTabelaErros;
import br.ufes.compiladores.pascalite.ide.modelos.ModeloTabelaSimbolos;
import br.ufes.compiladores.pascalite.ide.modelos.ModeloTabelaTokens;
import br.ufes.compiladores.pascalite.lexico.AnalisadorLexico;
import br.ufes.compiladores.pascalite.lexico.ErroLexico;
import br.ufes.compiladores.pascalite.lexico.TabelaSimbolos;
import br.ufes.compiladores.pascalite.lexico.Token;

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

/**
 * IDE completa para desenvolvimento e compilação do dialeto PascaLite.
 * Atende a todos os requisitos do Trabalho de Compiladores (UFES - Prof. Rodrigo Freitas Silva).
 */
public class IDEPascalite extends JFrame {
    private final PainelEditorCodigo painelEditor;
    private final ModeloTabelaTokens modeloTabelaTokens;
    private final ModeloTabelaSimbolos modeloTabelaSimbolos;
    private final ModeloTabelaErros modeloTabelaErros;

    private final JTable tabelaTokens;
    private final JTable tabelaSímbolos;
    private final JTable tabelaErros;
    private final JTabbedPane abasInferiores;
    private final JTextArea areaConsole;

    private final JLabel rotuloPosicaoCursor;
    private final JLabel rotuloStatus;
    private final JComboBox<String> seletorExemplos;

    private File arquivoAtual = null;

    public IDEPascalite() {
        super("PascaLite IDE - Ambiente de Desenvolvimento & Compilador (UFES)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 800);
        setLocationRelativeTo(null);

        // Modelos e Tabelas
        modeloTabelaTokens = new ModeloTabelaTokens();
        modeloTabelaSimbolos = new ModeloTabelaSimbolos();
        modeloTabelaErros = new ModeloTabelaErros();

        tabelaTokens = criarTabelaEstilizada(modeloTabelaTokens);
        tabelaSímbolos = criarTabelaEstilizada(modeloTabelaSimbolos);
        tabelaErros = criarTabelaEstilizada(modeloTabelaErros);

        // Clique duplo na tabela de erros leva até o erro no código fonte
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

        // Editor com suporte a linhas e análise em tempo real
        painelEditor = new PainelEditorCodigo();
        PainelNumeracaoLinha painelNumeros = new PainelNumeracaoLinha(painelEditor);

        JScrollPane scrollEditor = new JScrollPane(painelEditor);
        scrollEditor.setRowHeaderView(painelNumeros);
        scrollEditor.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 50)));

        // Aba inferior de resultados
        abasInferiores = new JTabbedPane();
        abasInferiores.setFont(new Font("Segoe UI", Font.BOLD, 12));
        abasInferiores.addTab("Erros Léxicos (0)", new JScrollPane(tabelaErros));
        abasInferiores.addTab("Fluxo de Tokens (0)", new JScrollPane(tabelaTokens));
        abasInferiores.addTab("Tabela de Símbolos", new JScrollPane(tabelaSímbolos));
        abasInferiores.addTab("Autômato Finito (AFD)", new PainelAFD());

        areaConsole = new JTextArea();
        areaConsole.setEditable(false);
        areaConsole.setBackground(new Color(24, 24, 24));
        areaConsole.setForeground(new Color(210, 210, 210));
        areaConsole.setFont(new Font("Consolas", Font.PLAIN, 12));
        abasInferiores.addTab("Console / Registro", new JScrollPane(areaConsole));

        // Divisão Vertical (Editor / Painéis)
        JSplitPane splitPrincipal = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollEditor, abasInferiores);
        splitPrincipal.setResizeWeight(0.55);
        splitPrincipal.setDividerSize(6);

        // Barra de Status
        JPanel barraStatus = new JPanel(new BorderLayout());
        barraStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 50, 50)),
                new EmptyBorder(4, 10, 4, 10)
        ));
        barraStatus.setBackground(new Color(30, 30, 30));

        rotuloStatus = new JLabel("Pronto | Dialeto PascaLite (Pascal-UFES)");
        rotuloStatus.setForeground(new Color(180, 180, 180));
        rotuloStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        rotuloPosicaoCursor = new JLabel("Linha: 1, Coluna: 1   |   UTF-8   |   Insensível a Maiúsculas");
        rotuloPosicaoCursor.setForeground(new Color(180, 180, 180));
        rotuloPosicaoCursor.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        barraStatus.add(rotuloStatus, BorderLayout.WEST);
        barraStatus.add(rotuloPosicaoCursor, BorderLayout.EAST);

        // Callbacks do editor
        painelEditor.definirRetornoCursor((linha, coluna) ->
                rotuloPosicaoCursor.setText(String.format("Linha: %d, Coluna: %d   |   UTF-8   |   Insensível a Maiúsculas", linha, coluna)));

        painelEditor.definirRetornoAnalise(this::aoFinalizarAnalise);

        // Menu e Barra de Ferramentas
        seletorExemplos = new JComboBox<>(new String[]{
                "-- Selecionar Exemplo Pronto --",
                "1. Programa Válido Completo (Anexo I)",
                "2. Programa com Erros Léxicos (Recuperação)",
                "3. Algoritmo Fatorial (com Função e For)",
                "4. Extensão com Registros e Enumerações (Ponto Extra)"
        });
        seletorExemplos.addActionListener(this::tratarSelecaoExemplo);

        setJMenuBar(criarBarraMenu());
        add(criarBarraFerramentas(), BorderLayout.NORTH);
        add(splitPrincipal, BorderLayout.CENTER);
        add(barraStatus, BorderLayout.SOUTH);

        // Inicializa com o exemplo padrão válido
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

        // Menu Arquivo
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

        // Menu Compilar
        JMenu menuCompilar = new JMenu("Compilar");
        JMenuItem itemExecutar = new JMenuItem("Executar Análise Léxica (F5)");
        itemExecutar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        itemExecutar.addActionListener(e -> executarAnaliseLexica());

        JMenuItem itemLimpar = new JMenuItem("Limpar Saídas");
        itemLimpar.addActionListener(e -> limparSaidas());

        menuCompilar.add(itemExecutar);
        menuCompilar.add(itemLimpar);

        // Menu Exemplos
        JMenu menuExemplos = new JMenu("Exemplos");
        JMenuItem ex1 = new JMenuItem("1. Programa Válido (Anexo I)");
        ex1.addActionListener(e -> carregarExemploValido());
        JMenuItem ex2 = new JMenuItem("2. Programa com Erros Léxicos (Recuperação)");
        ex2.addActionListener(e -> carregarExemploErros());
        JMenuItem ex3 = new JMenuItem("3. Fatorial (com Função e For)");
        ex3.addActionListener(e -> carregarExemploFatorial());
        JMenuItem ex4 = new JMenuItem("4. Extensão com Registros e Enumerações");
        ex4.addActionListener(e -> carregarExemploBonus());

        menuExemplos.add(ex1);
        menuExemplos.add(ex2);
        menuExemplos.add(ex3);
        menuExemplos.add(ex4);

        // Menu Ajuda
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
        btnNovo.setToolTipText("Criar novo arquivo");
        btnNovo.addActionListener(e -> novoArquivo());

        JButton btnAbrir = new JButton("Abrir");
        btnAbrir.setToolTipText("Abrir arquivo de código fonte (.pas)");
        btnAbrir.addActionListener(e -> abrirArquivo());

        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.setToolTipText("Salvar arquivo atual");
        btnSalvar.addActionListener(e -> salvarArquivo(false));

        JButton btnCompilar = new JButton("▶ Analisar Léxico (F5)");
        btnCompilar.setToolTipText("Executa a análise léxica completa e atualiza as tabelas");
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
        tb.add(new JLabel("Carregar Exemplo: "));
        tb.add(seletorExemplos);
        tb.add(Box.createHorizontalGlue());

        return tb;
    }

    public void executarAnaliseLexica() {
        String codigo = painelEditor.getText();
        TabelaSimbolos tabSimbolos = new TabelaSimbolos();
        AnalisadorLexico analisador = new AnalisadorLexico(codigo, tabSimbolos);

        long inicioTempo = System.currentTimeMillis();
        List<Token> tokens = analisador.analisar();
        long tempoDecorrido = System.currentTimeMillis() - inicioTempo;

        List<ErroLexico> erros = analisador.getErros();

        // Atualiza modelos
        modeloTabelaTokens.definirTokens(tokens);
        modeloTabelaSimbolos.atualizar(tabSimbolos);
        modeloTabelaErros.definirErros(erros);

        // Atualiza abas com contagens
        abasInferiores.setTitleAt(0, "Erros Léxicos (" + erros.size() + ")");
        abasInferiores.setTitleAt(1, "Fluxo de Tokens (" + tokens.size() + ")");

        // Log no console
        StringBuilder log = new StringBuilder();
        log.append("============================================================\n");
        log.append("  ANÁLISE LÉXICA - DIALETO PASCALITE\n");
        log.append("============================================================\n");
        log.append(String.format("Tempo de execução: %d ms\n", tempoDecorrido));
        log.append(String.format("Total de caracteres analisados: %d\n", codigo.length()));
        log.append(String.format("Total de tokens gerados: %d\n", tokens.size()));
        log.append(String.format("Total de erros léxicos: %d\n", erros.size()));
        log.append(String.format("Total de símbolos na tabela: %d\n", tabSimbolos.obterTodosSimbolos().size()));
        log.append("------------------------------------------------------------\n");

        if (erros.isEmpty()) {
            log.append("✔ Sucesso! Nenhum erro léxico encontrado.\n");
            rotuloStatus.setText(String.format("Análise concluída com sucesso! %d tokens gerados (0 erros).", tokens.size()));
            rotuloStatus.setForeground(new Color(100, 220, 100));
            abasInferiores.setSelectedIndex(1); // Foca nos tokens
        } else {
            log.append("⚠ ATENÇÃO: Foram encontrados erros léxicos no código fonte:\n\n");
            for (ErroLexico err : erros) {
                log.append(String.format("  • [Linha %d, Coluna %d] %s (Trecho: '%s')\n    Estratégia de Recuperação: %s\n\n",
                        err.getLinha(), err.getColuna(), err.getMensagem(), err.getLexemaInvalido(), err.getEstrategiaRecuperacao()));
            }
            rotuloStatus.setText(String.format("Atenção: %d erro(s) léxico(s) encontrado(s)!", erros.size()));
            rotuloStatus.setForeground(new Color(245, 100, 100));
            abasInferiores.setSelectedIndex(0); // Foca nos erros
        }

        areaConsole.setText(log.toString());
        painelEditor.executarAnalise();
    }

    private void aoFinalizarAnalise(List<Token> tokens, List<ErroLexico> erros) {
        abasInferiores.setTitleAt(0, "Erros Léxicos (" + erros.size() + ")");
        if (!erros.isEmpty()) {
            rotuloStatus.setText(String.format("Aviso: %d erro(s) léxico(s) detectado(s) em tempo real!", erros.size()));
            rotuloStatus.setForeground(new Color(245, 100, 100));
            modeloTabelaErros.definirErros(erros);
        } else {
            rotuloStatus.setText("Código limpo: nenhum erro léxico.");
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
                // ==========================================
                // Exemplo 1: Programa Válido do Dialeto PascaLite
                // Conforme a gramática do Anexo I
                // ==========================================
                PROGRAM ExemploValido;
                
                { Declarações de Variáveis e Constantes }
                VAR
                   contador, limite : INTEGER;
                   media : REAL;
                   letra : CHAR;
                   mensagem : STRING;
                
                CONST
                   MAX_ITERACOES : INTEGER = 100;
                   PI = 3.14159;
                   TITULO = 'Compilador PascaLite';
                
                { Procedimento de Teste }
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
                
                   (* Chamada de procedimento e fim do programa *)
                   ImprimeResult(limite);
                END.
                """;
        painelEditor.setText(codigo);
        painelEditor.setCaretPosition(0);
        executarAnaliseLexica();
    }

    private void carregarExemploErros() {
        String codigo = """
                // ==========================================
                // Exemplo 2: Demonstração de Detecção e
                // Recuperação de Erros Léxicos (Modo Pânico)
                // ==========================================
                PROGRAM TesteErros;
                VAR
                   nomeValido : STRING;
                   // ERRO 1: Identificador excede o limite máximo de 15 caracteres
                   esteIdentificadorTemMaisDeQuinzeCaracteres : INTEGER;
                   // ERRO 2: Caracteres especiais proibidos ($ e #)
                   salario$Base : REAL;
                   valor#Total : REAL;
                   // ERRO 3: Identificador inválido iniciando por dígitos
                   99nomes : STRING;
                   // ERRO 4: Número real malformado (ponto sem dígitos)
                   taxa : REAL;
                BEGIN
                   taxa := 42.abc;
                   salario$Base := 1500.50;
                   
                   // ERRO 5: Literal de texto não fechado antes do fim da linha
                   nomeValido := 'Texto sem aspas no final;
                   
                   esteIdentificadorTemMaisDeQuinzeCaracteres := 10;
                END.
                """;
        painelEditor.setText(codigo);
        painelEditor.setCaretPosition(0);
        executarAnaliseLexica();
    }

    private void carregarExemploFatorial() {
        String codigo = """
                PROGRAM CalculoFatorial;
                { Algoritmo clássico de Fatorial }
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
                   
                   // Usando laço FOR (extensão sugerida nas observações)
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
                // =================================================
                // Exemplo 4: Ponto Extra - Registros e Enumerações
                // =================================================
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
        modeloTabelaSimbolos.atualizar(new TabelaSimbolos());
        areaConsole.setText("");
        abasInferiores.setTitleAt(0, "Erros Léxicos (0)");
        abasInferiores.setTitleAt(1, "Fluxo de Tokens (0)");
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
                inst → ID := expr ; | ID [expr] := expr ; | ID (parametros2) ; |
                       IF expr THEN inst | IF expr THEN inst ELSE inst |
                       WHILE expr DO inst | REPEAT inst UNTIL expr ; |
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
                NUM → digitos | digitos.digitos
                digitos → dig digitos*
                dig → [0-9]
                ID → [A-Za-z][letra | dig | _]* (máximo 15 caracteres)
                LITERAL → '[letra | dig | CARACTER_ESPECIAL]*'
                """;
        JTextArea ta = new JTextArea(msg);
        ta.setFont(new Font("Consolas", Font.PLAIN, 12));
        ta.setEditable(false);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(650, 480));
        JOptionPane.showMessageDialog(this, sp, "Gramática Formal do Dialeto PascaLite", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exibirDialogoSobre() {
        String msg = """
                PascaLite IDE & Compilador v1.0
                Trabalho da Disciplina de Compiladores
                Ciência da Computação - Universidade Federal do Espírito Santo (UFES)
                Professor: Rodrigo Freitas Silva
                
                Recursos Implementados:
                ✔ Analisador Léxico baseado em Autômato Finito Determinístico (AFD)
                ✔ Insensibilidade a maiúsculas/minúsculas (Case-insensitive)
                ✔ Identificadores com limite de 15 caracteres
                ✔ Remoção e tratamento de comentários (//, {}, (* *)) e espaços
                ✔ Sublinhado ondulado vermelho no código fonte e detecção em tempo real
                ✔ Tabela de Símbolos completa com pré-definidos e identificadores
                ✔ Recuperação de Erros via Modo Pânico com sincronização
                ✔ Visualizador interativo do AFD (Grafo 2D + Tabela de Transições)
                ✔ Extensões: Suporte a estruturas de controle FOR e tipos RECORD/ENUM
                """;
        JOptionPane.showMessageDialog(this, msg, "Sobre o PascaLite", JOptionPane.INFORMATION_MESSAGE);
    }
}
