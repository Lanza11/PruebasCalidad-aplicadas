package com.prueba.tecnica.common.pipeline;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PipelineTest {

    @Test
    void process_CuandoNoHayFiltros_RetornaElMismoValor() {
        Pipeline<Integer> pipeline = new Pipeline<>();

        Integer resultado = pipeline.process(7);

        assertEquals(7, resultado);
    }

    @Test
    void process_CuandoHayUnFiltro_AplicaLaTransformacion() {
        Pipeline<Integer> pipeline = new Pipeline<>();
        pipeline.addFilter(input -> input + 3);

        Integer resultado = pipeline.process(7);

        assertEquals(10, resultado);
    }

    @Test
    void process_CuandoHayVariosFiltros_AplicaTodosEnOrden() {
        Pipeline<Integer> pipeline = new Pipeline<>();
        pipeline.addFilter(input -> input + 2)
                .addFilter(input -> input * 5)
                .addFilter(input -> input - 4);

        Integer resultado = pipeline.process(3);

        assertEquals(21, resultado);
    }
}
