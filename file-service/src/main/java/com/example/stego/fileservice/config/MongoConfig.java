package com.example.stego.fileservice.config;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;

@Configuration
public class MongoConfig {

    @Bean
    public GridFSBucket gridFSBucket(MongoDatabaseFactory mongoDbFactory) {
        MongoDatabase mongoDatabase = mongoDbFactory.getMongoDatabase();
        return GridFSBuckets.create(mongoDatabase, "files");
    }
}
