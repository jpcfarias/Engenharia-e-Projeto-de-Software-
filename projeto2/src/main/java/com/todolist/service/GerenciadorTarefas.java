package main.java.com.todolist.service;

import main.java.com.todolist.domain.StatusTarefa;
import main.java.com.todolist.domain.Tarefa;
import main.java.com.todolist.persistence.PersistenciaException;
import main.java.com.todolist.persistence.PersistenciaTarefas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class GerenciadorTarefas {

    private final List<Tarefa> tarefas;
    private final AtomicLong proximoId;
    private final PersistenciaTarefas persistencia;

    public GerenciadorTarefas(PersistenciaTarefas persistencia) {
        this.persistencia = persistencia;
        List<Tarefa> tarefasCarregadas;
        try {
             tarefasCarregadas = persistencia.carregar();
             System.out.println(tarefasCarregadas.size() + " tarefas carregadas.");
        } catch (PersistenciaException e) {
             System.err.println("AVISO: Não foi possível carregar tarefas salvas. Iniciando com lista vazia. Erro: " + e.getMessage());
             tarefasCarregadas = new ArrayList<>();
        }
        this.tarefas = new ArrayList<>(tarefasCarregadas);

        long maxId = tarefasCarregadas.stream()
                                     .mapToLong(Tarefa::getId)
                                     .max()
                                     .orElse(0L);
        this.proximoId = new AtomicLong(maxId + 1);
    }

    public Tarefa adicionarTarefa(String titulo, String descricao, LocalDate dataVencimento) {
        if (titulo == null || titulo.trim().isEmpty()) {
             throw new IllegalArgumentException("Título é obrigatório.");
        }
        long novoId = proximoId.getAndIncrement();
        Tarefa novaTarefa = new Tarefa(titulo, descricao, dataVencimento);
        novaTarefa.setId(novoId);
        tarefas.add(novaTarefa);
        salvarDados();
        return novaTarefa;
    }

    public Optional<Tarefa> buscarTarefaPorId(long id) {
        return tarefas.stream()
                      .filter(t -> t.getId() == id)
                      .findFirst();
    }

    public List<Tarefa> listarTodas() {
        return Collections.unmodifiableList(new ArrayList<>(tarefas));
    }

    public List<Tarefa> listarPendentes() {
        return tarefas.stream()
                      .filter(t -> t.getStatus() == StatusTarefa.PENDENTE)
                      .collect(Collectors.collectingAndThen(
                                Collectors.toList(),
                                Collections::unmodifiableList));
    }

    public List<Tarefa> listarConcluidas() {
        return tarefas.stream()
                      .filter(t -> t.getStatus() == StatusTarefa.CONCLUIDA)
                      .collect(Collectors.collectingAndThen(
                                Collectors.toList(),
                                Collections::unmodifiableList));
    }

    public boolean atualizarTarefa(long id, String novoTitulo, String novaDescricao, LocalDate novaData) {
        Optional<Tarefa> tarefaOpt = buscarTarefaPorId(id);
        if (tarefaOpt.isPresent()) {
            Tarefa tarefa = tarefaOpt.get();
            tarefa.setTitulo(novoTitulo);
            tarefa.setDescricao(novaDescricao);
            tarefa.setDataVencimento(novaData);
            salvarDados();
            return true;
        }
        return false;
    }

    public boolean marcarComoConcluida(long id) {
        Optional<Tarefa> tarefaOpt = buscarTarefaPorId(id);
        if (tarefaOpt.isPresent()) {
            Tarefa tarefa = tarefaOpt.get();
            tarefa.marcarComoConcluida();
            salvarDados();
            return true;
        }
        return false;
    }

     public boolean marcarComoPendente(long id) {
        Optional<Tarefa> tarefaOpt = buscarTarefaPorId(id);
        if (tarefaOpt.isPresent()) {
            Tarefa tarefa = tarefaOpt.get();
            tarefa.marcarComoPendente();
            salvarDados();
            return true;
        }
        return false;
    }

    public boolean excluirTarefa(long id) {
        boolean removido = tarefas.removeIf(t -> t.getId() == id);
        if (removido) {
            salvarDados();
        }
        return removido;
    }

    public void salvarDados() {
        try {
            persistencia.salvar(new ArrayList<>(tarefas));
        } catch (PersistenciaException e) {
            System.err.println("ERRO CRÍTICO: Falha ao salvar tarefas! " + e.getMessage());
        }
    }
}