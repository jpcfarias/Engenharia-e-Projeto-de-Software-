package main.java.com.todolist.domain;

import java.time.LocalDate;
import java.util.Objects;

public class Tarefa {
    private long id;
    private String titulo;
    private String descricao;
    private LocalDate dataVencimento;
    private StatusTarefa status;

    public Tarefa(String titulo, String descricao, LocalDate dataVencimento) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("Título não pode ser vazio.");
        }
        this.titulo = titulo.trim();
        this.descricao = descricao;
        this.dataVencimento = dataVencimento;
        this.status = StatusTarefa.PENDENTE;
    }

    Tarefa() {}

    public Tarefa(long id, String titulo, String descricao, LocalDate dataVencimento, StatusTarefa status) {
        this(titulo, descricao, dataVencimento);
        this.id = id;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public StatusTarefa getStatus() {
        return status;
    }

    public void setTitulo(String titulo) {
         if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("Título não pode ser vazio.");
        }
        this.titulo = titulo.trim();
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public void marcarComoConcluida() {
        this.status = StatusTarefa.CONCLUIDA;
    }

    public void marcarComoPendente() {
        this.status = StatusTarefa.PENDENTE;
    }

    public void setId(long id) {
        if(this.id == 0) {
             this.id = id;
        } else {
             System.err.println("Aviso: Tentativa de redefinir o ID da tarefa " + this.id);
        }
    }

    void setStatus(StatusTarefa status) {
        this.status = status;
     }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tarefa tarefa = (Tarefa) o;
        return id == tarefa.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Tarefa{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", descricao='" + (descricao != null ? descricao : "") + '\'' +
                ", dataVencimento=" + (dataVencimento != null ? dataVencimento : "N/A") +
                ", status=" + status +
                '}';
    }
}