package org.zabalburu.lauki.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zabalburu.lauki.domain.Tarea;

public interface TareaRepository extends JpaRepository<Tarea, Integer> {

}
