package demo.travel;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger logger = Logger.getLogger(ApiExceptionHandler.class.getName());
    @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<ChatDtos.ErrorResponse> validation(MethodArgumentNotValidException ex) { return ResponseEntity.badRequest().body(new ChatDtos.ErrorResponse("Please check your message and travel details.")); }
    @ExceptionHandler(HttpMessageNotReadableException.class) public ResponseEntity<ChatDtos.ErrorResponse> malformed() { return ResponseEntity.badRequest().body(new ChatDtos.ErrorResponse("The request body is invalid.")); }
    @ExceptionHandler(GeminiService.AiUnavailableException.class) public ResponseEntity<ChatDtos.ErrorResponse> ai(GeminiService.AiUnavailableException ex) { return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ChatDtos.ErrorResponse(ex.getMessage())); }
    @ExceptionHandler(NoResourceFoundException.class) public ResponseEntity<ChatDtos.ErrorResponse> notFound() { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ChatDtos.ErrorResponse("The requested resource was not found.")); }
    @ExceptionHandler(Exception.class) public ResponseEntity<ChatDtos.ErrorResponse> other(Exception ex, HttpServletRequest request) {
        logger.log(Level.SEVERE, "Unhandled HTTP error: method={0}, path={1}, exceptionType={2}",
                new Object[] { request.getMethod(), request.getRequestURI(), ex.getClass().getSimpleName() });
        return ResponseEntity.internalServerError().body(new ChatDtos.ErrorResponse("Something went wrong. Please try again."));
    }
}
