package com.universidad.exception;

public class RecursoNoDisponibleException extends RuntimeException {

    public RecursoNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public RecursoNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}