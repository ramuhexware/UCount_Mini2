package com.freddieapp.origination.repository;

import com.freddieapp.origination.domain.UcsOrgtnQuickSearchView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UcsOrgtnQuickSearchViewRepository extends JpaRepository<UcsOrgtnQuickSearchView, Long> {

    // Filter by search key substring
    @Query("SELECT v FROM UcsOrgtnQuickSearchView v WHERE UPPER(v.searchKey) LIKE UPPER(CONCAT('%', :searchTerm, '%'))")
    List<UcsOrgtnQuickSearchView> quickSearch(@Param("searchTerm") String searchTerm);

    // Paginated search over view
    Page<UcsOrgtnQuickSearchView> findBySearchKeyContainingIgnoreCase(String searchTerm, Pageable pageable);
}
