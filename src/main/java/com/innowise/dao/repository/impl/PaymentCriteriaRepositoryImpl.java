package com.innowise.dao.repository.impl;

import com.innowise.dao.repository.PaymentCriteriaRepository;
import com.innowise.model.dto.PaymentStatus;
import com.innowise.model.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class PaymentCriteriaRepositoryImpl implements PaymentCriteriaRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public BigDecimal findTotalAmountByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        MatchOperation match = Aggregation.match(
                Criteria.where("timestamp").gte(startDate.toInstant(ZoneOffset.UTC))
                        .lte(endDate.toInstant(ZoneOffset.UTC))
        );

        GroupOperation group = Aggregation.group().sum("paymentAmount").as("totalAmount");

        Aggregation aggregation = Aggregation.newAggregation(match, group);

        AggregationResults<Result> result = mongoTemplate.aggregate(aggregation, "payments", Result.class);

        return Objects.requireNonNullElse(result.getUniqueMappedResult(), new Result(BigDecimal.ZERO))
                .totalAmount();
    }

    private record Result(BigDecimal totalAmount) {}

    @Override
    public List<Payment> search(String userId, String orderId, PaymentStatus status, int page, int size) {

        List<Criteria> criteriaList = new ArrayList<>();

        if (userId != null && !userId.isBlank()) {
            criteriaList.add(Criteria.where("userId").is(userId));
        }
        if (orderId != null && !orderId.isBlank()) {
            criteriaList.add(Criteria.where("orderId").is(orderId));
        }
        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }

        Query query = new Query();

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        query.skip((long) page * size);
        query.limit(size);

        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));

        return mongoTemplate.find(query, Payment.class);
    }

}
