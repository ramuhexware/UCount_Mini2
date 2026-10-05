package com.freddieapp.origination.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "UCS_APPL_TRKG")
public class UcsApplTrkg implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_appl_trkg")
    private Long idApplTrkg;

    @Column(name = "id_appl")
    private String idAppl;

    @Column(name = "DTTM_CREATE")
    private LocalDateTime dttmCreate;

    public UcsApplTrkg() {}

    public Long getIdApplTrkg() {
        return idApplTrkg;
    }

    public void setIdApplTrkg(Long idApplTrkg) {
        this.idApplTrkg = idApplTrkg;
    }

    public String getIdAppl() {
        return idAppl;
    }

    public void setIdAppl(String idAppl) {
        this.idAppl = idAppl;
    }

    public LocalDateTime getDttmCreate() {
        return dttmCreate;
    }

    public void setDttmCreate(LocalDateTime dttmCreate) {
        this.dttmCreate = dttmCreate;
    }
}
