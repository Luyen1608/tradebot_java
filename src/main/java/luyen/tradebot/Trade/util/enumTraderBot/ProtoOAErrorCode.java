package luyen.tradebot.Trade.util.enumTraderBot;

import lombok.Getter;

@Getter
public enum ProtoOAErrorCode {
    OA_AUTH_TOKEN_EXPIRED(1, "Authorization"),
    ACCOUNT_NOT_AUTHORIZED(2, "When account is not authorized."),
    RET_NO_SUCH_LOGIN(12, "When such account no longer exists."),
    ALREADY_LOGGED_IN(14, "When client tries to authorize after it was already authorized."),
    INCORRECT_BOUNDARIES(35, "When requested period (from,to) is too large or invalid values are set to from/to."),
    RET_ACCOUNT_DISABLED(64, "When account is disabled."),
    CONNECTIONS_LIMIT_EXCEEDED(67, "Limit of connections is reached for this Open API client."),
    WORSE_GSL_NOT_ALLOWED(68, "Not allowed to increase risk for Positions with Guaranteed Stop Loss."),
    SYMBOL_HAS_HOLIDAY(69, "Trading disabled because symbol has holiday."),
    CH_CLIENT_AUTH_FAILURE(101, "Open API client is not activated or wrong client credentials."),
    CH_CLIENT_NOT_AUTHENTICATED(102, "When a command is sent for not authorized Open API client."),
    CH_CLIENT_ALREADY_AUTHENTICATED(103, "Client is trying to authenticate twice."),
    CH_ACCESS_TOKEN_INVALID(104, "Access token is invalid."),
    CH_SERVER_NOT_REACHABLE(105, "Trading service is not available."),
    CH_CTID_TRADER_ACCOUNT_NOT_FOUND(106, "Trading account is not found."),
    CH_OA_CLIENT_NOT_FOUND(107, "Could not find this client id."),
    REQUEST_FREQUENCY_EXCEEDED(108, "General"),
    SERVER_IS_UNDER_MAINTENANCE(109, "Server is under maintenance."),
    CHANNEL_IS_BLOCKED(110, "Operations are not allowed for this account."),
    NOT_SUBSCRIBED_TO_SPOTS(112, "Pricing"),
    ALREADY_SUBSCRIBED(113, "When subscription is requested for an active."),
    SYMBOL_NOT_FOUND(114, "Symbol not found."),
    UNKNOWN_SYMBOL(115, "Note: to be merged with SYMBOL_NOT_FOUND."),
    NO_QUOTES(117, "Trading"),
    NOT_ENOUGH_MONEY(118, "Not enough funds to allocate margin."),
    MAX_EXPOSURE_REACHED(119, "Max exposure limit is reached for a {trader, symbol, side}."),
    POSITION_NOT_FOUND(120, "Position not found."),
    ORDER_NOT_FOUND(121, "Order not found."),
    POSITION_NOT_OPEN(122, "When trying to close a position that it is not open."),
    POSITION_LOCKED(123, "Position in the state that does not allow to perform an operation."),
    TOO_MANY_POSITIONS(124, "Trading account reached its limit for max number of open positions and orders."),
    TRADING_BAD_VOLUME(125, "Invalid volume."),
    TRADING_BAD_STOPS(126, "Invalid stop price."),
    TRADING_BAD_PRICES(127, "Invalid price (e.g. negative)."),
    TRADING_BAD_STAKE(128, "Invalid stake volume (e.g. negative)."),
    PROTECTION_IS_TOO_CLOSE_TO_MARKET(129, "Invalid protection prices."),
    TRADING_BAD_EXPIRATION_DATE(130, "Invalid expiration."),
    PENDING_EXECUTION(131, "Unable to apply changes as position has an order under execution."),
    TRADING_DISABLED(132, "Trading is blocked for the symbol."),
    TRADING_NOT_ALLOWED(133, "Trading account is in read only mode."),
    UNABLE_TO_CANCEL_ORDER(134, "Unable to cancel order."),
    UNABLE_TO_AMEND_ORDER(135, "Unable to amend order."),
    SHORT_SELLING_NOT_ALLOWED(136, "Short selling is not allowed."),
    NOT_SUBSCRIBED_TO_PNL(137, "This session is not subscribed via ProtoOAv1PnLChangeSubscribeReq.");

    private final int code;
    private final String description;

    ProtoOAErrorCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ProtoOAErrorCode fromCode(int code) {
        for (ProtoOAErrorCode e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }
}
