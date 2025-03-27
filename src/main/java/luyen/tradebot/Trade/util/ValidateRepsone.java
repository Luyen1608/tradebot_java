package luyen.tradebot.Trade.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import luyen.tradebot.Trade.dto.respone.ResponseCtraderDTO;
import org.springframework.beans.factory.annotation.Value;

public class ValidateRepsone {

    @Value("${tradebot.prefix}")
    public static String prefix;

    @Value("${tradebot.systemType}")
    public static String systemType;


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
    public static ResponseCtraderDTO formatResponsePlaceOrder(String message) {
        ResponseCtraderDTO responseCtraderDTO = new ResponseCtraderDTO();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(message);

            // Lấy giá trị của payloadType
            int payloadType = rootNode.get("payloadType").asInt();
            // lấy giá trị clientMsgId
            String clientMsgId = rootNode.get("clientMsgId").asText();
            // kiểm tra clientMsgId nếu có prefix trade365 thì lấy giá trị
            if (clientMsgId.startsWith(prefix)) {
                responseCtraderDTO.setSystemType(systemType);
            }
            // Lấy payload
            JsonNode payloadNode = rootNode.get("payload");

            // lấy position là dạng jsonnode, cần kiểm tra xem có position trước không mới lấy node
            if (payloadNode.has("position")) {
                JsonNode positionNode = payloadNode.get("position");
                //lấy positionId
                int positionId = positionNode.get("positionId").asInt();
                responseCtraderDTO.setPositionId(positionId);
            }
            //lấy executionType
            int executionType = payloadNode.get("executionType").asInt();
            // Lấy errorCode và description
            String errorCode = payloadNode.has("errorCode") ? payloadNode.get("errorCode").asText() : "N/A";
            String description = payloadNode.has("description") ? payloadNode.get("description").asText() : "No description";

            // chuyển đoạn builder dưới thành setter
            responseCtraderDTO.setPayloadReponse(payloadType);
            responseCtraderDTO.setClientMsgId(clientMsgId);
            responseCtraderDTO.setErrorCode(errorCode);
            responseCtraderDTO.setDescription(description);
            responseCtraderDTO.setExecutionType(executionType);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseCtraderDTO;
    }
}
