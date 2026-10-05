package com.freddieapp.origination.domain;

import jakarta.persistence.*;

/**
 * JPA Entity mapping Counterparty Account to table 'ucs_cntprty_acct'.
 */
@Entity
@Table(name = "ucs_cntprty_acct")
public class UcsCntprtyAcct {

    @Id
    @Column(name = "id_cntprty_acct")
    private String idCntprtyAcct;

    @Column(name = "st_cntprty_acct")
    private String stCntprtyAcct;

    @Column(name = "role_cntprty_acct")
    private String roleCntprtyAcct;

    public UcsCntprtyAcct() {}

    public UcsCntprtyAcct(String idCntprtyAcct, String stCntprtyAcct, String roleCntprtyAcct) {
        this.idCntprtyAcct = idCntprtyAcct;
        this.stCntprtyAcct = stCntprtyAcct;
        this.roleCntprtyAcct = roleCntprtyAcct;
    }

    public String getIdCntprtyAcct() {
        return idCntprtyAcct;
    }

    public void setIdCntprtyAcct(String idCntprtyAcct) {
        this.idCntprtyAcct = idCntprtyAcct;
    }

    public String getStCntprtyAcct() {
        return stCntprtyAcct;
    }

    public void setStCntprtyAcct(String stCntprtyAcct) {
        this.stCntprtyAcct = stCntprtyAcct;
    }

    public String getRoleCntprtyAcct() {
        return roleCntprtyAcct;
    }

    public void setRoleCntprtyAcct(String roleCntprtyAcct) {
        this.roleCntprtyAcct = roleCntprtyAcct;
    }
}
