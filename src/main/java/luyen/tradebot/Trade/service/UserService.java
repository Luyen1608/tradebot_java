package luyen.tradebot.Trade.service;

import jakarta.validation.Valid;
import luyen.tradebot.Trade.controller.request.UserCreationRequest;
import luyen.tradebot.Trade.controller.request.UserPasswordRequest;
import luyen.tradebot.Trade.controller.request.UserUpdateRequest;
import luyen.tradebot.Trade.controller.response.UserResponse;
import luyen.tradebot.Trade.dto.request.UserRequestDTO;
import luyen.tradebot.Trade.dto.respone.UserDetailResponse;
import luyen.tradebot.Trade.util.UserStatus;

import java.util.List;

public interface UserService {

    List<UserResponse> fillAll();

    UserResponse findByid(Long id);

    UserResponse findByUsername(String username);

    UserResponse findByEmail(String email);

    long save(UserRequestDTO req);

    void update(UserUpdateRequest req);

    void updateUser(long id, UserRequestDTO req);

    void changeStatus(long id, UserStatus status);

    void changePassword(UserPasswordRequest req);

    void delete(Long id);

    UserDetailResponse getUser(long userId);

    List<UserDetailResponse> getAllUsers(int pageNo, int pageSize);

}
