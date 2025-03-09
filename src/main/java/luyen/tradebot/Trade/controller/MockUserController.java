package luyen.tradebot.Trade.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transaction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.configuration.Translator;
import luyen.tradebot.Trade.controller.request.UserCreationRequest;
import luyen.tradebot.Trade.dto.request.UserRequestDTO;
import luyen.tradebot.Trade.dto.respone.ResponseData;
import luyen.tradebot.Trade.dto.respone.ResponseError;
import luyen.tradebot.Trade.dto.respone.ResponseSuccess;
import luyen.tradebot.Trade.dto.respone.UserDetailResponse;
import luyen.tradebot.Trade.exception.ResourceNotFoundException;
import luyen.tradebot.Trade.service.UserService;
import luyen.tradebot.Trade.util.UserStatus;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mockUser")
@Validated
@Slf4j
@Tag(name = "Mockup User Controller")
@RequiredArgsConstructor()
public class MockUserController {

    private final UserService userService;

    @PostMapping(value = "/")
//    @RequestMapping(method=RequestMethod.POST, headers = "apiKey=v1.0")
//    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> addUser(@Valid @RequestBody UserRequestDTO request) {

        try {
            long userId = userService.save(request);
            return new ResponseData<>(HttpStatus.CREATED.value(), "Add user success",userId);
        } catch (Exception e) {
            log.error("error message = {} ",   e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Add User Fail");
        }


//        Map<String, Object> result = new LinkedHashMap<>();
//        result.put("status", HttpStatus.CREATED.value());
//        result.put("message","User created successfully");
////        result.put("data",userService.save(request));
//        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
    @Operation(summary = "summary", description = "description", responses = {
            @ApiResponse(responseCode = "202", description = "User Updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "ex name", summary = "ex summary",
                                    value = "" +
                                            "{\n" +
                                            "    \"status\": 202,\n" +
                                            "    \"message\": \"User Updated\",\n" +
                                            "    \"data\": 1\n" +
                                            "}" +
                                            "")))
    })
    @PutMapping("/{userId}")
//    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseData<?> updateUser(@PathVariable("userId") @Min(1) long id, @RequestBody UserRequestDTO user) {
        System.out.println("Request Update User=" + id);
        try {
            userService.updateUser(id, user);
            return new ResponseData<>(HttpStatus.OK.value(), "User Updated successfully");
        } catch (Exception e) {
            log.error("error message = {} ",   e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Update User Fail");
        }

    }

    @PatchMapping("/{userId}")
//    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseData<?> changeStatus(@PathVariable long userId, @RequestParam(required = false) UserStatus status) {
        System.out.println("Request Change Status=" + status);
        try {
            userService.changeStatus(userId, status);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "User changed Status");
        } catch (Exception e) {
            log.error("error message = {} ",   e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Update Status Fail");
        }
    }

    @DeleteMapping("/{userId}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseData<?> deleteUser(@PathVariable long userId) {
        System.out.println("Request Delete User=" + userId);
        try {
            userService.delete(userId);
            return new ResponseData<>(HttpStatus.NO_CONTENT.value(), "User Deleted Successfully");
        } catch (Exception e) {
            log.error("error message = {} ",   e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Delete User Fail");
        }
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseData<UserDetailResponse> getUser(@PathVariable @Min(1) long userId) {
        System.out.println("Request Get User=" + userId);
        try {
            return new ResponseData<>(HttpStatus.OK.value(), "Get user", userService.getUser(userId));
        } catch (ResourceNotFoundException e) {
            log.error("error message getUser = {} ",   e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }


//        return new UserRequestDTO("luyen", "doan", "luyenday1@gmail.com", "0393622627");
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public ResponseData<List<UserDetailResponse>> getListUser(
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        System.out.println("Request Get ListUser");
        return new ResponseData<>(HttpStatus.OK.value(), "Get List User", userService.getAllUsers(pageNo,pageSize));
    }
}
