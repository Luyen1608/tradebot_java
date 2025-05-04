package luyen.tradebot.Trade.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.respone.ResponseCtraderDTO;
import luyen.tradebot.Trade.util.enumTraderBot.PayloadType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Getter
public class ValidateRepsone {

    @Value("${tradebot.prefix}")
    private final String prefix;

    @Value("${tradebot.systemType}")
    private final String systemType;


    @Autowired
    public ValidateRepsone(@Value("${tradebot.prefix}") String prefix, @Value("${tradebot.systemType}") String systemType) {
        this.prefix = prefix;
        this.systemType = systemType;
    }


    @PostConstruct
    public void init() {
        log.info("prefix: {}", prefix);
        log.info("systemType: {}", systemType);
    }

    public static ResponseCtraderDTO formatResponse(String message) {
        ResponseCtraderDTO responseCtraderDTO = new ResponseCtraderDTO();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(message);

            // Lấy giá trị của payloadType
            int payloadType = rootNode.get("payloadType").asInt();
            // Lấy payload
            JsonNode payloadNode = rootNode.get("payload");
            // Lấy errorCode và description
            String errorCode = payloadNode.has("errorCode") ? payloadNode.get("errorCode").asText() : "N/A";
            String description = payloadNode.has("description") ? payloadNode.get("description").asText() : "No description";
            responseCtraderDTO.setPayloadReponse(payloadType);
            responseCtraderDTO.setErrorCode(errorCode);
            responseCtraderDTO.setDescription(description);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseCtraderDTO;
    }

    public ResponseCtraderDTO formatResponsePlaceOrder(String message) {
        ResponseCtraderDTO responseCtraderDTO = new ResponseCtraderDTO();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(message);

            // Lấy giá trị của payloadType
            int payloadType = rootNode.get("payloadType").asInt();
            // Lấy payload
            JsonNode payloadNode = rootNode.get("payload");
            if (payloadType == PayloadType.PROTO_OA_EXECUTION_EVENT.getValue()) {
                // lấy position là dạng jsonnode, cần kiểm tra xem có position trước không mới lấy node
                if (payloadNode.has("position")) {
                    JsonNode positionNode = payloadNode.get("position");

                    //lấy positionId
                    int positionId = positionNode.get("positionId").asInt();
                    responseCtraderDTO.setPositionId(positionId);
                }
                // lấy order là dạng jsonnode, cần kiểm tra xem có order trước không mới lấy node
                if (payloadNode.has("order")) {
                    JsonNode orderNode = payloadNode.get("order");
                    //lấy orderId
                    int orderId = orderNode.get("orderId").asInt();
                    responseCtraderDTO.setOrderCtraderId(orderId);
                    if (orderNode.has("tradeData")) {
                        JsonNode tradeData = orderNode.get("tradeData");
                        int volume = tradeData.get("volume").asInt();
                        responseCtraderDTO.setVolume(volume);
                    }
                    if (orderNode.has("closingOrder")) {
                        boolean closingOrder = orderNode.get("closingOrder").asBoolean();
                        responseCtraderDTO.setClosingOrder(closingOrder);
                    }
                }
                //lấy executionType
                int executionType = payloadNode.get("executionType").asInt();
                responseCtraderDTO.setExecutionType(executionType);
            }
            // lấy giá trị clientMsgId
            String clientMsgId = rootNode.get("clientMsgId").asText();
            // kiểm tra clientMsgId nếu có prefix trade365 thì lấy giá trị
            if (clientMsgId.startsWith(prefix)) {
                responseCtraderDTO.setSystemType(systemType);
            }
            // Lấy errorCode và description
            String errorCode = payloadNode.has("errorCode") ? payloadNode.get("errorCode").asText() : "";
            String description = payloadNode.has("description") ? payloadNode.get("description").asText() : "";

            // chuyển đoạn builder dưới thành setter
            responseCtraderDTO.setPayloadReponse(payloadType);
            responseCtraderDTO.setClientMsgId(clientMsgId);
            responseCtraderDTO.setErrorCode(errorCode);
            responseCtraderDTO.setDescription(description);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseCtraderDTO;
    }
}
