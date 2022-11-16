package eu.opertusmundi.common.repository.contract;

import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.MasterContractEntity;
import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.contract.ContractMessageCode;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractDto;

@Repository
@Transactional(readOnly = true)
public interface MasterContractRepository extends JpaRepository<MasterContractEntity, Integer> {

    @Query("SELECT c FROM Contract c WHERE c.id = :id")
    Optional<MasterContractEntity> findOneById(Integer id);

    @Query("SELECT c FROM Contract c LEFT OUTER JOIN c.provider pr WHERE (c.key = :contractKey) and (pr is null or pr.key = :providerKey)")
    Optional<MasterContractEntity> findOneByKey(UUID providerKey, UUID contractKey);

    @Query("SELECT c FROM Contract c INNER JOIN c.parent p LEFT OUTER JOIN c.provider pr "
         + "WHERE (c.title like :title or :title is null) and "
         + "      (pr is null or pr.key = :providerKey) "
         + "ORDER BY c.defaultContract DESC"
    )
    Page<MasterContractEntity> findAll(String title, UUID providerKey, Pageable pageable);

    @Override
    @Transactional(readOnly = false)
    default void deleteById(Integer id) throws ApplicationException {
        final MasterContractEntity e = this.findOneById(id).orElse(null);

        if (e == null) {
            throw ApplicationException.fromMessage(
                ContractMessageCode.CONTRACT_NOT_FOUND, "Record not found"
            );
        }

        // Remove parent link
        if (e.getParent() != null) {
            e.getParent().setPublished(null);
            e.setParent(null);
        }

        this.delete(e);
        this.flush();
    }

    default Page<MasterContractDto> findAllObjects(String title, UUID providerKey, Pageable pageable) {
        if (StringUtils.isBlank(title)) {
            title = null;
        } else {
            if (!title.startsWith("%")) {
                title = "%" + title;
            }
            if (!title.endsWith("%")) {
                title = title + "%";
            }
        }
        return this.findAll(title, providerKey, pageable).map(c -> c.toDto(false));
    }

    default Optional<MasterContractDto> findOneObject(int id) {
        return this.findOneById(id).map(c -> c.toDto(true));
    }

    default Optional<MasterContractDto> findOneObject(UUID providerKey, UUID contractKey) {
        return this.findOneByKey(providerKey, contractKey).map(c -> c.toDto(true));
    }

}
