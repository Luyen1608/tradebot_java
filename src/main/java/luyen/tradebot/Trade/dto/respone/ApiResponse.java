package luyen.tradebot.Trade.dto.respone;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response class for consistent response format
 * 
 * @param <T> the type of data in the response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /**
     * HTTP status code
     */
    private int status;
    
    /**
     * Response message
     */
    private String message;
    
    /**
     * Response data (included only if not null)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
}