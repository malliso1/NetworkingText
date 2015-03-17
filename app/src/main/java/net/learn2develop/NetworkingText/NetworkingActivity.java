package net.learn2develop.NetworkingText;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import net.learn2develop.Networking.R;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class NetworkingActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // ---access a Web Service using GET---
        new AccessWebServiceTask().execute("apple");
    }

    private InputStream OpenHttpConnection(String urlString)
            throws IOException {
        InputStream in = null;
        int response = -1;

        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");
        try {
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            response = httpConn.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        } catch (Exception ex) {
            Log.d("Networking", ex.getLocalizedMessage());
            throw new IOException("Error connecting");
        }
        return in;
    }

    private String WordDefinition(String word) {
        InputStream in = null;
        String strDefinition = "";
        try {
            in = OpenHttpConnection("http://services.aonaware.com/DictService/DictService.asmx/Define?word=" + word);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(in, null);

            int eventType = parser.getEventType();
            String textVal = "";

            while (eventType != XmlPullParser.END_DOCUMENT) {
                System.out.println("End document is " + XmlPullParser.END_DOCUMENT);
                if (eventType == XmlPullParser.TEXT) {
                    textVal = parser.getText() + "\n";
                }
                if(eventType == XmlPullParser.END_TAG) {
                    if ((parser.getName().equalsIgnoreCase("Name")) || (parser.getName().equalsIgnoreCase("WordDefinition"))) {
                        strDefinition = strDefinition + textVal + "\n";
                    }
                }
                eventType = parser.next();
                System.out.println("Current is " + eventType);
            }
            in.close();
        } catch (IOException e1) {
            Log.d("NetworkingActivity", e1.getLocalizedMessage());
        } catch (Exception e) {
            Log.d("Unknown exception.", e.getLocalizedMessage());
        }

        // ---return the definitions of the word---
        return strDefinition;
    }

    private class AccessWebServiceTask extends
            AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            return WordDefinition(urls[0]);
        }

        protected void onPostExecute(String result) {
            TextView tv = (TextView) findViewById(R.id.textView2);
            tv.setText(result);
        }
    }

    public void defineClick(View view) {
        EditText et = (EditText) findViewById(R.id.editText);
        new AccessWebServiceTask().execute(et.getText().toString());
    }

}