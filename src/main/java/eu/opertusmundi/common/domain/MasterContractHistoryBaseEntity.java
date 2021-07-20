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
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass 
public class MasterContractHistoryBaseEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(
        sequenceName = "contract.master_contract_hist_id_seq", name = "master_contract_hist_id_seq", allocationSize = 1
    )
    @GeneratedValue(generator = "master_contract_hist_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    protected Integer id ;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Getter
    protected final UUID key = UUID.randomUUID();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`owner`", nullable = false)
    @Getter
    @Setter
    protected HelpdeskAccountEntity owner;
    
    @OneToOne(
        optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = false
    )
    @JoinColumn(name = "`contract_root`")
    @Getter
    @Setter
    protected MasterContractHistoryEntity contractRoot;

    @OneToOne(
        optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = false
    )
    @JoinColumn(name = "`contract_parent`")
    @Getter
    @Setter
    protected MasterContractHistoryEntity contractParent;
    
    @Column(name = "`title`")
    @Getter
    @Setter
    protected String title;

    @Column(name = "`subtitle`")
    @Getter
    @Setter
    protected String subtitle;

    @Column(name = "`version`")
    @Getter
    @Setter
    protected String version;

    @NotNull
    @Column(name = "`status`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    protected EnumContractStatus status;
    
    @Column(name = "`created_at`")
    @Getter
    @Setter
    protected ZonedDateTime createdAt;

    @Column(name = "`modified_at`")
    @Getter
    @Setter
    protected ZonedDateTime modifiedAt;

}
