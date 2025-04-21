package main.java.com.todolist.persistence;

import main.java.com.todolist.domain.Tarefa;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PersistenciaJson implements PersistenciaTarefas {

    private final Path caminhoArquivo;
    private final ObjectMapper objectMapper;

    public PersistenciaJson(String nomeArquivo) {
        this.caminhoArquivo = Paths.get(nomeArquivo);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void salvar(List<Tarefa> tarefas) throws PersistenciaException {
        try {
            Path parentDir = caminhoArquivo.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            objectMapper.writeValue(caminhoArquivo.toFile(), tarefas);
        } catch (IOException e) {
            throw new PersistenciaException("Erro ao salvar tarefas no arquivo JSON: " + caminhoArquivo, e);
        }
    }

    @Override
    public List<Tarefa> carregar() throws PersistenciaException {
        File arquivo = caminhoArquivo.toFile();
        if (!arquivo.exists() || arquivo.length() == 0) {
            return new ArrayList<>();
        }
        try {
            List<Tarefa> tarefas = objectMapper.readValue(arquivo, new TypeReference<List<Tarefa>>() {});
            return tarefas != null ? tarefas : new ArrayList<>();
        } catch (IOException e) {
            throw new PersistenciaException("Erro ao carregar tarefas do arquivo JSON: " + caminhoArquivo + ". O arquivo pode estar corrompido.", e);
        }
    }
}