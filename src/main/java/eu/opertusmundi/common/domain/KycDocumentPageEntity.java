package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.kyc.KycDocumentPageDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "KycDocumentPage")
@Table(schema = "web", name = "`customer_kyc_document_page`")
public class KycDocumentPageEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.customer_kyc_document_page_id_seq", name = "customer_kyc_document_page_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "customer_kyc_document_page_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;
 
    @NotNull
    @Column(name = "`document`", updatable = false)
    @Getter
    @Setter
    private String document;
    
    @NotNull
    @ManyToOne(targetEntity = CustomerEntity.class)
    @JoinColumn(name = "customer", nullable = false)
    @Getter
    @Setter
    private CustomerEntity customer;
    
    @NotNull
    @Column(name = "`uploaded_on`")
    @Getter
    @Setter
    private ZonedDateTime uploadedOn;

    @NotNull
    @Column(name = "`file_name`")
    @Getter
    @Setter
    private String fileName;

    @NotNull
    @Column(name = "`file_type`")
    @Getter
    @Setter
    private String fileType;

    @NotNull
    @Column(name = "`file_size`")
    @Getter
    @Setter
    private Long fileSize;

    @Column(name = "`comment`")
    @Getter
    @Setter
    private String comment;
    
    @Column(name = "`tag`")
    @Getter
    @Setter
    private String tag;

    public KycDocumentPageDto toDto() {
        final KycDocumentPageDto p = new KycDocumentPageDto();

        p.setFileName(fileName);
        p.setFileSize(fileSize);
        p.setFileType(fileType);
        p.setTag(tag);
        p.setUploadedOn(uploadedOn);

        return p;
    }

}
