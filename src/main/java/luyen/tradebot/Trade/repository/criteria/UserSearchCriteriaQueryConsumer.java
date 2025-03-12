package luyen.tradebot.Trade.repository.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.function.Consumer;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchCriteriaQueryConsumer implements Consumer<SearchCriteria> {

    private CriteriaBuilder builder;
    private Predicate predicates;
    private Root root;

    @Override
    public void accept(SearchCriteria searchCriteria) {
        if (searchCriteria.getOperation().equals(">")){
            predicates =  builder.and(predicates,builder.greaterThanOrEqualTo(root.get(searchCriteria.getKeyword()), searchCriteria.getValue().toString()));
        } else if (searchCriteria.getOperation().equals("<")){
            predicates =  builder.and(predicates,builder.lessThanOrEqualTo(root.get(searchCriteria.getKeyword()), searchCriteria.getValue().toString()));
        } else {
            if (root.get(searchCriteria.getKeyword()).getJavaType() == String.class) {
                predicates =  builder.and(predicates,builder.like(root.get(searchCriteria.getKeyword()), "%"+searchCriteria.getValue().toString() + "%"));
            } else {
                predicates =  builder.and(predicates,builder.equal(root.get(searchCriteria.getKeyword()), searchCriteria.getValue().toString()));
            }
        }
    }

    @Override
    public Consumer<SearchCriteria> andThen(Consumer<? super SearchCriteria> after) {
        return Consumer.super.andThen(after);
    }
}
