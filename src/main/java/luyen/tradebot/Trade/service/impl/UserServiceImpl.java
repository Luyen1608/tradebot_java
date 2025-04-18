package luyen.tradebot.Trade.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.controller.request.UserPasswordRequest;
import luyen.tradebot.Trade.controller.request.UserUpdateRequest;
import luyen.tradebot.Trade.controller.response.UserResponse;
import luyen.tradebot.Trade.dto.request.AddressRequestDTO;
import luyen.tradebot.Trade.dto.request.UserRequestDTO;
import luyen.tradebot.Trade.dto.respone.PageResponse;
import luyen.tradebot.Trade.dto.respone.UserDetailResponse;
import luyen.tradebot.Trade.exception.ResourceNotFoundException;
import luyen.tradebot.Trade.model.AddressEntity;
import luyen.tradebot.Trade.model.UserEntity;
import luyen.tradebot.Trade.repository.AddressRepository;
import luyen.tradebot.Trade.repository.SearchRepository;
import luyen.tradebot.Trade.repository.UserRepository;
import luyen.tradebot.Trade.service.UserService;
import luyen.tradebot.Trade.util.UserStatus;
import luyen.tradebot.Trade.util.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j(topic = "USER-SERVICE")
@RequiredArgsConstructor()
public class UserServiceImpl implements UserService {

    private final AddressRepository addressRepository;

    private final UserRepository userRepository;

    private final SearchRepository searchRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UUID save(UserRequestDTO req) {
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
    }
    @Override
    public void updateUser(UUID id, UserRequestDTO req) {
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
//        user.setAddresses(convertToAddressSet(req.getAddresses()));
        userRepository.save(user);
        log.info("User has updated Successfully");
//        System.out.println("User has updated Successfully");
    }

    @Override
    public void changeStatus(UUID id, UserStatus status) {
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
    public UserResponse findByid(UUID id) {
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
    public void delete(UUID id) {
        userRepository.deleteById(id);
        log.info("User has deleted Successfully");
    }

    @Override
    public UserDetailResponse getUser(UUID userId) {
        UserEntity user = getUserById(userId);
        return UserDetailResponse.builder()
                .id(userId)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }
    @Override
    public PageResponse<?> getAllUsersWithSortBys(int pageNo, int pageSize, String sortBy) {
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }
        //check value
        List<Sort.Order> sorts = new ArrayList<>();
        if (StringUtils.hasLength(sortBy)) {
            //firstName:ASC
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                if (matcher.group(3).equalsIgnoreCase("desc")) {
                    sorts.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                } else {
                    sorts.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                }
            }
        }
        Pageable pageable = PageRequest.of(page,pageSize, Sort.by(sorts));
        Page<UserEntity> users = userRepository.findAll(pageable);
        List<UserDetailResponse> response = users.stream().map(user -> UserDetailResponse.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .build())
                .toList();
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(users.getTotalPages())
                .items(response)
                .build();
//        return users.stream().map(user -> UserDetailResponse.builder()
//                        .id(user.getId())
//                        .firstName(user.getFirstName())
//                        .lastName(user.getLastName())
//                        .email(user.getEmail())
//                        .phone(user.getPhone())
//                        .build())
//                .toList();
    }
    @Override
    public PageResponse<?> getAllUsersWithSortBysMultipleColums(int pageNo, int pageSize, String... sorts) {
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }
        //check value
        List<Sort.Order> orders = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
        for (String sortBy : sorts) {
            //firstName:ASC
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                if (matcher.group(3).equalsIgnoreCase("desc")) {
                    orders.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                } else {
                    orders.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                }
            }
        }
        Pageable pageable = PageRequest.of(page,pageSize, Sort.by(orders));
        Page<UserEntity> users = userRepository.findAll(pageable);
        List<UserDetailResponse> response = users.stream().map(user -> UserDetailResponse.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .build())
                .toList();
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(users.getTotalPages())
                .items(response)
                .build();
    }

    @Override
    public PageResponse<?> getAllUsersWithSearch(int pageNo, int pageSize, String search, String sortBy) {
        return searchRepository.getAllUsersWithSearch(pageNo, pageSize, search, sortBy);
    }

    @Override
    public PageResponse<?> advanceSearchByCriteria(int pageNo, int pageSize, String sortBy ,String address, String... search) {
        return searchRepository.advanceSearchUser(pageNo, pageSize,  sortBy,address, search);
    }

    @Override
    public PageResponse<?> searchBySpeciticaion(Pageable pageable, String[] user, String[] address) {
        Page<UserEntity> users = userRepository.findAll(pageable);
        return PageResponse.builder()
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalPage(users.getTotalPages())
                .items(users.stream().toList())
                .build();
    }


    private UserEntity getUserById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));
    }
}


