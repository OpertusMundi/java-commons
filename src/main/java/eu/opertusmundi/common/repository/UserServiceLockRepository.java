package eu.opertusmundi.common.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.RecordLockEntity;
import eu.opertusmundi.common.model.EnumRecordLock;
import eu.opertusmundi.common.model.RecordLockDto;

@Repository
@Transactional(readOnly = true)
public interface UserServiceLockRepository extends JpaRepository<RecordLockEntity, BigInteger> {

    @Query("SELECT a FROM Account a WHERE a.key = :key")
    Optional<AccountEntity> findAccountByKey(UUID key);

    @Query("SELECT l FROM RecordLock l "
         + "    LEFT OUTER JOIN UserService d "
         + "    ON l.recordId = d.id "
         + "WHERE l.recordType = 'USER_SERVICE' and d.key = :serviceKey")
    Optional<RecordLockEntity> findOne(UUID serviceKey);

    default Optional<RecordLockDto> findOneObject(UUID serviceKey) {
        return this.findOne(serviceKey).map(RecordLockEntity::toDto);
    }

    @Query("SELECT l FROM RecordLock l "
         + "    LEFT OUTER JOIN UserService d "
         + "    ON l.recordId = d.id "
         + "WHERE l.owner.key = :userKey and l.recordType = 'USER_SERVICE' and d.key = :serviceKey")
    Optional<RecordLockEntity> findOne(UUID userKey, UUID serviceKey);

    @Query("SELECT l FROM RecordLock l "
         + "    LEFT OUTER JOIN UserService d "
         + "    ON l.recordId = d.id "
         + "WHERE l.recordType = 'USER_SERVICE' and d.key in :keys")
    List<RecordLockEntity> findAll(List<UUID> keys);

    default List<RecordLockDto> findAllObjects(List<UUID> keys) {
        return this.findAll(keys).stream()
            .map(RecordLockEntity::toDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = false)
    default RecordLockDto create(EnumRecordLock recordType, int recordId, UUID ownerKey) {
        final RecordLockEntity lock  = new RecordLockEntity();
        final AccountEntity    owner = this.findAccountByKey(ownerKey).get();

        lock.setOwner(owner);
        lock.setRecordId(recordId);
        lock.setRecordType(recordType);

        this.saveAndFlush(lock);

        return lock.toDto();
    }
}
