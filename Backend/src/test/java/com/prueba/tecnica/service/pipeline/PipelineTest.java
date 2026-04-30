package com.prueba.tecnica.service.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.prueba.tecnica.common.pipeline.Pipeline;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PipelineTest {
    @Test
    void debeRetornarInputSinCambiosCuandoNoHayFiltros() {
        Pipeline<String> pipeline = new Pipeline<>();

        String result = pipeline.process("hola");

        assertEquals("hola", result);
    }

    @Test
    void debeAplicarFiltrosCuandoExisten() {
        Pipeline<String> pipeline = new Pipeline<>();
        pipeline.addFilter(input -> input.toUpperCase());
        pipeline.addFilter(input -> input + "!");

        String result = pipeline.process("hola");

        assertEquals("HOLA!", result);
    }

}
