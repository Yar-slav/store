package com.gridu.store.repository;

import com.gridu.store.model.CartStatus;
import com.gridu.store.model.UserEntity;
import com.gridu.store.model.CartEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepo extends CrudRepository<CartEntity, Long> {

    Optional<CartEntity> findByUserAndProductIdAndCartStatus(UserEntity user, Long id, CartStatus cartStatus);
    List<CartEntity> findAllByUserAndCartStatus(UserEntity user, CartStatus cartStatus);
    List<CartEntity> findAllByUser(UserEntity user);
    List<CartEntity> findAllByOrderId(Long orderId);
    List<CartEntity> findAllByOrderIdAndCartStatus(Long orderId, CartStatus cartStatus);

    void deleteByUserAndCartStatus(UserEntity user, CartStatus cartStatus);

}
