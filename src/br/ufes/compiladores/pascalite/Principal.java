package br.ufes.compiladores.pascalite;

import br.ufes.compiladores.pascalite.ide.IDEPascalite;
import br.ufes.compiladores.pascalite.lexico.AnalisadorLexico;
import br.ufes.compiladores.pascalite.lexico.EntradaSimbolo;
import br.ufes.compiladores.pascalite.lexico.ErroLexico;
import br.ufes.compiladores.pascalite.lexico.TabelaSimbolos;
import br.ufes.compiladores.pascalite.lexico.Token;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

/**
 * Ponto de entrada do compilador PascaLite.
 * - Sem argumentos: Inicia a IDE gráfica Swing (IDEPascalite).
 * - Com argumentos: Executa a análise léxica em modo linha de comando (CLI).
 */
public class Principal {
    public static void main(String[] args) {
        if (args.length > 0) {
            executarModoLinhaComando(args[0]);
        } else {
            executarModoGrafico();
        }
    }

    private static void executarModoGrafico() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Aplica o Look and Feel do sistema operacional
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            IDEPascalite ide = new IDEPascalite();
            ide.setVisible(true);
        });
    }

    private static void executarModoLinhaComando(String caminhoArquivo) {
        File arquivo = new File(caminhoArquivo);
        if (!arquivo.exists()) {
            System.err.println("Erro: Arquivo não encontrado: " + caminhoArquivo);
            System.exit(1);
        }

        try {
            String codigoFonte = Files.readString(arquivo.toPath());
            TabelaSimbolos tabelaSimbolos = new TabelaSimbolos();
            AnalisadorLexico analisador = new AnalisadorLexico(codigoFonte, tabelaSimbolos);

            System.out.println("================================================================================");
            System.out.println("            COMPILADOR PASCALITE - RELATÓRIO DE ANÁLISE LÉXICA                  ");
            System.out.println("================================================================================");
            System.out.println("Arquivo de entrada: " + arquivo.getAbsolutePath());
            System.out.println("Tamanho do fonte: " + codigoFonte.length() + " caracteres\n");

            List<Token> tokens = analisador.analisar();
            List<ErroLexico> erros = analisador.getErros();

            // 1. Tabela de Tokens
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println(" 1. FLUXO DE TOKENS RECONHECIDOS");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.printf("%-5s | %-6s | %-6s | %-16s | %-20s | %-20s%n",
                    "#", "Linha", "Coluna", "Tipo de Token", "Lexema", "Atributo/Valor");
            System.out.println("--------------------------------------------------------------------------------");
            for (int i = 0; i < tokens.size(); i++) {
                Token t = tokens.get(i);
                String attr = (t.getValorAtributo() != null) ? t.getValorAtributo().toString() : "-";
                System.out.printf("%-5d | %-6d | %-6d | %-16s | %-20s | %-20s%n",
                        i + 1, t.getLinha(), t.getColuna(), t.getTipo().name(), t.getLexema(), attr);
            }

            // 2. Erros Léxicos
            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.println(" 2. RELATÓRIO DE ERROS LÉXICOS");
            System.out.println("--------------------------------------------------------------------------------");
            if (erros.isEmpty()) {
                System.out.println("✔ Nenhum erro léxico encontrado no código fonte.");
            } else {
                System.out.printf("Total de erros encontrados: %d%n%n", erros.size());
                for (int i = 0; i < erros.size(); i++) {
                    ErroLexico err = erros.get(i);
                    System.out.printf("[%d] Linha %d, Coluna %d:%n", i + 1, err.getLinha(), err.getColuna());
                    System.out.printf("    Trecho com erro: '%s'%n", err.getLexemaInvalido());
                    System.out.printf("    Mensagem: %s%n", err.getMensagem());
                    System.out.printf("    Estratégia de Recuperação: %s%n%n", err.getEstrategiaRecuperacao());
                }
            }

            // 3. Tabela de Símbolos
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println(" 3. TABELA DE SÍMBOLOS (IDENTIFICADORES ENCONTRADOS NO PROGRAMA)");
            System.out.println("--------------------------------------------------------------------------------");
            List<EntradaSimbolo> simbolosUsuario = tabelaSimbolos.obterSimbolosUsuario();
            if (simbolosUsuario.isEmpty()) {
                System.out.println("Nenhum identificador de usuário cadastrado.");
            } else {
                System.out.printf("%-20s | %-15s | %-12s | %s%n",
                        "Identificador", "Chave Canônica", "Ocorrências", "Linhas");
                System.out.println("--------------------------------------------------------------------------------");
                for (EntradaSimbolo se : simbolosUsuario) {
                    System.out.printf("%-20s | %-15s | %-12d | %s%n",
                            se.getLexema(), se.getChaveCanonica(), se.getOcorrencias(), se.getLinhasFormatadas());
                }
            }
            System.out.println("================================================================================");

        } catch (Exception e) {
            System.err.println("Erro durante a execução: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
