package com.prueba.tecnica.config;

import com.prueba.tecnica.common.pipeline.Pipeline;
import com.prueba.tecnica.dto.SolicitudPriorizada;
import com.prueba.tecnica.service.pipeline.rules.AntiquityFilter;
import com.prueba.tecnica.service.pipeline.rules.ManualPriorityFilter;
import com.prueba.tecnica.service.pipeline.rules.TypeFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PipelineConfig {

    @Bean
    public Pipeline<SolicitudPriorizada> prioritizationPipeline(
            TypeFilter typeFilter,
            ManualPriorityFilter manualPriorityFilter,
            AntiquityFilter antiquityFilter) {

        return new Pipeline<SolicitudPriorizada>()
                .addFilter(typeFilter)
                .addFilter(manualPriorityFilter)
                .addFilter(antiquityFilter);
    }
}
