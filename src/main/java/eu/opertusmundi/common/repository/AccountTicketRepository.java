package eu.opertusmundi.common.repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.AccountTicketEntity;
import eu.opertusmundi.common.model.account.AccountTicketCommandDto;
import eu.opertusmundi.common.model.account.AccountTicketDto;
import eu.opertusmundi.common.model.account.EnumTicketStatus;

@Repository
@Transactional(readOnly = true)
public interface AccountTicketRepository extends JpaRepository<AccountTicketEntity, Integer> {

    @Query("SELECT a FROM Account a WHERE a.key = :key")
    Optional<AccountEntity> findAccountByKey(UUID key);

    @Query("""
        SELECT  t
        FROM    AccountTicket t LEFT OUTER JOIN t.owner o LEFT OUTER JOIN t.assignee a
        WHERE   (t.status in :status or :status is null) and
                (cast(:ownerKey as org.hibernate.type.UUIDCharType) IS NULL or o.key = :ownerKey) and
                (cast(:assigneeKey as org.hibernate.type.UUIDCharType) IS NULL or a.key = :assigneeKey)
    """ )
    Page<AccountTicketEntity> findAll(
        UUID ownerKey, UUID assigneeKey, Set<EnumTicketStatus> status, Pageable pageable
    );

    @Query("""
        SELECT  t
        FROM    AccountTicket t
        WHERE   (t.owner.key = :ownerKey) and (t.key = :ticketKey)
    """ )
    Optional<AccountTicketEntity> findOneByKey(UUID ownerKey, UUID ticketKey);

    @Transactional(readOnly = false)
    default AccountTicketDto create(AccountTicketCommandDto command) {
        final var owner  = this.findAccountByKey(command.getUserKey()).get();
        var       ticket = new AccountTicketEntity();

        ticket.setCreatedAt(ZonedDateTime.now());
        ticket.setMessage(command.getText());
        ticket.setMessageThreadKey(command.getMessageThreadKey());
        ticket.setOwner(owner);
        ticket.setResourceKey(command.getResourceKey());
        ticket.setStatus(EnumTicketStatus.OPEN);
        ticket.setSubject(command.getSubject());
        ticket.setType(command.getType());
        ticket.setUpdatedAt(ticket.getCreatedAt());

        ticket = this.saveAndFlush(ticket);

        return ticket.toDto();
    }

    @Transactional(readOnly = false)
    default AccountTicketDto setTicketMessageThread(UUID ownerKey, UUID ticketKey, UUID threadKey) {
        var ticket = this.findOneByKey(ownerKey, ticketKey).get();

        ticket.setMessageThreadKey(threadKey);
        ticket = this.saveAndFlush(ticket);

        return ticket.toDto();
    }

}
