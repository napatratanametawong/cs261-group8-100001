package com.example.lc2_booking_room.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex,
      HttpServletRequest req) {
    var body = new LinkedHashMap<String, Object>();
    body.put("path", req.getRequestURI());
    body.put("error", "ValidationError");
    body.put("message", ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    return ResponseEntity.badRequest().body(body); // 400
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, Object>> handleConflict(DataIntegrityViolationException ex,
      HttpServletRequest req) {
    var body = new LinkedHashMap<String, Object>();
    body.put("path", req.getRequestURI());
    body.put("error", "Conflict");
    body.put("message", "Constraint violation (possible duplicate slot or FK error).");
    return ResponseEntity.status(409).body(body); // 409
  }

  @ExceptionHandler({ IllegalArgumentException.class })
  public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException ex,
      HttpServletRequest req) {
    var body = new LinkedHashMap<String, Object>();
    body.put("path", req.getRequestURI());
    body.put("error", "BadRequest");
    body.put("message", ex.getMessage());
    return ResponseEntity.badRequest().body(body); // 400
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex,
      HttpServletRequest req) {
    var body = new LinkedHashMap<String, Object>();
    body.put("path", req.getRequestURI());
    body.put("error", "NotFound");
    body.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body); // 404
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, HttpServletRequest req) {
    var body = new LinkedHashMap<String, Object>();
    body.put("path", req.getRequestURI());
    body.put("error", ex.getClass().getSimpleName());
    body.put("message", ex.getMessage());
    return ResponseEntity.status(500).body(body); // 500
  }
}
