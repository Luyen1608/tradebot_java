package luyen.tradebot.Trade.service.impl;

import luyen.tradebot.Trade.dto.request.UserRequestDTO;
import luyen.tradebot.Trade.exception.ResourceNotFoundException;
import luyen.tradebot.Trade.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public int addUser(UserRequestDTO userRequestDTO) {
        System.out.println("Save user in DB");
        if (!userRequestDTO.getFirstName().equals("luyen")) {
            throw new ResourceNotFoundException("luyen khong ton tai");
        }
        return 0;
    }
}
