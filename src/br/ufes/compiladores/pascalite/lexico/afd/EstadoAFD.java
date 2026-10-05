package br.ufes.compiladores.pascalite.lexico.afd;

/**
 * Representa um estado do Autômato Finito Determinístico (AFD).
 */
public class EstadoAFD {
    private final String id;
    private final String nome;
    private final String descricao;
    private final boolean ehFinal;
    private final String tokenProduzido;

    public EstadoAFD(String id, String nome, String descricao, boolean ehFinal, String tokenProduzido) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.ehFinal = ehFinal;
        this.tokenProduzido = tokenProduzido;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean ehFinal() {
        return ehFinal;
    }

    public String getTokenProduzido() {
        return tokenProduzido;
    }

    @Override
    public String toString() {
        return nome + (ehFinal ? " [Final: " + tokenProduzido + "]" : "");
    }
}
