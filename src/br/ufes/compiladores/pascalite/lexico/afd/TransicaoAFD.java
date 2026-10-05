package br.ufes.compiladores.pascalite.lexico.afd;

/**
 * Representa uma transição entre dois estados no AFD sob um rótulo de entrada.
 */
public class TransicaoAFD {
    private final EstadoAFD estadoOrigem;
    private final EstadoAFD estadoDestino;
    private final String rotulo;
    private final String descricao;

    public TransicaoAFD(EstadoAFD estadoOrigem, EstadoAFD estadoDestino, String rotulo, String descricao) {
        this.estadoOrigem = estadoOrigem;
        this.estadoDestino = estadoDestino;
        this.rotulo = rotulo;
        this.descricao = descricao;
    }

    public EstadoAFD getEstadoOrigem() {
        return estadoOrigem;
    }

    public EstadoAFD getEstadoDestino() {
        return estadoDestino;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return String.format("%s --[%s]--> %s (%s)", estadoOrigem.getId(), rotulo, estadoDestino.getId(), descricao);
    }
}
