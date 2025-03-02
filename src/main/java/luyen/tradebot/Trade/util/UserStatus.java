package luyen.tradebot.Trade.util;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UserStatus {
    @JsonProperty("active")
    ACTIVE,

    @JsonProperty("inactive")
    INACTIVE,

    @JsonProperty("deleted")
    DELETED,

    @JsonProperty("none")
    NONE
}
