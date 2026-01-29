package com.example.demo.repositories;

import com.example.demo.models.branch.BankBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankBranchRepository extends JpaRepository<BankBranch,Long> {

    @Query("SELECT bb FROM BankBranch bb JOIN bb.location l " +
            "WHERE (:city IS NULL OR :city = '' OR UPPER(l.city) LIKE UPPER(CONCAT('%', :city, '%'))) " +
            "OR (:street IS NULL OR :street = '' OR UPPER(l.address) LIKE UPPER(CONCAT('%', :street, '%')))")
    List<BankBranch> searchByCityAndStreetPartially(
            @Param("city") String city,
            @Param("street") String street
    );

    @Query(value = "SELECT * FROM bank_branches " +
            "ORDER BY ST_Distance_Sphere(POINT(:userLon, :userLat), POINT(longitude, latitude)) " +
            "LIMIT 10",
            nativeQuery = true)
    List<BankBranch> findNearestBranchesNative(
            @Param("userLat") double userLat,
            @Param("userLon") double userLon
    );

    @Query("SELECT b FROM BankBranch b JOIN b.bankServices s WHERE s.bankServiceName = :serviceName")
    List<BankBranch> findByServiceName(@Param("serviceName") String serviceName);


    @Query("SELECT b FROM BankBranch b WHERE b.bankBranchName = :branchName ")
    Optional<BankBranch> findByName(@Param("branchName") String branchName);
}
