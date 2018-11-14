## About Multi Protocol File downloader
Program to download the files given at the URLs. It supports **file, ftp, http, https, jar, mailto, netdoc** protocols out of the box as these are by default registered with jvm. The code could be extended to custom protocols, and one example **(rot13)** has been provided.

1. The program uses Spring Retry to apply retry logic in case of Connection Timeout and IOException.
2. The program takes advantage of Java's Completable Future to download all the input urls in parallel.


The program employs the java NIO package for downloading the files. According to [https://docs.oracle.com/javase/8/docs/api/java/io/package-summary.html]
>The Java NIO package offers the possibility to transfer bytes between 2 Channels without buffering them into
           the application memory.
The transferTo() and transferFrom() methods are more efficient than simply reading from a stream using a
           buffer. Depending on the underlying operating system, the data can be transferred directly from the filesystem
           cache to our file without copying any bytes into the application memory.
      On Linux and UNIX systems, these methods use the zero-copy technique that reduces the number of context
           switches between the kernel mode and user mode.


> We could have also used Apache Commons IO's  IOUtils.copyLarge() method
### Using bash script for easy usage

  1. Pull the repository.
  2. Run the following command from the directory you pulled code to:
  
     ```
     ./multifiledownload.sh --urls="<list of url separated by WHITESPACE>" --directory="<outputDirectory>" 
     ```
     Example:
     ```
     ./multifiledownload.sh --urls="http://www.africau.edu/images/default/sample.pdf rot13://localhost:8000  http://insight.dev.schoolwires.com/HelpAssets/C2Assets/C2Files/C2ImportFamRelSample.csv" --directory="downloaded"
     ```
     
     File is created at \<base directory\> / \<smart file name\>. Where:
     
     * \<base directory\> is provided in **_application.properties_** file by default usind property **download.base.dir**, though it can be overriden through --directory program argument.
     * \<smart file name\> is generated such that two different URLs always end up in different files even if 
     downloaded in the same \<base directory\>.
     
     It is generated as per the following format: 
        ```
        /<sha1hex>_<filename>
        ```
        
     &nbsp;&nbsp;&nbsp;&nbsp; - \<sha1hex\> is the unique SHA-1 hash constituted from the URL
            
     &nbsp;&nbsp;&nbsp;&nbsp; - \<filename\> is the filename provided in the URL path
                 
      

### Making modifications to the code

You can modifications to any modifications to the code. Just don't forget to run the following script to update the jar.

```
sh update-jar.sh
```

Feel free to request to push the code to this repository if you want to add a feature or improve existing 
modules. Test your changes through unit and integration tests wherever appropriate. 

### Using Spring Retry

```

 public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        retryTemplate.registerListener(new RetryDownloadListener());

        ExponentialBackOffPolicy exponentialBackOffPolicy = new ExponentialBackOffPolicy();
        exponentialBackOffPolicy.setMultiplier(2.0D);
        exponentialBackOffPolicy.setInitialInterval(100L);
        exponentialBackOffPolicy.setMaxInterval(30000L);
        retryTemplate.setBackOffPolicy(exponentialBackOffPolicy);

        Map<Class<? extends Throwable>, Boolean> exceptionClassifier = new HashMap<>();
        exceptionClassifier.put(IOException.class, true);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(maxRetryAttempts, exceptionClassifier);

        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
    
retryTemplate.execute(
                    arg -> downloadAndSave(source, outputDirectory),
                    arg -> {

                        fileDownloaderUtils.deleteFileQuietly(source, outputDirectory);
                        log.error(
                                "Retry attempts exhausted to " +
                                        "download from sources: {}." +
                                        "Hence deleting the temp/partially downloaded file and returning as ERROR",
                                source);

                        return new FileDownloadResult()
                                .setDownloadStatusStatus(DownloadStatus
                                                                 .ERROR)
                                .setMessage("RetryAttemptsExhausted");

                    });
```

### Asynchronous processing using Completable Future
```
threadExecutor = new ThreadPoolExecutor(poolSize,
                                                poolSize, 1000, TimeUnit
                                                        .MILLISECONDS, new
                                                        LinkedBlockingQueue<>(100),
                                                new ThreadPoolExecutor
                                                        .CallerRunsPolicy());
private CompletableFuture<FileDownloadResult> downloadAndSaveTask(String source, String saveDirectory) {

        return CompletableFuture.supplyAsync(
                () -> downloadManager.downloadFromSource(source, saveDirectory),
                threadExecutor);

    }
    
 CompletableFuture.allOf(
                validSources
                        .stream().map(source -> downloadAndSaveTask(source, outputDirectory)).toArray
                        (CompletableFuture[]::new)
        )
                .thenAccept(result -> {
                    log.info("Download of all the sources have been completed/terminated. Program would exit now");
                    threadExecutor.shutdownNow();
                });    
```
### Extending to other protocols

1. Create a custom URLConnection implementation which performs the job in connect() method.
    ```
    public class CustomURLConnection extends URLConnection {
    
        protected CustomURLConnection(URL url) {
            super(url);
        }
    
        @Override
        public void connect() throws IOException {
            // Do your job here. As of now it merely prints "Connected!".
            System.out.println("Connected!");
        }
    
    }
    ```

2. Don't forget to override and implement other methods like getInputStream() accordingly.

    Create a custom URLStreamHandler implementation which returns it in openConnection().
    
    ```
    public class CustomURLStreamHandler extends URLStreamHandler {
    
        @Override
        protected URLConnection openConnection(URL url) throws IOException {
            return new CustomURLConnection(url);
        }
    
    }
    ```

    Don't forget to override and implement other methods if necessary.

3. Create a custom URLStreamHandlerFactory which creates and returns it based on the protocol.

    ```
    public class CustomURLStreamHandlerFactory implements URLStreamHandlerFactory {
    
        @Override
        public URLStreamHandler createURLStreamHandler(String protocol) {
            if ("customuri".equals(protocol)) {
                return new CustomURLStreamHandler();
            }
    
            return null;
        }
    
    }
    ```

    Note that protocols are always lowercase.

4. Register it during application's startup via URL#setURLStreamHandlerFactory()

    ```
    URL.setURLStreamHandlerFactory(new CustomURLStreamHandlerFactory());
    ```
    
    Note that the Javadoc explicitly says that you can set it at most once. So if you intend to support multiple custom protocols in the same application, you'd need to generify the custom URLStreamHandlerFactory implementation to cover them all inside the createURLStreamHandler() method.
    
5.  Don't forget to add a corresponding test-case in FileDownloaderTests just to make sure that new handler integrates 
      well with java.net.URL.

### Example to other protocol [rot13]

This section shows how we can extend the code to download files from other custom protocols like rot13.

**Example URL : rot13://localhost:8000**

1. Create the custom Input Stream to enncode the stream as here we do using rot13 system.

    ```
    public class rot13InputStream extends FilterInputStream {
    
        public rot13InputStream(InputStream i) {
            super(i);
        }
    
        public int read() throws IOException {
            return rot13(in.read());
        }
    
        /**
         * Rotates the input int to encode the stream of charaters
         *
         * @param c int to be transformed
         * @return the rotated int by 13 modulo 26
         */
        private int rot13(int c) {
            if ((c >= 'A') && (c <= 'Z')) c = (((c - 'A') + 13) % 26) + 'A';
            if ((c >= 'a') && (c <= 'z'))
                c = (((c - 'a') + 13) % 26) + 'a';
            return c;
        }
    }
    ```
 2. Create Custom URLConnection sub class.
   
    ```
       class CryptURLConnection extends URLConnection {
           static int defaultPort = 80;
           CryptInputStream cis;
       
           CryptURLConnection(URL url, String cryptype) {
               super(url);
               try {
                   String name = "com.abhishekjain.filedownloader.custom." + cryptype
                           + "CryptInputStream";
                   cis = (CryptInputStream) Class.forName(name).newInstance();
               } catch (Exception e) {
               }
           }
       
           synchronized public void connect() throws IOException {
               int port;
               if (cis == null)
                   throw new IOException("Crypt Class Not Found");
               if ((port = url.getPort()) == -1)
                   port = defaultPort;
               Socket s = new Socket(url.getHost(), port);
       
               // Send the filename in plaintext
               OutputStream server = s.getOutputStream();
               PrintWriter pw = new PrintWriter(
                       new OutputStreamWriter(server, "UTF-8"),
                       true);
       
               // Initialize the CryptInputStream
               cis.set(s.getInputStream(), server);
               connected = true;
           }
       
           synchronized public InputStream getInputStream()
                   throws IOException {
               if (!connected)
                   connect();
               return (cis);
           }
       
           public String getContentType() {
               return guessContentTypeFromName(url.getFile());
           }
       }       
       ```
 
 3. Create UrlStreamHandler to handle rot13 stream.
 
     ```
     public class Handler extends URLStreamHandler {
     
         protected void parseURL(URL url, String spec,
                                 int start, int end) {
             int slash = spec.indexOf('/');
             String cryptType = spec.substring(0, slash - 1);
             super.parseURL(url, spec, slash, end);
             setURL(url, cryptType, url.getHost(),
                    url.getPort(), url.getAuthority(), url.getUserInfo(), url.getPath(), url.getQuery(), url.getRef());
         }
     
         protected URLConnection openConnection(URL url)
                 throws IOException {
             String crypType = url.getProtocol();
             return new CryptURLConnection(url, crypType);
         }
     }
     ```  
 
4. Create the URLStreamFactory for the Handler

    ```
    public class OurURLStreamHandlerFactory implements URLStreamHandlerFactory {
        public URLStreamHandler createURLStreamHandler(String protocol) {
            if (protocol.equalsIgnoreCase("rot13"))
                return new Handler();
            else
                return null;
        }
    }
    ```
    
 5. Register the factory at spring boot start-up : **URL.setURLStreamHandlerFactory(new OurURLStreamHandlerFactory());**
    
     ```
     public static void main(String[] args) {
     
             URL.setURLStreamHandlerFactory(new OurURLStreamHandlerFactory());
     
             new SpringApplicationBuilder(FileDownloaderLauncher.class)
                     .web(WebApplicationType.NONE)
                     .run(args);
         }
      ```
      
 6. Now the URL like : rot13://localhost:8000 can be also downloaded from.
 
 7. To create a local socket server for we can use minimal python script :
 
 ```
 # first of all import the socket library 
 import socket
 
 # next create a socket object 
 s = socket.socket()
 print "Socket successfully created"
 
 # reserve a port on your computer in our 
 # case it is 12345 but it can be anything 
 port = 8000
 
 # Next bind to the port 
 # we have not typed any ip in the ip field 
 # instead we have inputted an empty string 
 # this makes the server listen to requests  
 # coming from other computers on the network 
 s.bind(('', port))
 print "socket binded to %s" %(port)
 
 # put the socket into listening mode 
 s.listen(5)
 print "socket is listening"
 
 # a forever loop until we interrupt it or  
 # an error occurs 
 while True:
 
    # Establish connection with client. 
    c, addr = s.accept()
    print 'Got connection from', addr
 
    # send a thank you message to the client.
    #"Why did the chicken cross the road?" ->  "Jul qvq gur puvpxra pebff gur ebnq?" [rot13 applied string] 
    c.send('Jul qvq gur puvpxra pebff gur ebnq?')
 
    # Close the connection with the client 
    c.close()
     
  ``` 
  
###Adding Unit Tests 
Unit Tests are added covering  **(Note: For test cases, active internet connection is needed for a few cases)**
* File Utils [Utility file for file downlaods]
* Download Service Layer [making Sure downloads are done in parallel]
* Download Manager [Along with Retry Logic]
* Exception Cases like MalformedURLException, IOException
* Custom Test Cases could be added to test custom protocols.
