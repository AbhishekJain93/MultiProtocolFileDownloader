package com.abhishekjain.filedownloader.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

@RunWith(MockitoJUnitRunner.class)
public class FileDownloaderUtilsTest {

    @Spy
    @InjectMocks
    private FileDownloaderUtils downloaderUtils = new FileDownloaderUtils();

    @Before
    public void before() throws Exception {
        FieldUtils.writeField(downloaderUtils, "connectTimeout", 1000, true);
        FieldUtils.writeField(downloaderUtils, "readTimeout", 1000, true);
    }

    @Test
    public void isValidUrl_valid_test() {

        Assert.assertTrue(downloaderUtils.isValidUrl("http://www.africau.edu/images/default/sample.pdf"));

    }

    @Test
    public void isValidUrl_local_url_test() {

        Assert.assertTrue(downloaderUtils.isValidUrl("http://localhost:8000/sample.pdf"));

    }

    @Test
    public void isValidUrl_no_protocol_url_test() {

        Assert.assertFalse(downloaderUtils.isValidUrl("www.africau.edu/images/default/sample.pdf"));

    }

    @Test
    public void isValidUrl_invalid_url_test() {

        Assert.assertFalse(downloaderUtils.isValidUrl("crypt:rot13://xyz.invalid:80/sample.pdf"));

    }

    @Test
    public void uniqueFileSaveLocation_valid_url_test() throws MalformedURLException {

        String saveLocation = downloaderUtils.uniqueFileSaveLocation(new URL("http://www.africau" +
                                                                                     ".edu/images/default/sample.pdf")
                , "download");

        Assert.assertEquals(
                "download/" + DigestUtils.sha1Hex("http-80-www.africau.edu-/images/default/sample.pdf") + "_sample.pdf",
                saveLocation);

    }

    @Test
    public void uniqueFileSaveLocation_without_directory_test() throws MalformedURLException {

        String saveLocation = downloaderUtils.uniqueFileSaveLocation(new URL("http://www.africau" +
                                                                                     ".edu/images/default/sample.pdf")
                , "");

        Assert.assertEquals(
                "/" + DigestUtils.sha1Hex("http-80-www.africau.edu-/images/default/sample.pdf") + "_sample.pdf",
                saveLocation);

    }

    @Test
    public void uniqueFileSaveLocation_without_path_test() throws MalformedURLException {

        String saveLocation = downloaderUtils.uniqueFileSaveLocation(new URL("http://www.africau" +
                                                                                     ".edu")
                , "download");

        Assert.assertEquals(
                "download/" + DigestUtils.sha1Hex("http-80-www.africau.edu") + "_",
                saveLocation);

    }

    @Test
    public void openStream_valid_test() throws IOException {

        InputStream actualIs = downloaderUtils.openStream(new URL(("http://www.africau.edu/images/default/sample" +
                ".pdf")));

        Assert.assertNotNull(actualIs);

    }

    @Test(expected = ConnectException.class)
    public void openStream_invalid_test() throws IOException {

        downloaderUtils.openStream(new URL(("http://localhost:8000/sample.pdf")));

    }

    @Test
    public void deleteFileQuietly_valid_test() throws IOException {

        String saveLocation = downloaderUtils.uniqueFileSaveLocation(new URL("http://www.africau" +
                                                                                     ".edu/images/default/sample.pdf")
                , "download");

        FileUtils.touch(new File(saveLocation));

        downloaderUtils.deleteFileQuietly("http://www.africau" +
                                                  ".edu/images/default/sample.pdf", "download");

        Assert.assertFalse(Files.exists(Paths.get(saveLocation)));

    }

    @Test
    public void deleteFileQuietly_file_not_found_test() throws IOException {

        String saveLocation = downloaderUtils.uniqueFileSaveLocation(new URL("http://www.africau" +
                                                                                     ".edu/images/default/sample.pdf")
                , "download");

        downloaderUtils.deleteFileQuietly("http://www.africau" +
                                                  ".edu/images/default/sample.pdf", "download");

        Assert.assertFalse(Files.exists(Paths.get(saveLocation)));

    }

    @Test(expected = MalformedURLException.class)
    public void deleteFileQuietly_invalid_destination_test() throws IOException {

        String saveLocation = downloaderUtils.uniqueFileSaveLocation(new URL("www.africau" +
                                                                                     ".edu/images/default/sample.pdf")
                , "download");

        downloaderUtils.deleteFileQuietly("http://www.africau" +
                                                  ".edu/images/default/sample.pdf", "download");

    }
}
