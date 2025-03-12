package luyen.tradebot.Trade.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
import luyen.tradebot.Trade.dto.respone.PageResponse;
import luyen.tradebot.Trade.dto.respone.UserDetailResponse;
import luyen.tradebot.Trade.model.AddressEntity;
import luyen.tradebot.Trade.model.UserEntity;
import luyen.tradebot.Trade.repository.criteria.SearchCriteria;
import luyen.tradebot.Trade.repository.criteria.UserSearchCriteriaQueryConsumer;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class SearchRepository {

    @PersistenceContext
    private EntityManager entityManager;



    public PageResponse<?> getAllUsersWithSearch(int pageNo, int pageSize, String search, String sorts) {
        //query ra list user
        StringBuilder sqlQuery = new StringBuilder("SELECT new luyen.tradebot.Trade.dto.respone.UserDetailResponse(u.id, u.firstName, u.lastName, u.email, u.phone) FROM User u where 1=1");
        if (StringUtils.hasLength(search)) {
            sqlQuery.append(" AND lower(u.firstName) like lower(:firstName)");
            sqlQuery.append(" OR lower(u.lastName) like lower(:lastName)");
            sqlQuery.append(" OR lower(u.email) like lower(:email)");
        }
        if (StringUtils.hasLength(sorts)) {
            //firstName:asc
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sorts);
            if (matcher.find()) {
                sqlQuery.append(String.format(" order by u.%s %s", matcher.group(1), matcher.group(3)));
            }
        }
        Query selectQuery = entityManager.createQuery(sqlQuery.toString());
        selectQuery.setFirstResult(pageNo);
        selectQuery.setMaxResults(pageSize);
        if (StringUtils.hasLength(search)) {
            selectQuery.setParameter("firstName", String.format("%%%s%%", search));
            selectQuery.setParameter("lastName", String.format("%%%s%%", search));
            selectQuery.setParameter("email", String.format("%%%s%%", search));
        }
        //add sorts

        List users = selectQuery.getResultList();

        //query ra số record
        StringBuilder sqlCountQuery = new StringBuilder(" SELECT count(*) FROM User u where 1=1");
        if (StringUtils.hasLength(search)) {
            sqlCountQuery.append(" AND lower(u.firstName) like lower(?1)");
            sqlCountQuery.append(" OR lower(u.lastName) like lower(?2)");
            sqlCountQuery.append(" OR lower(u.email) like lower(?3)");
        }

        Query selectCountQuery = entityManager.createQuery(sqlCountQuery.toString());
        if (StringUtils.hasLength(search)) {
            selectCountQuery.setParameter(1, String.format("%%%s%%", search));
            selectCountQuery.setParameter(2, String.format("%%%s%%", search));
            selectCountQuery.setParameter(3, String.format("%%%s%%", search));
        }
        Long totalElements = (Long) selectCountQuery.getSingleResult();

        System.out.println(users);

        Page<?> page = new PageImpl<Object>(users, PageRequest.of(pageNo,pageSize), totalElements);

        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .items(users.stream().toList())
                .build();
    }

    public PageResponse<?> advanceSearchUser(int pageNo, int pageSize,  String sorts, String address, String... searchs) {
        //firstName:hung, lastName:nguyen, address:hanoi
        // 1. get list user
        List<SearchCriteria> searchCriteria = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\w+?)(:|<|>)(.*)");
        if (searchs != null){
            for (String search : searchs) {
                //firstName:value
                Matcher matcher = pattern.matcher(search);
                if (matcher.find()) {
                    searchCriteria.add(new SearchCriteria(matcher.group(1),matcher.group(2),matcher.group(3)));
                }
            }
        }

        //2. get count record
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }
        Long totalElements = getTotalElement(searchCriteria, address);


        List<UserEntity> users = getUsers(page,pageSize, searchCriteria, sorts, address);
        return PageResponse.builder()
                .pageNo(pageNo) // offset = vi tri ban ghi cua danh sach
                .pageSize(pageSize)
                .totalPage(totalElements.intValue())
                .items(users)
                .build();
    }


    private List<UserEntity> getUsers(int pageNo, int pageSize, List<SearchCriteria> criteriaList, String sorts, String address) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> query = criteriaBuilder.createQuery(UserEntity.class);
        Root<UserEntity> root = query.from(UserEntity.class);


        // xu ly cac condition
        Predicate predicate = criteriaBuilder.conjunction();
        UserSearchCriteriaQueryConsumer queryConsumer = new UserSearchCriteriaQueryConsumer(criteriaBuilder, predicate, root);

        if (StringUtils.hasLength(address)) {
            Join<AddressEntity, UserEntity> addressJoin = root.join("addresses");
            Predicate addressPredicate = criteriaBuilder.like(addressJoin.get("city"), "%" + address + "%");
            //search tren tat ca cac file cua Address ?
            query.where(predicate, addressPredicate);
        } else {
            criteriaList.forEach(queryConsumer);
            predicate = queryConsumer.getPredicates();
            query.where(predicate);
        }

        if (StringUtils.hasLength(sorts)) {
            //firstName:asc
            Pattern pattern = Pattern.compile("(\\w+?)(:)(asc|desc)");
            Matcher matcher = pattern.matcher(sorts);
            if (matcher.find()) {
                String columnName = matcher.group(1);
                if (matcher.group(3).equalsIgnoreCase("desc")) {
                    query.orderBy(criteriaBuilder.desc(root.get(columnName)));
                } else {
                    query.orderBy(criteriaBuilder.asc(root.get(columnName)));
                }
            }
        }
        return entityManager.createQuery(query).setFirstResult(pageNo).setMaxResults(pageSize).getResultList();
    }

    private Long getTotalElement(List<SearchCriteria> criteriaList, String address) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<UserEntity> root = query.from(UserEntity.class);
        // xu ly cac condition
        Predicate predicate = criteriaBuilder.conjunction();
        UserSearchCriteriaQueryConsumer queryConsumer = new UserSearchCriteriaQueryConsumer(criteriaBuilder, predicate, root);

        if (StringUtils.hasLength(address)) {
            Join<AddressEntity, UserEntity> addressJoin = root.join("addresses");
            Predicate addressPredicate = criteriaBuilder.like(addressJoin.get("city"), "%" + address + "%");
            query.select(criteriaBuilder.count(root));
            query.where(predicate, addressPredicate);
        } else {
            criteriaList.forEach(queryConsumer);
            predicate = queryConsumer.getPredicates();
            query.select(criteriaBuilder.count(root));
            query.where(predicate);
        }

        return entityManager.createQuery(query).getSingleResult();

    }
}
