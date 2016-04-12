package au.gov.ga.hydroid.service;

/**
 * Generic interface for S3 objects, this is so other sources like file system can be used.
 */
public interface DataObjectSummary {
   String getKey();
   String getBucketName();
}
