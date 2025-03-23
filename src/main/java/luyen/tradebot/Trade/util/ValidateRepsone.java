package luyen.tradebot.Trade.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import luyen.tradebot.Trade.dto.respone.ResponseCtraderDTO;

public class ValidateRepsone {


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
}
