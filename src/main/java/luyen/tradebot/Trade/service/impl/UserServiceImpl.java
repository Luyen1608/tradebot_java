package luyen.tradebot.Trade.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.controller.request.UserPasswordRequest;
import luyen.tradebot.Trade.controller.request.UserUpdateRequest;
import luyen.tradebot.Trade.controller.response.UserResponse;
import luyen.tradebot.Trade.dto.request.AddressRequestDTO;
import luyen.tradebot.Trade.dto.request.UserRequestDTO;
import luyen.tradebot.Trade.dto.respone.UserDetailResponse;
import luyen.tradebot.Trade.exception.ResourceNotFoundException;
import luyen.tradebot.Trade.model.AddressEntity;
import luyen.tradebot.Trade.model.UserEntity;
import luyen.tradebot.Trade.repository.AddressRepository;
import luyen.tradebot.Trade.repository.UserRepository;
import luyen.tradebot.Trade.service.UserService;
import luyen.tradebot.Trade.util.UserStatus;
import luyen.tradebot.Trade.util.UserType;
import org.apache.catalina.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "USER-SERVICE")
@RequiredArgsConstructor()
public class UserServiceImpl implements UserService {

    private final AddressRepository addressRepository;


    private final UserRepository userRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long save(UserRequestDTO req) {
        log.info("Saving user {}", req);
        UserEntity user = UserEntity.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .gender(req.getGender())
                .birthDate(req.getDateOfBirth())
                .email(req.getEmail())
                .phone(req.getPhone())
                .userType(UserType.valueOf(req.getType().toUpperCase()))
                .userStatus(UserStatus.ACTIVE)
                .addresses(convertToAddressSet(req.getAddresses()))
                .build();
        req.getAddresses().forEach(a -> {
            user.saveAddresses(AddressEntity.builder()
                    .apartmentNumber(a.getApartmentNumber())
                    .floor(a.getFloor())
                    .building(a.getBuilding())
                    .streetNumber(a.getStreetNumber())
                    .street(a.getStreet())
                    .city(a.getCity())
                    .country(a.getCountry())
                    .addressType(a.getAddressType())
                    .build());
        });

        userRepository.save(user);
        log.info("User has save");

        return user.getId();

//        user.set
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
    }

    @Override
    public void updateUser(long id, UserRequestDTO req) {
        UserEntity user = getUserById(id);
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setGender(req.getGender());
        user.setBirthDate(req.getDateOfBirth());
        if (StringUtils.hasLength(req.getEmail()) && !req.getEmail().equals(user.getEmail())) {
            user.setEmail(req.getEmail());
        }
        user.setPhone(req.getPhone());
        user.setUserType(UserType.valueOf(req.getType().toUpperCase()));
        user.setUserStatus(UserStatus.ACTIVE);
        user.setAddresses(convertToAddressSet(req.getAddresses()));
        userRepository.save(user);
        log.info("User has updated Successfully");
//        System.out.println("User has updated Successfully");
    }
    @Override
    public void changeStatus(long id, UserStatus status) {
        UserEntity user = getUserById(id);
        user.setUserStatus(status);
        userRepository.save(user);
        log.info("User has changed Status Successfully");
    }
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


    private Set<AddressEntity> convertToAddressSet(Set<AddressRequestDTO> addresses) {
        Set<AddressEntity> result = new HashSet<>();
        addresses.forEach(address ->
                result.add(AddressEntity.builder()
                        .apartmentNumber(address.getApartmentNumber())
                        .floor(address.getFloor())
                        .building(address.getBuilding())
                        .streetNumber(address.getStreetNumber())
                        .street(address.getStreet())
                        .city(address.getCity())
                        .country(address.getCountry())
                        .addressType(address.getAddressType())
                        .build()));
        return result;
    }

    @Override
    public void update(UserUpdateRequest req) {

    }




    @Override
    public void changePassword(UserPasswordRequest req) {

    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
        log.info("User has deleted Successfully");
    }

    @Override
    public UserDetailResponse getUser(long userId) {
        UserEntity user = getUserById(userId);
        return UserDetailResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }

    @Override
    public List<UserDetailResponse> getAllUsers(int pageNo, int pageSize) {
        int p = 0;
        if (pageNo > 0) {
            p = (pageNo - 1);
        }
        Pageable pageable = PageRequest.of(p, pageSize);
        Page<UserEntity> users = userRepository.findAll(pageable);
        return users.stream().map(user-> UserDetailResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build())
                .toList();
    }

    private UserEntity getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));
    }
}
