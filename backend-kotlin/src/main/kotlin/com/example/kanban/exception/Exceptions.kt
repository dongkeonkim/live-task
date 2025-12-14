package com.example.kanban.exception

class EmailAlreadyExistsException(message: String) : RuntimeException(message)

class TaskNotFoundException(message: String) : RuntimeException(message)

class UnauthorizedAccessException(message: String) : RuntimeException(message)
