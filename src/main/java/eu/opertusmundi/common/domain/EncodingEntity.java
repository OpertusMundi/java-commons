package eu.opertusmundi.common.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.spatial.EncodingDto;
import lombok.Getter;

@Entity(name = "Encoding")
@Table(schema = "spatial", name = "`encoding`")
public class EncodingEntity {

    @Id
    @Column(name = "`code`", updatable = false)
    @Getter
    private String code;

    @NaturalId
    @Column(name = "`code_lower`", updatable = false)
    @Getter
    private String codeLower;

    @Column(name = "`active`")
    @Getter
    private boolean active;

    public EncodingDto toDto() {
        final EncodingDto e = new EncodingDto();
        e.setActive(active);
        e.setCode(code);
        return e;
    }

}
