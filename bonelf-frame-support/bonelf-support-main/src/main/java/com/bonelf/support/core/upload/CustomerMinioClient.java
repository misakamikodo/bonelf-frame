package com.bonelf.support.core.upload;

import com.google.common.collect.Multimap;
import io.minio.CreateMultipartUploadResponse;
import io.minio.ListPartsResponse;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.credentials.Provider;
import io.minio.credentials.StaticProvider;
import io.minio.errors.*;
import io.minio.messages.Part;
import io.minio.org.apache.commons.validator.routines.InetAddressValidator;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * 支持断点上传
 * @author ccy
 * @date 2021/8/25 0:31
 */
public class CustomerMinioClient extends MinioClient {

	public CustomerMinioClient(MinioClient client) {
		super(client);
	}

	public String initMultiPartUpload(String bucket,
									  String region,
									  String objectName,
									  Multimap<String, String> headers,
									  Multimap<String, String> extraQueryParams)
			throws IOException,NoSuchAlgorithmException, InsufficientDataException, ServerException, XmlParserException,
			InvalidResponseException, ErrorResponseException, InternalException, InvalidKeyException {
		CreateMultipartUploadResponse response = this.createMultipartUpload(bucket, region, objectName, headers, extraQueryParams);
		return response.result().uploadId();
	}

	@Override
	public ObjectWriteResponse completeMultipartUpload(String bucketName,
													   String region,
													   String objectName,
													   String uploadId,
													   Part[] parts,
													   Multimap<String, String> extraHeaders,
													   Multimap<String, String> extraQueryParams)
			throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, ServerException,
			InternalException, XmlParserException, InvalidResponseException, ErrorResponseException {

		return super.completeMultipartUpload(bucketName,
				region, objectName, uploadId, parts, extraHeaders, extraQueryParams);
	}

	@Override
	public ListPartsResponse listParts(String bucketName, String region, String objectName, Integer maxParts,
									   Integer partNumberMarker, String uploadId,
									   Multimap<String, String> extraHeaders,
									   Multimap<String, String> extraQueryParams)
			throws NoSuchAlgorithmException, InsufficientDataException, IOException, InvalidKeyException,
			ServerException, XmlParserException, ErrorResponseException, InternalException, InvalidResponseException {
		return super.listParts(bucketName, region, objectName, maxParts, partNumberMarker, uploadId,
				extraHeaders, extraQueryParams);
	}
}
