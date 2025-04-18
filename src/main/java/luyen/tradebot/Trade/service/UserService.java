package luyen.tradebot.Trade.service;

import luyen.tradebot.Trade.controller.request.UserPasswordRequest;
import luyen.tradebot.Trade.controller.request.UserUpdateRequest;
import luyen.tradebot.Trade.controller.response.UserResponse;
import luyen.tradebot.Trade.dto.request.UserRequestDTO;
import luyen.tradebot.Trade.dto.respone.PageResponse;
import luyen.tradebot.Trade.dto.respone.UserDetailResponse;
import luyen.tradebot.Trade.util.UserStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserResponse> fillAll();

    UserResponse findByid(UUID id);

    UserResponse findByUsername(String username);

    UserResponse findByEmail(String email);

    UUID save(UserRequestDTO req);

    void update(UserUpdateRequest req);

    void updateUser(UUID id, UserRequestDTO req);

    void changeStatus(UUID id, UserStatus status);

    void changePassword(UserPasswordRequest req);

    void delete(UUID id);

    UserDetailResponse getUser(UUID userId);

    PageResponse<?> getAllUsersWithSortBys(int pageNo, int pageSize, String sortBy);

    PageResponse<?> getAllUsersWithSortBysMultipleColums(int pageNo, int pageSize, String... sortBy);

    PageResponse<?> getAllUsersWithSearch(int pageNo, int pageSize, String search, String sortBy);

    PageResponse<?> advanceSearchByCriteria(int pageNo, int pageSize,  String sortBy,String address, String... search);

    PageResponse<?> searchBySpeciticaion(Pageable pageable, String[] user, String[] address);



}
