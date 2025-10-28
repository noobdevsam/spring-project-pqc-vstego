package com.example.stego.orchestrationservice.repos;

import com.example.stego.orchestrationservice.document.Job;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends MongoRepository<Job, String> {

    Optional<Job> findByJobId(String jobId);

    List<Job> findBySenderUserId(String senderUserId);

}
