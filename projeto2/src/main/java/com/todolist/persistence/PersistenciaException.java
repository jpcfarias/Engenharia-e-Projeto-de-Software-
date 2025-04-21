package main.java.com.todolist.persistence;

// Exceção customizada para erros de persistência
public class PersistenciaException extends RuntimeException {
    public PersistenciaException(String message, Throwable cause) {
        super(message, cause);
    }
     public PersistenciaException(String message) {
        super(message);
    }
}