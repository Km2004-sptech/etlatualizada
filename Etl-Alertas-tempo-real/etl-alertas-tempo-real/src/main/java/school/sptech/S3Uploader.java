package school.sptech;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Paths;

public class S3Uploader {

    private final S3Client s3;

    public S3Uploader(String region) {

        this.s3 = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public void uploadFile(String bucketName, String keyName, String localFilePath) {

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .contentType("application/json")
                .build();

        s3.putObject(request, Paths.get(localFilePath));

        System.out.println("Upload enviado para o S3 com sucesso!");
    }
}