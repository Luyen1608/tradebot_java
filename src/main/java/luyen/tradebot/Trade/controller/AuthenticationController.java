package luyen.tradebot.Trade.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.service.AuthenticationService;
import luyen.tradebot.Trade.util.SignInRequest;
import luyen.tradebot.Trade.util.TokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j(topic = "Authen")
@Validated
@RestController
@Controller
@RequestMapping("/auth")
@Tag(name = "Authentication controller")
@RequiredArgsConstructor
public class AuthenticationController {

//@Autowired
//private final AuthenticationService authenticationService;

    @PostMapping("/access-token")
    public ResponseEntity<TokenResponse> accessToken(@RequestBody SignInRequest request){
//        return new ResponseEntity<>(authenticationService.accessToken(request), HttpStatusCode.valueOf(HttpStatus.OK.value()));
        return null;
    }
}
