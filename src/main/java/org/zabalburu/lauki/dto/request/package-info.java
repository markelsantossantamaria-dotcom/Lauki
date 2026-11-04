package org.zabalburu.lauki.dto.request;

/**
 * Objetos de entrada (Requests).
 * <p>
 * Son los datos que el usuario **escribe en la página web** y manda al servidor.
 * </p>
 * 
 * Reglas:
 * - Solo contienen lo que el usuario puede rellenar.
 * - Llevan las validaciones obligatorias (como {@code @NotBlank} o {@code @Min}).
 * - No llevan IDs autogenerados ni contadores automáticos.
 */