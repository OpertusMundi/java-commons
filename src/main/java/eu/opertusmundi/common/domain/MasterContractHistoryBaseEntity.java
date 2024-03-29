package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.contract.EnumContractStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass 
@Getter
@Setter
public class MasterContractHistoryBaseEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(
        sequenceName = "contract.master_contract_hist_id_seq", name = "master_contract_hist_id_seq", allocationSize = 1
    )
    @GeneratedValue(generator = "master_contract_hist_id_seq", strategy = GenerationType.SEQUENCE)
    @Setter(AccessLevel.PROTECTED)
    protected Integer id ;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Setter(AccessLevel.PROTECTED)
    protected UUID key = UUID.randomUUID();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`owner`", nullable = false)
    protected HelpdeskAccountEntity owner;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`provider`", nullable = true)
    protected AccountEntity provider;
    
    @OneToOne(
        optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = false
    )
    @JoinColumn(name = "`contract_root`")
    protected MasterContractHistoryEntity contractRoot;

    @OneToOne(
        optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = false
    )
    @JoinColumn(name = "`contract_parent`")
    protected MasterContractHistoryEntity contractParent;
    
    @Column(name = "`title`")
    protected String title;

    @Column(name = "`subtitle`")
    protected String subtitle;

    @Column(name = "`version`")
    protected String version;

    @NotNull
    @Column(name = "`status`")
    @Enumerated(EnumType.STRING)
    protected EnumContractStatus status;
    
    @Column(name = "`created_at`")
    protected ZonedDateTime createdAt;

    @Column(name = "`modified_at`")
    protected ZonedDateTime modifiedAt;
    
    @NotNull
    @Column(name = "`default_contract`")
    protected boolean defaultContract;

}
