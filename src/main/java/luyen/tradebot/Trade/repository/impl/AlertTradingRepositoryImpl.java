package luyen.tradebot.Trade.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import luyen.tradebot.Trade.model.AlertTradingEntity;
import luyen.tradebot.Trade.repository.AlertTradingRepository;
import luyen.tradebot.Trade.util.enumTraderBot.AcctionTrading;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Repository
public class AlertTradingRepositoryImpl implements AlertTradingRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<AlertTradingEntity> findAll(Pageable pageable, String search, String sorts, LocalDateTime timestamp) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AlertTradingEntity> query = cb.createQuery(AlertTradingEntity.class);
        Root<AlertTradingEntity> root = query.from(AlertTradingEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        // Add predicates for each search criterion
        if (search != null && !search.isEmpty()) {
            String[] searchCriteria = search.split(",");
            for (String criterion : searchCriteria) {
                String[] keyValue = criterion.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    switch (key) {
                        case "signalToken":
                            predicates.add(cb.like(cb.lower(root.get("signalToken")), "%" + value.toLowerCase() + "%"));
//                            predicates.add(cb.equal(root.get("signalToken"), value));
                            break;
                        case "instrument":
                            predicates.add(cb.like(cb.lower(root.get("instrument")), "%" + value.toLowerCase() + "%"));
                            break;
                        case "action":
                            predicates.add(cb.equal(root.get("action"), AcctionTrading.valueOf(value.toUpperCase())));
                            break;
                        // Add more cases as needed for other fields
                    }
                }
            }
        }
//        if (timestamp != null) {
//            predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), timestamp));
//        }
//
        if (timestamp != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), timestamp));
        }
        query.where(predicates.toArray(new Predicate[0]));

        if (sorts != null && !sorts.isEmpty()) {
            String[] sortParams = sorts.split(":");
            if (sortParams.length == 2) {
                if ("asc".equalsIgnoreCase(sortParams[1])) {
                    query.orderBy(cb.asc(root.get(sortParams[0])));
                } else {
                    query.orderBy(cb.desc(root.get(sortParams[0])));
                }
            }
        }

        TypedQuery<AlertTradingEntity> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<AlertTradingEntity> resultList = typedQuery.getResultList();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<AlertTradingEntity> countRoot = countQuery.from(AlertTradingEntity.class);
        countQuery.select(cb.count(countRoot)).where(predicates.toArray(new Predicate[0]));
        // Use the getTotalElement method to get the total count
        Long totalElements = getTotalElement(search, timestamp);


        return new PageImpl<>(resultList, pageable, totalElements);
    }
    private Long getTotalElement(String search, LocalDateTime timestamp) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<AlertTradingEntity> root = countQuery.from(AlertTradingEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        // Add predicates for each search criterion
        if (search != null && !search.isEmpty()) {
            String[] searchCriteria = search.split(",");
            for (String criterion : searchCriteria) {
                String[] keyValue = criterion.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    switch (key) {
                        case "signalToken":
                            predicates.add(cb.like(cb.lower(root.get("signalToken")), "%" + value.toLowerCase() + "%"));
                            break;
                        case "instrument":
                            predicates.add(cb.like(cb.lower(root.get("instrument")), "%" + value.toLowerCase() + "%"));
                            break;
                        case "action":
                            predicates.add(cb.equal(root.get("action"), AcctionTrading.valueOf(value.toUpperCase())));
                            break;
                        // Add more cases as needed for other fields
                    }
                }
            }
        }

        if (timestamp != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), timestamp));
        }

        countQuery.select(cb.count(root)).where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends AlertTradingEntity> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends AlertTradingEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public void deleteAllInBatch(Iterable<AlertTradingEntity> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    /**
     * @param aLong
     * @deprecated
     */
    @Override
    public AlertTradingEntity getOne(Long aLong) {
        return null;
    }

    /**
     * @param aLong
     * @deprecated
     */
    @Override
    public AlertTradingEntity getById(Long aLong) {
        return null;
    }

    @Override
    public AlertTradingEntity getReferenceById(Long aLong) {
        return null;
    }

    @Override
    public <S extends AlertTradingEntity> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends AlertTradingEntity> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends AlertTradingEntity> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends AlertTradingEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends AlertTradingEntity> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends AlertTradingEntity> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends AlertTradingEntity, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends AlertTradingEntity> S save(S entity) {
        return null;
    }

    @Override
    public <S extends AlertTradingEntity> List<S> saveAll(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public Optional<AlertTradingEntity> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public List<AlertTradingEntity> findAll() {
        return List.of();
    }

    @Override
    public List<AlertTradingEntity> findAllById(Iterable<Long> longs) {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public void delete(AlertTradingEntity entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends AlertTradingEntity> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public List<AlertTradingEntity> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<AlertTradingEntity> findAll(Pageable pageable) {
        return null;
    }
}
