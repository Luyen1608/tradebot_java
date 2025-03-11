package luyen.tradebot.Trade.repository.criteria;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class SearchCriteria {

    private String keyword; //firstName, email, lastName

    private String operation; // == >  <

    private Object value; //

}
