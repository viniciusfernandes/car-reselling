package br.com.revendacarros.infrastructure.storage;

import br.com.revendacarros.domain.model.TipoDocumento;
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

class LocalDocumentoStorageTest {

    @TempDir
    Path tempDir;

    private LocalDocumentoStorage storage;

    private static final UUID VEICULO_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID DOCUMENTO_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final TipoDocumento TIPO_DOCUMENTO = TipoDocumento.NOTA_FISCAL;

    @BeforeEach
    void setUp() {
        storage = new LocalDocumentoStorage(tempDir.toString());
    }

    private InputStream streamOf(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void testReturnsRelativePathWithExpectedFormat() {
        String key = storage.store(VEICULO_ID, TIPO_DOCUMENTO, DOCUMENTO_ID, "contract.pdf", streamOf("data"));

        String expected = Path.of(VEICULO_ID.toString(), TIPO_DOCUMENTO.name(), DOCUMENTO_ID + "_contract.pdf").toString();
        assertThat(key).isEqualTo(expected);
    }

    @Test
    void testWritesFileContentToDisk() throws IOException {
        String content = "binary content";

        String key = storage.store(VEICULO_ID, TIPO_DOCUMENTO, DOCUMENTO_ID, "nota_fiscal.pdf", streamOf(content));

        Path storedFile = tempDir.resolve(key);
        assertThat(storedFile).exists();
        assertThat(Files.readString(storedFile, StandardCharsets.UTF_8)).isEqualTo(content);
    }

    @Test
    void testCreatesIntermediateDirectories() {
        storage.store(VEICULO_ID, TIPO_DOCUMENTO, DOCUMENTO_ID, "file.pdf", streamOf("data"));

        Path expectedDir = tempDir.resolve(VEICULO_ID.toString()).resolve(TIPO_DOCUMENTO.name());
        assertThat(expectedDir).isDirectory();
    }

    // -------------------------------------------------------------------------
    // Sanitizacao do nome do arquivo
    // -------------------------------------------------------------------------

    @Test
    void testSanitizesBackslashInNomeOriginalArquivo() {
        String key = storage.store(VEICULO_ID, TIPO_DOCUMENTO, DOCUMENTO_ID, "dir\\file.pdf", streamOf("data"));

        String expectedKey = Path.of(VEICULO_ID.toString(), TIPO_DOCUMENTO.name(), DOCUMENTO_ID + "_dir_file.pdf").toString();
        assertThat(key).isEqualTo(expectedKey);
    }

    @Test
    void testSanitizesForwardSlashInNomeOriginalArquivo() {
        String key = storage.store(VEICULO_ID, TIPO_DOCUMENTO, DOCUMENTO_ID, "dir/file.pdf", streamOf("data"));

        String expectedKey = Path.of(VEICULO_ID.toString(), TIPO_DOCUMENTO.name(), DOCUMENTO_ID + "_dir_file.pdf").toString();
        assertThat(key).isEqualTo(expectedKey);
    }

    @Test
    void testSanitizesBothPathSeparatorsInNomeOriginalArquivo() {
        String key = storage.store(VEICULO_ID, TIPO_DOCUMENTO, DOCUMENTO_ID, "a\\b/c.pdf", streamOf("data"));

        String expectedKey = Path.of(VEICULO_ID.toString(), TIPO_DOCUMENTO.name(), DOCUMENTO_ID + "_a_b_c.pdf").toString();
        assertThat(key).isEqualTo(expectedKey);
    }

    @Test
    void testNomeArquivoSanitizadoEGravadoNoDisco() throws IOException {
        String key = storage.store(VEICULO_ID, TIPO_DOCUMENTO, DOCUMENTO_ID, "sub\\doc.pdf", streamOf("content"));

        Path storedFile = tempDir.resolve(key);
        assertThat(storedFile).exists();
        assertThat(storedFile.getFileName().toString()).isEqualTo(DOCUMENTO_ID + "_sub_doc.pdf");
    }

    // -------------------------------------------------------------------------
    // Todos os tipos de documento
    // -------------------------------------------------------------------------

    @ParameterizedTest(name = "testStoresFileForTipoDocumento [{0}]")
    @EnumSource(TipoDocumento.class)
    void testStoresFileForEachTipoDocumento(TipoDocumento tipoDocumento) throws IOException {
        String content = "content for " + tipoDocumento.name();

        String key = storage.store(VEICULO_ID, tipoDocumento, DOCUMENTO_ID, "doc.pdf", streamOf(content));

        Path expectedDir = tempDir.resolve(VEICULO_ID.toString()).resolve(tipoDocumento.name());
        Path storedFile = tempDir.resolve(key);

        assertThat(expectedDir).isDirectory();
        assertThat(storedFile).exists();
        assertThat(key).isEqualTo(
            Path.of(VEICULO_ID.toString(), tipoDocumento.name(), DOCUMENTO_ID + "_doc.pdf").toString()
        );
        assertThat(Files.readString(storedFile, StandardCharsets.UTF_8)).isEqualTo(content);
    }

    @ParameterizedTest(name = "testKeyContainsTipoDocumentoNome [{0}]")
    @EnumSource(TipoDocumento.class)
    void testKeyContainsTipoDocumentoNome(TipoDocumento tipoDocumento) {
        String key = storage.store(VEICULO_ID, tipoDocumento, DOCUMENTO_ID, "file.pdf", streamOf("data"));

        assertThat(key).contains(tipoDocumento.name());
    }

    @ParameterizedTest(name = "testCreatesDirectoryNamedAfterTipoDocumento [{0}]")
    @EnumSource(TipoDocumento.class)
    void testCreatesDirectoryNamedAfterTipoDocumento(TipoDocumento tipoDocumento) {
        storage.store(VEICULO_ID, tipoDocumento, DOCUMENTO_ID, "file.pdf", streamOf("data"));

        Path typeDir = tempDir.resolve(VEICULO_ID.toString()).resolve(tipoDocumento.name());
        assertThat(typeDir).isDirectory();
    }

    @ParameterizedTest(name = "testTiposDocumentoDiferentesSaoArmazenadosEmDiretoriosSeparados [{0}]")
    @EnumSource(TipoDocumento.class)
    void testTiposDocumentoDiferentesSaoArmazenadosEmDiretoriosSeparados(TipoDocumento tipoDocumento) {
        UUID docId = UUID.randomUUID();

        String key = storage.store(VEICULO_ID, tipoDocumento, docId, "file.pdf", streamOf("data"));

        assertThat(key).startsWith(Path.of(VEICULO_ID.toString(), tipoDocumento.name()) + java.io.File.separator);
    }

    // -------------------------------------------------------------------------
    // Overwrite existing file
    // -------------------------------------------------------------------------

    @Test
    void testReplacesExistingFileWhenStoredTwiceWithSameKey() throws IOException {
        storage.store(VEICULO_ID, TIPO_DOCUMENTO, DOCUMENTO_ID, "doc.pdf", streamOf("first version"));
        String key = storage.store(VEICULO_ID, TIPO_DOCUMENTO, DOCUMENTO_ID, "doc.pdf", streamOf("second version"));

        Path storedFile = tempDir.resolve(key);
        assertThat(Files.readString(storedFile, StandardCharsets.UTF_8)).isEqualTo("second version");
    }

    // -------------------------------------------------------------------------
    // Failure scenarios
    // -------------------------------------------------------------------------

    @Test
    void testThrowsDocumentoStorageExceptionWhenCreateDirectoriesFails() {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class, Mockito.CALLS_REAL_METHODS)) {
            mockedFiles.when(() -> Files.createDirectories(any(Path.class)))
                    .thenThrow(new IOException("disk full"));

            assertThatThrownBy(() ->
                    storage.store(VEICULO_ID, TIPO_DOCUMENTO, DOCUMENTO_ID, "file.pdf", streamOf("data")))
                    .isInstanceOf(DocumentoStorageException.class)
                    .hasMessageContaining(VEICULO_ID.toString())
                    .hasMessageContaining(TIPO_DOCUMENTO.name())
                    .hasMessageContaining(DOCUMENTO_ID.toString())
                    .hasMessageContaining("file.pdf")
                    .hasCauseInstanceOf(IOException.class);
        }
    }

    @Test
    void testThrowsDocumentoStorageExceptionWhenCopyFails() {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class, Mockito.CALLS_REAL_METHODS)) {
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), eq(StandardCopyOption.REPLACE_EXISTING)))
                    .thenThrow(new IOException("write error"));

            assertThatThrownBy(() ->
                    storage.store(VEICULO_ID, TIPO_DOCUMENTO, DOCUMENTO_ID, "file.pdf", streamOf("data")))
                    .isInstanceOf(DocumentoStorageException.class)
                    .hasMessageContaining(VEICULO_ID.toString())
                    .hasMessageContaining(TIPO_DOCUMENTO.name())
                    .hasMessageContaining(DOCUMENTO_ID.toString())
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
                    storage.store(VEICULO_ID, TIPO_DOCUMENTO, DOCUMENTO_ID, "file.pdf", streamOf("data")))
                    .isInstanceOf(DocumentoStorageException.class)
                    .hasMessageContaining(tempDir.toString());
        }
    }
}
