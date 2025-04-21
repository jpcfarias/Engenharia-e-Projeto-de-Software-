package main.java.com.todolist.ui;

import main.java.com.todolist.domain.StatusTarefa;
import main.java.com.todolist.domain.Tarefa;
import main.java.com.todolist.persistence.PersistenciaJson;
import main.java.com.todolist.persistence.PersistenciaTarefas;
import main.java.com.todolist.service.GerenciadorTarefas;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class AplicacaoConsole {

    private final GerenciadorTarefas gerenciador;
    private final Scanner scanner;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String NOME_ARQUIVO_DADOS = "tarefas.json";

    public AplicacaoConsole() {
        PersistenciaTarefas persistencia = new PersistenciaJson(NOME_ARQUIVO_DADOS);
        this.gerenciador = new GerenciadorTarefas(persistencia);
        this.scanner = new Scanner(System.in);
    }

    public void executar() {
        int opcao;
        do {
            exibirMenu();
            opcao = lerOpcao();
            processarOpcao(opcao);
        } while (opcao != 0);

        System.out.println("Saindo da aplicação. Até logo!");
        scanner.close();
    }

    private void exibirMenu() {
        System.out.println("\n--- Menu To-Do List ---");
        System.out.println("1. Adicionar Nova Tarefa");
        System.out.println("2. Listar Todas as Tarefas");
        System.out.println("3. Listar Tarefas Pendentes");
        System.out.println("4. Listar Tarefas Concluídas");
        System.out.println("5. Marcar Tarefa como Concluída");
        System.out.println("6. Editar Tarefa");
        System.out.println("7. Excluir Tarefa");
        System.out.println("8. Marcar Tarefa como Pendente");
        System.out.println("0. Sair");
        System.out.print("Escolha uma opção: ");
    }

    private int lerOpcao() {
        while (!scanner.hasNextInt()) {
            System.out.println("Opção inválida. Por favor, digite um número.");
            scanner.next();
            System.out.print("Escolha uma opção: ");
        }
        int opcao = scanner.nextInt();
        scanner.nextLine();
        return opcao;
    }

     private long lerIdTarefa() {
        long id = -1;
        while (id < 0) {
             System.out.print("Digite o ID da tarefa: ");
             if (scanner.hasNextLong()) {
                 id = scanner.nextLong();
                 if (id < 0) {
                     System.out.println("ID não pode ser negativo.");
                 }
             } else {
                 System.out.println("Entrada inválida. Digite um número para o ID.");
                 scanner.next();
             }
        }
        scanner.nextLine();
        return id;
     }

    private void processarOpcao(int opcao) {
        switch (opcao) {
            case 1:
                adicionarTarefa();
                break;
            case 2:
                listarTarefas(gerenciador.listarTodas(), "Todas as Tarefas");
                break;
            case 3:
                listarTarefas(gerenciador.listarPendentes(), "Tarefas Pendentes");
                break;
            case 4:
                listarTarefas(gerenciador.listarConcluidas(), "Tarefas Concluídas");
                break;
            case 5:
                marcarTarefa(StatusTarefa.CONCLUIDA);
                break;
            case 6:
                editarTarefa();
                break;
            case 7:
                excluirTarefa();
                break;
            case 8:
                marcarTarefa(StatusTarefa.PENDENTE);
                break;
            case 0:
                break;
            default:
                System.out.println("Opção inválida. Tente novamente.");
        }
    }

    private void adicionarTarefa() {
        System.out.println("\n--- Adicionar Nova Tarefa ---");
        System.out.print("Título: ");
        String titulo = scanner.nextLine();
        System.out.print("Descrição (opcional): ");
        String descricao = scanner.nextLine();
        LocalDate dataVencimento = lerDataOpcional("Data de Vencimento (dd/MM/yyyy, opcional): ");

        try {
            Tarefa nova = gerenciador.adicionarTarefa(titulo, descricao, dataVencimento);
            System.out.println("Tarefa adicionada com sucesso! ID: " + nova.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("Erro ao adicionar tarefa: " + e.getMessage());
        }
    }

     private LocalDate lerDataOpcional(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            if (input == null || input.trim().isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(input.trim(), DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Formato de data inválido. Use dd/MM/yyyy ou deixe em branco.");
            }
        }
    }

    private void listarTarefas(List<Tarefa> tarefas, String tituloLista) {
        System.out.println("\n--- " + tituloLista + " ---");
        if (tarefas.isEmpty()) {
            System.out.println("Nenhuma tarefa encontrada.");
        } else {
            for (Tarefa t : tarefas) {
                String dataStr = t.getDataVencimento() != null ? DATE_FORMATTER.format(t.getDataVencimento()) : "N/A";
                System.out.printf("ID: %d | Título: %s | Status: %s | Vencimento: %s | Descrição: %s%n",
                        t.getId(),
                        t.getTitulo(),
                        t.getStatus().getDescricao(),
                        dataStr,
                        t.getDescricao() != null ? t.getDescricao() : "");
            }
        }
    }

    private void marcarTarefa(StatusTarefa novoStatus) {
         String acao = novoStatus == StatusTarefa.CONCLUIDA ? "Concluir" : "Reabrir (Marcar como Pendente)";
         System.out.println("\n--- " + acao +" Tarefa ---");
         long id = lerIdTarefa();

        boolean sucesso;
        if (novoStatus == StatusTarefa.CONCLUIDA) {
             sucesso = gerenciador.marcarComoConcluida(id);
        } else {
            sucesso = gerenciador.marcarComoPendente(id);
        }

        if (sucesso) {
            System.out.println("Status da tarefa ID " + id + " atualizado para " + novoStatus.getDescricao() + ".");
        } else {
            System.out.println("Tarefa com ID " + id + " não encontrada.");
        }
    }

    private void editarTarefa() {
        System.out.println("\n--- Editar Tarefa ---");
        long id = lerIdTarefa();

        Optional<Tarefa> tarefaOpt = gerenciador.buscarTarefaPorId(id);
        if (tarefaOpt.isEmpty()) {
            System.out.println("Tarefa com ID " + id + " não encontrada.");
            return;
        }

        Tarefa tarefa = tarefaOpt.get();
        System.out.println("Título atual: " + tarefa.getTitulo());
        System.out.print("Novo título (Enter para manter): ");
        String novoTitulo = scanner.nextLine();
        if (novoTitulo.trim().isEmpty()) {
            novoTitulo = tarefa.getTitulo();
        }

        System.out.println("Descrição atual: " + (tarefa.getDescricao() != null ? tarefa.getDescricao() : ""));
        System.out.print("Nova descrição (Enter para manter): ");
        String novaDescricao = scanner.nextLine();
        if (novaDescricao.trim().isEmpty()) {
            novaDescricao = tarefa.getDescricao();
        }

        LocalDate dataAtual = tarefa.getDataVencimento();
        System.out.println("Data de vencimento atual: " + (dataAtual != null ? DATE_FORMATTER.format(dataAtual) : "N/A"));
        LocalDate novaData = lerDataOpcional("Nova data (dd/MM/yyyy, Enter para manter): ");
        if (novaData == null) {
            novaData = dataAtual;
        }

        boolean atualizou = gerenciador.atualizarTarefa(id, novoTitulo, novaDescricao, novaData);
        if (atualizou) {
            System.out.println("Tarefa atualizada com sucesso!");
        } else {
            System.out.println("Erro ao atualizar tarefa.");
        }
    }

    private void excluirTarefa() {
        System.out.println("\n--- Excluir Tarefa ---");
        long id = lerIdTarefa();

        boolean excluiu = gerenciador.excluirTarefa(id);
        if (excluiu) {
            System.out.println("Tarefa excluída com sucesso!");
        } else {
            System.out.println("Tarefa com ID " + id + " não encontrada.");
        }
    }

    public static void main(String[] args) {
        AplicacaoConsole app = new AplicacaoConsole();
        app.executar();
    }
}