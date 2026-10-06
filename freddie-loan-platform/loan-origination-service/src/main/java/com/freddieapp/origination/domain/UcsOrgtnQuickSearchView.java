package com.freddieapp.origination.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import java.io.Serializable;

/**
 * Read-only JPA Entity mapping the database quick search view 'ucs_orgtn_quick_search_view'.
 */
@Entity
@Immutable
@Table(name = "ucs_orgtn_quick_search_view", schema = "yu09")
public class UcsOrgtnQuickSearchView implements Serializable {

    @Id
    @Column(name = "\"ROW_ID\"")
    private Long rowId;

    @Column(name = "\"ORGANIZATION_ID\"")
    private String organizationId;

    @Column(name = "\"INSTITUTION_NAME\"")
    private String institutionName;

    @Column(name = "\"ACCOUNT_IDENTIFIER\"")
    private String accountIdentifier;

    @Column(name = "\"ACCOUNT_NAME\"")
    private String accountName;

    @Column(name = "\"CITY\"")
    private String city;

    @Column(name = "\"STATE\"")
    private String state;

    @Column(name = "\"SEARCH_KEY\"")
    private String searchKey;

    public UcsOrgtnQuickSearchView() {}

    public Long getRowId() { return rowId; }
    public String getOrganizationId() { return organizationId; }
    public String getInstitutionName() { return institutionName; }
    public String getAccountIdentifier() { return accountIdentifier; }
    public String getAccountName() { return accountName; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getSearchKey() { return searchKey; }
}
