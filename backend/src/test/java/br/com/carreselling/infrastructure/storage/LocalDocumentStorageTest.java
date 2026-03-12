package br.com.carreselling.infrastructure.storage;

import br.com.carreselling.domain.model.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

class LocalDocumentStorageTest {

    @TempDir
    Path tempDir;

    private LocalDocumentStorage storage;

    private static final UUID VEHICLE_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID DOCUMENT_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final DocumentType DOCUMENT_TYPE = DocumentType.INVOICE;

    @BeforeEach
    void setUp() {
        storage = new LocalDocumentStorage(tempDir.toString());
    }

    private InputStream streamOf(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void testReturnsRelativePathWithExpectedFormat() {
        String key = storage.store(VEHICLE_ID, DOCUMENT_TYPE, DOCUMENT_ID, "contract.pdf", streamOf("data"));

        String expected = Path.of(VEHICLE_ID.toString(), DOCUMENT_TYPE.name(), DOCUMENT_ID + "_contract.pdf").toString();
        assertThat(key).isEqualTo(expected);
    }

    @Test
    void testWritesFileContentToDisk() throws IOException {
        String content = "binary content";

        String key = storage.store(VEHICLE_ID, DOCUMENT_TYPE, DOCUMENT_ID, "invoice.pdf", streamOf(content));

        Path storedFile = tempDir.resolve(key);
        assertThat(storedFile).exists();
        assertThat(Files.readString(storedFile, StandardCharsets.UTF_8)).isEqualTo(content);
    }

    @Test
    void testCreatesIntermediateDirectories() {
        storage.store(VEHICLE_ID, DOCUMENT_TYPE, DOCUMENT_ID, "file.pdf", streamOf("data"));

        Path expectedDir = tempDir.resolve(VEHICLE_ID.toString()).resolve(DOCUMENT_TYPE.name());
        assertThat(expectedDir).isDirectory();
    }

    // -------------------------------------------------------------------------
    // File name sanitization
    // -------------------------------------------------------------------------

    @Test
    void testSanitizesBackslashInOriginalFileName() {
        String key = storage.store(VEHICLE_ID, DOCUMENT_TYPE, DOCUMENT_ID, "dir\\file.pdf", streamOf("data"));

        String expectedKey = Path.of(VEHICLE_ID.toString(), DOCUMENT_TYPE.name(), DOCUMENT_ID + "_dir_file.pdf").toString();
        assertThat(key).isEqualTo(expectedKey);
    }

    @Test
    void testSanitizesForwardSlashInOriginalFileName() {
        String key = storage.store(VEHICLE_ID, DOCUMENT_TYPE, DOCUMENT_ID, "dir/file.pdf", streamOf("data"));

        String expectedKey = Path.of(VEHICLE_ID.toString(), DOCUMENT_TYPE.name(), DOCUMENT_ID + "_dir_file.pdf").toString();
        assertThat(key).isEqualTo(expectedKey);
    }

    @Test
    void testSanitizesBothPathSeparatorsInOriginalFileName() {
        String key = storage.store(VEHICLE_ID, DOCUMENT_TYPE, DOCUMENT_ID, "a\\b/c.pdf", streamOf("data"));

        String expectedKey = Path.of(VEHICLE_ID.toString(), DOCUMENT_TYPE.name(), DOCUMENT_ID + "_a_b_c.pdf").toString();
        assertThat(key).isEqualTo(expectedKey);
    }

    @Test
    void testSanitizedFileNameIsActuallyWrittenToDisk() throws IOException {
        String key = storage.store(VEHICLE_ID, DOCUMENT_TYPE, DOCUMENT_ID, "sub\\doc.pdf", streamOf("content"));

        Path storedFile = tempDir.resolve(key);
        assertThat(storedFile).exists();
        assertThat(storedFile.getFileName().toString()).isEqualTo(DOCUMENT_ID + "_sub_doc.pdf");
    }

    // -------------------------------------------------------------------------
    // All document types
    // -------------------------------------------------------------------------

    @ParameterizedTest(name = "testStoresFileForDocumentType [{0}]")
    @EnumSource(DocumentType.class)
    void testStoresFileForEachDocumentType(DocumentType documentType) throws IOException {
        String content = "content for " + documentType.name();

        String key = storage.store(VEHICLE_ID, documentType, DOCUMENT_ID, "doc.pdf", streamOf(content));

        Path expectedDir = tempDir.resolve(VEHICLE_ID.toString()).resolve(documentType.name());
        Path storedFile = tempDir.resolve(key);

        assertThat(expectedDir).isDirectory();
        assertThat(storedFile).exists();
        assertThat(key).isEqualTo(
            Path.of(VEHICLE_ID.toString(), documentType.name(), DOCUMENT_ID + "_doc.pdf").toString()
        );
        assertThat(Files.readString(storedFile, StandardCharsets.UTF_8)).isEqualTo(content);
    }

    @ParameterizedTest(name = "testKeyContainsDocumentTypeName [{0}]")
    @EnumSource(DocumentType.class)
    void testKeyContainsDocumentTypeName(DocumentType documentType) {
        String key = storage.store(VEHICLE_ID, documentType, DOCUMENT_ID, "file.pdf", streamOf("data"));

        assertThat(key).contains(documentType.name());
    }

    @ParameterizedTest(name = "testCreatesDirectoryNamedAfterDocumentType [{0}]")
    @EnumSource(DocumentType.class)
    void testCreatesDirectoryNamedAfterDocumentType(DocumentType documentType) {
        storage.store(VEHICLE_ID, documentType, DOCUMENT_ID, "file.pdf", streamOf("data"));

        Path typeDir = tempDir.resolve(VEHICLE_ID.toString()).resolve(documentType.name());
        assertThat(typeDir).isDirectory();
    }

    @ParameterizedTest(name = "testDifferentDocumentTypesAreStoredInSeparateDirectories [{0}]")
    @EnumSource(DocumentType.class)
    void testDifferentDocumentTypesAreStoredInSeparateDirectories(DocumentType documentType) {
        UUID docId = UUID.randomUUID();

        String key = storage.store(VEHICLE_ID, documentType, docId, "file.pdf", streamOf("data"));

        assertThat(key).startsWith(VEHICLE_ID + "/" + documentType.name() + "/");
    }

    // -------------------------------------------------------------------------
    // Overwrite existing file
    // -------------------------------------------------------------------------

    @Test
    void testReplacesExistingFileWhenStoredTwiceWithSameKey() throws IOException {
        storage.store(VEHICLE_ID, DOCUMENT_TYPE, DOCUMENT_ID, "doc.pdf", streamOf("first version"));
        String key = storage.store(VEHICLE_ID, DOCUMENT_TYPE, DOCUMENT_ID, "doc.pdf", streamOf("second version"));

        Path storedFile = tempDir.resolve(key);
        assertThat(Files.readString(storedFile, StandardCharsets.UTF_8)).isEqualTo("second version");
    }

    // -------------------------------------------------------------------------
    // Failure scenarios
    // -------------------------------------------------------------------------

    @Test
    void testThrowsDocumentStorageExceptionWhenCreateDirectoriesFails() {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class, Mockito.CALLS_REAL_METHODS)) {
            mockedFiles.when(() -> Files.createDirectories(any(Path.class)))
                    .thenThrow(new IOException("disk full"));

            assertThatThrownBy(() ->
                    storage.store(VEHICLE_ID, DOCUMENT_TYPE, DOCUMENT_ID, "file.pdf", streamOf("data")))
                    .isInstanceOf(DocumentStorageException.class)
                    .hasMessageContaining(VEHICLE_ID.toString())
                    .hasMessageContaining(DOCUMENT_TYPE.name())
                    .hasMessageContaining(DOCUMENT_ID.toString())
                    .hasMessageContaining("file.pdf")
                    .hasCauseInstanceOf(IOException.class);
        }
    }

    @Test
    void testThrowsDocumentStorageExceptionWhenCopyFails() {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class, Mockito.CALLS_REAL_METHODS)) {
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), eq(StandardCopyOption.REPLACE_EXISTING)))
                    .thenThrow(new IOException("write error"));

            assertThatThrownBy(() ->
                    storage.store(VEHICLE_ID, DOCUMENT_TYPE, DOCUMENT_ID, "file.pdf", streamOf("data")))
                    .isInstanceOf(DocumentStorageException.class)
                    .hasMessageContaining(VEHICLE_ID.toString())
                    .hasMessageContaining(DOCUMENT_TYPE.name())
                    .hasMessageContaining(DOCUMENT_ID.toString())
                    .hasMessageContaining("file.pdf")
                    .hasCauseInstanceOf(IOException.class);
        }
    }

    @Test
    void testExceptionMessageContainsResolvedTargetPath() {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class, Mockito.CALLS_REAL_METHODS)) {
            mockedFiles.when(() -> Files.createDirectories(any(Path.class)))
                    .thenThrow(new IOException("permission denied"));

            assertThatThrownBy(() ->
                    storage.store(VEHICLE_ID, DOCUMENT_TYPE, DOCUMENT_ID, "file.pdf", streamOf("data")))
                    .isInstanceOf(DocumentStorageException.class)
                    .hasMessageContaining(tempDir.toString());
        }
    }
}
