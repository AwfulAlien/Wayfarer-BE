package demo.travel;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<ChatDtos.ErrorResponse> validation(MethodArgumentNotValidException ex) { return ResponseEntity.badRequest().body(new ChatDtos.ErrorResponse("Please check your message and travel details.")); }
    @ExceptionHandler(HttpMessageNotReadableException.class) public ResponseEntity<ChatDtos.ErrorResponse> malformed() { return ResponseEntity.badRequest().body(new ChatDtos.ErrorResponse("The request body is invalid.")); }
    @ExceptionHandler(GeminiService.AiUnavailableException.class) public ResponseEntity<ChatDtos.ErrorResponse> ai(GeminiService.AiUnavailableException ex) { return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ChatDtos.ErrorResponse(ex.getMessage())); }
    @ExceptionHandler(Exception.class) public ResponseEntity<ChatDtos.ErrorResponse> other(Exception ex) { return ResponseEntity.internalServerError().body(new ChatDtos.ErrorResponse("Something went wrong. Please try again.")); }
}
