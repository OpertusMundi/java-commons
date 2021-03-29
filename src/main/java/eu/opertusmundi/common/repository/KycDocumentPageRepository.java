package eu.opertusmundi.common.repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.CustomerEntity;
import eu.opertusmundi.common.domain.KycDocumentPageEntity;
import eu.opertusmundi.common.model.kyc.KycDocumentPageCommandDto;

@Repository
@Transactional(readOnly = true)
public interface KycDocumentPageRepository extends JpaRepository<KycDocumentPageEntity, Integer> {

    @Query("SELECT a FROM Account a "
         + "LEFT OUTER JOIN FETCH a.profile p "
         + "WHERE a.key = :key")
    Optional<AccountEntity> findAccountByKey(@Param("key") UUID key);
    
    @Transactional(readOnly = false)
    default void create(KycDocumentPageCommandDto command) {
        final AccountEntity         account  = this.findAccountByKey(command.getUserKey()).orElse(null);
        final CustomerEntity        customer = account.getCustomer(command.getCustomerType());
        final KycDocumentPageEntity document = new KycDocumentPageEntity();

        document.setComment(command.getComment());
        document.setCustomer(customer);
        document.setDocument(command.getKycDocumentId());
        document.setFileName(command.getFileName());
        document.setFileSize(command.getFileSize());
        document.setFileType(command.getFileType());
        document.setTag(command.getTag());
        document.setUploadedOn(ZonedDateTime.now());

        this.saveAndFlush(document);
    }

}
