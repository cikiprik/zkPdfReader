/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.zkoss.util.media.AMedia;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.Textbox;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class pdfRead extends GenericForwardComposer {

    Iframe report;
    String idPath;
    Textbox txtUrl;
    Button btnGo;
    
    // enkripsi
    private static final String UNICODE_FORMAT = "UTF8";
    public static final String DES_ENCRYPTION_SCHEME = "DES";
    private KeySpec myKeySpec;
    private SecretKeyFactory mySecretKeyFactory;
    private Cipher cipher;
    byte[] keyAsBytes;
    private String myEncryptionKey;
    private String myEncryptionScheme;
    SecretKey key;
    // enkripsi
    
    public pdfRead() throws Exception
    {
        myEncryptionKey = "ThisIsSecretEncryptionKey";
        myEncryptionScheme = DES_ENCRYPTION_SCHEME;
        keyAsBytes = myEncryptionKey.getBytes(UNICODE_FORMAT);
        myKeySpec = new DESKeySpec(keyAsBytes);
        mySecretKeyFactory = SecretKeyFactory.getInstance(myEncryptionScheme);
        cipher = Cipher.getInstance(myEncryptionScheme);
        key = mySecretKeyFactory.generateSecret(myKeySpec);
    }
    
    public String encrypt(String unencryptedString) {
        String encryptedString = null;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] plainText = unencryptedString.getBytes(UNICODE_FORMAT);
            byte[] encryptedText = cipher.doFinal(plainText);
            BASE64Encoder base64encoder = new BASE64Encoder();
            encryptedString = base64encoder.encode(encryptedText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedString;
    }
    /**
     * Method To Decrypt An Ecrypted String
     */
    public String decrypt(String encryptedString) {
        String decryptedText=null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            BASE64Decoder base64decoder = new BASE64Decoder();
            byte[] encryptedText = base64decoder.decodeBuffer(encryptedString);
            byte[] plainText = cipher.doFinal(encryptedText);
            decryptedText= bytes2String(plainText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedText;
    }
    /**
     * Returns String From An Array Of Bytes
     */
    private static String bytes2String(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i  < bytes.length; i++) {
            stringBuffer.append((char) bytes[i]);
        }

        return stringBuffer.toString();
    }
    

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
   
        
    }
    
    public void onClick$btnGo() throws Exception{
        try {
            readPdfFromUrl(txtUrl.getValue());
        } catch (MalformedURLException ex) {
            Logger.getLogger(pdfRead.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void readPdfFromFile(String path) {
        try {
            FileInputStream in = new FileInputStream(path);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buf = new byte[5 * 1024];
//            byte[] buf = new byte[10*1024];
            int bytesRead;
            while ((bytesRead = in.read(buf, 0, buf.length)) >= 0) { // Read in next chunk of pdf 
                os.write(buf, 0, bytesRead); // write it out 
            }
            AMedia am = new AMedia("reportpdf", "pdf", "application/pdf", os.toByteArray());
            report.setContent(am);
        } catch (Exception e) {
            System.out.println("err" + e);
        }

    }

    public void readPdfFromUrl(String url) throws MalformedURLException, Exception {
        URL urlnya = new URL(url);
        String namafile;
        Date d = new Date();
        // kode 
        String dataAkun = "Surat Keterangan Domisili Usaha dari Kelurahan dan Kecamatan yang masih berlaku atau Fotocopy dilegalisir Kecamatan";
        pdfRead myEncryptor= new pdfRead();
        String dataAkunEnkripsi=myEncryptor.encrypt(dataAkun);
        String dataAkunDekripsi=myEncryptor.decrypt("AYxJhAMLk4fftR5XcqYDoUO4GaMY7sNX19k5SRxWWedGj6RB3tSUUmtdx1KHYf19EE1rLonm/3XikRnNoG/Q0rXkbiXO3QXHnmn2douw6SSwrirgYGoHmR3U4wmGUCxXqAERQFd5rdCZcK0CII7sPsb2uiTYqA97");
 
        System.out.println("Text Asli: "+dataAkun);
        System.out.println("Text Terenkripsi :" + dataAkunEnkripsi);
        System.out.println("Text Terdekripsi :"+dataAkunDekripsi);
        
        // kode 
        byte[] ba1 = new byte[5 * 1024];
//        byte[] ba1 = new byte[10*1024];
        int baLength;
        InputStream is1 = null;
        ByteArrayOutputStream bios = new ByteArrayOutputStream();
        ByteArrayOutputStream biosPlusWatermark = new ByteArrayOutputStream();
        ByteArrayOutputStream biosPlusWatermarkQr = new ByteArrayOutputStream();
        AMedia amedia = null;
        try {

//        report.setSrc(url);
//            ssl disable
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            URLConnection urlConn = urlnya.openConnection();
            try {

                // Read the PDF from the URL and save to a local file
                is1 = urlnya.openStream();
                while ((baLength = is1.read(ba1)) != -1) {
                    bios.write(ba1, 0, baLength);
                }
            } catch (Exception e) {
            } finally {

                is1.close();
                bios.close();
            }

            // add watermark
            try {
                PdfReader reader = new PdfReader(bios.toByteArray());
                int n = reader.getNumberOfPages();
                // create a stamper that will copy the document to a new file
                try {
                    // Read the PDF from the URL and save to a local file
                    is1 = urlnya.openStream();
                    while ((baLength = is1.read(ba1)) != -1) {
                        biosPlusWatermark.write(ba1, 0, baLength);
                    }
                } catch (Exception e) {
                } finally {

                    is1.close();
                    biosPlusWatermark.close();
                }

                PdfStamper stamp = new PdfStamper(reader, biosPlusWatermark, '\0', true);
                int i = 0;
                PdfContentByte under;
                
                Image img = Image.getInstance("img/watermark.png");
                img.setTransparency(new int[]{0x00, 0x10});
                img.setAbsolutePosition(0, 0);
                while (i < n) {
                    i++;
                    under = stamp.getOverContent(i);
                    under.addImage(img);
                }
                stamp.close();
            } catch (Exception e) {
                System.out.println("err watermark:" + e);
                amedia = new AMedia("Dokumen", "pdf", "application/pdf", bios.toByteArray());
            }
            // add QRcode
            try {
                PdfReader reader = new PdfReader(biosPlusWatermark.toByteArray());

                int n = reader.getNumberOfPages();
                BarcodeQRCode qrcode = new BarcodeQRCode(dataAkunEnkripsi, 50, 50, null);
                Image image = qrcode.getImage();
                Image mask = qrcode.getImage();
                mask.makeMask();
                image.setImageMask(mask);
                // create a stamper that will copy the document to a new file
                try {
                    // Read the PDF from the URL and save to a local file
                    is1 = urlnya.openStream();
                    while ((baLength = is1.read(ba1)) != -1) {
                        biosPlusWatermarkQr.write(ba1, 0, baLength);
                    }
                } catch (Exception e) {
                } finally {
                    is1.close();
                    biosPlusWatermarkQr.close();
                }

                PdfStamper stamp = new PdfStamper(reader, biosPlusWatermarkQr, '\0', false);
                int i = 0;
                PdfContentByte under;
                Image img = Image.getInstance(image);
                img.setTransparency(new int[]{0x00, 0x10});
                img.setAbsolutePosition(0, 0);
                while (i < n) {
                    i++;
                    under = stamp.getOverContent(i);
                    under.addImage(img);
                }
                stamp.close();
                amedia = new AMedia("Dokumen", "pdf", "application/pdf", biosPlusWatermarkQr.toByteArray());
            } catch (Exception e) {
                System.out.println("err qrcode:" + e);
                amedia = new AMedia("Dokumen", "pdf", "application/pdf", bios.toByteArray());
            }

            report.setContent(amedia);

        } catch (Exception e) {
            System.out.println("url tidak dapat diakses");
        }

    }

}
