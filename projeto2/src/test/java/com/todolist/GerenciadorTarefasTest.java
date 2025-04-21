package test.java.com.todolist;

import com.example.todolist.domain.StatusTarefa;
import com.example.todolist.domain.Tarefa;
import com.example.todolist.persistence.PersistenciaException;
import com.example.todolist.persistence.PersistenciaTarefas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GerenciadorTarefasTest {

    @Mock
    private PersistenciaTarefas persistenciaMock;

    @InjectMocks
    private GerenciadorTarefas gerenciador;

    @BeforeEach
    void setUp() {
        try {
            when(persistenciaMock.carregar()).thenReturn(new ArrayList<>());
        } catch (PersistenciaException e) {
            fail("Setup failed: Mockito threw PersistenciaException on when()", e);
        }
    }

    @Test
    @DisplayName("Deve adicionar uma tarefa com sucesso")
    void adicionarTarefa_ComDadosValidos_DeveRetornarTarefaComId() throws PersistenciaException {
        String titulo = "Comprar pão";
        String desc = "Na padaria da esquina";
        LocalDate data = LocalDate.now().plusDays(1);

        Tarefa adicionada = gerenciador.adicionarTarefa(titulo, desc, data);

        assertNotNull(adicionada);
        assertTrue(adicionada.getId() > 0, "ID deve ser positivo");
        assertEquals(titulo, adicionada.getTitulo());
        assertEquals(desc, adicionada.getDescricao());
        assertEquals(data, adicionada.getDataVencimento());
        assertEquals(StatusTarefa.PENDENTE, adicionada.getStatus());

        verify(persistenciaMock, times(1)).salvar(anyList());
    }

    @Test
    @DisplayName("Não deve adicionar tarefa com título vazio")
    void adicionarTarefa_ComTituloVazio_DeveLancarExcecao() {
        String tituloVazio = "   ";
        String desc = "Descrição";
        LocalDate data = LocalDate.now();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gerenciador.adicionarTarefa(tituloVazio, desc, data);
        });
        assertEquals("Título é obrigatório.", exception.getMessage());

        try {
            verify(persistenciaMock, never()).salvar(anyList());
        } catch (PersistenciaException e) {
            fail("Mock interaction check failed unexpectedly", e);
        }
    }

    @Test
    @DisplayName("Deve buscar tarefa por ID existente")
    void buscarTarefaPorId_QuandoIdExiste_DeveRetornarOptionalComTarefa() {
        Tarefa t1 = gerenciador.adicionarTarefa("Tarefa 1", null, null);

        Optional<Tarefa> encontradaOpt = gerenciador.buscarTarefaPorId(t1.getId());

        assertTrue(encontradaOpt.isPresent(), "Deveria encontrar a tarefa");
        assertEquals(t1.getId(), encontradaOpt.get().getId());
        assertEquals("Tarefa 1", encontradaOpt.get().getTitulo());
    }

    @Test
    @DisplayName("Não deve buscar tarefa por ID inexistente")
    void buscarTarefaPorId_QuandoIdNaoExiste_DeveRetornarOptionalVazio() {
        long idInexistente = 999L;

        Optional<Tarefa> encontradaOpt = gerenciador.buscarTarefaPorId(idInexistente);

        assertTrue(encontradaOpt.isEmpty(), "Não deveria encontrar tarefa com ID inexistente");
    }

    @Test
    @DisplayName("Deve listar todas as tarefas")
    void listarTodas_DeveRetornarListaImutavelComTodasTarefas() {
        Tarefa t1 = gerenciador.adicionarTarefa("Tarefa A", "", null);
        Tarefa t2 = gerenciador.adicionarTarefa("Tarefa B", "", LocalDate.now());
        gerenciador.marcarComoConcluida(t2.getId());

        List<Tarefa> todas = gerenciador.listarTodas();

        assertNotNull(todas);
        assertEquals(2, todas.size());
        assertTrue(todas.stream().anyMatch(t -> t.getId() == t1.getId()));
        assertTrue(todas.stream().anyMatch(t -> t.getId() == t2.getId()));

        assertThrows(UnsupportedOperationException.class, () -> {
            todas.add(new Tarefa("Invalida", null, null));
        });
    }

    @Test
    @DisplayName("Deve listar apenas tarefas pendentes")
    void listarPendentes_DeveRetornarApenasTarefasComStatusPendente() {
        Tarefa t1 = gerenciador.adicionarTarefa("Pendente 1", "", null);
        Tarefa t2 = gerenciador.adicionarTarefa("Concluida 1", "", LocalDate.now());
        Tarefa t3 = gerenciador.adicionarTarefa("Pendente 2", "", null);
        gerenciador.marcarComoConcluida(t2.getId());

        List<Tarefa> pendentes = gerenciador.listarPendentes();

        assertNotNull(pendentes);
        assertEquals(2, pendentes.size());
        assertTrue(pendentes.stream().allMatch(t -> t.getStatus() == StatusTarefa.PENDENTE));
        assertTrue(pendentes.stream().anyMatch(t -> t.getId() == t1.getId()));
        assertTrue(pendentes.stream().anyMatch(t -> t.getId() == t3.getId()));
        assertFalse(pendentes.stream().anyMatch(t -> t.getId() == t2.getId()));
    }

    @Test
    @DisplayName("Deve listar apenas tarefas concluídas")
    void listarConcluidas_DeveRetornarApenasTarefasComStatusConcluida() {
        Tarefa t1 = gerenciador.adicionarTarefa("Pendente 1", "", null);
        Tarefa t2 = gerenciador.adicionarTarefa("Concluida 1", "", LocalDate.now());
        Tarefa t3 = gerenciador.adicionarTarefa("Concluida 2", "", null);
        gerenciador.marcarComoConcluida(t2.getId());
        gerenciador.marcarComoConcluida(t3.getId());

        List<Tarefa> concluidas = gerenciador.listarConcluidas();

        assertNotNull(concluidas);
        assertEquals(2, concluidas.size());
        assertTrue(concluidas.stream().allMatch(t -> t.getStatus() == StatusTarefa.CONCLUIDA));
        assertTrue(concluidas.stream().anyMatch(t -> t.getId() == t2.getId()));
        assertTrue(concluidas.stream().anyMatch(t -> t.getId() == t3.getId()));
        assertFalse(concluidas.stream().anyMatch(t -> t.getId() == t1.getId()));
    }

    @Test
    @DisplayName("Deve atualizar uma tarefa existente com sucesso")
    void atualizarTarefa_QuandoIdExiste_DeveRetornarTrueEAtualizarDados() throws PersistenciaException {
        Tarefa original = gerenciador.adicionarTarefa("Título Original", "Desc Original", LocalDate.now());
        long idParaAtualizar = original.getId();
        String novoTitulo = "Título Atualizado";
        String novaDesc = "Desc Atualizada";
        LocalDate novaData = LocalDate.now().plusDays(5);

        boolean atualizou = gerenciador.atualizarTarefa(idParaAtualizar, novoTitulo, novaDesc, novaData);

        assertTrue(atualizou, "Atualização deveria retornar true");

        Optional<Tarefa> atualizadaOpt = gerenciador.buscarTarefaPorId(idParaAtualizar);
        assertTrue(atualizadaOpt.isPresent(), "Tarefa atualizada deveria ser encontrada");
        Tarefa atualizada = atualizadaOpt.get();
        assertEquals(novoTitulo, atualizada.getTitulo());
        assertEquals(novaDesc, atualizada.getDescricao());
        assertEquals(novaData, atualizada.getDataVencimento());
        assertEquals(StatusTarefa.PENDENTE, atualizada.getStatus());

        verify(persistenciaMock, times(2)).salvar(anyList());
    }

    @Test
    @DisplayName("Não deve atualizar tarefa com ID inexistente")
    void atualizarTarefa_QuandoIdNaoExiste_DeveRetornarFalse() throws PersistenciaException {
        long idInexistente = 999L;

        boolean atualizou = gerenciador.atualizarTarefa(idInexistente, "Novo Titulo", "", null);

        assertFalse(atualizou, "Atualização de ID inexistente deveria retornar false");
        verify(persistenciaMock, never()).salvar(anyList());
    }

    @Test
    @DisplayName("Deve marcar tarefa como concluída")
    void marcarComoConcluida_QuandoIdExiste_DeveAlterarStatusParaConcluida() throws PersistenciaException {
        Tarefa tarefa = gerenciador.adicionarTarefa("Para Concluir", "", null);
        assertEquals(StatusTarefa.PENDENTE, tarefa.getStatus());

        boolean sucesso = gerenciador.marcarComoConcluida(tarefa.getId());

        assertTrue(sucesso);
        Optional<Tarefa> buscadaOpt = gerenciador.buscarTarefaPorId(tarefa.getId());
        assertTrue(buscadaOpt.isPresent());
        assertEquals(StatusTarefa.CONCLUIDA, buscadaOpt.get().getStatus());

        verify(persistenciaMock, times(2)).salvar(anyList());
    }

    @Test
    @DisplayName("Deve marcar tarefa como pendente")
    void marcarComoPendente_QuandoIdExiste_DeveAlterarStatusParaPendente() throws PersistenciaException {
        Tarefa tarefa = gerenciador.adicionarTarefa("Para Reabrir", "", null);
        gerenciador.marcarComoConcluida(tarefa.getId());
        assertEquals(StatusTarefa.CONCLUIDA, gerenciador.buscarTarefaPorId(tarefa.getId()).get().getStatus());

        boolean sucesso = gerenciador.marcarComoPendente(tarefa.getId());

        assertTrue(sucesso);
        Optional<Tarefa> buscadaOpt = gerenciador.buscarTarefaPorId(tarefa.getId());
        assertTrue(buscadaOpt.isPresent());
        assertEquals(StatusTarefa.PENDENTE, buscadaOpt.get().getStatus());

        verify(persistenciaMock, times(3)).salvar(anyList());
    }

    @Test
    @DisplayName("Deve excluir tarefa existente")
    void excluirTarefa_QuandoIdExiste_DeveRetornarTrueERemoverDaLista() throws PersistenciaException {
        Tarefa t1 = gerenciador.adicionarTarefa("Tarefa 1", null, null);
        Tarefa t2 = gerenciador.adicionarTarefa("Tarefa para Excluir", null, null);
        long idParaExcluir = t2.getId();
        assertEquals(2, gerenciador.listarTodas().size());

        boolean excluiu = gerenciador.excluirTarefa(idParaExcluir);

        assertTrue(excluiu, "Exclusão deveria retornar true");
        assertEquals(1, gerenciador.listarTodas().size(), "Lista deveria ter 1 tarefa restante");
        assertTrue(gerenciador.buscarTarefaPorId(idParaExcluir).isEmpty(), "Tarefa excluída não deveria ser encontrada");
        assertTrue(gerenciador.buscarTarefaPorId(t1.getId()).isPresent(), "Outra tarefa não deveria ser afetada");

        verify(persistenciaMock, times(3)).salvar(anyList());
    }

    @Test
    @DisplayName("Não deve excluir tarefa com ID inexistente")
    void excluirTarefa_QuandoIdNaoExiste_DeveRetornarFalse() throws PersistenciaException {
        gerenciador.adicionarTarefa("Tarefa Existente", null, null);
        long idInexistente = 999L;
        assertEquals(1, gerenciador.listarTodas().size());

        boolean excluiu = gerenciador.excluirTarefa(idInexistente);

        assertFalse(excluiu, "Exclusão de ID inexistente deveria retornar false");
        assertEquals(1, gerenciador.listarTodas().size(), "Lista não deveria mudar de tamanho");

        verify(persistenciaMock, times(1)).salvar(anyList());
    }
}