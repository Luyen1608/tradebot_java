package luyen.tradebot.Trade.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.request.UserRequestDTO;
import luyen.tradebot.Trade.dto.respone.ResponseData;
import luyen.tradebot.Trade.dto.respone.ResponseError;
import luyen.tradebot.Trade.dto.respone.ResponseSuccess;
import luyen.tradebot.Trade.service.UserService;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Validated
@Slf4j
@Tag(name = "User Controller")
public class UserController {

//    @Autowired
//    private UserService userService;

    @Operation(summary = "Add user", description = "API create new user")
    @PostMapping(value = "/")
//    @RequestMapping(method=RequestMethod.POST, headers = "apiKey=v1.0")
//    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Integer> addUser(@Valid @RequestBody UserRequestDTO user) {
//        return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Can not add user");
        log.info("Adding user: {} {}" , user.getFirstName(), user.getLastName());
        try {
//            userService.addUser(user);
            return new ResponseData<>(HttpStatus.CREATED.value(), "User created", 1);
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
//            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Save user failed");
        }
    }

    @Operation(summary = "Update User", description = "description", responses = {
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
    public ResponseData<?> updateUser(@PathVariable("userId") @Min(1) int id, @RequestBody UserRequestDTO user) {
        System.out.println("Request Update User=" + id);
        return new ResponseData<>(HttpStatus.ACCEPTED.value(), "User updated");
    }

    @PatchMapping("/{userId}")
//    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseData<?> changeStatus(@PathVariable int userId, @RequestParam(required = false) boolean status) {
        System.out.println("Request Change Status=" + status);
        return new ResponseData<>(HttpStatus.ACCEPTED.value(), "User changed Status");
    }

    @DeleteMapping("/{userId}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseData<?> deleteUser(@PathVariable int userId) {
        System.out.println("Request Delete User=" + userId);
        return new ResponseData<>(HttpStatus.NO_CONTENT.value(), "User Deleted");
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseData<UserRequestDTO> getUser(@PathVariable @Min(1) int userId) {
        System.out.println("Request Get User=" + userId);
        return new ResponseData<>(HttpStatus.OK.value(), "Get user",
                new UserRequestDTO("luyen", "doan", "luyenday1@gmail.com", "0393622627"));
//        return new UserRequestDTO("luyen", "doan", "luyenday1@gmail.com", "0393622627");
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public ResponseData<List<UserRequestDTO>> getListUser(
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        System.out.println("Request Get ListUser");
        return new ResponseData<>(HttpStatus.OK.value(), "Get List User", List.of(new UserRequestDTO("luyen", "doan", "luyenday1@gmail.com", "0393622627"),
                new UserRequestDTO("luyen", "doan", "luyenday1@gmail.com", "0393622627")));
    }
}
