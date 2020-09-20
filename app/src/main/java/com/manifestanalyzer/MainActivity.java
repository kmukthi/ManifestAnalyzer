package com.manifestanalyzer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MainActivity extends AppCompatActivity {

    private List<ApplicationInfo> apps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.init();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void init() {
        PackageManager pm = getBaseContext().getPackageManager();
        this.apps = pm.getInstalledApplications(
                PackageManager.GET_META_DATA | PackageManager.GET_SHARED_LIBRARY_FILES
        );
    }

    public void parseManifestXmlForYoutube(View view) {
        TextView displayContent = (TextView) findViewById(R.id.outputTextView);
        try {
            String path = this.apps.stream()
                    .filter(a -> a.sourceDir.toLowerCase().contains("youtube"))
                    .findFirst()
                    .map(a -> a.publicSourceDir)
                    .orElseThrow(() -> new IllegalArgumentException("Youtube does not exist"));
            byte[] xml = this.getCompressedXmlFromApk(path);
            String outPut = new ManiFestXmlParser().decompressXML(xml);
            displayContent.setText(outPut);
        } catch (IllegalArgumentException e) {
            displayContent.setText("Could not find the app");
        } catch (IOException e) {
            displayContent.setText("Error while reading compressed file");
        }

    }

    public void clearOutput(View view) {
        TextView displayContent = (TextView) findViewById(R.id.outputTextView);
        displayContent.setText("");
    }

    private byte[] getCompressedXmlFromApk(String path) throws IOException{
        byte[] xml = null;
        try {
            ZipFile apk = new ZipFile(path);
            ZipEntry manifest = apk.getEntry("AndroidManifest.xml");
            if (manifest != null){
                Log.d("ManifestGetter", "Manifest size = " + manifest.getSize());
                InputStream stream = apk.getInputStream(manifest);
                xml = new byte[stream.available()];
                stream.read(xml);
                stream.close();
            }
            apk.close();
        } catch (IOException e) {
            // Logging can be done here
            e.printStackTrace();
            throw e;
        }
        return xml;
    }
}