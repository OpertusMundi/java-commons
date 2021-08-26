package eu.opertusmundi.common.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.HelpdeskAccountEntity;
import eu.opertusmundi.common.model.account.helpdesk.EnumHelpdeskRole;
import eu.opertusmundi.common.model.account.helpdesk.HelpdeskAccountCommandDto;
import eu.opertusmundi.common.model.account.helpdesk.HelpdeskAccountDto;
import eu.opertusmundi.common.model.account.helpdesk.HelpdeskProfileCommandDto;

@Repository
@Transactional(readOnly = true)
public interface HelpdeskAccountRepository extends JpaRepository<HelpdeskAccountEntity, Integer> {

	@Query("SELECT count(a) FROM HelpdeskAccount a INNER JOIN a.roles r WHERE r.role = :role")
	long countByRole(@Param("role") EnumHelpdeskRole role);

	Optional<HelpdeskAccountEntity> findOneByEmail(String email);

	Optional<HelpdeskAccountEntity> findOneByEmailAndIdNot(String email, Integer id);

	@Query("select count(a) from HelpdeskAccount a inner join a.roles r where r.role = :role")
	Optional<Long> countUsersWithRole(@Param("role") EnumHelpdeskRole role);

	@Query("SELECT a FROM HelpdeskAccount a WHERE a.email like :email")
	Page<HelpdeskAccountEntity> findAllByEmailContains(
		@Param("email")String email,
		Pageable pageable
	);

    @Query("SELECT a FROM HelpdeskAccount a WHERE a.key in :keys")
    List<HelpdeskAccountEntity> findAllByKey(@Param("keys") List<UUID> keys);

	@Modifying
	@Query("UPDATE HelpdeskAccount a SET a.active = :active WHERE a.id = :id")
	@Transactional(readOnly = false)
	void setActive(@Param("id") Integer id, @Param("active") boolean active);

	@Modifying
	@Query("UPDATE HelpdeskAccount a SET a.blocked = :blocked WHERE a.id = :id")
	@Transactional(readOnly = false)
	void setBlocked(@Param("id") Integer id, @Param("blocked") boolean blocked);

	@Transactional(readOnly = false)
	default HelpdeskAccountDto setPassword(Integer id, String password) {

		// Retrieve entity from repository
		final HelpdeskAccountEntity accountEntity = this.findById(id).orElse(null);

		if (accountEntity == null) {
			throw new EntityNotFoundException();
		}

		final PasswordEncoder encoder = new BCryptPasswordEncoder();

		accountEntity.setPassword(encoder.encode(password));
		accountEntity.setModifiedOn(ZonedDateTime.now());

		return this.saveAndFlush(accountEntity).toDto();
	}

	@Transactional(readOnly = false)
	default HelpdeskAccountDto saveFrom(Integer creatorId, HelpdeskAccountCommandDto command) {
        final ZonedDateTime now     = ZonedDateTime.now();
        final HelpdeskAccountEntity creator = creatorId == null ? null : this.findById(creatorId).orElse(null);

		HelpdeskAccountEntity accountEntity = null;

		if (command.getId() != null) {
			// Retrieve entity from repository
			accountEntity = this.findById(command.getId()).orElse(null);

			if (accountEntity == null) {
				throw new EntityNotFoundException();
			}
		} else {
			// Create a new entity
			accountEntity = new HelpdeskAccountEntity();

            accountEntity.setCreatedOn(now);
            accountEntity.setEmail(command.getEmail());
            accountEntity.setEmailVerified(false);

            final PasswordEncoder encoder = new BCryptPasswordEncoder();
            accountEntity.setPassword(encoder.encode(command.getPassword()));
		}

		// Account properties
		accountEntity.setActive(command.isActive());
		accountEntity.setBlocked(command.isBlocked());
		accountEntity.setFirstName(command.getFirstName());
		accountEntity.setImage(command.getImage());
		accountEntity.setImageMimeType(command.getImageMimeType());
		accountEntity.setLastName(command.getLastName());
		accountEntity.setLocale(command.getLocale());
		accountEntity.setMobile(command.getMobile());
		accountEntity.setModifiedOn(now);
		accountEntity.setPhone(command.getPhone());

		// Roles
		final EnumHelpdeskRole[] currentRoles = accountEntity.getRoles().stream().toArray(EnumHelpdeskRole[]::new);
		for (final EnumHelpdeskRole role : currentRoles) {
			if (!command.getRoles().contains(role)) {
				accountEntity.revoke(role);
			}
		}

		for (final EnumHelpdeskRole role : command.getRoles()) {
			if (!accountEntity.hasRole(role)) {
				accountEntity.grant(role, creator);
			}
		}

		return this.saveAndFlush(accountEntity).toDto();
	}

	@Transactional(readOnly = false)
	default HelpdeskAccountDto saveProfile(HelpdeskProfileCommandDto command) {
		HelpdeskAccountEntity accountEntity = null;

		if (command.getId() != null) {
			// Retrieve entity from repository
			accountEntity = this.findById(command.getId()).orElse(null);

			if (accountEntity == null) {
				throw new EntityNotFoundException();
			}
		} else {
			throw new EntityNotFoundException();
		}

		accountEntity.updateProfile(command);

		return this.saveAndFlush(accountEntity).toDto();
	}

}
