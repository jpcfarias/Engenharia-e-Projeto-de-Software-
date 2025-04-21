package main.java.com.todolist.persistence;

import main.java.com.todolist.domain.Tarefa;
import java.util.List;

public interface PersistenciaTarefas {
    /**
     * Salva a lista de tarefas.
     * @param tarefas Lista de tarefas a serem salvas.
     * @throws PersistenciaException Se ocorrer um erro durante a operação.
     */
    void salvar(List<Tarefa> tarefas) throws PersistenciaException;

    /**
     * Carrega a lista de tarefas.
     * @return A lista de tarefas carregada. Retorna lista vazia se não houver dados.
     * @throws PersistenciaException Se ocorrer um erro durante a operação.
     */
    List<Tarefa> carregar() throws PersistenciaException;
}