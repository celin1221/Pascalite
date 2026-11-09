package br.ufes.compiladores.pascalite;

import br.ufes.compiladores.pascalite.ide.IDEPascalite;
import br.ufes.compiladores.pascalite.lexico.AnalisadorLexico;
import br.ufes.compiladores.pascalite.lexico.EntradaSimbolo;
import br.ufes.compiladores.pascalite.lexico.ErroLexico;
import br.ufes.compiladores.pascalite.lexico.TabelaSimbolos;
import br.ufes.compiladores.pascalite.lexico.Token;
import br.ufes.compiladores.pascalite.sintatico.AnalisadorSintatico;
import br.ufes.compiladores.pascalite.sintatico.ErroSintatico;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

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
            AnalisadorLexico lexico = new AnalisadorLexico(codigoFonte, tabelaSimbolos);

            System.out.println("================================================================================");
            System.out.println("          COMPILADOR PASCALITE - RELATÓRIO LÉXICO E SINTÁTICO                   ");
            System.out.println("================================================================================");
            System.out.println("Arquivo: " + arquivo.getAbsolutePath());
            System.out.println("Tamanho: " + codigoFonte.length() + " caracteres\n");

            List<Token> tokens = lexico.analisar();
            List<ErroLexico> errosLexicos = lexico.getErros();

            AnalisadorSintatico sintatico = new AnalisadorSintatico(tokens);
            List<ErroSintatico> errosSintaticos = sintatico.analisar();

            System.out.println("--------------------------------------------------------------------------------");
            System.out.println(" FLUXO DE TOKENS RECONHECIDOS");
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

            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.println(" ERROS LÉXICOS");
            System.out.println("--------------------------------------------------------------------------------");
            if (errosLexicos.isEmpty()) {
                System.out.println("Nenhum erro léxico encontrado.");
            } else {
                System.out.printf("Total de erros léxicos: %d%n%n", errosLexicos.size());
                for (int i = 0; i < errosLexicos.size(); i++) {
                    ErroLexico err = errosLexicos.get(i);
                    System.out.printf("[%d] Linha %d, Coluna %d - '%s': %s%n",
                            i + 1, err.getLinha(), err.getColuna(), err.getLexemaInvalido(), err.getMensagem());
                }
            }

            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.println(" ANÁLISE SINTÁTICA");
            System.out.println("--------------------------------------------------------------------------------");
            if (errosSintaticos.isEmpty()) {
                System.out.println("Nenhum erro ou aviso sintático encontrado.");
            } else {
                for (int i = 0; i < errosSintaticos.size(); i++) {
                    ErroSintatico err = errosSintaticos.get(i);
                    String tipo = err.ehAviso() ? "AVISO" : "ERRO";
                    System.out.printf("[%d] [%s] Linha %d, Coluna %d (próximo a '%s'): %s%n",
                            i + 1, tipo, err.getLinha(), err.getColuna(), err.getLexema(), err.getMensagem());
                }
            }

            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.println(" TABELA DE SÍMBOLOS (IDENTIFICADORES)");
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
