package luyen.tradebot.Trade.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookPayload {
    private String type;
    private String table;
    @JsonProperty("record")
    private Map<String, Object> record;

    @JsonProperty("new_record")
    private Map<String, Object> newRecord;

    @JsonProperty("old_record")
    private Map<String, Object> oldRecord;

    // Add this field if "schema" is expected
    @JsonProperty("schema")
    private String schema;

}