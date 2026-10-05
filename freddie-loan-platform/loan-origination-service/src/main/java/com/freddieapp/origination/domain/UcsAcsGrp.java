package com.freddieapp.origination.domain;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Entity with predefined Named JPQL query at entity level.
 */
@Entity
@Table(name = "UCS_ACS_GRP")
@NamedQuery(name = "UcsAcsGrp.findAll", query = "SELECT u FROM UcsAcsGrp u")
public class UcsAcsGrp extends Auditable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_acs_grp")
    private Long idAcsGrp;

    @Column(name = "nm_acs_grp")
    private String nmAcsGrp;

    @Column(name = "dsc_acs_grp")
    private String dscAcsGrp;

    public UcsAcsGrp() {}

    public UcsAcsGrp(String nmAcsGrp, String dscAcsGrp) {
        this.nmAcsGrp = nmAcsGrp;
        this.dscAcsGrp = dscAcsGrp;
    }

    public Long getIdAcsGrp() {
        return idAcsGrp;
    }

    public void setIdAcsGrp(Long idAcsGrp) {
        this.idAcsGrp = idAcsGrp;
    }

    public String getNmAcsGrp() {
        return nmAcsGrp;
    }

    public void setNmAcsGrp(String nmAcsGrp) {
        this.nmAcsGrp = nmAcsGrp;
    }

    public String getDscAcsGrp() {
        return dscAcsGrp;
    }

    public void setDscAcsGrp(String dscAcsGrp) {
        this.dscAcsGrp = dscAcsGrp;
    }
}
