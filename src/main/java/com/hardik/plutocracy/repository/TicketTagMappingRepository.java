package com.hardik.plutocracy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hardik.plutocracy.entity.TicketTagMapping;

@Repository
public interface TicketTagMappingRepository extends JpaRepository<TicketTagMapping, Integer> {

}