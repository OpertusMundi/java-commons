package eu.opertusmundi.common.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.CustomerEntity;

@Repository
@Transactional(readOnly = true)
public interface CustomerRepository extends JpaRepository<CustomerEntity, Integer> {

    @Query("SELECT p.consumer FROM AccountProfile p WHERE p.consumer.email = :email and p.account.id <> :id")
    Optional<CustomerEntity> findConsumerByEmailAndAccountIdNot(String email, Integer id);

    @Query("SELECT p.provider FROM AccountProfile p WHERE p.provider.email = :email and p.account.id <> :id")
    Optional<CustomerEntity> findProviderByEmailAndAccountIdNot(String email, Integer id);

    @Query("SELECT p.provider FROM AccountProfile p WHERE p.provider.pidNamespace = :namespace and p.account.id <> :id")
    Optional<CustomerEntity> findProviderByNamespaceAndAccountIdNot(String namespace, Integer id);

    @Query("SELECT p.provider FROM AccountProfile p WHERE p.provider.companyNumber = :companyNumber and p.account.id <> :id")
    Optional<CustomerEntity> findProviderByCompanyNumberAndAccountIdNot(String companyNumber, Integer id);

    @Query("SELECT c FROM Customer c WHERE c.paymentProviderUser = :paymentProviderUser")
    Optional<CustomerEntity> findCustomerByProviderUserId(String paymentProviderUser);

    @Query("SELECT c FROM Customer c WHERE c.paymentProviderWallet = :paymentProviderWallet")
    Optional<CustomerEntity> findCustomerByProviderWalletId(String paymentProviderWallet);

}
