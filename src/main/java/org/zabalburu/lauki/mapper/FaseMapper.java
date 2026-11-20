package org.zabalburu.lauki.mapper;

import org.springframework.stereotype.Component;
import org.zabalburu.lauki.domain.Fase;

@Component
public class FaseMapper {

    public FaseDTO toDTO(Fase entity) {
        if (entity == null) {
            return null;
        }

        return new FaseDTO(
            entity.getId(),
            entity.getGrupo() != null ? entity.getGrupo().getId() : null,
            entity.getNombreFase(),
            entity.getOrden()
        );
    }

    public record FaseDTO(
        Integer id,
        Integer idGrupo,
        String nombreFase,
        Integer orden
    ) {}
}