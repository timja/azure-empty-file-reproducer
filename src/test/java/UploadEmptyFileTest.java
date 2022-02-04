import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import com.azure.storage.file.share.models.ShareFileUploadOptions;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class UploadEmptyFileTest {

    private static final String SHARE_NAME = "testing";

    private static final String STORAGE_ACCOUNT_NAME = System.getenv("STORAGE_ACCOUNT_NAME");
    private static final String STORAGE_ACCOUNT_KEY = System.getenv("STORAGE_ACCOUNT_KEY");
    private static final String FILE_SHARE_ENDPOINT;

    static {
        FILE_SHARE_ENDPOINT = "https://" + STORAGE_ACCOUNT_NAME + ".file.core.windows.net";
    }

    @Test
    void uploadEmptyFile() throws IOException {
        Path tempFile = Files.createTempFile("azure-file-share", ".txt");

        uploadFile(getShareFileClient(tempFile.getFileName().toString()), tempFile.toFile());
    }

    public static ShareServiceClient getShareClient() {
        return new ShareServiceClientBuilder()
                .credential(new StorageSharedKeyCredential(STORAGE_ACCOUNT_NAME, STORAGE_ACCOUNT_KEY))
                .endpoint(FILE_SHARE_ENDPOINT)
                .buildClient();
    }

    private ShareFileClient getShareFileClient(String fileName) {
        final ShareServiceClient shareServiceClient = getShareClient();
        final ShareClient fileShare = shareServiceClient.getShareClient(SHARE_NAME);
        if (!fileShare.exists()) {
            fileShare.create();
        }

        ShareDirectoryClient rootDirectoryClient = fileShare.getRootDirectoryClient();
        return rootDirectoryClient.getFileClient(fileName);
    }

    private void uploadFile(ShareFileClient fileClient, File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            long bytes = Files.size(file.toPath());
            fileClient.create(bytes);

            ShareFileUploadOptions fileUploadOptions = new ShareFileUploadOptions(bis);
            fileClient
                    .uploadWithResponse(fileUploadOptions, null, null);
        }
    }
}
