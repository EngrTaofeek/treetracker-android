package org.greenstand.android.TreeTracker.api

import android.annotation.SuppressLint
import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.internal.StaticCredentialsProvider

import com.amazonaws.services.s3.AmazonS3
//import com.amazonaws.services.s3.AmazonS3ClientBuilder

import com.amazonaws.services.s3.model.AccessControlList
import com.amazonaws.services.s3.model.GroupGrantee
import com.amazonaws.services.s3.model.Permission
import com.amazonaws.services.s3.model.PutObjectRequest

import org.greenstand.android.TreeTracker.BuildConfig

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import com.amazonaws.regions.Region
import com.amazonaws.services.s3.AmazonS3Client
import timber.log.Timber


class ObjectStorageClient private constructor() {

    private var s3Client: AmazonS3? = null

    // Acceleration IS available in android SDK
    // https://github.com/aws-amplify/aws-sdk-android/blob/master/aws-android-sdk-s3/src/main/java/com/amazonaws/services/s3/S3ClientOptions.java#L114
    // https://github.com/aws-amplify/aws-sdk-android/issues/515
    // private var transferUtility: TransferUtility

    init {

        val basicAWSCredentials = BasicAWSCredentials(BuildConfig.OBJECT_STORAGE_ACCESS_KEY, BuildConfig.OBJECT_STORAGE_SECRET_KEY)
        val bucketName = BuildConfig.OBJECT_STORAGE_BUCKET
        val credentialsProvider = StaticCredentialsProvider(basicAWSCredentials)

        if(BuildConfig.USE_AWS_S3) {

            val clientRegion = BuildConfig.OBJECT_STORAGE_ENDPOINT
            Timber.tag("Acceleration").d(BuildConfig.OBJECT_STORAGE_ACCESS_KEY +':'+ BuildConfig.OBJECT_STORAGE_SECRET_KEY + ':' + BuildConfig.OBJECT_STORAGE_BUCKET + ':' + clientRegion)
            try {

                // Create an Amazon S3 client that is configured to use the accelerate endpoint.
                val region = Region.getRegion(clientRegion);
                s3Client = AmazonS3Client(credentialsProvider, region, ClientConfiguration())

                // Enable Transfer Acceleration for the specified bucket.
                // This line is not necessary
                //s3Client?.setS3ClientOptions(S3ClientOptions.builder().setAccelerateModeEnabled(true).build());
                /*
                s3Client?.setBucketAccelerateConfiguration(
                    SetBucketAccelerateConfigurationRequest (bucketName,
                    BucketAccelerateConfiguration (
                            BucketAccelerateStatus.Enabled
                    )
                ));
                */

                // Verify that transfer acceleration is enabled for the bucket.
                // You need the right permissions to get this
                /*val accelerateStatus = s3Client?.getBucketAccelerateConfiguration (
                         GetBucketAccelerateConfigurationRequest (bucketName))
                    ?.status
                Timber.tag("Acceleration").d("Bucket accelerate status: " + accelerateStatus)
                */

            } catch (e : AmazonServiceException) {
                // The call was transmitted successfully, but Amazon S3 couldn't process
                // it, so it returned an error response.
                e.printStackTrace()
                throw(e)

            } catch (e : Exception) {
                // Amazon S3 couldn't be contacted for a response, or the client
                // couldn't parse the response from Amazon S3.
                e.printStackTrace()
                throw(e)
            }

        } else {

            s3Client = AmazonS3Client(credentialsProvider)
            s3Client?.setEndpoint(String.format("https://%s/", BuildConfig.OBJECT_STORAGE_ENDPOINT))

        }




    }

    @SuppressLint("SimpleDateFormat")
    @Throws(AmazonClientException::class)
    fun put(path: String): String {
        val acl = AccessControlList()
        acl.grantPermission(GroupGrantee.AllUsers, Permission.Read)

        val image = File(path)
        val timeStamp = SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(Date())

        val dosKey = timeStamp + '_'.toString() + UUID.randomUUID() + '_'.toString() + image.name
        val poRequest = PutObjectRequest(BuildConfig.OBJECT_STORAGE_BUCKET, dosKey, image)
        poRequest.withAccessControlList(acl)
        val poResult = s3Client?.putObject(poRequest)
        return String.format("https://%s.%s/%s",  BuildConfig.OBJECT_STORAGE_BUCKET,
            BuildConfig.OBJECT_STORAGE_ENDPOINT, dosKey)
    }

    companion object {


        private var sInstance: ObjectStorageClient? = null

        fun instance(): ObjectStorageClient {
            if (sInstance == null) {
                sInstance = ObjectStorageClient()
            }
            return sInstance!!
        }
    }

}
