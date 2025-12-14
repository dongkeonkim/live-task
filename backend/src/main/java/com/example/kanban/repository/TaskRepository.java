package com.example.kanban.repository;

import com.example.kanban.entity.Task;
import com.example.kanban.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByUserOrderByOrderAsc(User user);
}
