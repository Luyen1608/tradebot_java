package luyen.tradebot.Trade.service.impl;

import io.swagger.v3.oas.annotations.servers.Server;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.controller.request.UserCreationRequest;
import luyen.tradebot.Trade.controller.request.UserPasswordRequest;
import luyen.tradebot.Trade.controller.request.UserUpdateRequest;
import luyen.tradebot.Trade.controller.response.UserResponse;
import luyen.tradebot.Trade.service.UserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j(topic = "USER-SERVICE")
//@RequiredArgsConstructor()
public class UserServiceImpl implements UserService {

//    @Autowired
//    private AddressRepository addressRepository;

//    @Autowired
//    private UserRepository userRepository;

    @Override
    public List<UserResponse> fillAll() {
        return List.of();
    }

    @Override
    public UserResponse findByid(Long id) {
        return null;
    }

    @Override
    public UserResponse findByUsername(String username) {
        return null;
    }

    @Override
    public UserResponse findByEmail(String email) {
        return null;
    }

    @Override
//    @Transactional(rollbackFor = Exception.class)
    public long save(UserCreationRequest req) {
//        log.info("Saving user {}", req);
//        UserEntity user = new UserEntity();
//        user.setFirstName(req.getFirstName());
//        user.setLastName(req.getLastName());
//        user.setGender(req.getGender());
//        user.setBirthDate(req.getBirthDate());
//        user.setEmail(req.getEmail());
//        user.setPhone(req.getPhone());
//        user.setUserType(req.getUserType());
//        user.setUserStatus(UserStatus.ACTIVE);
////        userRepository.save(user);
//        if (user.getId() != null){
//            List<AddressEntity> addresses = new ArrayList<>();
//            req.getAddress().forEach(address -> {
//                AddressEntity addressEntity = new AddressEntity();
//                addressEntity.setApartmentNumber(address.getApartmentNumber());
//                addressEntity.setFloor(address.getFloor());
//                addressEntity.setBuilding(address.getBuilding());
//                addressEntity.setStreetNumber(address.getStreetNumber());
//                addressEntity.setStreet(address.getStreet());
//                addressEntity.setCity(address.getCity());
//                addressEntity.setCountry(address.getCountry());
//                addressEntity.setAddressType(address.getAddressType());
//                addresses.add(addressEntity);
//            });
////            addressRepository.saveAll(addresses);
//            log.info("Save Addresses {}", addresses);
//        }
//        return user.getId();
    return 1l;
    }

    @Override
    public void update(UserUpdateRequest req) {

    }

    @Override
    public void changePassword(UserPasswordRequest req) {

    }

    @Override
    public void delete(Long id) {

    }
}
