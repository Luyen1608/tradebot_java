package luyen.tradebot.Trade.exception;


import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Date;

//@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlerValidatorException(Exception e, WebRequest request) {
        System.out.println("=>>>>>>>>>ErrorResponse");
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
        errorResponse.setTimestamp(new Date());
        String message = e.getMessage();
        if (e instanceof MethodArgumentNotValidException) {
            int start = message.lastIndexOf("[");
            int end = message.lastIndexOf("]");
            message = message.substring(start + 1, end - 1);
            errorResponse.setError("Payload invalid");
        } else if (e instanceof ConstraintViolationException) {
            message = message.substring(message.indexOf(" ")+1);
            errorResponse.setError("Parameter invalid");
        }
        errorResponse.setMessage(message);

        return errorResponse;


    }
    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handlerInternalServerException(Exception e, WebRequest request) {
        System.out.println("=>>>>>>>>>ErrorResponse");
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
        errorResponse.setTimestamp(new Date());
        String message = e.getMessage();
        errorResponse.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        if (e instanceof MethodArgumentTypeMismatchException) {
            errorResponse.setMessage("Failed to convert value to type");
        }
        return errorResponse;


    }
}
