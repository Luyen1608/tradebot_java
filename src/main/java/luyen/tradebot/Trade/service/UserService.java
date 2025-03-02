package luyen.tradebot.Trade.service;

import jakarta.validation.Valid;
import luyen.tradebot.Trade.dto.request.UserRequestDTO;

public interface UserService {

    int addUser(UserRequestDTO userRequestDTO);

}
