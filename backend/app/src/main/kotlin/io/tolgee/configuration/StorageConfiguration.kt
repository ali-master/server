/*
 * Copyright (c) 2020. Tolgee
 */

package io.tolgee.configuration

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import io.tolgee.configuration.tolgee.TolgeeProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StorageConfiguration(properties: TolgeeProperties) {

  private val s3config = properties.fileStorage.s3

  @Bean
  fun getS3(): AmazonS3? {
    if (s3config.enabled) {
      val credentials: AWSCredentials = BasicAWSCredentials(
        s3config.accessKey,
        s3config.secretKey
      )

      val endpointConfig = AwsClientBuilder.EndpointConfiguration(s3config.endpoint, s3config.signingRegion)

      return AmazonS3ClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(credentials))
        .withEndpointConfiguration(endpointConfig)
        .enablePathStyleAccess()
        .build()
    }
    return null
  }
}
